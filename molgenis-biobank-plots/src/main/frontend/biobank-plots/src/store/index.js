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
  return constraints.join(';')
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
    attributeCharts: [{ // http://molgenis09.gcc.rug.nl/api/v2/WP2_RP?q=DNAm==true;biobank_abbr==NTR&aggs=x==sex
      title: 'Sex',
      rows: [
        ['sex', 194, 127, 456]
      ],
      columns: [
        {type: 'string', label: 'label'},
        {type: 'number', label: 'female'},
        {type: 'number', label: 'male'},
        {type: 'number', label: 'Unknown'}
      ]
    }, { // http://molgenis09.gcc.rug.nl/api/v2/WP2_RP?q=DNAm==true;biobank_abbr==NTR&aggs=x==DNA and
      // http://molgenis09.gcc.rug.nl/api/v2/WP2_RP?q=DNAm==true;biobank_abbr==NTR&aggs=x==rnaseq
      title: 'DNA',
      rows: [
        ['DNA', 13, 764, 0],
        ['rnaseq', 473, 304, 0]
      ],
      columns: [
        {type: 'string', label: 'label'},
        {type: 'number', label: 'True'},
        {type: 'number', label: 'False'},
        {type: 'number', label: 'Unknown'}
      ]
    }],
    server: {
      apiUrl: 'https://molgenis09.gcc.rug.nl/api/'
    },
    token: '3121096f23304f378b98d57fbdce8d76'
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
    },
    setAttributeCharts: function (state, charts) {
      state.attributeCharts = charts
    }
  },
  actions: {
    getBiobanks: function ({ commit, state }) {
      get(state.server, 'v2/WP2_biobanks', state.token)
        .then(response => { commit('setBiobanks', response.items) })
    },
    setBiobank: function ({commit, state}, biobank) {
      console.log('setBiobank')
      commit('setAttributeCharts', [])
      commit('setFilter', {name: 'biobank', value: biobank})
      const filter = rsql(state)
      const q = filter.length ? `q=${filter};biobank_abbr==${biobank}&` : `biobank_abbr==${biobank}&`
      get(state.server, `/v2/WP2_RP?${q}&aggs=x==sex`, state.token)
        .then(response => {
          const sexGraph = {
            rows: [['sex', ...response.aggs.matrix.map(row => row[0])]],
            columns: [
            {type: 'string', label: 'label'},
            {type: 'number', label: 'Female'},
            {type: 'number', label: 'Male'},
            {type: 'number', label: 'Unknown'}
            ]
          }
          const attributeGraphs = [sexGraph]
          commit('setAttributeCharts', attributeGraphs)
        })
    },
    setFilterAsync: function ({commit, state}, {name, value}) {
      commit('setFilter', {name, value})
      const filter = rsql(state)
      const q = filter.length ? `q=${filter}&` : ''
      get(state.server, `v2/WP2_RP?${q}aggs=x==biobank_abbr`, state.token)
        .then(response => { commit('setAggs', response.aggs) })
    }
  },
  strict: true
})
