import {transformer, parser} from "../rsql";

/**
 * Translates RSQL to a human readable string
 *
 * TODO Get label values for reference attributes => Does this matter for BBMRI? Identifiers are e.g. PLASMA or CRYOGENIC
 *
 * @param RSQL An RSQL string
 * @returns a human readable representation of an RSQL query
 */
export function getHumanReadable(rsql) {
    const groupBySelectorConstraint = transformer.groupBySelector(parser.parse(rsql))
    return groupBySelectorConstraint && Object.keys(groupBySelectorConstraint)
            .map(attributeName => getHumanReadableFragment(groupBySelectorConstraint[attributeName]))
            .join('\n')
}

function getHumanReadableFragment(constraint) {
    return constraint.operator ? getComplexFilterLineHumanReadableFragment(constraint)
        : getSimpleFilterLineHumanReadableFragment(constraint)
}

function getSimpleFilterLineHumanReadableFragment(constraint) {
    return constraint.selector + getComparisonHumanReadable(constraint.comparison) + constraint.arguments
}

function getComplexFilterLineHumanReadableFragment(constraint) {
    const operator = getOperatorHumanReadable(constraint.operator)
    const humanReadableParts = constraint.operands.map(getHumanReadableFragment)
    return humanReadableParts.join(operator)
}

function getComparisonHumanReadable(comparison) {
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

function getOperatorHumanReadable(operator) {
    switch (operator) {
        case 'OR':
            return ' or '
        case 'AND':
            return ' and '
    }
}

