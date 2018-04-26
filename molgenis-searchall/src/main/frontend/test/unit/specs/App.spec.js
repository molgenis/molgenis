import App from 'App'

describe('App', () => {
  it('should use "molgenis-searchall" as name', () => {
    expect(App.name).to.equal('molgenis-searchall')
  })
})
