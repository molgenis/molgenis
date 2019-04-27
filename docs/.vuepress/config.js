module.exports = {
  title: 'MOLGENIS guide',
  description: "For scientific data",
  themeConfig: {
    lastUpdated: 'Last Updated', // string | boolean
    repo: 'molgenis/molgenis',
    docsDir: 'docs',
    editLinks: true,
    searchMaxSuggestions: 20,
    nav: [
      {
        text: 'Guide',
        link: '/background'
      },
      {
        text: 'Releases',
        link: 'https://github.com/molgenis/molgenis/releases'
      },
      {
        text: 'Website',
        link: 'http://molgenis.org'
      },

    ],
    sidebar: [
      {
        title: "Introduction",
        children: [
          'background.md',
          'quickstart/guide-quickstart']
      },
      {
        title: 'Find, view, query',
        children: [
          'user_documentation/finding-data/guide-explore',
          'user_documentation/finding-data/guide-navigator',
          'user_documentation/finding-data/guide-search',
          'user_documentation/admin-features/security/guide-authentication'
        ]
      },
      {
        title: 'Data management',
        children: [
          'user_documentation/import-data/ref-emx',
          'user_documentation/import-data/guide-upload',
          'user_documentation/import-data/guide-quick-upload',
          'user_documentation/modify-data/guide-metadata-manager',
          'user_documentation/guide-questionnaire',
          'user_documentation/modify-data/guide-emx-download',
          'user_documentation/modify-data/ref-expressions'
        ]
      },
      {
        title: 'Access control',
        children: [
          'user_documentation/admin-features/security/guide-user-management',
          'user_documentation/admin-features/security/guide-groups-roles',
          'user_documentation/admin-features/security/guide-permission-manager'
        ]
      },
      {
        title: 'Data processing',
        children: [
          'user_documentation/scripts/guide-scripts',
          'developer_documentation/ref-R',
          'user_documentation/scripts/guide-R',
          'developer_documentation/ref-python',
          'user_documentation/scripts/guide-python',
          'user_documentation/scripts/guide-schedule'
        ]
      },
      {
        title: 'Configuration',
        children: [
          'quickstart/guide-docker',
          'quickstart/guide-tomcat',
          'user_documentation/admin-features/guide-settings',
          'user_documentation/admin-features/guide-customize',
          'user_documentation/import-data/guide-l10n',
          'user_documentation/admin-features/guide-app-manager',
          'developer_documentation/creating-themes'
        ]
      },
      {
        title: 'Interoperability',
        children: [
          'developer_documentation/ref-swagger',
          'developer_documentation/ref-rest',
          'developer_documentation/ref-rest2',
          'developer_documentation/ref-api-files',
          'developer_documentation/ref-upload',
          'developer_documentation/beacon',
          'developer_documentation/guide-fair',
          'developer_documentation/ref-RSQL'
        ]
      },
      {
        title: 'For developers',
        children: [
          'quickstart/guide-local-compile',
          'developer_documentation/intellij',
          'developer_documentation/technologies',
          'developer_documentation/app-development',
          'developer_documentation/dynamic-decorators',
          'developer_documentation/frontend-development',
          'developer_documentation/integration-tests',
          'developer_documentation/jobs',
          'developer_documentation/security'
        ]
      }
    ]
  }
}
