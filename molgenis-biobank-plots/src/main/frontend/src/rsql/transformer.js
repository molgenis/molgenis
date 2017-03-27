import {toRsqlValue} from './escaping'

/**
 * Transforms RSQL constraint tree to RSQL query string
 */
export const transformToRSQL = getRsqlFromConstraint

export function getRsqlFromConstraint (constraint) {
  return constraint.operator ? getRsqlFromComplexConstraint(constraint)
        : getRsqlFromSimpleConstraint(constraint)
}

function getRsqlFromSimpleConstraint (constraint) {
  return toRsqlValue(constraint.selector) + constraint.comparison + toRsqlValue(constraint.arguments)
}

function getRsqlFromComplexConstraint (constraint) {
  const operator = constraint.operator === 'OR' ? ',' : ';'
  const rsqlParts = constraint.operands.map(getRsqlFromConstraint)
  return '(' + rsqlParts.join(operator) + ')'
}
export default {
  transformToRSQL
}
