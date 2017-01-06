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

function toRange(attribute, constraint) {
    return {
        attribute,
        type: 'RANGE',
        lines: constraint.operands.map(toRangeLine)
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
    return intersperse((constraint.operands||[constraint]).map(o => toComplexLine(labels, o)), "AND")
}

function toComplexRefOr(labels, constraint) {
    return [].concat.apply([], intersperse((constraint.operands||[constraint]).map(o => toComplexRefAnd(labels, o)), "OR"))
}

function toComplexRef(attribute, labels, constraint) {
    return {
        'type': 'COMPLEX_REF',
        'attribute': attribute,
        'lines': toComplexRefOr(labels, constraint)
    }
}

export {groupBySelector, toRangeLine, toRange, toText, toSimpleRef, toComplexRef, toComplexLine}

export default {groupBySelector, toRangeLine, toRange, toText, toSimpleRef, toComplexRef, toComplexLine}