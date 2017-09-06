import { expect } from 'chai'

/**
 * A utility function to help you in testing Vuex actions
 *
 * @example <caption>Example of how to call the testAction function</caption>
 *
 *  ---------actions.js-----------
 *
 *  import api from 'whatever-you-use-as-api'
 *
 *  export const GET_A_NICE_RESPONSE = '__GET_A_NICE_RESPONSE__'
 *
 *  export default {
 *    [GET_A_NICE_RESPONSE] ({commit}, id) {
 *      api.get('/api/response/' + id).then(response => {
 *        commit(SET_RESPONSE, response)
 *      }
 *    }
 *  }
 *
 *  -------actions.spec.js--------
 *
 *  import td from 'testdouble'
 *  import api from 'whatever-you-use-as-api'
 *  import { testAction } from '@molgenis/molgenis-js-test-utils'
 *  import actions from 'store/actions'
 *
 *  it('call an api, and call a mutation with the response', done => {
 *     const response = 'got a nice response'
 *
 *     const get = td.function('api.get')
 *     td.when(get('/api/response/my_id')).thenResolve(response)
 *     td.replace(api, 'get', get)
 *
 *     const options = {
 *       payload: 'my_id',
 *       expectedMutations: [
 *         {type: SET_RESPONSE, payload: response}
 *       ]
 *     }
 *
 *     testAction(actions.__GET_NICE_RESPONSE__, options, done)
 *  })
 *
 * @param action the action you want to test
 * @param options an options object containing payload, state, array of expected mutations, and an array of expected actions
 * @param done used to call done() which closes the Promise from calling an asynchronous api
 */
export const testAction = (action, options, done) => {
  const payload = options.payload ? options.payload : null
  const state = options.state ? options.state : {}
  const expectedMutations = options.expectedMutations ? options.expectedMutations : []
  const expectedActions = options.expectedActions ? options.expectedActions : []

  let mutationCount = 0
  let actionCount = 0

  const commit = (type, payload) => {
    const mutation = expectedMutations[mutationCount]

    try {
      expect(mutation.type).to.equal(type)
      if (payload) {
        expect(mutation.payload).to.deep.equal(payload)
      }
    } catch (error) {
      done(error)
    }

    mutationCount++
    if (mutationCount >= expectedMutations.length) {
      done()
    }
  }

  const dispatch = (type, payload) => {
    const action = expectedActions[actionCount]

    try {
      expect(action.type).to.equal(type)
      if (payload) {
        expect(action.payload).to.deep.equal(payload)
      }
    } catch (error) {
      done(error)
    }

    actionCount++
    if (mutationCount >= expectedMutations.length) {
      done()
    }
  }

  action({commit, dispatch, state}, payload)

  if (expectedMutations.length === 0 && expectedActions.length === 0) {
    expect(mutationCount).to.equal(0)
    expect(actionCount).to.equal(0)
    done()
  }
}

export default {testAction}
