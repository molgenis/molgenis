// Import Vue and the component being tested
import Vue from 'vue'
import MetadataManagerAttributeEditForm from '../../../src/components/MetadataManagerAttributeEditForm.vue'

describe('MetadataManagerAttributeEditForm', () => {
  it('renders correctly with different props', () => {
    const vm = new Vue(MetadataManagerAttributeEditForm).$mount()
    console.log('mounted object: ' + JSON.stringify(vm.isOneToManyType))
   // expect(vm.name).toBe() = 'metadata-manager-attribute-edit-form'
    // vm.message = 'foo'
    // wait a "tick" after state change before asserting DOM updates
    // Vue.nextTick(() => {
    //   expect(vm.$el.textContent).toBe('foo')
    //   // done()
    // })
  })
})

