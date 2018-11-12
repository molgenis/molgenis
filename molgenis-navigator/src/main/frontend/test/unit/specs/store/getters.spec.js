import getters from '@/store/getters'

describe('getters', () => {
  describe('query', () => {
    it('should return the route query param q value', () => {
      const state = {
        route: {
          query: {
            q: 'text'
          }
        }
      }

      expect(getters.query(state)).to.eql('text')
    })
    it('should return undefined if route query param q is undefined', () => {
      const state = {
        route: {
          query: {
          }
        }
      }

      expect(getters.query(state)).to.eql(undefined)
    })
  })
  describe('folderId', () => {
    it('should return the route param folderId', () => {
      const state = {
        route: {
          params: {
            folderId: 'folderId'
          }
        }
      }

      expect(getters.folderId(state)).to.eql('folderId')
    })
    it('should return undefined if route param folderId is undefined', () => {
      const state = {
        route: {
          params: {
          }
        }
      }

      expect(getters.folderId(state)).to.eql(undefined)
    })
  })
  describe('folderPath', () => {
    it('should return the folder path for the folder', () => {
      const state = {
        folder: {
          id: 'grandchild',
          label: 'grandchild',
          readonly: false,
          parent: {
            id: 'child',
            label: 'child',
            readonly: false,
            parent: {
              id: 'parent',
              label: 'parent',
              readonly: false
            }
          }
        }
      }
      expect(getters.folderPath(state)).to.have.ordered.deep.members(
        [{
          id: 'parent',
          label: 'parent'
        }, {
          id: 'child',
          label: 'child'
        }, {
          id: 'grandchild',
          label: 'grandchild'
        }])
    })
    it('should return an empty array when folder is absent', () => {
      const state = {
        folder: null
      }
      expect(getters.folderPath(state)).to.eql([])
    })
  })
  describe('nrSelectedResources', () => {
    it('should return the number of selected resources', () => {
      const state = {
        selectedResources: [{id: '0', label: 'label0'}, {id: '1', label: 'label1'}]
      }

      expect(getters.nrSelectedResources(state)).to.eql(2)
    })
  })
  describe('nrClipboardResources', () => {
    it('should return the number of clipboard resourcse', () => {
      const state = {
        clipboard: {
          mode: 'CUT',
          resources: [{id: '0', label: 'label0'}, {id: '1', label: 'label1'}]
        }
      }
      expect(getters.nrClipboardResources(state)).to.eql(2)
    })
    it('should return zero when clipboard is absent', () => {
      const state = {
        clipboard: null
      }
      expect(getters.nrClipboardResources(state)).to.eql(0)
    })
  })
})
