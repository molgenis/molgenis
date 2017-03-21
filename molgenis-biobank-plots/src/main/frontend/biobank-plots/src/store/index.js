import Vuex from 'vuex'
import {get} from '../molgenisApi'

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
    token: 'e4da2ad59e8c4b339c2225bc7911d0d5'
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
      const attributes = ['smoking', 'sex', 'rnaseq', 'wbcc', 'DNA', 'DNAm']
      const promises = attributes.map(attr => get(state.server, `/v2/WP2_RP?${q}&aggs=x==${attr}`, state.token))
      Promise.all(promises).then(
        responses => {
          const humanReadable = {
            'T': 'True',
            'F': 'False',
            null: 'Unknown',
            'male': 'Male',
            'female': 'Female'
          }
          const smokingGraph = {
            title: 'Smoking',
            rows: [['smoking', ...responses[0].aggs.matrix.map(row => row[0])]],
            columns: [
              {type: 'string', label: 'label'},
              ...responses[0].aggs.xLabels.map(l => ({type: 'number', label: humanReadable[l]}))
            ]
          }
          const sexGraph = {
            title: 'Sex',
            rows: [['sex', ...responses[1].aggs.matrix.map(row => row[0])]],
            columns: [
              {type: 'string', label: 'label'},
              ...responses[1].aggs.xLabels.map(l => ({type: 'number', label: humanReadable[l]}))
            ]
          }
          const datatypeGraph = {
            title: 'Data types',
            columns: [
              {type: 'string', label: 'label'},
              {type: 'number', label: 'True'},
              {type: 'number', label: 'False'},
              {type: 'number', label: 'Unknown'}
            ],
            rows: [
              ['rnaseq', ...responses[2].aggs.matrix.map(row => row[0])],
              ['wbcc', ...responses[3].aggs.matrix.map(row => row[0])],
              ['DNA', ...responses[4].aggs.matrix.map(row => row[0])],
              ['DNAm', ...responses[5].aggs.matrix.map(row => row[0])]
            ]
          }
          const attributeGraphs = [datatypeGraph, sexGraph, smokingGraph]
          commit('setAttributeCharts', attributeGraphs)
        }
      )
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
