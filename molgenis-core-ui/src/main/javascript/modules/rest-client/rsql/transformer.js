/**
 * Transforms parsed filter RSQL to a map
 */
function groupBySelector(tree) {
    const operands = tree.operator === 'AND' ? tree.operands : [tree]
    return operands.reduce(combine, {})
}

function combine(acc, constraint) {
    const selector = findSelector(constraint)
    return {...acc, [selector]: constraint}
}

function findSelector(constraint) {
    return constraint.selector || findSelector(constraint.operands[0])
}

function transformModelPart(attribute, labels, constraint) {
    switch (attribute.fieldType) {
        case 'BOOL':
            return toBool(attribute, constraint)
        case 'EMAIL':
        case 'HTML':
        case 'HYPERLINK':
        case 'ENUM':
        case 'SCRIPT':
        case 'TEXT':
        case 'STRING':
            return toText(attribute, constraint)
        case 'DATE_TIME':
        case 'DATE':
        case 'DECIMAL':
        case 'INT':
        case 'LONG':
            return toRange(attribute, constraint)
        case 'FILE':
        case 'XREF':
        case 'CATEGORICAL':
        case 'CATEGORICAL_MREF':
            return toSimpleRef(attribute, labels, constraint)
        case 'MREF':
        case 'ONE_TO_MANY':
            return toComplexRef(attribute, labels, constraint)
        case 'COMPOUND':
            throw 'Unsupported data type: ' + attribute.fieldType
        default:
            throw 'Unknown data type: ' + attribute.fieldType
    }
}

// TODO: Write code to collect all arguments mentioned in a constraint
// function getArguments(constraint) {
//     return constraint.arguments || constraint.operands.
// }

function toBool(attribute, constraint) {
    return {
        attribute,
        type: "BOOL",
        value: constraint.arguments
    }
}

function toRange(attribute, constraint) {
    const operands = constraint.operator === 'OR' ? constraint.operands : [constraint]
    return {
        attribute,
        type: 'RANGE',
        lines: operands.map(toRangeLine)
    }
}

function toRangeLine(constraint) {
    return (constraint.operands || [constraint]).reduce((acc, operand) => {
        return {...acc, [operand.comparison === "=ge=" ? "from" : "to"]: operand.arguments}
    }, {})
}

function toText(attribute, constraint) {
    return {
        attribute,
        type: 'TEXT',
        lines: constraint.operands.map(o => o.arguments)
    }
}

function toSimpleRef(attribute, labels, constraint) {
    return {
        'type': 'SIMPLE_REF',
        'attribute': attribute,
        'values': (constraint.operands || [constraint]).map(o => {
            const value = o.arguments
            return {'label': labels[value], value}
        })
    }
}

function toComplexLine(labels, group) {
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

function toComplexRef(attribute, labels, constraint) {
    return {
        'type': 'COMPLEX_REF',
        'attribute': attribute,
        'lines': toComplexRefOr(labels, constraint)
    }
}

export {
    groupBySelector,
    toBool,
    toRangeLine,
    toRange,
    toText,
    toSimpleRef,
    toComplexRef,
    toComplexLine,
    transformModelPart
}

export default {
    groupBySelector,
    transformModelPart
}