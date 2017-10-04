import Navigator from 'components/Navigator.vue'

describe('Navigator', () => {
  describe('when created', () => {
    it('should use "Navigator" as name', () => {
      expect(Navigator.name).to.equal('Navigator')
    })
  })

  describe('submitQueryWithSpecialCharacters', () => {
    it('should clear the query and dispatch a call to fetch all packages with special characters', () => {
      Navigator.methods.$store = {
        state: {
          query: 'my-query with spaces and ""'
        },
        dispatch: sinon.spy(),
        commit: sinon.spy()
      }

      Navigator.methods.submitQuery()
      Navigator.methods.$store.commit.should.have.been.calledWith('__SET_PACKAGES__', [])
      Navigator.methods.$store.commit.should.have.been.calledWith('__RESET_PATH__')
      Navigator.methods.$store.dispatch.should.have.been.calledWith('__QUERY_PACKAGES__', 'my-query with spaces and ""')
      Navigator.methods.$store.dispatch.should.have.been.calledWith('__QUERY_ENTITIES__', 'my-query with spaces and ""')
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
      Navigator.methods.$store.commit.should.have.been.calledWith('__SET_QUERY__', undefined)
      Navigator.methods.$store.dispatch.should.have.been.calledWith('__GET_STATE_FOR_PACKAGE__', 'foobar')
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

  describe('reset', () => {
    it('should clear the query and dispatch a call to reset the state', () => {
      Navigator.methods.$store = {
        dispatch: sinon.spy(),
        commit: sinon.spy()
      }

      Navigator.methods.reset('foobar')
      Navigator.methods.$store.commit.should.have.been.calledWith('__SET_QUERY__', undefined)
      Navigator.methods.$store.dispatch.should.have.been.calledWith('__RESET_STATE__')
    })
  })
})
