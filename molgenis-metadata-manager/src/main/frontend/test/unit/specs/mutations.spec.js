import mutations from '../../../src/store/mutations'

describe('mutations', () => {
  describe('Testing mutation SET_PACKAGES', () => {
    it('Filters out the "system" packages for non system users, sorts the packages by label stores them in the store', () => {
      const state = {
        packages: []
      }
      const packages = [
        {id: 'sys_idx', label: 'Index'},
        {id: 'sys_sec', label: 'Security'},
        {id: 'sys', label: 'System'},
        {id: 'sys_md', label: 'Meta'},
        {id: 'base', label: 'Default'},
        {id: 'bbb', label: 'ZZZ'},
        {id: 'aaa', label: 'AAA'}
      ]

      const expectedPackages = [
        {id: 'aaa', label: 'AAA'},
        {id: 'base', label: 'Default'},
        {id: 'bbb', label: 'ZZZ'}
      ]
      mutations.__SET_PACKAGES__(state, packages)

      expect(state.packages).to.deep.equal(expectedPackages)
    })
  })

  describe('Testing mutation CREATE_ALERT', () => {
    it('Updates alert message', () => {
      const state = {
        alert: {
          message: null,
          type: null
        }
      }
      const alert = {
        message: 'Hello',
        type: 'success'
      }
      mutations.__CREATE_ALERT__(state, alert)
      expect(state.alert.message).to.equal('Hello')
    })
    it('Updates alert type', () => {
      const state = {
        alert: {
          message: null,
          type: null
        }
      }
      const alert = {
        message: 'Hello',
        type: 'success'
      }
      mutations.__CREATE_ALERT__(state, alert)
      expect(state.alert.type).to.equal('success')
    })
  })

  describe('Testing mutation SET_ENTITY_TYPES', () => {
    it('Filters out the "system" entities for non system users, sorts the entities by label stores them in the store', () => {
      const state = {
        alert: {
          entityTypes: []
        }
      }

      const payload = [
        {id: 'B', label: 'B entity'},
        {id: 'A', label: 'A entity'},
        {id: 'C', label: 'C entity'},
        {id: 'E', label: 'E entity'},
        {id: 'D', label: 'D entity'},
        {id: 'sys_sec', label: 'Security'}
      ]

      const expected = [
        {id: 'A', label: 'A entity'},
        {id: 'B', label: 'B entity'},
        {id: 'C', label: 'C entity'},
        {id: 'D', label: 'D entity'},
        {id: 'E', label: 'E entity'}
      ]

      mutations.__SET_ENTITY_TYPES__(state, payload)
      expect(state.entityTypes).to.deep.equal(expected)
    })
  })

  describe('Testing mutation SET_ATTRIBUTE_TYPES', () => {
    it('should set a list of attribute types', () => {
      const state = {
        attributeTypes: []
      }

      const attributeTypes = ['string', 'int', 'xref']

      mutations.__SET_ATTRIBUTE_TYPES__(state, attributeTypes)
      expect(state.attributeTypes).to.deep.equal(attributeTypes)
    })
  })

  describe('Testing mutation SET_EDITOR_ENTITY_TYPE', () => {
    it('Sets selected entity type to edit', () => {
      const state = {
        editorEntityType: {},
        initialEditorEntityType: {}
      }
      const editorEntityType = {
        id: 'root_gender',
        labelI18n: {},
        description: 'Gender options',
        abstract0: false,
        attributes: [
          {
            aggregatable: false,
            auto: false,
            descriptionI18n: {},
            enumOptions: [],
            id: 'bla',
            labelI18n: {},
            name: 'id',
            nullable: false,
            readonly: true,
            tags: [],
            type: 'string',
            unique: true,
            visible: true
          },
          {
            aggregatable: false,
            auto: false,
            descriptionI18n: {},
            enumOptions: [],
            id: 'bladibla',
            labelI18n: {},
            name: 'label',
            nullable: false,
            readonly: true,
            tags: [],
            type: 'string',
            unique: true,
            visible: true
          }
        ],
        backend: 'postgreSQL',
        idAttribute: {id: 'bla', label: 'id'},
        label: 'Gender',
        labelAttribute: {id: 'bladibla', label: 'label'},
        lookupAttributes: [
          {id: 'bla', label: 'id'},
          {id: 'bladibla', label: 'label'}
        ],
        package0: {id: 'root', label: 'root'},
        tags: []
      }
      mutations.__SET_EDITOR_ENTITY_TYPE__(state, editorEntityType)
      expect(state.editorEntityType).to.deep.equal(editorEntityType)
      expect(state.initialEditorEntityType).to.deep.equal(JSON.parse(JSON.stringify(editorEntityType)))
    })
  })

  describe('Testing mutation UPDATE_EDITOR_ENTITY_TYPE', () => {
    it('should update the description of the EditorEntityType', () => {
      const state = {
        editorEntityType: {
          description: 'description'
        }
      }

      const update = {
        key: 'description',
        value: 'Option-selection-list for gender'
      }

      mutations.__UPDATE_EDITOR_ENTITY_TYPE__(state, update)
      expect(state.editorEntityType.description).to.equal('Option-selection-list for gender')
    })

    it('should update the idAttribute of the EditorEntityType', () => {
      const state = {
        editorEntityType: {
          idAttribute: null,
          attributes: [
            {id: '1'}
          ]
        }
      }

      const update = {
        key: 'idAttribute',
        value: {id: '1', label: 'idAttribute'}
      }

      const expected = {
        editorEntityType: {
          idAttribute: {id: '1', label: 'idAttribute', readonly: true, unique: true, nullable: false},
          attributes: [
            {id: '1', label: 'idAttribute', readonly: true, unique: true, nullable: false}
          ]
        }
      }

      mutations.__UPDATE_EDITOR_ENTITY_TYPE__(state, update)
      expect(state).to.deep.equal(expected)
    })
  })

  describe('Testing mutation UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE', () => {
    it('Updates the selected attribute IN the editorEntityType attribute list', () => {
      const state = {
        selectedAttributeId: '2',
        editorEntityType: {
          attributes: [
            {id: '1', name: 'attribute1'},
            {id: '2', name: 'attribute2'},
            {id: '3', name: 'attribute3'}
          ]
        }
      }

      const expected = [
        {id: '1', name: 'attribute1'},
        {id: '2', name: 'updated name'},
        {id: '3', name: 'attribute3'}
      ]

      mutations.__UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE__(state, {key: 'name', value: 'updated name'})
      expect(state.editorEntityType.attributes).to.deep.equal(expected)
    })
    it('Updates the selected attribute in the editorEntityType attribute list when type = onetomany and becomes xref', () => {
      const state = {
        selectedAttributeId: '1',
        editorEntityType: {
          attributes: [
            {
              id: '1',
              name: 'attribute 1',
              type: 'onetomany',
              mappedByAttribute: {id: '4', label: 'xrefattr', entity: {id: 'abcde', label: 'label'}},
              refEntityType: {id: 'refEntityId'}
            },
            {id: '2', name: 'attribute 2', type: 'int', mappedByAttribute: null, refEntityType: null},
            {id: '3', name: 'attribute 3', type: 'string', mappedByAttribute: null, refEntityType: null}
          ]
        }
      }

      const expected = [
        {id: '1', name: 'attribute 1', type: 'xref', mappedByAttribute: null, refEntityType: null, orderBy: null},
        {id: '2', name: 'attribute 2', type: 'int', mappedByAttribute: null, refEntityType: null},
        {id: '3', name: 'attribute 3', type: 'string', mappedByAttribute: null, refEntityType: null}
      ]

      mutations.__UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE__(state, {key: 'type', value: 'xref'})
      expect(state.editorEntityType.attributes).to.deep.equal(expected)
    })
    it('Updates the selected attribute in the editorEntityType attribute list when type = xref and becomes onetomany', () => {
      const state = {
        selectedAttributeId: '1',
        editorEntityType: {
          attributes: [
            {
              id: '1',
              name: 'attribute 1',
              type: 'xref',
              mappedByAttribute: {id: '4', label: 'xrefattr', entity: {id: 'abcde', label: 'label'}},
              orderBy: {orders: [{attributeName: 'id', order: 'ASC'}]},
              refEntityType: null
            },
            {id: '2', name: 'attribute 2', type: 'int', mappedByAttribute: null, refEntityType: null},
            {id: '3', name: 'attribute 3', type: 'string', mappedByAttribute: null, refEntityType: null}
          ]
        }
      }

      const expected = [
        {id: '1', name: 'attribute 1', type: 'onetomany', mappedByAttribute: null, refEntityType: null, orderBy: null},
        {id: '2', name: 'attribute 2', type: 'int', mappedByAttribute: null, refEntityType: null},
        {id: '3', name: 'attribute 3', type: 'string', mappedByAttribute: null, refEntityType: null}
      ]

      mutations.__UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE__(state, {key: 'type', value: 'onetomany'})
      expect(state.editorEntityType.attributes).to.deep.equal(expected)
    })
    it('updates refEntity in selected attribute when mappedBy attribute is chosen', () => {
      const state = {
        selectedAttributeId: '1',
        editorEntityType: {
          attributes: [
            {id: '1', name: 'attribute 1', type: 'onetomany', mappedByAttribute: null, refEntityType: null},
            {id: '2', name: 'attribute 2', type: 'int', mappedByAttribute: null, refEntityType: null},
            {id: '3', name: 'attribute 3', type: 'string', mappedByAttribute: null, refEntityType: null}
          ]
        }
      }

      const expected = [
        {
          id: '1',
          name: 'attribute 1',
          type: 'onetomany',
          mappedByAttribute: {id: 'idAttr1', label: 'testAttr', entity: {id: 'idEntity1', label: 'testEntity'}},
          orderBy: null,
          refEntityType: {id: 'idEntity1', label: 'testEntity'}
        },
        {id: '2', name: 'attribute 2', type: 'int', mappedByAttribute: null, refEntityType: null},
        {id: '3', name: 'attribute 3', type: 'string', mappedByAttribute: null, refEntityType: null}
      ]

      mutations.__UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE__(state, {
        key: 'mappedByAttribute',
        value: {id: 'idAttr1', label: 'testAttr', entity: {id: 'idEntity1', label: 'testEntity'}}
      })
      expect(state.editorEntityType.attributes).to.deep.equal(expected)
    })
  })

  describe('Testing mutation SET_SELECTED_ATTRIBUTE_ID', () => {
    it('Updates the selected attribute ID', () => {
      const state = {
        selectedAttributeId: null
      }

      const id = 'newAttributeId'
      mutations.__SET_SELECTED_ATTRIBUTE_ID__(state, id)
      expect(state.selectedAttributeId).to.equal('newAttributeId')
    })
  })

  describe('Testing mutation DELETE_SELECTED_ATTRIBUTE', () => {
    it('should remove an attribute from the list of editorEntityType attributes based on the selected attribute id', () => {
      const state = {
        selectedAttributeId: '1',
        editorEntityType: {
          attributes: [
            {id: '1', name: 'attribute1'},
            {id: '2', name: 'attribute2'},
            {id: '3', name: 'attribute3'}
          ]
        }
      }

      const expected = [
        {id: '2', name: 'attribute2'},
        {id: '3', name: 'attribute3'}
      ]

      mutations.__DELETE_SELECTED_ATTRIBUTE__(state, '1')
      expect(state.editorEntityType.attributes).to.deep.equal(expected)
    })
  })

  describe('Testing mutation UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE_ORDER', () => {
    it('should move the index of the selected attribute from 2 to 1', () => {
      const state = {
        editorEntityType: {
          attributes: [
            {id: '1', name: 'attribute1'},
            {id: '2', name: 'attribute2'},
            {id: '3', name: 'attribute3'}
          ]
        }
      }

      const expected = [
        {id: '1', name: 'attribute1'},
        {id: '3', name: 'attribute3'},
        {id: '2', name: 'attribute2'}
      ]

      mutations.__UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE_ORDER__(state, {moveOrder: 'up', selectedAttributeIndex: 2})
      expect(state.editorEntityType.attributes).to.deep.equal(expected)
    })

    it('should move the index of the selected attribute from 0 to 1', () => {
      const state = {
        editorEntityType: {
          attributes: [
            {id: '1', name: 'attribute1'},
            {id: '2', name: 'attribute2'},
            {id: '3', name: 'attribute3'}
          ]
        }
      }

      const expected = [
        {id: '2', name: 'attribute2'},
        {id: '1', name: 'attribute1'},
        {id: '3', name: 'attribute3'}
      ]

      mutations.__UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE_ORDER__(state, {moveOrder: 'down', selectedAttributeIndex: 0})
      expect(state.editorEntityType.attributes).to.deep.equal(expected)
    })
  })

  describe('Testing mutation SET_LOADING', () => {
    it('should set loading to true', () => {
      const state = {
        loading: 0
      }

      const expected = 1

      mutations.__SET_LOADING__(state, true)
      expect(state.loading).to.equal(expected)
    })

    it('should set loading to false', () => {
      const state = {
        loading: 1
      }

      const expected = 0

      mutations.__SET_LOADING__(state, false)
      expect(state.loading).to.equal(expected)
    })
  })
})
