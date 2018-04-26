import mutations from 'src/store/mutations'

describe('mutations', () => {
  describe('Testing mutation SET_PACKAGES', () => {
    it('should set the list of packages in the state', () => {
      const state = {
        packages: []
      }

      const packages = [
        {id: 'p1'},
        {id: 'p2'},
        {id: 'p3'}
      ]

      mutations.__SET_PACKAGES__(state, packages)
      expect(state.packages).to.deep.equal(packages)
    })
  })

  describe('Testing mutation SET_PATH', () => {
    it('should set the path in the state', () => {
      const state = {
        path: []
      }

      const path = [
        {id: 'p1'},
        {id: 'p2'},
        {id: 'p3'}
      ]

      mutations.__SET_PATH__(state, path)
      expect(state.path).to.deep.equal(path)
    })
  })

  describe('Testing mutation RESET_PATH', () => {
    it('should reset the path array', () => {
      const state = {
        path: [
          {
            id: 'p1'
          },
          {
            id: 'p2'
          }
        ]
      }

      mutations.__RESET_PATH__(state)
      expect(state.path).to.deep.equal([])
    })
  })

  describe('Testing mutation SET_ENTITIES', () => {
    it('should set the entities in the state ', () => {
      const state = {
        entities: []
      }

      const entities = [
        {id: 'e1'},
        {id: 'e2'}
      ]

      mutations.__SET_ENTITIES__(state, entities)
      expect(state.entities).to.deep.equal(entities)
    })
  })

  describe('Testing mutation SET_QUERY', () => {
    it('should set the query in the state', () => {
      const state = {
        query: null
      }

      const query = 'query'

      mutations.__SET_QUERY__(state, query)
      expect(state.query).to.equal(query)
    })
  })

  describe('Testing mutation SET_ERROR', () => {
    it('should set the error in the state', () => {
      const state = {
        error: null
      }

      const error = 'error'

      mutations.__SET_ERROR__(state, error)
      expect(state.error).to.equal(error)
    })
  })
})
