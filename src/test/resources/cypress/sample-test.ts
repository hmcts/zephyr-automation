describe('Login feature', () => {
  it('user logs in', {
    retries: 1,
    tags: ['@JIRA-TEST-KEY:ABC-1']
  }, () => {
    // implementation
  });

  it('user logs out', {
    retries: 0
  }, () => {
    // implementation
  });

  it('should render component', () => {
    setupComponent(SOME_DASHBOARD);
    cy.get(L.app).should('exist');
  });
});

