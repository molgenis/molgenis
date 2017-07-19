import getters from 'store/getters'

describe('getters', () => {
  describe('umlData', () => {
    it('should aggregate the variants in the state', () => {
      const state = {
        'umlData': {
          'entityTypes': [
            {
              'id': 'sys_App',
              'name': 'App',
              'attributes': [
                {
                  'id': 'app',
                  'label': 'Application',
                  'isIdAttribute': 'false',
                  'notnullable': 'false',
                  'type': 'string'
                }
              ]
            }, {
              'id': 'sys_FileMeta',
              'name': 'File metadata',
              'attributes': [
                {
                  'id': 'filemetadata',
                  'label': 'Filemeta',
                  'isIdAttribute': 'false',
                  'notnullable': 'false',
                  'type': 'string'
                }
              ]
            }, {
              'id': 'sys_FreemarkerTemplate',
              'name': 'Freemarker template',
              'attributes': [
                {
                  'id': 'freemarker',
                  'label': 'Freemarker',
                  'isIdAttribute': 'false',
                  'notnullable': 'false',
                  'type': 'string'
                 }
               ]
            }, {
              'id': 'sys_ImportRun',
              'name': 'Import',
              'attributes': [
                {
                  'id': 'importrun',
                  'label': 'Import',
                  'isIdAttribute': 'false',
                  'notnullable': 'false',
                  'type': 'string'
                }
              ]
            }, {
              'id': 'sys_Language',
              'name': 'Language',
              'attributes': [
                {
                  'id': 'language',
                  'label': 'Language',
                  'isIdAttribute': 'false',
                  'notnullable': 'false',
                  'type': 'string'
                }
              ]
            }, {
              'id': 'sys_L10nString',
              'name': 'Localization',
              'attributes': [
                {
                  'id': 'local',
                  'label': 'Localization',
                  'isIdAttribute': 'false',
                  'notnullable': 'false',
                  'type': 'string'
                }
              ]
            }, {
              'id': 'sys_Questionnaire',
              'name': 'Questionnaire',
              'attributes': [
                {
                  'id': 'questionnaire',
                  'label': 'Questionnaire',
                  'isIdAttribute': 'false',
                  'notnullable': 'false',
                  'type': 'string'
                }
              ]
            }, {
              'id': 'sys_StaticContent',
              'name': 'Static content',
              'attributes': [
                {
                  'id': 'static',
                  'label': 'Static content',
                  'isIdAttribute': 'false',
                  'notnullable': 'false',
                  'type': 'string'
                }
              ]
            }
          ]
        }
      }
      const result = getters.umlData(state)
      console.log(JSON.stringify(result, null, 2))
      const expected = {
        nodeData: [
          {
            key: 'sys_App',
            items: [
              {name: 'Application: String', iskey: true, figure: 'Cubel', color: 'yellow'}
            ]
          },
          {
            key: 'sys_FileMeta',
            items: [
              {name: 'Filemeta: String', iskey: true, figure: 'Cubel', color: 'yellow'}
            ]
          },
          {
            key: 'sys_FreemarkerTemplate',
            items: [
              {name: 'Freemarker: String', iskey: true, figure: 'Cubel', color: 'yellow'}
            ]
          },
          {
            key: 'sys_ImportRun',
            items: [
              {name: 'Import: String', iskey: true, figure: 'Cubel', color: 'yellow'}
            ]
          },
          {
            key: 'sys_Language',
            items: [
              {name: 'Language: String', iskey: true, figure: 'Cubel', color: 'yellow'}
            ]
          },
          {
            key: 'sys_L10nString',
            items: [
              {name: 'Localization: String', iskey: true, figure: 'Cubel', color: 'yellow'}
            ]
          },
          {
            key: 'sys_Questionnaire',
            items: [
              {name: 'Questionnaire: String', iskey: true, figure: 'Cubel', color: 'yellow'}
            ]
          },
          {
            key: 'sys_StaticContent',
            items: [
              {name: 'Static content: String', iskey: true, figure: 'Cubel', color: 'yellow'}
            ]
          },
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
