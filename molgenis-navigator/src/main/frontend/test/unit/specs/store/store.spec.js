import api from '@molgenis/molgenis-api-client'
import store from 'src/store'

describe('store', function () {
  describe('initial state', function () {
    it('should have a empty package list', () => {
      expect(store.state.packages).to.be.empty
    })
  })

  describe('actions', () => {
    let get = sinon.stub(api, 'get')
    afterEach(function () {
      get.reset()
    })

    describe('QUERY_PACKAGES', function () {
      it('should fetch the packages and store them in the state', function (done) {
        // mock api call
        const package1 = {id: 'pack1', label: 'packLabel1'}
        const apiResponse = {
          items: [package1]
        }
        let getSuccess = Promise.resolve(apiResponse)
        get.onFirstCall().returns(getSuccess)

        // execute
        store.dispatch('QUERY_PACKAGES', 'my-test-query')
          .then(function () {
            expect(store.state.packages[0]).to.equal(package1)
            done()
          })
      })

      it('should pass the error message to the state when the get fails', function (done) {
        // mock api response
        let getFail = Promise.reject('an error yo')
        get.onFirstCall().returns(getFail)

        // execute
        store.dispatch('QUERY_PACKAGES', 'my-test-query')
          .catch(function () {
            expect(store.state.error).to.equal('an error yo')
            done()
          })
      })
    })

    describe('QUERY_ENTITIES', function () {
      it('should query the entities and store result in the state', function (done) {
        const entities = [{id: 'e1', label: 'el1'}]
        const apiResponse = {
          items: entities
        }
        let getSuccess = Promise.resolve(apiResponse)
        get.onFirstCall().returns(getSuccess)

        // execute
        store.dispatch('QUERY_ENTITIES', 'my-test-query')
          .then(function () {
            expect(store.state.entities[0].id).to.equal('e1')
            expect(store.state.entities[0].label).to.equal('el1')
            expect(store.state.entities[0].description).to.equal(undefined)
            expect(store.state.entities[0].type).to.equal('entity')
            done()
          })
      })

      it('should set the error when the query fails', function (done) {
        // mock api response
        let getFail = Promise.reject('error on entity query')
        get.onFirstCall().returns(getFail)

        // execute
        store.dispatch('QUERY_ENTITIES', 'my-test-query')
          .catch(function () {
            expect(store.state.error).to.equal('error on entity query')
            done()
          })
      })
    })

    describe('RESET_STATE', function () {
      it('should place the root level packages on the state and clear the path en entities ', function (done) {
        const child = {id: 'c1', label: 'child', parent: 'parent'}
        const root1 = {id: 'r1', label: 'root1'}
        const root2 = {id: 'r2', label: 'root2'}
        const apiResponse = {
          items: [child, root1, root2]
        }
        let getSuccess = Promise.resolve(apiResponse)
        get.onFirstCall().returns(getSuccess)
        store.dispatch('RESET_STATE').then(function () {
          expect(store.state.packages.length).to.equal(2)
          expect(store.state.packages[0]).to.equal(root1)
          expect(store.state.packages[1]).to.equal(root2)
          expect(store.state.path).to.be.empty
          expect(store.state.enties).to.be.empty
          done()
        })
      })
    })

    describe('GET_STATE_FOR_PACKAGE', function (done) {
      it('should when no package if given reset the state)', function () {
        const package1 = {id: 'pack1', label: 'packLabel1'}
        const package2 = {id: 'pack2', label: 'packLabel2'}
        const package3 = {id: 'pack3', label: 'packLabel3'}
        const apiResponse = {
          items: [package1]
        }
        let getSuccess = Promise.resolve(apiResponse)
        get.onFirstCall().returns(getSuccess)

        store.dispatch('GET_STATE_FOR_PACKAGE', '').then(function () {
          expect(store.state.packages[0]).to.equal(package1)
          expect(store.state.packages[1]).to.equal(package2)
          expect(store.state.packages[2]).to.equal(package3)
          expect(store.state.path).to.be.empty
          expect(store.state.enties).to.be.empty
          done()
        })
      })
      it('should set the error is the given package id is not found', function (done) {
        const apiResponse = {
          items: [
            {id: 'pack1', label: 'packLabel1'},
            {id: 'pack3', label: 'packLabel3'}
          ]
        }
        let getSuccess = Promise.resolve(apiResponse)
        get.onFirstCall().returns(getSuccess)

        store.dispatch('GET_STATE_FOR_PACKAGE', 'pack2')
          .catch(function () {
            expect(store.state.error).to.equal('couldn\'t find package.')
            done()
          })
      })
      it('should fetch the content for the given packages and build the path', function (done) {
        const apiResponse = {
          items: [
            {id: 'level0', label: 'child', parent: {id: 'level1'}},
            {id: 'level1', label: 'parent', parent: {id: 'level2'}},
            {id: 'level2', label: 'grandparent'}
          ]
        }
        const entities = [{id: 'e1', label: 'el1'}]
        let getPackageSuccess = Promise.resolve(apiResponse)
        let getEntitiesSuccess = Promise.resolve({items: entities})
        get.onFirstCall().returns(getPackageSuccess)
        get.onSecondCall().returns(getEntitiesSuccess)

        store.dispatch('GET_STATE_FOR_PACKAGE', 'level1')
          .then(function () {
            expect(store.state.packages[0].id).to.equal('level0')
            expect(store.state.entities[0].id).to.equal('e1')
            expect(store.state.path[0].label).to.equal('grandparent')
            expect(store.state.path[1].label).to.equal('parent')
            done()
          })
      })
    })
  })
})
