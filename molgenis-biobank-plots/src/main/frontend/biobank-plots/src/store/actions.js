import {get} from '../molgenisApi'
import {URL} from 'isomorphic-url'
import {zip} from '../utils'
import {SET_BIOBANKS, SET_FILTER, SET_AGGS, SET_ATTRIBUTE_CHARTS, RESET_FILTERS} from './mutations'
import {biobankGraphRsql, attributeGraphRsql} from './getters'

export const GET_BIOBANKS = 'GET_BIOBANKS'
export const SET_BIOBANK = 'SET_BIOBANK'
export const SET_FILTER_ASYNC = 'SET_FILTER_ASYNC'
export const RESET_FILTERS_ASYNC = 'RESET_FILTERS_ASYNC'
export const REFRESH_GRAPH = 'REFRESH_GRAPH'
export const REFRESH_ATTRIBUTE_GRAPHS = 'REFRESH_ATTRIBUTE_GRAPHS'

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
      ['RNAseq', ...matrixValues(responses[2].aggs)],
      ['wbcc', ...matrixValues(responses[3].aggs)],
      ['DNA', ...matrixValues(responses[4].aggs)],
      ['DNAm', ...matrixValues(responses[5].aggs)]
    ]
  }
}

export default {
  [GET_BIOBANKS] ({ commit, state }) {
    get(state.apiUrl + 'v2/WP2_biobanks', state.token)
      .then(response => { commit(SET_BIOBANKS, response.items) })
  },
  [SET_BIOBANK] ({commit, dispatch}, biobank) {
    commit(SET_FILTER, {name: 'biobank', value: biobank})
    dispatch(REFRESH_ATTRIBUTE_GRAPHS)
  },
  [SET_FILTER_ASYNC] ({commit, dispatch}, {name, value}) {
    commit(SET_FILTER, {name, value})
    dispatch(REFRESH_GRAPH)
    dispatch(REFRESH_ATTRIBUTE_GRAPHS)
  },
  [RESET_FILTERS_ASYNC] ({commit, dispatch}) {
    commit(RESET_FILTERS)
    dispatch(REFRESH_GRAPH)
    dispatch(REFRESH_ATTRIBUTE_GRAPHS)
  },
  [REFRESH_GRAPH] ({commit, state}) {
    const {token, apiUrl} = state
    const url = new URL(apiUrl + 'v2/WP2_RP')
    const q = biobankGraphRsql(state)
    if (q) {
      url.searchParams.append('q', q)
    }
    url.searchParams.append('aggs', 'x==biobank_abbr')
    get(url, token).then(response => { commit(SET_AGGS, response.aggs) })
  },
  [REFRESH_ATTRIBUTE_GRAPHS] ({commit, state}) {
    commit(SET_ATTRIBUTE_CHARTS, [])
    const {token, apiUrl} = state
    const url = new URL(apiUrl + 'v2/WP2_RP')
    const q = attributeGraphRsql(state)
    if (q) {
      url.searchParams.append('q', q)
    }
    const attributes = ['smoking', 'sex', 'rnaseq', 'wbcc', 'DNA', 'DNAm']
    const promises = attributes.map(attr => {
      url.searchParams.set('aggs', `x==${attr}`)
      return get(url, token)
    })
    Promise.all(promises).then(
      responses => {
        const smokingGraph = {
          title: 'Smoking',
          rows: [['Smoking', ...responses[0].aggs.matrix.map(row => row[0])]],
          columns: [
            {type: 'string', label: 'label'},
            ...responses[0].aggs.xLabels.map(l => ({type: 'number', label: humanReadable[l]}))
          ]
        }
        const sexGraph = {
          title: 'Gender',
          rows: [['Gender', ...responses[1].aggs.matrix.map(row => row[0])]],
          columns: [
            {type: 'string', label: 'label'},
            ...responses[1].aggs.xLabels.map(l => ({type: 'number', label: humanReadable[l]}))
          ]
        }
        const attributeGraphs = [datatypeGraph(responses), sexGraph, smokingGraph]
        commit(SET_ATTRIBUTE_CHARTS, attributeGraphs)
      }
    )
  }
}
