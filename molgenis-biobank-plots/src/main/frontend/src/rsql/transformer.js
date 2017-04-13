// @flow
import {toRsqlValue} from './escaping'
import type {Constraint} from './Constraint'
// eslint-disable-next-line
import {SimpleConstraint, ComplexConstraint} from './Constraint'

/**
 * Transforms RSQL constraint tree to RSQL query string
 */
export const transformToRSQL = getRsqlFromConstraint

export function getRsqlFromConstraint (constraint: Constraint): string {
  return constraint instanceof ComplexConstraint
    ? getRsqlFromComplexConstraint(constraint)
    : getRsqlFromSimpleConstraint(constraint)
}

function getRsqlFromSimpleConstraint (constraint: SimpleConstraint): string {
  return toRsqlValue(constraint.selector) + constraint.comparison + toRsqlValue(constraint.arguments.toString())
}

function getRsqlFromComplexConstraint (constraint: ComplexConstraint): string {
  const operator: string = constraint.operator === 'OR' ? ',' : ';'
  const rsqlParts: Array<string> = constraint.operands.map(getRsqlFromConstraint)
  return '(' + rsqlParts.join(operator) + ')'
}
export default {
  transformToRSQL
}
