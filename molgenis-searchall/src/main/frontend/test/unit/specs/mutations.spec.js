import mutations from 'store/mutations'

describe('mutations', () => {
  describe('SET_RESULTS', () => {
    it('should set the results in the state', () => {
      const state = {
        result: {
          query: '',
          response: null
        }
      }

      const payload = {
        query: 'test',
        response: {
          packages: [
            {id: 'package1'}
          ],
          entityTypes: [
            {id: 'entityType1'}
          ]
        }
      }

      mutations.__SET_RESULTS__(state, payload)
      expect(state.result).to.deep.equal(payload)
    })
  })

  describe('SET_ERRORS', () => {
    it('should set the errors in the state', () => {
      const state = {
        error: ''
      }

      const payload = 'error'

      mutations.__SET_ERRORS__(state, payload)
      expect(state.error).to.equal(payload)
    })
  })

  describe('SET_LOADING', () => {
    it('should set loading in the state', () => {
      const state = {
        loading: false
      }

      const payload = true

      mutations.__SET_LOADING__(state, payload)
      expect(state.loading).to.equal(payload)
    })
  })

  describe('RESET_RESPONSE', () => {
    it('should reset the result object in the state', () => {
      const state = {
        result: {
          query: 'test',
          response: {
            id: 'awesome object'
          }
        }
      }

      mutations.__RESET_RESPONSE__(state)
      expect(state.result).to.deep.equal({
        query: '',
        response: null
      })
    })
  })
})
