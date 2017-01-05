/**
 * Utility functions to:
 * - Parse RSQL into Javascript Filter components
 * - Parse ComplexFilters into RSQL
 *
 * TODO Fetch all data in one request
 * TODO Create functions for duplicate code
 *
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
     * Parse all filters for a single attribute
     *
     * Examples:
     * match = (INT=ge=1;INT=le=5)
     * match = (STRING=q=str1,STRING=q=str2)
     * match = ((MREF=q=A,MREF=q=B);MREF=q=C)
     * match = BOOL==false
     * match = TEXT=q=This is a long text
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
     * Based on type, either fetches the labels first or registers the filter directly
     */
    function fetchAttribute(restApi, entityName, filterElementMap) {
        var attributeName = filterElementMap.attributeName
        restApi.getAsync('/api/v1/' + entityName + '/meta/' + attributeName).then(function (attribute) {
            if (attribute.fieldType === 'MREF' || attribute.fieldType === 'XREF' ||
                attribute.fieldType === 'FILE' || attribute.fieldType === 'CATEGORICAL_MREF' ||
                attribute.fieldType === 'CATEGORICAL' || attribute.fieldType === 'ONE_TO_MANY') {

                restApi.getAsync(attribute.refEntity.href).then(function (meta) {
                    var idAttribute = meta.idAttribute
                    var labelAttribute = meta.labelAttribute

                    fetchLabels(meta.name, restApi, attribute, filterElementMap, idAttribute, labelAttribute)
                })
            } else {
                registerFilters(attribute, filterElementMap)
            }
        })
    }

    /**
     * Fetches label values for reference type filters
     */
    function fetchLabels(refEntityName, restApi, attribute, filterElementMap, idAttribute, labelAttribute) {
        var values = '(' + filterElementMap.values.join(",") + ')'
        var uri = '/api/v2/' + refEntityName + '?q=' + idAttribute + '=in=' + values

        restApi.getAsync(uri).then(function (response) {
            var labels = []

            $.each(response.items, function (index) {
                var item = response.items[index]
                labels.push(item[labelAttribute])
            })

            if (labels.length === 0) labels = filterElementMap.values
            registerFilters(attribute, filterElementMap, labels)
        })
    }

    /**
     * Registers the filter within the JavaScript global scope
     * Allows for the filter to be shown in the UI
     */
    function registerFilters(attribute, filterElementMap, labels) {
        var filters = [createFilters(attribute, filterElementMap, labels)]

        // Update attribute filters with created complexFilter
        $(document).trigger('updateAttributeFilters', {'filters': filters})
    }

    function createFilters(attribute, filterElementMap, labels) {
        var filters = []
        var values = filterElementMap.values
        var fromValue = filterElementMap.fromValue
        var toValue = filterElementMap.toValue

        switch (attribute.fieldType) {
            case 'EMAIL':
            case 'HTML':
            case 'HYPERLINK':
            case 'ENUM':
            case 'SCRIPT':
            case 'TEXT':
            case 'STRING':
                return createComplexFilterForString(attribute, values, labels, 'OR')
            case 'DATE_TIME':
            case 'DATE':
            case 'DECIMAL':
            case 'INT':
            case 'LONG':
                return createComplexFilter(attribute, values, labels, undefined, fromValue, toValue)
            case 'MREF':
            case 'ONE_TO_MANY':
                return createComplexFilter(attribute, values, labels, undefined)
            case 'FILE':
            case 'XREF':
                return createSimpleFilter(attribute, values, labels, 'OR')
            case 'BOOL':
            case 'CATEGORICAL':
            case 'CATEGORICAL_MREF':
                return createSimpleFilter(attribute, values)
            case 'COMPOUND' :
                throw 'Unsupported data type: ' + attribute.fieldType;
            default:
                throw 'Unknown data type: ' + attribute.fieldType;
        }
    }

    /**
     * Creates complex filters with a list of values
     * For reference types, labels are added to the SimpleFilter objects
     */
    function createComplexFilter(attribute, values, labels, andOr, fromValue, toValue) {
        var simpleFilter = createSimpleFilter(attribute, values, labels, andOr, fromValue, toValue)

        var complexFilterElement = new molgenis.dataexplorer.filter.ComplexFilterElement(attribute);
        complexFilterElement.simpleFilter = simpleFilter;
        complexFilterElement.operator = andOr;

        var complexFilter = new molgenis.dataexplorer.filter.ComplexFilter(attribute);
        complexFilter.addComplexFilterElement(complexFilterElement);

        return complexFilter
    }

    function createComplexFilterForString(attribute, values, labels, andOr, fromValue, toValue) {
        var complexFilter = new molgenis.dataexplorer.filter.ComplexFilter(attribute);

        $.each(values, function (index) {
            var simpleFilter = new molgenis.dataexplorer.filter.SimpleFilter(attribute, fromValue, toValue, values[index]);
            var complexFilterElement = new molgenis.dataexplorer.filter.ComplexFilterElement(attribute);
            complexFilterElement.simpleFilter = simpleFilter;

            complexFilter.addComplexFilterElement(complexFilterElement);
        })

        return complexFilter
    }

    /**
     * Creates a simple filter, corresponding to a single line in the filter forms
     *
     * @param attribute the attribute to filter on
     * @param values the values to be compared to
     * @param labels the labels of the values, only relevant for reference type attributes
     * @param andor whether the comparisons in this filter are to be ANDed or ORred with each other, only relevant if more than one value is provided
     */
    function createSimpleFilter(attribute, values, labels, andor, fromValue, toValue) {
        var simpleFilter = new molgenis.dataexplorer.filter.SimpleFilter(attribute, fromValue, toValue, values);
        simpleFilter.operator = andor
        simpleFilter.labels = labels
        return simpleFilter
    }

    /**
     *
     * Translates a list of filter rules into RSQL
     *
     * FIXME createRsqlQuery encodes AND jQuery.param() encodes, resulting in double
     * FIXME encoding for values and attributenames, and single encoding of operators
     *
     * FIXME FilterWizard creates an invalid RSQL entry with all attributes, having undefined values
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