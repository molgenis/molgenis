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

// TODO: Write code to collect all arguments mentioned in a constraint
// function getArguments(constraint) {
//     return constraint.arguments || constraint.operands.
// }

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
        lines: constraint.operands.map(o => o.arguments)
    }
}

function toSimpleRef(labels, constraint) {
    return {
        'type': 'SIMPLE_REF',
        'values': (constraint.operands || [constraint]).map(o => {
            const value = o.arguments
            return {'label': labels[value], value}
        })
    }
}

export function toComplexLine(labels, group) {
    return {
        'operator': group.operator,
        'values': (group.operands || [group.arguments]).map(o => {
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
    return {
        'type': 'COMPLEX_REF',
        'lines': toComplexRefOr(labels, constraint)
    }
}

export default {
    groupBySelector,
    transformModelPart
}