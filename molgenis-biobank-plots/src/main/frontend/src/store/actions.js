// @flow
import {aggregateX, getEntityCollection} from '../molgenisApi'
import 'url-polyfill'
import {SET_BIOBANKS, SET_FILTER, SET_AGGS, SET_ATTRIBUTE_CHARTS, RESET_FILTERS} from './mutations'
import {biobankGraphRsql, attributeGraphRsql} from './getters'
import {zip} from 'ramda'

// eslint-disable-next-line
import type {State, Biobank, Charts} from './state'

export const GET_BIOBANKS = 'GET_BIOBANKS'
export const SET_BIOBANK = 'SET_BIOBANK'
export const SET_FILTER_ASYNC = 'SET_FILTER_ASYNC'
export const RESET_FILTERS_ASYNC = 'RESET_FILTERS_ASYNC'
export const REFRESH_GRAPH = 'REFRESH_GRAPH'
export const REFRESH_ATTRIBUTE_GRAPHS = 'REFRESH_ATTRIBUTE_GRAPHS'

const matrixValues = aggs => {
  const zipped = zip(aggs.xLabels, aggs.matrix.map(row => row[0]))
  const values = zipped.reduce((acc, val) => ({...acc, [val[0]]: val[1]}), {})
  return { values }
}

type ActionParams = {
  commit: Function,
  dispatch: Function,
  state: State
}

export default {
  [GET_BIOBANKS] (params: ActionParams) {
    const {commit, state: {token, apiUrl, entities: {biobanks}}} = params
    getEntityCollection(apiUrl, biobanks, token)
      .then(response => { commit(SET_BIOBANKS, response.items) })
  },
  [SET_BIOBANK] (params: ActionParams, biobank: Biobank) {
    const {commit, dispatch} = params
    commit(SET_FILTER, {name: 'biobank', value: biobank})
    dispatch(REFRESH_ATTRIBUTE_GRAPHS)
  },
  [SET_FILTER_ASYNC] (params: ActionParams, filter: {name: string, value: any}) {
    const {commit, dispatch} = params
    commit(SET_FILTER, filter)
    dispatch(REFRESH_GRAPH)
    dispatch(REFRESH_ATTRIBUTE_GRAPHS)
  },
  [RESET_FILTERS_ASYNC] (params: ActionParams) {
    const {commit, dispatch} = params
    commit(RESET_FILTERS)
    dispatch(REFRESH_GRAPH)
    dispatch(REFRESH_ATTRIBUTE_GRAPHS)
  },
  [REFRESH_GRAPH] (params: ActionParams) {
    const {commit, state} = params
    const {token, apiUrl, entities: {samples}} = state
    const rsql = biobankGraphRsql(state)
    console.log('rsql', rsql)
    aggregateX(apiUrl, samples, 'biobank', rsql, token)
      .then(response => { commit(SET_AGGS, response.aggs) })
  },
  [REFRESH_ATTRIBUTE_GRAPHS] (params: ActionParams) {
    const {commit, state} = params
    const {token, apiUrl, entities: {samples}} = state
    commit(SET_ATTRIBUTE_CHARTS, [])
    const rsql = attributeGraphRsql(state)
    const attributes = ['smoking', 'sex', 'transcriptome', 'wbcc', 'genotypes', 'metabolome', 'methylome', 'wgs', 'ageGroup']
    const promises = attributes.map(attr => aggregateX(apiUrl, samples, attr, rsql, token))
    Promise.all(promises).then(
      responses => {
        const attributeGraphs: Charts = {
          'data_types': {
            title: 'Data types',
            columns: [
              {type: 'number', label: 'Available', key: 'T'},
              {type: 'number', label: 'Unavailable', key: 'F'}
            ],
            rows: [
              {label: 'transcriptome', ...matrixValues(responses[2].aggs)},
              {label: 'wbcc', ...matrixValues(responses[3].aggs)},
              {label: 'genotypes', ...matrixValues(responses[4].aggs)},
              {label: 'metabolome', ...matrixValues(responses[5].aggs)},
              {label: 'methylome', ...matrixValues(responses[6].aggs)},
              {label: 'wgs', ...matrixValues(responses[7].aggs)}
            ]
          },
          'age': {
            title: 'Age distribution',
            columns: [
              {type: 'number', label: '<20', key: '<20'},
              {type: 'number', label: '20-30', key: '20-30'},
              {type: 'number', label: '30-40', key: '30-40'},
              {type: 'number', label: '40-50', key: '40-50'},
              {type: 'number', label: '50-60', key: '50-60'},
              {type: 'number', label: '60-70', key: '60-70'},
              {type: 'number', label: '70-80', key: '70-80'},
              {type: 'number', label: '>80', key: '>80'},
              {type: 'number', label: 'Unknown', key: 'null'}
            ],
            rows: [
              {label: 'Age', ...matrixValues(responses[8].aggs)}
            ]
          },
          'smoking': {
            title: 'Smoking data',
            columns: [
              {type: 'number', label: 'Available', key: 'T'},
              {type: 'number', label: 'Not available', key: 'F'},
              {type: 'number', label: 'Unknown', key: 'null'}
            ],
            rows: [
              {label: 'Smoking data', ...matrixValues(responses[0].aggs)}
            ]
          },
          'gender': {
            title: 'Sex',
            columns: [
              {type: 'number', label: 'Male', key: 'male'},
              {type: 'number', label: 'Female', key: 'female'},
              {type: 'number', label: 'Unknown', key: 'null'}
            ],
            rows: [
              {label: 'Sex', ...matrixValues(responses[1].aggs)}
            ]
          }
        }
        commit(SET_ATTRIBUTE_CHARTS, attributeGraphs)
      }
    )
  }
}
