import Vue from 'vue'
import Vuex from 'vuex'
import BootstrapVue from 'bootstrap-vue/dist/bootstrap-vue.esm'
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'
import {get} from '../molgenisApi'

Vue.use(Vuex)
Vue.use(BootstrapVue)

function zip (arrays) {
  return arrays[0].map(function (_, i) {
    return arrays.map(function (array) { return array[i] })
  })
}

const rsql = state => {
  const constraints = ['rnaseq', 'DNAm', 'DNA', 'wbcc']
    .filter(attr => state[attr])
    .map(attr => attr + '==true')
  const sexConstraints = ['male', 'female']
    .filter(sex => state[sex])
    .map(sex => 'sex==' + sex)
  if (sexConstraints.length) {
    constraints.push('(' + sexConstraints.join(',') + ')')
  }
  const smokingConstraints = []
  if (state.nonSmoking) {
    smokingConstraints.push('smoking==false')
  }
  if (state.smoking) {
    smokingConstraints.push('smoking==true')
  }
  if (smokingConstraints.length) {
    constraints.push('(' + smokingConstraints.join(',') + ')')
  }
  return constraints.length ? `q=${constraints.join(';')}&` : ''
}

export default new Vuex.Store({
  state: {
    rnaseq: false,
    DNAm: false,
    DNA: false,
    wbcc: false,
    metabolomics: false,
    male: false,
    female: false,
    smoking: false,
    nonSmoking: false,
    biobank: null,
    aggs: [],
    biobanks: [],
    server: {
      apiUrl: 'https://molgenis09.gcc.rug.nl/api/'
    },
    token: 'c9e0671dd1f243e4a5794bf57152d8b7'
  },
  mutations: {
    setFilter: function (state, {name, value}) {
      state[name] = value
    },
    setAggs: function (state, {matrix, xLabels}) {
      const sampleCounts = matrix.map(row => row[0])
      const biobanks = xLabels.map(biobank => biobank.abbr)
      const aggs = zip([biobanks, sampleCounts])
        .reduce((agg, v) => ([...agg, v]), [])
      state.aggs = aggs
    },
    setBiobanks: function (state, items) {
      state.biobanks = items
    }
  },
  actions: {
    getBiobanks: function ({ commit, state }) {
      get(state.server, 'v2/WP2_biobanks', state.token)
        .then(response => { commit('setBiobanks', response.items) })
    },
    setFilterAsync: function ({commit, state}, {name, value}) {
      commit('setFilter', {name, value})
      get(state.server, `v2/WP2_RP?${rsql(state)}aggs=x==biobank_abbr`, state.token)
        .then(response => { commit('setAggs', response.aggs) })
    }
  },
  strict: true
})
