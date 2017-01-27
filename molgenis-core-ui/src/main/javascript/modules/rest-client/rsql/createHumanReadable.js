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
            .map(attribute => getHumanReadableFragment(attribute, groupBySelectorConstraint[attribute]) + '\n')
            .join('')
}

function getHumanReadableFragment(attribute, constraint) {
    return constraint.operator ? getComplexFilterLineHumanReadableFragment(attribute, constraint, '')
        : getSimpleFilterLineHumanReadableFragment(attribute, constraint.comparison, constraint.arguments)
}

function getSimpleFilterLineHumanReadableFragment(attribute, comparison, argument) {
    return attribute + getComparisonHumanReadable(comparison) + argument
}

function getComplexFilterLineHumanReadableFragment(attribute, constraint, humanReadableString) {
    const operator = getOperatorHumanReadable(constraint.operator)
    const humanReadableParts = []
    constraint.operands.map(operand => operand.operator ? humanReadableParts.push(getComplexFilterLineHumanReadableFragment(attribute, operand, humanReadableString))
        : humanReadableParts.push(getSimpleFilterLineHumanReadableFragment(attribute, operand.comparison, operand.arguments)))
    humanReadableString += humanReadableParts.join(operator)
    return humanReadableString
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

