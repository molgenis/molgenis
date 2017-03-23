import {zip} from '../utils'

export const RESET_FILTERS = 'RESET_FILTERS'
export const SET_FILTER = 'SET_FILTER'
export const SET_AGGS = 'SET_AGGS'
export const SET_BIOBANKS = 'SET_BIOBANKS'
export const SET_ATTRIBUTE_CHARTS = 'SET_ATTRIBUTE_CHARTS'

export default {
  [RESET_FILTERS] (state) {
    state.transcriptome = false
    state.methylome = false
    state.genotypes = false
    state.wbcc = false
    state.metabolome = false
    state.wgs = false
    state.male = false
    state.female = false
    state.smoking = false
    state.nonSmoking = false
    state.biobank = null
    state.belowTwenty = false
    state.twentyThirty = false
    state.thirtyFourty = false
    state.fourtyFifty = false
    state.fiftySixty = false
    state.sixtySeventy = false
    state.seventyEighty = false
    state.aboveEigthy = false
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
    state.charts = charts
  }
}
