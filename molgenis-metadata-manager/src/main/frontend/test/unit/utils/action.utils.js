import { expect } from 'chai'

export default (action, payload, state, expectedMutations, expectedActions, done) => {
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
