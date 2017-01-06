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

        var attributes = Object.keys(model)
        var filters = []
        var promises = []

        $.each(attributes, function (index) {
            var attributeName = attributes[index]
            promises.push(restApi.getAsync('/api/v1/' + entityName + '/meta/' + attributeName).then(function (metadata) {
                // Returns a simple or complex filter
                filters.push(parseModelPart(metadata, model[attributeName]))
            }));
        })

        Promise.all(promises).then(
            function () {
                $(document).trigger('updateAttributeFilters', {'filters': filters})
            }
        )
    }

    /**
     *
     *
     *
     * @param attribute
     * @param model
     *
     */
    function parseModelPart(attribute, model) {
        var specificModelPart = molgenis.rsql.transformer.transformModelPart(attribute.fieldType, {
            'ref1': 'label1',
            'ref2': 'label2'
        }, model)
        switch (specificModelPart.type) {
            case 'TEXT':
                console.log(specificModelPart)
                return createTextFilter(attribute, specificModelPart)
            case 'RANGE':
                console.log(specificModelPart)
                return createRangeFilter(attribute, specificModelPart)
            case 'SIMPLE_REF':
                console.log(specificModelPart)
                return createSimpleRefFilter(attribute, specificModelPart)
            case 'COMPLEX_REF':
                console.log(specificModelPart)
                return createComplexRefFilter(attribute, specificModelPart)
            case 'BOOL':
                console.log(specificModelPart)
                return createBoolFilter(attribute, specificModelPart)
        }
    }

    /**
     *
     * @param attribute
     * @param model
     * @returns {ComplexFilter}
     */
    function createTextFilter(attribute, model) {
        var lines = model.lines

        // Create a SimpleFilter for every line, operator between lines is always 'OR'
        var complexFilter = new molgenis.dataexplorer.filter.ComplexFilter(attribute)
        $.each(lines, function (index) {
            var line = lines[index]

            var simpleFilter = new molgenis.dataexplorer.filter.SimpleFilter(attribute, undefined, undefined, line)
            var complexFilterElement = new molgenis.dataexplorer.filter.ComplexFilterElement(attribute)

            complexFilterElement.simpleFilter = simpleFilter
            complexFilter.addComplexFilterElement(complexFilterElement)
        })
        return complexFilter
    }

    /**
     *
     * @param attribute
     * @param model
     * @returns {ComplexFilter}
     */
    function createRangeFilter(attribute, model) {
        var lines = model.lines

        // Create one SimpleFilter for every from - to line, operator between lines is always 'OR'
        var complexFilter = new molgenis.dataexplorer.filter.ComplexFilter(attribute)
        $.each(lines, function (index) {
            var line = lines[index]

            var simpleFilter = new molgenis.dataexplorer.filter.SimpleFilter(attribute, line.from, line.to)
            var complexFilterElement = new molgenis.dataexplorer.filter.ComplexFilterElement(attribute)

            complexFilterElement.simpleFilter = simpleFilter
            complexFilter.addComplexFilterElement(complexFilterElement)
        })
        return complexFilter
    }

    /**
     *
     * @param attribute
     * @param model
     * @returns {SimpleFilter}
     */
    function createSimpleRefFilter(attribute, model) {
        var values = model.values

        var simpleFilter = new molgenis.dataexplorer.filter.SimpleFilter(attribute, undefined, undefined)
        $.each(values, function (index) {
            var value = values[index]

            simpleFilter.getValues().push(value.value)
            simpleFilter.getLabels().push(value.label)
        })

        console.log(simpleFilter.getValues(), simpleFilter.getLabels())

        return simpleFilter
    }

    function createComplexRefFilter(attribute, model) {
        var lines = model.lines

        var complexFilter = new molgenis.dataexplorer.filter.ComplexFilter(attribute)
        for (var index = 0; index < lines.length; index++) {
            var line = lines[index]

            var simpleFilter
            var complexFilterElement

            if (line.operator !== undefined) {
                var lineOperator = line.operator
                var lineValues = line.values

                var values = []
                var labels = []

                $.each(lineValues, function (index) {
                    var lineValue = lineValues[index]
                    values.push(lineValue.value)
                    labels.push(lineValue.label)
                })

                simpleFilter = new molgenis.dataexplorer.filter.SimpleFilter(attribute, undefined, undefined, values)
                simpleFilter.labels = labels
                simpleFilter.operator = lineOperator

                complexFilterElement = new molgenis.dataexplorer.filter.ComplexFilterElement(attribute)
                complexFilterElement.simpleFilter = simpleFilter
            } else {
                complexFilterElement.operator = line
                complexFilter.addComplexFilterElement(complexFilterElement)
            }
        }

        if (complexFilter.getComplexFilterElements().length === 0) complexFilter.addComplexFilterElement(complexFilterElement)
        return complexFilter
    }

    /**
     * Creates a simple filter for boolean attributes
     *
     * @param attribute
     * @param model
     * @returns {SimpleFilter}
     */
    function createBoolFilter(attribute, model) {
        var simpleFilter = new molgenis.dataexplorer.filter.SimpleFilter(attribute, undefined, undefined, model.value);
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

}
($, window.top.molgenis = window.top.molgenis || {}));