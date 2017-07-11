import getters from 'store/getters'

describe('getters', () => {
  describe('graph', () => {
    it('should aggregate the variants in the state', () => {
      const state = {
        '__entityTypeId': 'sys_md_Package',
        'id': 'sys',
        'label': 'System',
        'description': 'Package containing all system entities',
        'children': [
          {
            '__entityTypeId': 'sys_md_Package',
            '__idValue': 'sys_idx',
            '__labelValue': 'Index'
          }, {
            '__entityTypeId': 'sys_md_Package',
            '__idValue': 'sys_job',
            '__labelValue': 'Jobs'
          }, {
            '__entityTypeId': 'sys_md_Package',
            '__idValue': 'sys_mail',
            '__labelValue': 'Mail'
          }, {
            '__entityTypeId': 'sys_md_Package',
            '__idValue': 'sys_map',
            '__labelValue': 'Mapper'
          }, {
            '__entityTypeId': 'sys_md_Package',
            '__idValue': 'sys_md',
            '__labelValue': 'Meta'
          }, {
            '__entityTypeId': 'sys_md_Package',
            '__idValue': 'sys_ont',
            '__labelValue': 'Ontology'
          }, {
            '__entityTypeId': 'sys_md_Package',
            '__idValue': 'sys_scr',
            '__labelValue': 'Script'
          }, {
            '__entityTypeId': 'sys_md_Package',
            '__idValue': 'sys_sec',
            '__labelValue': 'Security'
          }, {
            '__entityTypeId': 'sys_md_Package',
            '__idValue': 'sys_set',
            '__labelValue': 'Settings'
          }
        ],
        'entityTypes': [
          {
            '__entityTypeId': 'sys_md_EntityType',
            '__idValue': 'sys_App',
            '__labelValue': 'App'
          }, {
            '__entityTypeId': 'sys_md_EntityType',
            '__idValue': 'sys_FileMeta',
            '__labelValue': 'File metadata'
          }, {
            '__entityTypeId': 'sys_md_EntityType',
            '__idValue': 'sys_FreemarkerTemplate',
            '__labelValue': 'Freemarker template'
          }, {
            '__entityTypeId': 'sys_md_EntityType',
            '__idValue': 'sys_ImportRun',
            '__labelValue': 'Import'
          }, {
            '__entityTypeId': 'sys_md_EntityType',
            '__idValue': 'sys_Language',
            '__labelValue': 'Language'
          }, {
            '__entityTypeId': 'sys_md_EntityType',
            '__idValue': 'sys_L10nString',
            '__labelValue': 'Localization'
          }, {
            '__entityTypeId': 'sys_md_EntityType',
            '__idValue': 'sys_Questionnaire',
            '__labelValue': 'Questionnaire'
          }, {
            '__entityTypeId': 'sys_md_EntityType',
            '__idValue': 'sys_StaticContent',
            '__labelValue': 'Static content'
          }
        ],
        'tags': []
      }
      const result = getters.graph(state)
      console.log(JSON.stringify(result, null, 2))
      const expected = {
        nodeData: [
          {
            key: 'sys_md_EntityType',
            items: [
              {name: 'App', iskey: false, figure: 'Decision', color: 'blue'},
              {name: 'File metadata', iskey: false, figure: 'Decision', color: 'blue'},
              {name: 'Freemarker template', iskey: false, figure: 'Decision', color: 'blue'},
              {name: 'Import', iskey: false, figure: 'Decision', color: 'blue'},
              {name: 'Language', iskey: false, figure: 'Decision', color: 'blue'},
              {name: 'Localization', iskey: false, figure: 'Decision', color: 'blue'},
              {name: 'Questionnaire', iskey: false, figure: 'Decision', color: 'blue'},
              {name: 'Static content', iskey: false, figure: 'Decision', color: 'blue'}]
          }
        ],
        linkData: [
          // {from: 'Products', to: 'Suppliers', text: '0..N', toText: '1'},
          // {from: 'Products', to: 'Categories', text: '0..N', toText: '1'},
          // {from: 'Order Details', to: 'Products', text: '0..N', toText: '1'}
        ]
      }
      console.log(JSON.stringify(expected, null, 2))
      expect(result).to.deep.equal(expected)
    })
  })
})
