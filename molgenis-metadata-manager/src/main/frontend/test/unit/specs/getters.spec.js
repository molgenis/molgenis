import { expect } from 'chai'
import getters, { getConfirmBeforeDeletingProperties, getConfirmBeforeLeavingProperties } from 'src/store/getters'

const i18n = {
  'confirm-before-delete-title': 'You are about to delete',
  'confirm-before-delete-text': 'Are you sure you want to continue?',
  'confirm-before-delete-confirm': 'Yes, delete',
  'confirm-before-delete-cancel': 'Cancel',
  'confirm-before-leaving-title': 'There are unsaved changes',
  'confirm-before-leaving-text': 'All unsaved changes will be lost, are you sure you want to continue?',
  'confirm-before-leaving-confirm': 'Yes',
  'confirm-before-leaving-cancel': 'No, stay'
}

const t = (key) => {
  return i18n[key]
}

describe('getters', () => {
  describe('getConfirmBeforeDeletingProperties', () => {
    it('should return a property object usable by sweetalert2 that asks before delete', () => {
      const actual = getConfirmBeforeDeletingProperties('1', t)
      const expected = {
        title: 'You are about to delete 1',
        text: 'Are you sure you want to continue?',
        type: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Yes, delete',
        cancelButtonText: 'Cancel'
      }

      expect(actual).to.deep.equal(expected)
    })
  })

  describe('getConfirmBeforeLeavingProperties', () => {
    it('should return a property object usable by sweetalert2 that asks before leaving screen', () => {
      const actual = getConfirmBeforeLeavingProperties(t)
      const expected = {
        title: 'There are unsaved changes',
        text: 'All unsaved changes will be lost, are you sure you want to continue?',
        type: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Yes',
        cancelButtonText: 'No, stay'
      }

      expect(actual).to.deep.equal(expected)
    })
  })

  describe('getEditorEntityTypeAttributes', () => {
    it('Should return the attributes for the editorEntityType present in the state', () => {
      const state = {
        editorEntityType: {
          attributes: [
            {id: '1', label: 'attribute1'},
            {id: '2', label: 'attribute2'},
            {id: '3', label: 'attribute3'}
          ]
        }
      }

      const actual = getters.getEditorEntityTypeAttributes(state)
      const expected = [
        {id: '1', label: 'attribute1'},
        {id: '2', label: 'attribute2'},
        {id: '3', label: 'attribute3'}
      ]

      expect(actual).to.deep.equal(expected)
    })
  })

  describe('getAbstractEntities', () => {
    it('Should filter a list of entityTypes and only return those that are abstract', () => {
      const state = {
        entityTypes: [
          {id: '1', isAbstract: true},
          {id: '2', isAbstract: false},
          {id: '3', isAbstract: true},
          {id: '4', isAbstract: false},
          {id: '5', isAbstract: false}
        ]
      }

      const actual = getters.getAbstractEntities(state)
      const expected = [{id: '1', isAbstract: true}, {id: '3', isAbstract: true}]

      expect(actual).to.deep.equal(expected)
    })
  })

  describe('getAttributeTree', () => {
    it('Should return a list of rootAttributes with added keys children and selected', () => {
      const state = {
        editorEntityType: {
          attributes: [
            {id: '1'},
            {id: '2'},
            {id: '3'},
            {id: '4', parent: {id: '3'}},
            {id: '5', parent: {id: '3'}}
          ]
        }
      }

      const actual = getters.getAttributeTree(state, getters)
      const expected = [
        {id: '1', children: [], selected: false},
        {id: '2', children: [], selected: false},
        {
          id: '3',
          children: [
            {id: '4', parent: {id: '3'}, children: [], selected: false},
            {id: '5', parent: {id: '3'}, children: [], selected: false}
          ],
          selected: false
        }
      ]

      expect(actual).to.deep.equal(expected)
    })
  })

  describe('getIndexOfSelectedAttribute', () => {
    it('should return the index of the selected attribute', () => {
      // state.editorEntityType && state.editorEntityType.attributes.findIndex(attribute => attribute.id === state.selectedAttributeId),
      const state = {
        selectedAttributeId: '3',
        editorEntityType: {
          attributes: [
            {id: '1'},
            {id: '2'},
            {id: '3'},
            {id: '4'},
            {id: '5'}
          ]
        }
      }

      const actual = getters.getIndexOfSelectedAttribute(state)
      const expected = 2

      expect(actual).to.deep.equal(expected)
    })
  })

  describe('getSelectedAttribute', () => {
    let state = {
      editorEntityType: {
        attributes: [
          {id: '1', name: 'attribute1'},
          {id: '2', name: 'attribute2'},
          {id: '3', name: 'attribute3'},
          {id: '4', name: 'attribute4'},
          {id: '5', name: 'attribute5'}
        ]
      }
    }

    it('Should retrieve an attribute object from a list of attributes based on the selectedAttributeID from the state', () => {
      state = {...state, selectedAttributeId: '4'}

      const actual = getters.getSelectedAttribute(state)
      const expected = {id: '4', name: 'attribute4'}

      expect(actual).to.deep.equal(expected)
    })

    it('Should retrieve nothing because the selectedAttributeID in the state is null', () => {
      state = {...state, selectedAttributeId: null}

      const actual = getters.getSelectedAttribute(state)
      const expected = undefined

      expect(actual).to.deep.equal(expected)
    })
  })

  describe('getCompoundAttributes', () => {
    it('should retrieve all compound attributes', () => {
      const state = {
        editorEntityType: {
          attributes: [
            {id: '1', type: 'string'},
            {id: '2', type: 'categorical'},
            {id: '3', type: 'xref'},
            {id: '4', type: 'compound'},
            {id: '5', type: 'int'}
          ]
        }
      }

      const actual = getters.getCompoundAttributes(state)
      const expected = [{id: '4', type: 'compound'}]

      expect(actual).to.deep.equal(expected)
    })
  })

  describe('getSelectedEntityType', () => {
    it('should retrieve the selectedEntityType from the list of entityTypes based on an identifier', () => {
      // state => state.entityTypes.find(entityType => entityType.id === state.selectedEntityTypeId),
      const state = {
        entityTypes: [
          {id: '1', selected: false},
          {id: '2', selected: false},
          {id: '3', selected: true},
          {id: '4', selected: false},
          {id: '5', selected: false}
        ],
        selectedEntityTypeId: '3'
      }

      const actual = getters.getSelectedEntityType(state)
      const expected = {id: '3', selected: true}

      expect(actual).to.deep.equal(expected)
    })
  })
})
