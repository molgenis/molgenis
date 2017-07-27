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
              'package': {
                'id': 'test'
              },
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
              'package': {
                'id': 'test'
              },
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
              'package': {
                'id': 'test'
              },
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
              'package': {
                'id': 'test'
              },
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
              'package': {
                'id': 'test'
              },
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
              'package': {
                'id': 'test'
              },
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
              'package': {
                'id': 'test'
              },
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
              'package': {
                'id': 'test'
              },
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
            abstract: '',
            color: '#FFFFFF',
            extends: '',
            group: 'test',
            items: [
              {name: 'Application: String', iskey: true, figure: 'Triangle', color: '#F0EF9A'}
            ]
          },
          {
            key: 'sys_FileMeta',
            abstract: '',
            color: '#FFFFFF',
            extends: '',
            group: 'test',
            items: [
              {name: 'Filemeta: String', iskey: true, figure: 'Triangle', color: '#F0EF9A'}
            ]
          },
          {
            key: 'sys_FreemarkerTemplate',
            abstract: '',
            color: '#FFFFFF',
            extends: '',
            group: 'test',
            items: [
              {name: 'Freemarker: String', iskey: true, figure: 'Triangle', color: '#F0EF9A'}
            ]
          },
          {
            key: 'sys_ImportRun',
            abstract: '',
            color: '#FFFFFF',
            extends: '',
            group: 'test',
            items: [
              {name: 'Import: String', iskey: true, figure: 'Triangle', color: '#F0EF9A'}
            ]
          },
          {
            key: 'sys_Language',
            abstract: '',
            color: '#FFFFFF',
            extends: '',
            group: 'test',
            items: [
              {name: 'Language: String', iskey: true, figure: 'Triangle', color: '#F0EF9A'}
            ]
          },
          {
            key: 'sys_L10nString',
            abstract: '',
            color: '#FFFFFF',
            extends: '',
            group: 'test',
            items: [
              {name: 'Localization: String', iskey: true, figure: 'Triangle', color: '#F0EF9A'}
            ]
          },
          {
            key: 'sys_Questionnaire',
            abstract: '',
            color: '#FFFFFF',
            extends: '',
            group: 'test',
            items: [
              {name: 'Questionnaire: String', iskey: true, figure: 'Triangle', color: '#F0EF9A'}
            ]
          },
          {
            key: 'sys_StaticContent',
            abstract: '',
            color: '#FFFFFF',
            extends: '',
            group: 'test',
            items: [
              {name: 'Static content: String', iskey: true, figure: 'Triangle', color: '#F0EF9A'}
            ]
          },
          {
            color: '#F0F0E9',
            isGroup: true,
            key: 'test'
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
