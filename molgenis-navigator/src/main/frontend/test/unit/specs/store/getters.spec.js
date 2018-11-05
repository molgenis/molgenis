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

      expect(getters.query(state)).to.deep.equal('text')
    })
    it('should return undefined if route query param q is undefined', () => {
      const state = {
        route: {
          query: {
          }
        }
      }

      expect(getters.query(state)).to.deep.equal(undefined)
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

      expect(getters.folderId(state)).to.deep.equal('folderId')
    })
    it('should return undefined if route param folderId is undefined', () => {
      const state = {
        route: {
          params: {
          }
        }
      }

      expect(getters.folderId(state)).to.deep.equal(undefined)
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
      expect(getters.folderPath(state)).to.deep.equal([])
    })
  })
  describe('nrSelectedItems', () => {
    it('should return the number of selected items', () => {
      const state = {
        selectedItems: [{id: '0', label: 'label0'}, {id: '1', label: 'label1'}]
      }

      expect(getters.nrSelectedItems(state)).to.deep.equal(2)
    })
  })
  describe('nrClipboardItems', () => {
    it('should return the number of clipboard items', () => {
      const state = {
        clipboard: {
          mode: 'cut',
          items: [{id: '0', label: 'label0'}, {id: '1', label: 'label1'}]
        }
      }
      expect(getters.nrClipboardItems(state)).to.deep.equal(2)
    })
    it('should return zero when clipboard is absent', () => {
      const state = {
        clipboard: null
      }
      expect(getters.nrClipboardItems(state)).to.deep.equal(0)
    })
  })
})
