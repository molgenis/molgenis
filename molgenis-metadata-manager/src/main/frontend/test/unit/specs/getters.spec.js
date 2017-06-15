import { expect } from 'chai'
import getters from 'src/store/getters'

describe('getters', () => {
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
      state = {...state, selectedAttributeID: '4'}

      const actual = getters.getSelectedAttribute(state)
      const expected = {id: '4', name: 'attribute4'}

      expect(actual).to.deep.equal(expected)
    })

    it('Should retrieve nothing because the selectedAttributeID in the state is null', () => {
      state = {...state, selectedAttributeID: null}

      const actual = getters.getSelectedAttribute(state)
      const expected = undefined

      expect(actual).to.deep.equal(expected)
    })
  })

  describe('getIndexOfSelectedAttribute', () => {
    it('Should return the index of the selected attribute', () => {
      const state = {
        selectedAttributeID: '1',
        editorEntityType: {
          attributes: [
            {id: '5', name: 'attribute5'},
            {id: '3', name: 'attribute3'},
            {id: '2', name: 'attribute2'},
            {id: '1', name: 'attribute1'},
            {id: '4', name: 'attribute4'}
          ]
        }
      }

      const actual = getters.getIndexOfSelectedAttribute(state)
      const expected = 3

      expect(actual).to.deep.equal(expected)
    })
  })
})
