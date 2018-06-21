import slugService from '../../../../src/service/slugService'

describe('slugify', () => {
  it('should return a slugified string with spaces', () => {
    const textToSlugify = 'test group'
    const slugifiedText = slugService.slugify(textToSlugify)

    expect(slugifiedText).to.equal('test-group')
  })
  it('should return a slugified string with camel casing', () => {
    const textToSlugify = 'Test Group'
    const slugifiedText = slugService.slugify(textToSlugify)

    expect(slugifiedText).to.equal('test-group')
  })
  it('should return a slugified string with commas and dots', () => {
    const textToSlugify = 'Test, Group; 1.'
    const slugifiedText = slugService.slugify(textToSlugify)

    expect(slugifiedText).to.equal('test-group-1')
  })
})
