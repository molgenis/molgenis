import {get} from '../molgenisApi'
import {zip} from './utils'
import {SET_BIOBANKS, SET_FILTER, SET_AGGS, SET_ATTRIBUTE_CHARTS} from './mutations'

export const GET_BIOBANKS = 'GET_BIOBANKS'
export const SET_BIOBANK = 'SET_BIOBANK'
export const SET_FILTER_ASYNC = 'SET_FILTER_ASYNC'

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

const humanReadable = {
  'T': 'True',
  'F': 'False',
  null: 'Unknown',
  'male': 'Male',
  'female': 'Female'
}

const datatypeGraph = responses => {
  const matrixValues = aggs => {
    const zipped = zip([aggs.xLabels, aggs.matrix.map(row => row[0])])
    const vals = zipped.reduce((acc, val) => ({...acc, [humanReadable[val[0]]]: val[1]}), {True: 0, False: 0, Unknown: 0})
    return [vals.True, vals.False, vals.Unknown]
  }
  return {
    title: 'Data types',
    columns: [
      {type: 'string', label: 'label'},
      {type: 'number', label: 'True'},
      {type: 'number', label: 'False'},
      {type: 'number', label: 'Unknown'}
    ],
    rows: [
      ['rnaseq', ...matrixValues(responses[2].aggs)],
      ['wbcc', ...matrixValues(responses[3].aggs)],
      ['DNA', ...matrixValues(responses[4].aggs)],
      ['DNAm', ...matrixValues(responses[5].aggs)]
    ]
  }
}

export const actions = {
  [GET_BIOBANKS] ({ commit, state }) {
    get(state.server, 'v2/WP2_biobanks', state.token)
      .then(response => { commit(SET_BIOBANKS, response.items) })
  },
  [SET_BIOBANK] ({commit, state}, biobank) {
    commit(SET_ATTRIBUTE_CHARTS, [])
    commit(SET_FILTER, {name: 'biobank', value: biobank})
    const filter = rsql(state)
    const q = filter.length ? `q=${filter};biobank_abbr==${biobank}&` : `biobank_abbr==${biobank}&`
    const attributes = ['smoking', 'sex', 'rnaseq', 'wbcc', 'DNA', 'DNAm']
    const promises = attributes.map(attr => get(state.server, `/v2/WP2_RP?${q}&aggs=x==${attr}`, state.token))
    Promise.all(promises).then(
      responses => {
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
        const attributeGraphs = [datatypeGraph(responses), sexGraph, smokingGraph]
        commit(SET_ATTRIBUTE_CHARTS, attributeGraphs)
      }
    )
  },
  [SET_FILTER_ASYNC] ({commit, state}, {name, value}) {
    commit(SET_FILTER, {name, value})
    const filter = rsql(state)
    const q = filter.length ? `q=${filter}&` : ''
    get(state.server, `v2/WP2_RP?${q}aggs=x==biobank_abbr`, state.token)
      .then(response => { commit(SET_AGGS, response.aggs) })
  }
}
