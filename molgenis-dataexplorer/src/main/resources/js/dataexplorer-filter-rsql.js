/**
 * Utility functions to:
 * - Parse RSQL into Javascript Filter components
 * - Parse ComplexFilters into RSQL
 */
(function ($, molgenis) {

    molgenis.dataexplorer = molgenis.dataexplorer || {};
    var self = molgenis.dataexplorer.rsql = molgenis.dataexplorer.rsql || {};

    /**
     * Create filters JavaScript components from RSQL
     */
    self.createFiltersFromRsql = function createFilters(rsql, restApi, entityName) {
        // https://regex101.com/r/zAT0Yc/1
        // regex matches all ';' not between '()'
        const rsqlRegex = /\;(?![^\(]*\))/g;
        var rsqlMatch, match

        var previousIndex = 0
        while ((rsqlMatch = rsqlRegex.exec(rsql)) !== null) {
            if (rsqlMatch.index === rsqlRegex.lastIndex) {
                // This is necessary to avoid infinite loops with zero-width matches
                rsqlRegex.lastIndex++
            }

            // Use the indices to determine attribute filters
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

        // https://regex101.com/r/P7SYDG/1
        // Parses a piece of RSQL into attribute, operator, and value
        var filterRegex = /(\w+)(=\w*=)([\w|\W]+)/g

        $.each(rsqlParts, function (index) {
            var rsqlPart = rsqlParts[index]

            // Remove trailing or leading braces
            rsqlPart = rsqlPart.replace(/^\(+|\)+$/, '')

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

                restApi.getAsync(attribute.refEntity.href).then(function (meta) {
                    var labelAttribute = meta.labelAttribute
                    fetchLabels(attribute.refEntity.hrefCollection, restApi, attribute, filterElementMap)
                })
            } else {
                registerFilters(attribute, undefined, filterElementMap)
            }
        })
    }

    /**
     * Fetches label values for reference type filters
     * TODO For every value, retrieve the label
     */
    function fetchLabels(hrefCollection, restApi, attribute, filterElementMap) {
        var value = filterElementMap.values[0]
        restApi.getAsync(hrefCollection + '/' + value + '/' + labelAttribute).then(function (labelEntity) {
            registerFilters(attribute, labelEntity.label, filterElementMap)
        })
    }

    /**
     * Registers the filter within the JavaScript global scope
     * Allows for the filter to be shown in the UI
     *
     * TODO add labels if reference types
     */
    function registerFilters(attribute, labels, filterElementMap) {
        var complexFilters = []

        if (filterElementMap.values.length === 0 && (filterElementMap.fromValue !== undefined || filterElementMap.toValue !== undefined)) {
            // If values length === 0 and we have a to or from filter, create a numeric filter
            complexFilters.push(createNumericFilter(attribute, filterElementMap.fromValue, filterElementMap.toValue))
        } else {
            complexFilters = createComplexFilter(attribute, filterElementMap.values, labels)
        }
        // Update attribute filters with created complexFilter
        $(document).trigger('updateAttributeFilters', {'filters': complexFilters});
    }

    /**
     * Creates complex filters with a list of values
     * For reference types, labels are added to the SimpleFilter objects
     */
    function createComplexFilter(attribute, values, labels) {
        var complexFilters = []
        var complexFilter = new molgenis.dataexplorer.filter.ComplexFilter(attribute);

        $.each(values, function (index) {
            var simpleFilter = new molgenis.dataexplorer.filter.SimpleFilter(attribute, undefined, undefined, values[index]);
            if (labels !== undefined) simpleFilter.getLabels().push(labels)

            var complexFilterElement = new molgenis.dataexplorer.filter.ComplexFilterElement(attribute);
            complexFilterElement.simpleFilter = simpleFilter;
            complexFilter.addComplexFilterElement(complexFilterElement);
        })
        complexFilters.push(complexFilter)

        return complexFilters
    }

    /**
     * Creates filters with from and / or to values
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
     *
     * Translates a list of filter rules into RSQL
     *
     * FIXME createRsqlQuery encodes AND jQuery.param() encodes, resulting in double
     * FIXME encoding for values and attributenames, and single encoding of operators
     */
    self.translateFilterRulesToRSQL = function translateFilterRulesToRSQL(rules, existingRSQL) {
        var newFilterRSQL = molgenis.createRsqlQuery(rules)
        var newAttributeName = newFilterRSQL.split('=')[0]

        // By changing a filter and adding an or statement, the rsql contains a '('
        // at the start, remove this
        while (newAttributeName.charAt(0) === '(') {
            newAttributeName = newAttributeName.substr(1);
        }

        if (existingRSQL !== undefined) {
            existingRSQL = removeExistingFilterFromRsql(newAttributeName, existingRSQL)
            if (existingRSQL === '') {
                existingRSQL = newFilterRSQL
            } else {
                existingRSQL = existingRSQL + ';' + newFilterRSQL
            }
        } else {
            existingRSQL = newFilterRSQL
        }
        return existingRSQL
    }

    self.removeFilterFromRsqlState = function removeFilterFromRsqlState(newAttributeName, existingRSQL) {
        return removeExistingFilterFromRsql(newAttributeName, existingRSQL)
    }

    /**
     * Remove a filter from the existing RSQL based on the attribute name
     */
    function removeExistingFilterFromRsql(newAttributeName, existingRSQL) {
        var rsqlRegex = /\;(?![^\(]*\))/g;
        var rsqlMatch, match, rsql = ''

        var previousIndex = 0
        while ((rsqlMatch = rsqlRegex.exec(existingRSQL)) !== null) {
            if (rsqlMatch.index === rsqlRegex.lastIndex) {
                rsqlRegex.lastIndex++
            }

            var currentIndex = rsqlMatch.index
            match = existingRSQL.substring(previousIndex, currentIndex)
            previousIndex = currentIndex + 1

            var existingAttributeName = match.split('=')[0]
            existingAttributeName = existingAttributeName.replace(/^\(+|\)+$/, '')

            if (existingAttributeName !== newAttributeName) {
                rsql = rsql + match + ';'
            }
        }

        // Include the last attribute filter as well
        match = existingRSQL.substring(previousIndex, existingRSQL.length)
        var existingAttributeName = match.split('=')[0]
        existingAttributeName = existingAttributeName.replace(/^\(+|\)+$/, '')

        if (existingAttributeName !== newAttributeName) {
            rsql = rsql + match
        }

        return rsql
    }

}($, window.top.molgenis = window.top.molgenis || {}));