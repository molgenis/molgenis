// Import Vue and the component being tested
import Vue from 'vue'
import Vuex from 'vuex'
import store from '../../../../src/store/index'
import { UPDATE_EDITOR_ENTITY_TYPE, SET_SELECTED_ATTRIBUTE_ID, SET_EDITOR_ENTITY_TYPE } from '../../../../src/store/mutations'
import MetadataManagerAttributeEditForm from '../../../../src/components/MetadataManagerAttributeEditForm.vue'

Vue.use(Vuex)

describe('MetadataManagerAttributeEditForm', () => {
  // Mock i18n to avoid server calls
  const i18n = function (x) { return x }
  Vue.prototype.$t = i18n
  Vue.filter('i18n', i18n)

  const vm = new Vue({
    store,
    template: '<MetadataManagerAttributeEditForm />',
    components: { MetadataManagerAttributeEditForm }
  })

  // vul de store met 1 attribuut en selecteer deze
  store.commit(SET_EDITOR_ENTITY_TYPE, { attributes: [] })
  store.commit(UPDATE_EDITOR_ENTITY_TYPE, { key: 'attributes', value: [{id: 'sasdfsadfdafs'}] })
  store.commit(SET_SELECTED_ATTRIBUTE_ID, 'sasdfsadfdafs')

  vm.$mount()
  const form = vm.$children[0]

  it('sets refEntityType when you specify mappedByAttribute', (done) => {
    form.type = 'ONETOMANY'
    form.mappedByAttribute = { id: 'aaaa', label: 'author', entity: { id: 'b_book', label: 'book' } }

    expect(form.selectedAttribute.refEntityType).to.deep.equal({ id: 'b_book', label: 'book' })

    done()
  })

  it('wipes refEntityType when you change back type', (done) => {
    form.type = 'ONETOMANY'
    form.mappedByAttribute = { id: 'aaaa', label: 'author', entity: { id: 'b_book', label: 'book' } }

    expect(form.selectedAttribute.refEntityType).to.deep.equal({ id: 'b_book', label: 'book' })

    form.type = 'INT'

    // see http://chaijs.com/api/bdd/
    // I think it is something with the binding of objects in expect mode
    // When you access the property in the selectedAttribute the attribute has the right value
    expect(form.selectedAttribute.refEntityType).to.be.a('null')
    expect(form.selectedAttribute.mappedByAttribute).to.be.a('null')

    done()
  })
})
