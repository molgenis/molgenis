/**
 * Utility functions to:
 * - Parse RSQL into Javascript Filter components
 * - Parse ComplexFilters into RSQL
 *
 * TODO Create functions for duplicate code
 *
 */
(function ($, molgenis) {

        molgenis.dataexplorer = molgenis.dataexplorer || {};
        var self = molgenis.dataexplorer.rsql = molgenis.dataexplorer.rsql || {};

        /**
         * Transform an RSQL query string to a set of Simple and Complex filters
         *
         * @param rsql
         * @param restApi
         * @param entityName
         */
        self.createFiltersFromRsql = function createFilters(rsql, restApi, entityName) {
            var tree = molgenis.rsql.parser.parse(rsql)
            var model = molgenis.rsql.transformer.groupBySelector(tree)

            var attributeQueryString = Object.keys(model).join(',')

            restApi.getAsync('/api/v2/' + entityName + '?attrs=' + attributeQueryString).then(function (data) {
                var attributes = data.meta.attributes
                var filters = []

                $.each(attributes, function (index) {
                    var attribute = attributes[index]
                    var filter = parseModelPart(attribute, model[attribute.name])

                    filters.push(filter)
                })

                $(document).trigger('updateAttributeFilters', {'filters': filters})
            })
        }

        function parseModelPart(attribute, model) {
            switch (attribute.fieldType) {
                case 'EMAIL':
                case 'HTML':
                case 'HYPERLINK':
                case 'ENUM':
                case 'SCRIPT':
                case 'TEXT':
                case 'STRING':
                    return createTextFilter(attribute, model)
                case 'DATE_TIME':
                case 'DATE':
                case 'DECIMAL':
                case 'INT':
                case 'LONG':
                    return createRangeFilter(attribute, model)
                case 'FILE':
                case 'XREF':
                case 'CATEGORICAL':
                case 'CATEGORICAL_MREF':
                    return createSimpleRefFilter(attribute, model)
                case 'MREF':
                case 'ONE_TO_MANY':
                    return createComplexRefFilter(attribute, model)
                case 'BOOL':
                    return createSimpleFilter(attribute, model.arguments)
                case 'COMPOUND' :
                    throw 'Unsupported data type: ' + attribute.fieldType;
                default:
                    throw 'Unknown data type: ' + attribute.fieldType;
            }
        }

        function createTextFilter(attribute, model) {
            var textModel = molgenis.rsql.transformer.toText(attribute, model)
            var lines = textModel.lines

            // Create a SimpleFilter for every line, operator between lines is always 'OR'
            var complexFilter = new molgenis.dataexplorer.filter.ComplexFilter(attribute);
            $.each(lines, function (index) {
                var line = lines[index]

                var simpleFilter = new molgenis.dataexplorer.filter.SimpleFilter(attribute, undefined, undefined, line);
                var complexFilterElement = new molgenis.dataexplorer.filter.ComplexFilterElement(attribute);

                complexFilterElement.simpleFilter = simpleFilter;
                complexFilter.addComplexFilterElement(complexFilterElement);
            })
            return complexFilter
        }

        function createRangeFilter(attribute, model) {
            var rangeModel = molgenis.rsql.transformer.toRange(attribute, model)
            var lines = rangeModel.lines

            // Create one SimpleFilter for every from - to line, operator between lines is always 'OR'
            var complexFilter = new molgenis.dataexplorer.filter.ComplexFilter(attribute);
            $.each(lines, function (index) {
                var line = lines[index]

                var simpleFilter = new molgenis.dataexplorer.filter.SimpleFilter(attribute, line.from, line.to);
                var complexFilterElement = new molgenis.dataexplorer.filter.ComplexFilterElement(attribute);

                complexFilterElement.simpleFilter = simpleFilter;
                complexFilter.addComplexFilterElement(complexFilterElement);
            })
            return complexFilter
        }

        function createSimpleRefFilter(attribute, model) {
            var simpleRefModel = molgenis.transformer.toSimpleRef(attribute, model)
            var args = simpleRefModel.args

            var values = []
            var labels = []

            $.each(args, function (index) {
                var arg = args[index]
                values.push(arg.value)
                labels.push(arg.label)
            })

            var simpleFilter = new molgenis.dataexplorer.filter.SimpleFilter(attribute, undefined, undefined, values);
            simpleFilter.labels = labels

            return simpleFilter
        }

        function createComplexRefFilter(attribute, model) {
        }

        /**
         * Creates a simple filter, corresponding to a single line in the filter forms
         *
         * @param attribute the attribute to filter on
         * @param value the values to be compared to
         * @param labels the labels of the values, only relevant for reference type attributes
         * @param operator whether the comparisons in this filter are to be ANDed or ORred with each other,
         * only relevant if more than one value is provided and these values are also within the same line (e.g. xref filters)
         */
        function createSimpleFilter(attribute, value, fromValue, toValue, labels, operator) {
            var simpleFilter = new molgenis.dataexplorer.filter.SimpleFilter(attribute, fromValue, toValue, value);
            simpleFilter.labels = labels
            simpleFilter.operator = operator

            return simpleFilter
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

    }
    ($, window.top.molgenis = window.top.molgenis || {})
)
;