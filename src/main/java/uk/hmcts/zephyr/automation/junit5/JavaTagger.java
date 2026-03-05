package uk.hmcts.zephyr.automation.junit5;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.TestTag;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
public class JavaTagger {

    private static final String ANNOTATIONS_PACKAGE = "uk.hmcts.zephyr.automation.junit5.annotations";

    static {
        StaticJavaParser.getParserConfiguration().setLanguageLevel(LanguageLevel.JAVA_21);
    }

    public void addAnnotation(
        String fullyQualifiedClassName,
        String methodName,
        AnnotationDescriptor descriptor,
        TestTag testTag
    ) {
        Path javaFilePath = resolveTestFilePath(fullyQualifiedClassName);
        log.info("Adding {} annotation to {}#{} via {}",
            descriptor.simpleName(),
            fullyQualifiedClassName,
            methodName,
            javaFilePath
        );
        try {
            CompilationUnit compilationUnit = parseCompilationUnit(javaFilePath);
            TypeDeclaration<?> targetType = findTargetType(compilationUnit, fullyQualifiedClassName);
            MethodDeclaration method = targetType.getMethodsByName(methodName).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unable to find method '" + methodName
                    + "' in class '" + fullyQualifiedClassName + "'"));
            if (method.getAnnotationByClass(descriptor.annotationClass()).isPresent()) {
                log.info(
                    "Method {}#{} already has {} annotation",
                    fullyQualifiedClassName,
                    methodName,
                    descriptor.simpleName()
                );
                return;
            }
            AnnotationExpr annotationExpr = descriptor.createAnnotationExpression(testTag);
            method.addAnnotation(annotationExpr);
            ensureImport(compilationUnit, descriptor);
            Files.writeString(javaFilePath, LexicalPreservingPrinter.print(compilationUnit));
            log.info(
                "Successfully added {} annotation to {}#{}",
                descriptor.simpleName(),
                fullyQualifiedClassName,
                methodName
            );
        } catch (IOException | ParseProblemException e) {
            throw new RuntimeException("Failed to update annotations for class '" + fullyQualifiedClassName + "'", e);
        }
    }

    public static AnnotationDescriptor descriptor(Class<? extends Annotation> annotationClass, boolean hasValue) {
        return new AnnotationDescriptor(annotationClass, hasValue);
    }

    private static CompilationUnit parseCompilationUnit(Path javaFilePath) throws IOException {
        CompilationUnit compilationUnit = StaticJavaParser.parse(Files.readString(javaFilePath));
        return LexicalPreservingPrinter.setup(compilationUnit);
    }

    private static Path resolveTestFilePath(String className) {
        int lastDot = className.lastIndexOf('.');
        if (lastDot < 0) {
            throw new IllegalArgumentException("Class name must contain a package: " + className);
        }
        String packagePath = className.substring(0, lastDot).replace('.', '/');
        String simpleNames = className.substring(lastDot + 1);
        String outerClassName = simpleNames.split("\\$")[0];
        String basePath = Config.getBasePath();
        if (basePath == null || basePath.isBlank()) {
            throw new IllegalStateException("Base path is not configured");
        }
        return Paths.get(basePath)
            .resolve(packagePath)
            .resolve(outerClassName + ".java");
    }

    private static TypeDeclaration<?> findTargetType(CompilationUnit compilationUnit, String className) {
        List<String> hierarchy = resolveClassHierarchy(className);
        TypeDeclaration<?> current = null;
        for (String simpleName : hierarchy) {
            if (current == null) {
                current = compilationUnit.getTypes().stream()
                    .filter(type -> type.getNameAsString().equals(simpleName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                        "Unable to find top level type '" + simpleName + "' in " + className));
            } else {
                current = current.getMembers().stream()
                    .filter(BodyDeclaration::isTypeDeclaration)
                    .map(BodyDeclaration::asTypeDeclaration)
                    .filter(type -> type.getNameAsString().equals(simpleName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                        "Unable to find nested type '" + simpleName + "' in " + className));
            }
        }
        return current;
    }

    private static List<String> resolveClassHierarchy(String className) {
        int lastDot = className.lastIndexOf('.');
        if (lastDot < 0) {
            throw new IllegalArgumentException("Class name must contain a package: " + className);
        }
        return Arrays.asList(className.substring(lastDot + 1).split("\\$"));
    }

    private static void ensureImport(CompilationUnit compilationUnit, AnnotationDescriptor descriptor) {
        boolean hasSpecificImport = compilationUnit.getImports().stream()
            .filter(importDecl -> !importDecl.isAsterisk() && !importDecl.isStatic())
            .map(ImportDeclaration::getNameAsString)
            .anyMatch(name -> name.equals(descriptor.qualifiedName()));
        boolean hasWildcard = compilationUnit.getImports().stream()
            .filter(importDecl -> importDecl.isAsterisk() && !importDecl.isStatic())
            .map(ImportDeclaration::getNameAsString)
            .anyMatch(name -> name.equals(ANNOTATIONS_PACKAGE));
        if (!hasSpecificImport && !hasWildcard) {
            compilationUnit.addImport(descriptor.qualifiedName());
        }
    }

    public record AnnotationDescriptor(Class<? extends Annotation> annotationClass, boolean hasValue) {

        AnnotationExpr createAnnotationExpression(TestTag testTag) {
            if (!hasValue) {
                return StaticJavaParser.parseAnnotation("@" + simpleName());
            }
            String value = Optional.ofNullable(testTag.value())
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Tag type '" + annotationClass().getSimpleName() + "' requires a value"));
            return StaticJavaParser.parseAnnotation("@" + simpleName() + "(\"" + escape(value) + "\")");
        }

        String simpleName() {
            return annotationClass().getSimpleName();
        }

        String qualifiedName() {
            return annotationClass().getName();
        }

        private String escape(String input) {
            return input.replace("\\", "\\\\").replace("\"", "\\\"");
        }
    }
}
