import slugService from '../../../../src/service/asyncUtilService'

describe('callAfter', () => {
  it('should call the give function after n seconds', () => {
    let called = false

    const myFunc = () => {
      called = true
    }

    slugService.callAfter(myFunc, 200)

    setTimeout(() => {
      expect(called).to.equal(true)
    }, 300)
  })
})
