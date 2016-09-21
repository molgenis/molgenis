/**
 * Returns all atomic attributes. In case of compound attributes (attributes
 * consisting of multiple atomic attributes) only the descendant atomic
 * attributes are returned. The compound attribute itself is not returned.
 *
 * @param attributes
 * @param restClient
 */
export function getAtomicAttributes(attributes, restClient) {
    var atomicAttributes = [];

    function createAtomicAttributesRec(attributes) {
        $.each(attributes, function (i, attribute) {
            if (attribute.fieldType === 'COMPOUND') {
                // FIXME improve performance by retrieving async
                attribute = restClient.get(attribute.href, {
                    'expand': ['attributes']
                });
                createAtomicAttributesRec(attribute.attributes);
            } else
                atomicAttributes.push(attribute);
        });
    }

    createAtomicAttributesRec(attributes);
    return atomicAttributes;
}

/**
 * Returns all compound attributes. In case of compound attributes
 * (attributes consisting of multiple atomic attributes) only the descendant
 * atomic attributes are returned. The compound attribute itself is not
 * returned.
 *
 * @param attributes
 * @param restClient
 */
export function getCompoundAttributes(attributes, restClient) {
    var compoundAttributes = [];

    function createAtomicAttributesRec(attributes) {
        $.each(attributes, function (i, attribute) {
            if (attribute.fieldType === 'COMPOUND') {
                // FIXME improve performance by retrieving async
                attribute = restClient.get(attribute.href, {
                    'expand': ['attributes']
                });
                compoundAttributes.push(attribute);
                createAtomicAttributesRec(attribute.attributes);
            }
        });
    }

    createAtomicAttributesRec(attributes);
    return compoundAttributes;
}

export function getAllAttributes(attributes, restClient) {
    var tree = [];

    function createAttributesRec(attributes) {
        $.each(attributes, function (i, attribute) {
            tree.push(attribute);
            if (attribute.fieldType === 'COMPOUND') {
                // FIXME improve performance by retrieving async
                attribute = restClient.get(attribute.href, {
                    'expand': ['attributes']
                });
                createAttributesRec(attribute.attributes);
            }
        });
    }

    createAttributesRec(attributes);
    return tree;
}

export function getAttributeLabel(attribute) {
    var label = attribute.label || attribute.name;
    if (attribute.parent) {
        var parentLabel = attribute.parent.label || attribute.parent.name;
        label = parentLabel + '.' + label;
    }

    return label;
}

export function isRefAttr(attr) {
    switch (attr.fieldType) {
        case 'CATEGORICAL':
        case 'CATEGORICAL_MREF':
        case 'MREF':
        case 'ONE_TO_MANY':
        case 'XREF':
        case 'FILE':
            return true;
        default:
            return false;
    }
}

export function isXrefAttr(attr) {
    return attr.fieldType === 'CATEGORICAL' || attr.fieldType === 'XREF' || attr.fieldType === 'FILE';
}

export function isMrefAttr(attr) {
    return attr.fieldType === 'CATEGORICAL_MREF' || attr.fieldType === 'MREF' || attr.fieldType == 'ONE_TO_MANY';
}

export function isCompoundAttr(attr) {
    return attr.fieldType === 'COMPOUND';
}