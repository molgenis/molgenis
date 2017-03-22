import { transformToRSQL } from '../rsql/transformer'
import { getHumanReadable } from '../rsql'

const mapChart = (chart) => ({
  columns: [{label: 'label', type: 'string'}, ...chart.columns],
  rows: chart.rows.map(row => [row.label, ...chart.columns.map(column => row.values[column.key] || 0)]),
  title: chart.title
})

const attributeCharts = (state) => state.charts.map(mapChart)

const constraints = (state, includingBiobank) => {
  const booleanConstraints = ['rnaseq', 'DNAm', 'DNA', 'wbcc']
    .filter(attr => state[attr])
    .map(attr => ({selector: attr, comparison: '==', arguments: 'true'}))

  let sexConstraints = ['male', 'female']
    .filter(sex => state[sex])
    .map(sex => ({selector: 'sex', comparison: '==', arguments: sex}))
  if (sexConstraints.length > 1) {
    sexConstraints = [{operator: 'OR', operands: sexConstraints}]
  }

  let smokingConstraints = []
  if (state.nonSmoking) {
    smokingConstraints.push({selector: 'smoking', comparison: '==', arguments: false})
  }
  if (state.smoking) {
    smokingConstraints.push({selector: 'smoking', comparison: '==', arguments: true})
  }
  if (smokingConstraints.length > 1) {
    smokingConstraints = [{operator: 'OR', operands: smokingConstraints}]
  }

  const biobankConstraint = []
  if (state.biobank && includingBiobank) {
    biobankConstraint.push({selector: 'biobank_abbr', comparison: '==', arguments: state.biobank})
  }

  return [...booleanConstraints, ...sexConstraints, ...smokingConstraints, ...biobankConstraint]
}

const rsqlTree = (state, includingBiobank) => {
  const operands = constraints(state, includingBiobank)
  return operands.length === 0 ? null
    : (operands.length === 1 ? operands[0]
      : {operator: 'AND', operands: operands})
}

export const biobankGraphRsql = (state) => {
  const rsql = rsqlTree(state, false)
  return rsql ? transformToRSQL(rsql) : null
}

export const attributeGraphRsql = (state) => {
  const rsql = rsqlTree(state, true)
  return rsql ? transformToRSQL(rsql) : null
}

export const readableFilters = (state) => {
  const rsql = rsqlTree(state, true)
  return rsql ? getHumanReadable(rsql) : null
}

export default {
  rsqlTree,
  biobankGraphRsql,
  attributeGraphRsql,
  readableFilters,
  attributeCharts
}
