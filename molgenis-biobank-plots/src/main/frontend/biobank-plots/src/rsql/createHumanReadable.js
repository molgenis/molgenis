/**
 * Translates RSQL constraint tree into to a human readable string
 *
 * @param constraint An RSQL constraint tree
 * @returns a human readable representation of an RSQL query
 */
export const getHumanReadable = (constraint) => constraint.operator === 'AND'
  ? constraint.operands.map(getHumanReadableFragment)
  : getHumanReadableFragment(constraint)

function getHumanReadableFragment (constraint) {
  return constraint.operator ? getComplexFilterLineHumanReadableFragment(constraint)
    : getSimpleFilterLineHumanReadableFragment(constraint)
}

function getSimpleFilterLineHumanReadableFragment (constraint) {
  return constraint.selector + getComparisonHumanReadable(constraint.comparison) + constraint.arguments
}

function getComplexFilterLineHumanReadableFragment (constraint) {
  const operator = getOperatorHumanReadable(constraint.operator)
  const humanReadableParts = constraint.operands.map(getHumanReadableFragment)
  return humanReadableParts.join(operator)
}

function getComparisonHumanReadable (comparison) {
  switch (comparison) {
    case '=q=':
      return ' contains '
    case '==':
      return ' equals '
    case '=ge=':
      return ' is greater than or equal to '
    case '=le=':
      return ' is less than or equal to '
  }
}

function getOperatorHumanReadable (operator) {
  switch (operator) {
    case 'OR':
      return ' or '
    case 'AND':
      return ' and '
  }
}

