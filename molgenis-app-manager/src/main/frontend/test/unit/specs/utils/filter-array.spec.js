import filterArray from 'src/utils/filter-array'

describe('filterArray function', () => {
  const array = [
    {label: 'test', description: 'description'},
    {label: 'te', description: 'oh dear'},
    {label: 'tes', description: 'no match for you'},
    {label: 'test #3418', description: 'a match?'},
    {label: 'will it match?', description: 'this will match test'}
  ]

  const expected = [
    {label: 'test', description: 'description'},
    {label: 'test #3418', description: 'a match?'},
    {label: 'will it match?', description: 'this will match test'}
  ]

  it('should filter an array of objects based on the provided search query', () => {
    const query = 'test'
    const actual = filterArray(array, query)
    expect(actual).to.deep.equal(expected)
  })

  it('should filter an array of objects regardless of casing', () => {
    const query = 'TeSt'
    const actual = filterArray(array, query)
    expect(actual).to.deep.equal(expected)
  })
})
