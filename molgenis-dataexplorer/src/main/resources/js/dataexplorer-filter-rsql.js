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

        var filters = []
        var promises = []

        $.each(Object.keys(model), function () {
            promises.push(restApi.getAsync('/api/v1/' + entityName + '/meta/' + this).then(function (attribute) {
                // Returns a simple or complex filter
                filters.push(parseModelPart(attribute, model[this]))
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
            'ref2': 'label2',
            'ref3': 'label3',
            'ref4': 'label4',
            'ref5': 'label5'
        }, model)

        switch (specificModelPart.type) {
            case 'TEXT':
                return createTextFilter(attribute, specificModelPart)
            case 'RANGE':
                return createRangeFilter(attribute, specificModelPart)
            case 'SIMPLE_REF':
                return createSimpleRefFilter(attribute, specificModelPart)
            case 'COMPLEX_REF':
                return createComplexRefFilter(attribute, specificModelPart)
            case 'BOOL':
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
        // Create a SimpleFilter for every line, operator between lines is always 'OR'
        var complexFilter = new molgenis.dataexplorer.filter.ComplexFilter(attribute)
        $.each(model.lines, function () {
            var simpleFilter = new molgenis.dataexplorer.filter.SimpleFilter(attribute, undefined, undefined, this)
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
        // Create one SimpleFilter for every from - to line, operator between lines is always 'OR'
        var complexFilter = new molgenis.dataexplorer.filter.ComplexFilter(attribute)
        $.each(model.lines, function () {
            var simpleFilter = new molgenis.dataexplorer.filter.SimpleFilter(attribute, this.from, this.to)
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
        var simpleFilter = new molgenis.dataexplorer.filter.SimpleFilter(attribute, undefined, undefined)
        $.each(model.values, function () {
            simpleFilter.getValues().push(this.value)
            simpleFilter.getLabels().push(this.label)
        })
        return simpleFilter
    }

    /**
     *
     * @param attribute
     * @param model
     * @returns {ComplexFilter}
     */
    function createComplexRefFilter(attribute, model) {
        var lines = model.lines
        lines.push('OR')

        var complexFilter = new molgenis.dataexplorer.filter.ComplexFilter(attribute)
        for (var index = 0; index < lines.length; index += 2) {
            var line = lines[index]

            var simpleFilter = new molgenis.dataexplorer.filter.SimpleFilter(attribute, undefined, undefined)
            $.each(line.values, function () {
                simpleFilter.getValues().push(this.value)
                simpleFilter.getLabels().push(this.label)
            })
            simpleFilter.operator = line.operator

            var complexFilterElement = new molgenis.dataexplorer.filter.ComplexFilterElement(attribute)
            complexFilterElement.simpleFilter = simpleFilter
            complexFilterElement.operator = lines[index + 1]

            complexFilter.addComplexFilterElement(complexFilterElement)
        }
        console.log(complexFilter)
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

}($, window.top.molgenis = window.top.molgenis || {}));