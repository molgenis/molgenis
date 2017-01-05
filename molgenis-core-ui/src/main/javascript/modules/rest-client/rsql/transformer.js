/**
 * Transforms parsed filter RSQL to a map
 */
function groupBySelector(tree) {
    return (tree.operands || [tree]).reduce(combine, {})
}

function combine(acc, constraint) {
    const selector = findSelector(constraint)
    return {...acc, [selector]: constraint}
}

function findSelector(constraint) {
    return constraint.selector || findSelector(constraint.operands[0])
}

export {groupBySelector}