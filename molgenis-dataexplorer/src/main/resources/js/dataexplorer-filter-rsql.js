/**
 * Utility functions to parse RSQL into Javascript Filter components
 */
(function ($, molgenis) {

    molgenis.dataexplorer = molgenis.dataexplorer || {};
    var self = molgenis.dataexplorer.rsql = molgenis.dataexplorer.rsql || {};

    /**
     * Create filters JavaScript components from RSQL
     */
    self.createFiltersFromRsql = function createFilters(rsql, restApi, entityName) {
        // https://regex101.com/r/zAT0Yc/1 regex matches all ';' not between '()'
        const rsqlRegex = /\;(?![^\(]*\))/g;
        var rsqlMatch, match

        var previousIndex = 0
        while ((rsqlMatch = rsqlRegex.exec(rsql)) !== null) {
            if (rsqlMatch.index === rsqlRegex.lastIndex) {
                // This is necessary to avoid infinite loops with zero-width matches
                rsqlRegex.lastIndex++
            }

            // Use the indices to determine RSQL filters
            var currentIndex = rsqlMatch.index
            match = rsql.substring(previousIndex, currentIndex)
            previousIndex = currentIndex + 1
            createFilterForAttribute(match, restApi, entityName)
        }
        // Include the last attribute filter as well
        match = rsql.substring(previousIndex, rsql.length)
        createFilterForAttribute(match, restApi, entityName)
    }

    /**
     * Parse all filters for a single attribute e.g.:
     * match = (count=ge=1;count=le=5)
     * match = id=q=5
     */
    function createFilterForAttribute(match, restApi, entityName) {
        var rsqlParts = match.split(';')
        if (rsqlParts.length === 1) {
            rsqlParts = rsqlParts[0].split(',')
        }

        var filterMatch, attributeName, operator, filterValue, values = [], filterElementMap = {}

        // Creates groups for the three parts of a filter attribute, operator and filter value
        // e.g. id=q=1 becomes g1 -> 'id' g2 -> '=q=' g3 -> '1'
        var filterRegex = /(\w+)(=\w*=)([\w|\W]+)/g

        $.each(rsqlParts, function (index) {
            var rsqlPart = rsqlParts[index]

            while ((filterMatch = filterRegex.exec(rsqlPart)) !== null) {
                if (filterMatch.index === filterRegex.lastIndex) {
                    filterRegex.lastIndex++
                }

                attributeName = filterMatch[1]
                operator = filterMatch[2]
                filterValue = filterMatch[3]

                switch (operator) {
                    case '=ge=':
                    case '=gt=' :
                        filterElementMap['fromValue'] = filterValue
                        break
                    case '=le=':
                    case '=lt=':
                        filterElementMap['toValue'] = filterValue
                        break
                    case '=q=':
                    case '==':
                    case '=like=':
                        values.push(filterValue)
                        break
                    default:
                        throw 'This operator is currently not supported for bookmarkable filters [' + operator + ']';
                }
            }
        })

        filterElementMap['values'] = values
        filterElementMap['attributeName'] = attributeName
        fetchAttribute(restApi, entityName, filterElementMap)
    }

    /**
     * Fetches the attribute
     * Based on type, either fetches the value label first or registers the filter directly
     */
    function fetchAttribute(restApi, entityName, filterElementMap) {
        var attributeName = filterElementMap.attributeName
        restApi.getAsync('/api/v1/' + entityName + '/meta/' + attributeName).then(function (attribute) {
            if (attribute.fieldType === 'MREF' || attribute.fieldType === 'XREF' ||
                attribute.fieldType === 'FILE' || attribute.fieldType === 'CATEGORICAL_MREF' ||
                attribute.fieldType === 'CATEGORICAL' || attribute.fieldType === 'ONE_TO_MANY') {

                fetchLabels(attribute.refEntity.href, attribute.refEntity.hrefCollection, restApi, attribute, filterElementMap)
            } else {
                registerFilters(attribute, undefined, filterElementMap)
            }
        })
    }

    /**
     * Fetches label values for reference type filters
     * TODO For every value, retrieve the label
     */
    function fetchLabels(href, hrefCollection, restApi, attribute, filterElementMap) {

        var value = filterElementMap.values[0]
        restApi.getAsync(href).then(function (meta) {
            var labelAttribute = meta.labelAttribute
            restApi.getAsync(hrefCollection + '/' + value + '/' + labelAttribute).then(function (labelEntity) {
                registerFilters(attribute, labelEntity.label, filterElementMap)
            })
        })
    }

    /**
     * Registers the filter within the JavaScript global scope
     * Allows for the filter to be shown in the UI
     *
     * TODO add labels if reference types
     */
    function registerFilters(attribute, label, filterElementMap) {
        var complexFilters = []
        if (filterElementMap.values.length === 0 && (filterElementMap.fromValue !== undefined || filterElementMap.toValue !== undefined)) {
            // If values length === 0 and we have a to or from filter, create a numeric filter
            complexFilters.push(createNumericFilter(attribute, filterElementMap.fromValue, filterElementMap.toValue))
        } else {
            var complexFilter = new molgenis.dataexplorer.filter.ComplexFilter(attribute);
            var values = filterElementMap.values

            $.each(values, function (index) {
                var simpleFilter = new molgenis.dataexplorer.filter.SimpleFilter(attribute, undefined, undefined, values[index]);
                // if (label !== undefined) simpleFilter.getLabels().push(label)
                var complexFilterElement = new molgenis.dataexplorer.filter.ComplexFilterElement(attribute);
                complexFilterElement.simpleFilter = simpleFilter;
                complexFilter.addComplexFilterElement(complexFilterElement);
            })
            complexFilters.push(complexFilter)
        }
        // Update attribute filters with created complexFilter
        $(document).trigger('updateAttributeFilters', {'filters': complexFilters});
    }

    /**
     * Creates filters with from and to values
     */
    function createNumericFilter(attribute, fromValue, toValue) {
        var attributeFilter = new molgenis.dataexplorer.filter.SimpleFilter(attribute, fromValue, toValue);
        var complexFilter = new molgenis.dataexplorer.filter.ComplexFilter(attribute);
        var complexFilterElement = new molgenis.dataexplorer.filter.ComplexFilterElement(attribute);

        complexFilterElement.simpleFilter = attributeFilter;
        complexFilter.addComplexFilterElement(complexFilterElement);

        return complexFilter
    }

    /**
     * - Create RSQL based on the query rules
     * - Parse the attrName from the generated RSQL
     * - If the state.q already contains a filter for the attribute, overwrite that filter
     * - If the state.q is still undefined, add the RSQL to state.q
     * - If the state.q does not contain the exact same RSQL, add it to state.q
     *
     * TODO RSQL can be less encoded i.e. id=q=1 instead of id%3Dq%3D1
     *
     * @param rules Javascript query rules
     */
    self.addFilterToRsqlState = function addFilterToRsqlState(rules) {
        var rsql = molgenis.createRsqlQuery(rules)
        var attrName = rsql.split('=')[0]

        // By changing a filter and adding an or statement, the rsql contains a '('
        // at the start, remove this
        while (attrName.charAt(0) === '(') {
            attrName = attrName.substr(1);
        }

        if (state.q !== undefined) {
            removeExistingFilterFromRsql(attrName)
            if (state.q === '') {
                state.q = rsql
            } else {
                state.q = state.q + ';' + rsql
            }
        } else {
            state.q = rsql
        }
        pushState()
    }

    self.removeFilterFromRsqlState = function removeFilterFromRsqlState(attrName) {
        removeExistingFilterFromRsql(attrName)
        pushState()
    }

    /**
     * Based on the name of attribute, remove the filter for
     * that attribute from the RSQL
     *
     * @param attrName
     */
    function removeExistingFilterFromRsql(attrName) {
        var rsqlRegex = /[^;,|\(.*\)]+/g
        var rsqlMatch

        var rsql = state.q

        // id=q=1;(count=ge=1;count=l3=5) goes in
        while ((rsqlMatch = rsqlRegex.exec(state.q)) !== null) {
            if (rsqlMatch.index === rsqlRegex.lastIndex) {
                rsqlRegex.lastIndex++
            }

            // 1. id=q=1
            // 2. (count=ge=1;count=l3=5)
            var outerMatch = rsqlMatch[0]
            var rsqlAttribute = outerMatch.split('=')[0]

            if (rsqlAttribute === attrName) {
                // Remove the exact filter i.e. id=q=1, because this
                // filter is getting an update
                rsql = rsql.replace(outerMatch, '')

                // (count=ge=1;count=l3=5) gets replaced into (;)
                // (str=q=1,str=q=2) gets replaced into (,)
                // Remove these nonsense strings from the rsql
                rsql = rsql.replace(/\(;\)|\(,\)/, '')
            }

            // Remove trailing and leading ;
            rsql = rsql.replace(/^;+|;+$/, '');
        }
        console.log(rsql)

        state.q = rsql
    }

}($, window.top.molgenis = window.top.molgenis || {}));