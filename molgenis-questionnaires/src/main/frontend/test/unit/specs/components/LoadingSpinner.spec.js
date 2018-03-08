import LoadingSpinner from 'src/components/LoadingSpinner'
import { shallow } from '@vue/test-utils'

describe('LoadingSpinner component', () => {
  const propsData = {message: 'loading message'}

  it('should render a message', () => {
    const wrapper = shallow(LoadingSpinner, {propsData})

    const p = wrapper.find('p')
    expect(p.text()).to.equal('loading message')
  })
})
