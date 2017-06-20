import Navigator from 'components/Navigator.vue'

describe('Navigator', () => {
  describe('when created', () => {
    it('should use "Navigator" as name', () => {
      expect(Navigator.name).to.equal('Navigator')
    })
  })

  describe('submitQuery', () => {
    it('should clear the query and dispatch a call to fetch all packages', () => {
      Navigator.methods.$store = {
        state: {
          query: 'my-query'
        },
        dispatch: sinon.spy(),
        commit: sinon.spy()
      }

      Navigator.methods.submitQuery()
      Navigator.methods.$store.commit.should.have.been.calledWith('SET_PACKAGES', [])
      Navigator.methods.$store.commit.should.have.been.calledWith('RESET_PATH')
      Navigator.methods.$store.dispatch.should.have.been.calledWith('QUERY_PACKAGES', 'my-query')
      Navigator.methods.$store.dispatch.should.have.been.calledWith('QUERY_ENTITIES', 'my-query')
    })
  })

  describe('clearQuery', () => {
    it('should clear the query and dispatch a call to fetch all packages', () => {
      Navigator.methods.$store = {
        dispatch: sinon.spy(),
        commit: sinon.spy()
      }

      Navigator.methods.clearQuery('foobar')
      Navigator.methods.$store.commit.should.have.been.calledWith('SET_QUERY', undefined)
      Navigator.methods.$store.dispatch.should.have.been.calledWith('QUERY_PACKAGES')
    })
  })

  describe('selectPackage', () => {
    Navigator.methods.$store = {
      state: {},
      dispatch: sinon.spy(),
      commit: sinon.spy()
    }

    it('should clear the query and fetch the package by id', () => {
      Navigator.methods.selectPackage('foobar')
      Navigator.methods.$store.commit.should.have.been.calledWith('SET_QUERY', undefined)
      Navigator.methods.$store.dispatch.should.have.been.calledWith('GET_STATE_FOR_PACKAGE', 'foobar')
    })
  })

  describe('isLast', () => {
    const item1 = {
      id: 1
    }
    const item2 = {
      id: 2
    }
    const item3 = {
      id: 3
    }
    const list = [item1, item2, item3]

    it('should should return true if the item is the last item in the list', () => {
      expect(Navigator.methods.isLast(list, item3)).to.equal(true)
    })

    it('should should return false if the item is not last item in the list', () => {
      expect(Navigator.methods.isLast(list, item2)).to.equal(false)
      expect(Navigator.methods.isLast(list, item1)).to.equal(false)
    })
  })
})
