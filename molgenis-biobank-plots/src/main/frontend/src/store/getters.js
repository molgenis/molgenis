
import type {State, Chart} from './state'
import type {Constraint} from '../rsql/Constraint'
// eslint-disable-next-line
import {SimpleConstraint, ComplexConstraint} from '../rsql/Constraint'

type GoogleChart = {
  title: string,
  columns: Array<{label: string, type: string}>,
  rows: Array<Array<any>>
}

export type GoogleCharts = {
  gender: GoogleChart,
  smoking: Chart,
  data_types: Chart,
  age: Chart
}

const mapChart = (chart: ?Chart): ?GoogleChart => (chart && {
  columns: [{label: 'label', type: 'string'}, ...chart.columns],
  rows: chart.rows.map(row => [row.label, ...chart.columns.map(column => row.values[column.key] || 0)]),
  title: chart.title
})

function attributeCharts (state: State): GoogleCharts {
  const {charts} = state
  return {
    gender: mapChart(charts.gender),
    smoking: mapChart(charts.smoking),
    data_types: mapChart(charts.data_types),
    age: mapChart(charts.age)
  }
}

function constraints (state: State, includingBiobank: boolean): Array<Constraint> {
  const booleanConstraints: Array<SimpleConstraint> = ['smoking', 'sex', 'transcriptome', 'wbcc', 'genotypes', 'metabolome', 'methylome', 'wgs']
    .filter(attr => state[attr])
    .map(attr => (new SimpleConstraint(attr, '==', 'true')))

  let sexConstraints: Array<Constraint> = ['male', 'female']
    .filter(sex => state[sex])
    .map(sex => (new SimpleConstraint('sex', '==', sex)))
  if (sexConstraints.length > 1) {
    sexConstraints = [new ComplexConstraint('OR', sexConstraints)]
  }

  let smokingConstraints: Array<Constraint> = []
  if (state.nonSmoking) {
    smokingConstraints.push(new SimpleConstraint('smoking', '==', false))
  }
  if (state.smoking) {
    smokingConstraints.push(new SimpleConstraint('smoking', '==', true))
  }
  if (smokingConstraints.length > 1) {
    smokingConstraints = [new ComplexConstraint('OR', smokingConstraints)]
  }

  let ageConstraints: Array<Constraint> = []
  if (state.belowTwenty || state.twentyThirty || state.thirtyFourty ||
    state.fourtyFifty || state.fiftySixty || state.sixtySeventy ||
    state.seventyEighty || state.aboveEigthy) {
    if (state.belowTwenty) {
      ageConstraints.push(new SimpleConstraint('ageGroup', '==', '<20'))
    }
    if (state.twentyThirty) {
      ageConstraints.push(new SimpleConstraint('ageGroup', '==', '20-30'))
    }
    if (state.thirtyFourty) {
      ageConstraints.push(new SimpleConstraint('ageGroup', '==', '30-40'))
    }
    if (state.fourtyFifty) {
      ageConstraints.push(new SimpleConstraint('ageGroup', '==', '40-50'))
    }
    if (state.fiftySixty) {
      ageConstraints.push(new SimpleConstraint('ageGroup', '==', '50-60'))
    }
    if (state.sixtySeventy) {
      ageConstraints.push(new SimpleConstraint('ageGroup', '==', '60-70'))
    }
    if (state.seventyEighty) {
      ageConstraints.push(new SimpleConstraint('ageGroup', '==', '70-80'))
    }
    if (state.aboveEigthy) {
      ageConstraints.push(new SimpleConstraint('ageGroup', '==', '>80'))
    }
    if (ageConstraints.length > 1) {
      ageConstraints = [new ComplexConstraint('OR', ageConstraints)]
    }
  }

  const biobankConstraint: Array<Constraint> = []
  if (state.biobank && includingBiobank) {
    biobankConstraint.push(new SimpleConstraint('biobank', '==', state.biobank))
  }

  return [...booleanConstraints, ...sexConstraints, ...smokingConstraints, ...ageConstraints, ...biobankConstraint]
}

const rsqlTree = (state: State, includingBiobank: boolean): ?Constraint => {
  const operands = constraints(state, includingBiobank)
  return operands.length === 0 ? null
    : (operands.length === 1 ? operands[0]
      : new ComplexConstraint('AND', operands))
}

export const biobankGraphRsql = (state: State): ?Constraint => rsqlTree(state, false)
export const attributeGraphRsql = (state: State): ?Constraint => rsqlTree(state, true)

export default {
  rsqlTree,
  biobankGraphRsql,
  attributeGraphRsql,
  attributeCharts
}
