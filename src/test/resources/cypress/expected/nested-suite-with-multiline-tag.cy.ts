describe('Account flows', () => {
  describe('when user is admin', () => {
    it('admin can approve', {
      retries: 1,
      tags: ['@JIRA-KEY:ZE-500']
    }, () => {
      // admin scenario
    });
  });

  describe('when user is ' +
      'guest', () => {
    it('guest sees limited dashboard', {
      retries: 0,
      viewportWidth: 1024,
      tags: [
        '@JIRA-LABEL:dashboard',
        '@ACCESS:limited'
      ]
    }, () => {
      // dashboard scenario
    });

    it('logs out ' + 'automatically', {
      viewportHeight: 768,
      tags: [
        '@JIRA-LABEL:logout'
      ],
      retries: 2
    }, () => {
      // logout scenario
    });

    it('deep ' + 'nest ' + 'target', {
      retries: 0,
      tags: [
        '@INITIAL'
      ],
      env: {
        featureFlag: true
      }
    }, () => {
      // deeply nested scenario
    });

    it('multi ' +
        'line ' +
        'title', {
      retries: 0,
      tags: ['@MULTI', '@MULTILINE']
    }, () => {
      // multi-line title scenario
    });
  });
});