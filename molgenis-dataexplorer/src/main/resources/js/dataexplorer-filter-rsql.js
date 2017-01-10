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
            var attributeName = this
            promises.push(restApi.getAsync('/api/v1/' + entityName + '/meta/' + this).then(function (attribute) {
                filters.push(parseModelPart(attribute, model[attributeName]))
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
     * Uses a transformed model to create the correct type of filter
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
     * Creates a complex filter for string, text etc..
     *
     * @param attribute
     * @param model
     * @returns {ComplexFilter}
     */
    function createTextFilter(attribute, model) {
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
     * Creates a complex filter for int, long and decimal
     *
     * @param attribute
     * @param model
     * @returns {ComplexFilter}
     */
    function createRangeFilter(attribute, model) {
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
     * Creates a simple filter for xref, file, categorical and categorical_mref
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
     * Creates a complex filter for mref and one_to_many
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
        return complexFilter
    }

    /**
     * Creates a simple filter for boolean
     *
     * @param attribute
     * @param model
     * @returns {SimpleFilter}
     */
    function createBoolFilter(attribute, model) {
        return new molgenis.dataexplorer.filter.SimpleFilter(attribute, undefined, undefined, model.value)
    }

    /**
     * Translates a list of filter rules into RSQL
     */
    self.translateFilterRulesToRSQL = function translateFilterRulesToRSQL(attributeFilterRSQL, existingRSQL) {
        var existingModel = {}
        if (existingRSQL) existingModel = molgenis.rsql.transformer.groupBySelector(molgenis.rsql.parser.parse(existingRSQL))
        var attributeModel = molgenis.rsql.transformer.groupBySelector(molgenis.rsql.parser.parse(attributeFilterRSQL))


        // Merge existing model with attribute model, overwriting filters if the filter for that attribute already exists
        $.extend(existingModel, attributeModel);
        return molgenis.rsql.transformer.transformToRSQL(existingModel)
    }

    /**
     * Remove a filter from the existing RSQL based on the attribute name
     */
    self.removeFilterFromRsqlState = function removeFilterFromRsqlState(attribute, existingRSQL) {
        var existingModel = molgenis.rsql.transformer.groupBySelector(molgenis.rsql.parser.parse(existingRSQL))
        delete existingModel[attribute]
        return molgenis.rsql.transformer.transformToRSQL(existingModel)
    }

}($, window.top.molgenis = window.top.molgenis || {}));