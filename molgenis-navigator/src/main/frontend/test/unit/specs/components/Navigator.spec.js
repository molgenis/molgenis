import Navigator from 'components/Navigator.vue'

describe('Navigator', () => {
  describe('when created', () => {
    it('should use "Navigator" as name', () => {
      expect(Navigator.name).to.equal('Navigator')
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

  describe('Search', () => {
    it('should reset to initial state when search query is empty', (done) => {
      Navigator.methods.$store = {
        state: {
          query: ''
        },
        dispatch: sinon.spy(),
        commit: sinon.spy()
      }

      Navigator.methods.submitQuery()

      setTimeout(function () {
        Navigator.methods.$store.commit.should.have.been.calledWith('__SET_PACKAGES__', [])
        Navigator.methods.$store.commit.should.have.been.calledWith('__RESET_PATH__')
        Navigator.methods.$store.dispatch.should.have.been.calledWith('__RESET_STATE__')
        done()
      }, 250)
    })

    it('should dispatch a call to fetch all packages with special characters', () => {
      Navigator.methods.$store = {
        state: {
          query: 'my-query with spaces and ""'
        },
        dispatch: sinon.spy(),
        commit: sinon.spy()
      }

      Navigator.methods.submitQuery()

      setTimeout(function () {
        Navigator.methods.$store.commit.should.have.been.calledWith('__SET_PACKAGES__', [])
        Navigator.methods.$store.commit.should.have.been.calledWith('__RESET_PATH__')
        Navigator.methods.$store.dispatch.should.have.been.calledWith('__QUERY_PACKAGES__', 'my-query with spaces and ""')
        Navigator.methods.$store.dispatch.should.have.been.calledWith('__QUERY_ENTITIES__', 'my-query with spaces and ""')
      }, 250)
    })
  })

  describe('toggleSelected', () => {
    it('should select the entity type for the given item', () => {
      const entityTypeId0 = 'entity-type-id-0'
      Navigator.methods.$store = {
        dispatch: sinon.spy()
      }

      Navigator.methods.toggleSelected({type: 'entity', id: entityTypeId0}, true)
      Navigator.methods.$store.dispatch.should.have.been.calledWith('__SELECT_ENTITY_TYPE__', entityTypeId0)
    })

    it('should deselect the entity type for the given item', () => {
      const entityTypeId0 = 'entity-type-id-0'
      Navigator.methods.$store = {
        dispatch: sinon.spy()
      }

      Navigator.methods.toggleSelected({type: 'entity', id: entityTypeId0}, false)
      Navigator.methods.$store.dispatch.should.have.been.calledWith('__DESELECT_ENTITY_TYPE__', entityTypeId0)
    })

    it('should select the package for the given item', () => {
      const packageId0 = 'package-id-0'
      Navigator.methods.$store = {
        dispatch: sinon.spy()
      }

      Navigator.methods.toggleSelected({id: packageId0}, true)
      Navigator.methods.$store.dispatch.should.have.been.calledWith('__SELECT_PACKAGE__', packageId0)
    })

    it('should deselect the package for the given item', () => {
      const packageId0 = 'package-id-0'
      Navigator.methods.$store = {
        dispatch: sinon.spy()
      }

      Navigator.methods.toggleSelected({id: packageId0}, false)
      Navigator.methods.$store.dispatch.should.have.been.calledWith('__DESELECT_PACKAGE__', packageId0)
    })
  })

  describe('isSelected', () => {
    it('should return true if the entity type is selected', () => {
      const entityTypeId0 = 'entity-type-id-0'
      Navigator.methods.$store = {
        state: {
          selectedEntityTypeIds: [entityTypeId0]
        }
      }

      expect(Navigator.methods.isSelected({type: 'entity', id: entityTypeId0})).to.equal(true)
    })

    it('should return false if the entity type is selected', () => {
      const entityTypeId0 = 'entity-type-id-0'
      Navigator.methods.$store = {
        state: {
          selectedEntityTypeIds: []
        }
      }

      expect(Navigator.methods.isSelected({type: 'entity', id: entityTypeId0})).to.equal(false)
    })

    it('should return true if the package is selected', () => {
      const packageId0 = 'package-id-0'
      Navigator.methods.$store = {
        state: {
          selectedPackageIds: [packageId0]
        }
      }

      expect(Navigator.methods.isSelected({id: packageId0})).to.equal(true)
    })

    it('should return false if the package is selected', () => {
      const packageId0 = 'package-id-0'
      Navigator.methods.$store = {
        state: {
          selectedPackageIds: []
        }
      }

      expect(Navigator.methods.isSelected({id: packageId0})).to.equal(false)
    })
  })

  describe('toggleAllSelected', () => {
    it('should select all entity types and packages', () => {
      Navigator.methods.$store = {
        dispatch: sinon.spy()
      }

      Navigator.methods.toggleAllSelected(true)
      Navigator.methods.$store.dispatch.should.have.been.calledWith('__SELECT_ALL_PACKAGES_AND_ENTITY_TYPES__')
    })

    it('should deselect all entity types and packages', () => {
      Navigator.methods.$store = {
        dispatch: sinon.spy()
      }

      Navigator.methods.toggleAllSelected(false)
      Navigator.methods.$store.dispatch.should.have.been.calledWith('__DESELECT_ALL_PACKAGES_AND_ENTITY_TYPES__')
    })
  })

  describe('deleteSelectedItems', () => {
    it('should delete all selected items', () => {
      Navigator.methods.$store = {
        data: {
          allSelected: false
        },
        dispatch: sinon.spy()
      }

      Navigator.methods.deleteSelectedItems()
      Navigator.methods.$store.dispatch.should.have.been.calledWith('__DELETE_SELECTED_PACKAGES_AND_ENTITY_TYPES__')
    })
  })
})
