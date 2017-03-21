import {zip} from './utils'

export const RESET_FILTERS = 'RESET_FILTERS'
export const SET_FILTER = 'SET_FILTER'
export const SET_AGGS = 'SET_AGGS'
export const SET_BIOBANKS = 'SET_BIOBANKS'
export const SET_ATTRIBUTE_CHARTS = 'SET_ATTRIBUTE_CHARTS'

export const mutations = {
  [RESET_FILTERS] (state) {
    state.rnaseq = false
    state.DNAm = false
    state.DNA = false
    state.wbcc = false
    state.metabolomics = false
    state.male = false
    state.female = false
    state.smoking = false
    state.nonSmoking = false
    state.biobank = null
  },
  [SET_FILTER] (state, {name, value}) {
    state[name] = value
  },
  [SET_AGGS] (state, {matrix, xLabels}) {
    const sampleCounts = matrix.map(row => row[0])
    const biobanks = xLabels.map(biobank => biobank.abbr)
    state.aggs = zip([biobanks, sampleCounts])
    state.numberOfSamples = sampleCounts.reduce((a, b) => a + b, 0)
  },
  [SET_BIOBANKS] (state, items) {
    state.biobanks = items
  },
  [SET_ATTRIBUTE_CHARTS] (state, charts) {
    state.attributeCharts = charts
  }
}
