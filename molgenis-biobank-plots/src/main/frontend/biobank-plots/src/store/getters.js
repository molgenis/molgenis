import { transformToRSQL } from '../rsql/transformer'

const mapChart = (chart) => ({
  columns: [{label: 'label', type: 'string'}, ...chart.columns],
  rows: chart.rows.map(row => [row.label, ...chart.columns.map(column => row.values[column.key] || 0)]),
  title: chart.title
})

const attributeCharts = (state) => ({
  gender: mapChart(state.charts.gender),
  smoking: mapChart(state.charts.smoking)
})

const constraints = (state, includingBiobank) => {
  const booleanConstraints = ['smoking', 'sex', 'transcriptome', 'wbcc', 'genotypes', 'metabolome', 'methylome', 'wgs']
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

  let ageConstraints = []
  if (!state.belowTwenty || !state.twentyThirty || !state.thirtyFourty ||
    !state.fourtyFifty || !state.fiftySixty || !state.sixtySeventy ||
    !state.seventyEighty || !state.aboveEigthy) {
    if (state.belowTwenty) {
      ageConstraints.push({selector: 'ageGroup', comparison: '==', arguments: '<20'})
    }
    if (state.twentyThirty) {
      ageConstraints.push({selector: 'ageGroup', comparison: '==', arguments: '20-30'})
    }
    if (state.thirtyFourty) {
      ageConstraints.push({selector: 'ageGroup', comparison: '==', arguments: '30-40'})
    }
    if (state.fourtyFifty) {
      ageConstraints.push({selector: 'ageGroup', comparison: '==', arguments: '40-50'})
    }
    if (state.fiftySixty) {
      ageConstraints.push({selector: 'ageGroup', comparison: '==', arguments: '50-60'})
    }
    if (state.sixtySeventy) {
      ageConstraints.push({selector: 'ageGroup', comparison: '==', arguments: '60-70'})
    }
    if (state.seventyEighty) {
      ageConstraints.push({selector: 'ageGroup', comparison: '==', arguments: '70-80'})
    }
    if (state.aboveEigthy) {
      ageConstraints.push({selector: 'ageGroup', comparison: '==', arguments: '>80'})
    }
    if (ageConstraints.length > 1) {
      ageConstraints = [{operator: 'OR', operands: ageConstraints}]
    }
  }

  const biobankConstraint = []
  if (state.biobank && includingBiobank) {
    biobankConstraint.push({selector: 'biobank', comparison: '==', arguments: state.biobank})
  }

  return [...booleanConstraints, ...sexConstraints, ...smokingConstraints, ...ageConstraints, ...biobankConstraint]
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

export default {
  rsqlTree,
  biobankGraphRsql,
  attributeGraphRsql,
  attributeCharts
}
