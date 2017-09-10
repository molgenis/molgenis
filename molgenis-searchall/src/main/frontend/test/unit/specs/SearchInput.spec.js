import SearchInput from 'components/SearchInput.vue'

describe('SearchInput', () => {
  describe('when created', () => {
    it('should use "search-input" as name', () => {
      expect(SearchInput.name).to.equal('search-input')
    })
  })

  describe('submitQuery', () => {
    it('should submit the query and update the "submitted" flag', () => {
      SearchInput.methods.$store = {
        state: {
          query: 'my-query'
        },
        dispatch: sinon.spy(),
        commit: sinon.spy()
      }

      SearchInput.methods.submitQuery()
      SearchInput.methods.$store.commit.should.have.been.calledWith('SET_SUBMITTED', true)
      SearchInput.methods.$store.dispatch.should.have.been.calledWith('SEARCH_ALL', 'my-query')
    })
  })
  describe('clearQuery', () => {
    it('should clear the query and update the "submitted" flag', () => {
      SearchInput.methods.$store = {
        state: {
          query: 'my-query'
        },
        dispatch: sinon.spy(),
        commit: sinon.spy()
      }

      SearchInput.methods.clearQuery()
      SearchInput.methods.$store.commit.should.have.been.calledWith('SET_SUBMITTED', false)
      SearchInput.methods.$store.commit.should.have.been.calledWith('SET_SEARCHTERM', undefined)
    })
  })
})
