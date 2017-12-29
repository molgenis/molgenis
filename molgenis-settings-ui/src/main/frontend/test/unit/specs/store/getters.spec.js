import { expect } from 'chai'
import { getMappedFields } from '@/store/getters'

describe('getters', () => {
  describe('getMappedFields', () => {
    it('should return a a forms schema object', () => {
      const state = {
        rawSettings: {
          meta:
            {
              attributes: [
                {
                  name: 'string'
                }
              ]
            }
        },
        settings: []
      }

      const actual = getMappedFields(state)
      const expected = [{
        id: 'string'
      }]

      expect(actual.id).to.deep.equal(expected.id)
    })
  })
})
