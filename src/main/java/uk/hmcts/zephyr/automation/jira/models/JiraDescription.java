package uk.hmcts.zephyr.automation.jira.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

// Atlassian Document Format: https://developer.atlassian.com/cloud/jira/platform/apis/document/structure/
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraDescription {
    private final RootType type = RootType.DOC;
    private final Integer version = 1;
    private List<BlockNode> content;

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type",
                  visible = true)
    @JsonSubTypes({
        @JsonSubTypes.Type(value = ParagraphNode.class, name = "paragraph"),
        @JsonSubTypes.Type(value = PanelNode.class, name = "panel"),
        @JsonSubTypes.Type(value = TableNode.class, name = "table")
    })
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public abstract static class BlockNode {
        abstract BlockType getType();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParagraphNode extends BlockNode {
        private final BlockType type = BlockType.PARAGRAPH;
        private List<InlineNode> content;
        private ParagraphAttrs attrs;

        public ParagraphNode(InlineNode... content) {
            this.content = new ArrayList<>(List.of(content));
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PanelNode extends BlockNode {
        private final BlockType type = BlockType.PANEL;
        private List<ParagraphNode> content;
        private PanelAttrs attrs;

        public PanelNode(ParagraphNode... content) {
            this.content = new ArrayList<>(List.of(content));
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TableNode extends BlockNode {
        private final BlockType type = BlockType.TABLE;
        private List<TableRowNode> content;
        private TableAttrs attrs;

        public TableNode(TableRowNode... content) {
            this.content = new ArrayList<>(List.of(content));
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TableRowNode {
        private final TableContentType type = TableContentType.TABLE_ROW;
        private List<TableCellContentNode> content;

        public TableRowNode(TableCellContentNode... content) {
            this.content = new ArrayList<>(List.of(content));
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type",
                  visible = true)
    @JsonSubTypes({
        @JsonSubTypes.Type(value = TableCellNode.class, name = "tableCell"),
        @JsonSubTypes.Type(value = TableHeaderNode.class, name = "tableHeader")
    })
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public abstract static class TableCellContentNode {
        public abstract TableContentType getType();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TableCellNode extends TableCellContentNode {
        private final TableContentType type = TableContentType.TABLE_CELL;
        private TableCellAttrs attrs;
        private List<ParagraphNode> content;

        public TableCellNode(TableCellAttrs attrs, ParagraphNode... content) {
            this.attrs = attrs;
            this.content = new ArrayList<>(List.of(content));
        }

        public TableCellNode(ParagraphNode... content) {
            this(null, content);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TableHeaderNode extends TableCellContentNode {
        private final TableContentType type = TableContentType.TABLE_HEADER;
        private TableCellAttrs attrs;
        private List<ParagraphNode> content;

        public TableHeaderNode(TableCellAttrs attrs, ParagraphNode... content) {
            this.attrs = attrs;
            this.content = new ArrayList<>(List.of(content));
        }

        public TableHeaderNode(ParagraphNode... content) {
            this(null, content);
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type",
                  visible = true)
    @JsonSubTypes({
        @JsonSubTypes.Type(value = TextNode.class, name = "text"),
        @JsonSubTypes.Type(value = HardBreakNode.class, name = "hardBreak")
    })
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public abstract static class InlineNode {
        public abstract InlineType getType();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TextNode extends InlineNode {
        private final InlineType type = InlineType.TEXT;
        private String text;
        private List<MarkNode> marks;

        public TextNode(String text, MarkNode... marks) {
            this.text = text;
            this.marks = List.of(marks);
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HardBreakNode extends InlineNode {
        private final InlineType type = InlineType.HARD_BREAK;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type",
                  visible = true)
    @JsonSubTypes({
        @JsonSubTypes.Type(value = StrongMark.class, name = "strong"),
        @JsonSubTypes.Type(value = LinkMark.class, name = "link")
    })
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public abstract static class MarkNode {
        public abstract MarkType getType();
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StrongMark extends MarkNode {
        private final MarkType type = MarkType.STRONG;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LinkMark extends MarkNode {
        private final MarkType type = MarkType.LINK;
        private LinkAttrs attrs;

        public LinkMark(String href) {
            this.attrs = new LinkAttrs();
            this.attrs.setHref(href);
        }
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParagraphAttrs {
        private String localId;
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PanelAttrs {
        private PanelType panelType;
        private String localId;
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LinkAttrs {
        private String href;
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TableAttrs {
        private TableDisplayMode displayMode;
        private Boolean isNumberColumnEnabled;
        private TableLayout layout;
        private String localId;
        private Integer width;
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TableCellAttrs {
        private Integer colspan;
        private Integer rowspan;
        private List<Integer> colwidth;
        private String background;
        private String localId;
        private TableCellValign valign;
    }

    public enum RootType {
        @JsonProperty("doc") DOC
    }

    public enum BlockType {
        @JsonProperty("paragraph") PARAGRAPH,
        @JsonProperty("panel") PANEL,
        @JsonProperty("table") TABLE
    }

    public enum TableContentType {
        @JsonProperty("tableRow") TABLE_ROW,
        @JsonProperty("tableCell") TABLE_CELL,
        @JsonProperty("tableHeader") TABLE_HEADER
    }

    public enum TableDisplayMode {
        @JsonProperty("default") DEFAULT,
        @JsonProperty("fixed") FIXED
    }

    public enum TableLayout {
        @JsonProperty("wide") WIDE,
        @JsonProperty("full-width") FULL_WIDTH,
        @JsonProperty("center") CENTER,
        @JsonProperty("align-end") ALIGN_END,
        @JsonProperty("align-start") ALIGN_START,
        @JsonProperty("default") DEFAULT
    }

    public enum TableCellValign {
        @JsonProperty("top") TOP,
        @JsonProperty("middle") MIDDLE,
        @JsonProperty("bottom") BOTTOM
    }

    public enum InlineType {
        @JsonProperty("text") TEXT,
        @JsonProperty("hardBreak") HARD_BREAK
    }

    public enum MarkType {
        @JsonProperty("strong") STRONG,
        @JsonProperty("link") LINK
    }

    public enum PanelType {
        @JsonProperty("info") INFO,
        @JsonProperty("note") NOTE,
        @JsonProperty("tip") TIP,
        @JsonProperty("warning") WARNING,
        @JsonProperty("error") ERROR,
        @JsonProperty("success") SUCCESS,
        @JsonProperty("custom") CUSTOM
    }
}
