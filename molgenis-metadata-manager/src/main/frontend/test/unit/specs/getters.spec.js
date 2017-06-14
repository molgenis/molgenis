import { expect } from 'chai'
import getters from 'src/store/getters'

describe('getters', () => {
  describe('getPackages', () => {
    it('Should retrieve all packages from the state', () => {
      const state = {
        packages: ['package1', 'package2']
      }

      const actual = getters.getPackages(state)
      const expected = ['package1', 'package2']

      expect(actual).to.deep.equal(expected)
    })
  })

  describe('getEntityTypes', () => {
    it('Should retrieve all entityTypes from the state', () => {
      const state = {
        entityTypes: ['entityType1', 'entityType2']
      }

      const actual = getters.getEntityTypes(state)
      const expected = ['entityType1', 'entityType2']

      expect(actual).to.deep.equal(expected)
    })
  })

  describe('getSelectedEntityType', () => {
    it('Should retrieve an entityType object from a list of entityTypes based on the entityTypeID in the URL', () => {
      const state = {
        entityTypes: [
          {id: '1', name: 'entityType1'}, {id: '2', name: 'entityType2'}
        ],
        route: {
          params: {
            entityTypeID: '2'
          }
        }
      }

      const actual = getters.getSelectedEntityType(state)
      const expected = {id: '2', name: 'entityType2'}

      expect(actual).to.deep.equal(expected)
    })
  })

  describe('getEditorEntityType', () => {
    it('Should retrieve the editorEntityType from the state', () => {
      const state = {
        editorEntityType: {id: '1', name: 'editorEntityType1'}
      }

      const actual = getters.getEditorEntityType(state)
      const expected = {id: '1', name: 'editorEntityType1'}

      expect(actual).to.deep.equal(expected)
    })
  })

  describe('getAlert', () => {
    it('Should retrieve the alert object from the state', () => {
      const state = {
        alert: {
          type: 'success',
          message: 'this test succeeds!'
        }
      }

      const actual = getters.getAlert(state)
      const expected = {type: 'success', message: 'this test succeeds!'}

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

  // getAttributeTree: (state, getters) => {
  //   const allAttributes = state.editorEntityType.attributes
  //   const rootAttributes = allAttributes.filter(attribute => !attribute.parent)
  //   const addChildren = attr => {
  //     const children = allAttributes.filter(attribute => attribute.parent && attribute.parent.id === attr.id)
  //     const offspring = children.map(addChildren)
  //     const selected = getters.getSelectedAttribute && attr.id === getters.getSelectedAttribute.id
  //     return {...attr, children: offspring, selected}
  //   }
  //   return rootAttributes.map(addChildren)
  // }

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
})
