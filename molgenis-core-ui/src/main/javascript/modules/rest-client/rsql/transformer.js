import {toRsqlValue} from "./createRsqlQuery";

/**
 * Transforms map to RSQL
 */
export function transformToRSQL(constraint) {
    return Object.keys(constraint)
        .map(k => constraint[k])
        .map(getRsqlFromConstraint)
        .join(';');
}

function getRsqlFromConstraint(constraint) {
    return constraint.operator ? getRsqlFromComplexConstraint(constraint)
        : getRsqlFromSimpleConstraint(constraint)
}

function getRsqlFromSimpleConstraint(constraint) {
    const rsqlValue = constraint.arguments.constructor === Array ? '(' + constraint.arguments.map(toRsqlValue).join(',') + ')' : toRsqlValue(constraint.arguments)
    return toRsqlValue(constraint.selector) + constraint.comparison + rsqlValue
}

function getRsqlFromComplexConstraint(constraint) {
    const operator = constraint.operator === 'OR' ? ',' : ';'
    const rsqlParts = constraint.operands.map(getRsqlFromConstraint)
    return '(' + rsqlParts.join(operator) + ')'
}

/**
 * Transforms parsed filter RSQL to a map
 */
export function groupBySelector(tree) {
    let operands
    if (tree.operands && new Set(tree.operands.map(o => findSelector(o))).size > 1) {
        operands = tree.operands
    } else {
        operands = [tree]
    }
    return operands.reduce(combine, {})
}

function combine(acc, constraint) {
    const selector = findSelector(constraint)
    return {...acc, [selector]: constraint}
}

function findSelector(constraint) {
    return constraint.selector || findSelector(constraint.operands[0])
}

/**
 * Transforms a parsed RSQL constraint to a model element that
 * @param fieldType the type of the attribute that is filtered upon
 * @param labels the label values to fill in for the arguments in the constraint
 * @param constraint the parsed RSQL constraint to transform
 * @returns model element with a type
 */
export function transformModelPart(fieldType, labels, constraint) {
    switch (fieldType) {
        case 'BOOL':
            return toBool(constraint)
        case 'EMAIL':
        case 'HTML':
        case 'HYPERLINK':
        case 'ENUM':
        case 'SCRIPT':
        case 'TEXT':
        case 'STRING':
            return toText(constraint)
        case 'DATE_TIME':
        case 'DATE':
        case 'DECIMAL':
        case 'INT':
        case 'LONG':
            return toRange(constraint)
        case 'FILE':
        case 'XREF':
        case 'CATEGORICAL':
        case 'CATEGORICAL_MREF':
            return toSimpleRef(labels, constraint)
        case 'MREF':
        case 'ONE_TO_MANY':
            return toComplexRef(labels, constraint)
        case 'COMPOUND':
            throw 'Unsupported data type: ' + fieldType
        default:
            throw 'Unknown data type: ' + fieldType
    }
}

export function getArguments(constraint) {
    if (constraint.arguments) {
        if (constraint.arguments.constructor === Array) {
            return new Set(constraint.arguments)
        } else {
            return new Set([constraint.arguments])
        }
    }
    let result = new Set()
    constraint.operands.map(o => getArguments(o).forEach(a => result.add(a)))
    return result;
}

function toBool(constraint) {
    return {
        type: "BOOL",
        value: constraint.arguments
    }
}

function toRange(constraint) {
    const operands = constraint.operator === 'OR' ? constraint.operands : [constraint]
    return {
        type: 'RANGE',
        lines: operands.map(toRangeLine)
    }
}

export function toRangeLine(constraint) {
    return (constraint.operands || [constraint]).reduce((acc, operand) => {
        return {...acc, [operand.comparison === "=ge=" ? "from" : "to"]: operand.arguments}
    }, {})
}

function toText(constraint) {
    return {
        type: 'TEXT',
        lines: constraint.operands ? constraint.operands.map(o => o.arguments) : [constraint.arguments]
    }
}

function toSimpleRef(labels, constraint) {
    const addLabel = value => ({'label': labels[value], value})

    let values
    if( constraint.comparison === '=in=' ) {
        values = constraint.arguments
    } else {
      values = (constraint.operands || [constraint]).map(o => o.arguments)
    }

    return {
      'type': 'SIMPLE_REF',
      values : values.map(addLabel)
    }
}

export function toComplexLine(labels, group) {
    return {
        'operator': group.operator,
        'values': (group.operands || (group.arguments.constructor === Array ? group.arguments : [group.arguments])).map(o => {
            const value = o.arguments || o
            return {'label': labels[value], value}
        })
    }
}

function intersperse(arr, sep) {
    return arr.reduce((a, v) => [...a, v, sep], []).slice(0, -1)
}

function toComplexRefAnd(labels, constraint) {
    if (constraint.operator === "OR") {
        // this is the bottom level
        return toComplexLine(labels, constraint)
    }
    return intersperse((constraint.operands || [constraint]).map(o => toComplexLine(labels, o)), "AND")
}

function toComplexRefOr(labels, constraint) {
    return [].concat.apply([], intersperse((constraint.operands || [constraint]).map(o => toComplexRefAnd(labels, o)), "OR"))
}

function toComplexRef(labels, constraint) {
    return constraint.operator && constraint.operator === 'AND' ? {
      'type': 'COMPLEX_REF',
      'lines': toComplexRefAnd(labels, constraint)
    } : {
      'type': 'COMPLEX_REF',
      'lines': toComplexRefOr(labels, constraint)
    }
}

export default {
    groupBySelector,
    transformModelPart,
    transformToRSQL,
    getArguments
}