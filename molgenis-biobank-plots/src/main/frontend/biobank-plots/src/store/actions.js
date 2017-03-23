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

const matrixValues = aggs => {
  const zipped = zip([aggs.xLabels, aggs.matrix.map(row => row[0])])
  const values = zipped.reduce((acc, val) => ({...acc, [val[0]]: val[1]}), {})
  return { values }
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
        const attributeGraphs = [
          {
            title: 'Data types',
            columns: [
            {type: 'number', label: 'Available', key: 'T'},
            {type: 'number', label: 'Unavailable', key: 'F'},
            {type: 'number', label: 'Unknown', key: 'null'}
            ],
            rows: [
            {label: 'RNAseq', ...matrixValues(responses[2].aggs)},
            {label: 'wbcc', ...matrixValues(responses[3].aggs)},
            {label: 'DNA', ...matrixValues(responses[4].aggs)},
            {label: 'DNAm', ...matrixValues(responses[5].aggs)}
            ]
          }, {
            title: 'Smoking',
            columns: [
              {type: 'number', label: 'Smoking', key: 'T'},
              {type: 'number', label: 'Non-Smoking', key: 'F'},
              {type: 'number', label: 'Unknown', key: 'null'}
            ],
            rows: [
              {label: 'Smoking', ...matrixValues(responses[0].aggs)}
            ]
          }, {
            title: 'Gender',
            columns: [
              {type: 'number', label: 'Male', key: 'male'},
              {type: 'number', label: 'Female', key: 'female'},
              {type: 'number', label: 'Unknown', key: 'null'}
            ],
            rows: [
              {label: 'Gender', ...matrixValues(responses[1].aggs)}
            ]
          }
        ]
        commit(SET_ATTRIBUTE_CHARTS, attributeGraphs)
      }
    )
  }
}
