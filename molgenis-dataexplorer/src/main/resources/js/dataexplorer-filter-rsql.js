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
        fetchLabels(rsql, entityName, restApi).then(function (transformedModelPartsMap) {
            var filters = []
            $.each(Object.keys(transformedModelPartsMap), function () {
                var transformedModelPart = transformedModelPartsMap[this]
                filters.push(parseModelPart(transformedModelPart.attribute, transformedModelPart.model))
            })
            $(document).trigger('updateAttributeFilters', {'filters': filters})
        })
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
        switch (model.type) {
            case 'TEXT':
                return createTextFilter(attribute, model)
            case 'RANGE':
                return createRangeFilter(attribute, model)
            case 'SIMPLE_REF':
                return createSimpleRefFilter(attribute, model)
            case 'COMPLEX_REF':
                return createComplexRefFilter(attribute, model)
            case 'BOOL':
                return createBoolFilter(attribute, model)
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
     * Fetches labels for filters on reference type attributes
     *
     * @param rsql
     * @param entityName
     * @param restApi
     * @returns {Promise}
     */
    function fetchLabels(rsql, entityName, restApi) {
        var tree = molgenis.rsql.parser.parse(rsql)
        var constraintsBySelector = molgenis.rsql.transformer.groupBySelector(tree)

        var transformedModelPartsMap = {}
        var promises = []

        $.each(Object.keys(constraintsBySelector), function () { // per attribute in RSQL
            var attributeName = this
            var constraint = constraintsBySelector[attributeName]

            // Retrieve V1 metadata for every attribute so we support legacy javascript
            promises.push(getV1Attribute(entityName, attributeName, restApi).then(function (attribute) {

                // Only retrieve labels if the attribute has a refEntity
                if (attribute.refEntity) {
                    var values = molgenis.rsql.transformer.getArguments(constraint)

                    // Retrieve the items you want with an 'IN' query
                    return getV2LabelValues(attribute.refEntity.name, attribute.refEntity.idAttribute, attribute.refEntity.labelAttribute, values, restApi).then(function (labels) {
                        var model = molgenis.rsql.transformer.transformModelPart(attribute.fieldType, labels, constraint)
                        transformedModelPartsMap[attributeName] = {
                            'attribute': attribute,
                            'model': model
                        }
                    })
                } else {
                    var model = molgenis.rsql.transformer.transformModelPart(attribute.fieldType, [], constraint)
                    transformedModelPartsMap[attributeName] = {
                        'attribute': attribute,
                        'model': model
                    }
                }
            }))
        })

        return Promise.all(promises).then(function () {
            return transformedModelPartsMap
        })
    }

    function getV1Attribute(entityName, attributeName, restApi) {
        return restApi.getAsync('/api/v1/' + entityName + '/meta/' + attributeName + '?expand=refEntity')
    }

    function getV2LabelValues(refEntityName, refEntityIdAttribute, refEntityLabelAttribute, values, restApi) {
        var ids = Array.from(values.values()).join(',')
        var requestUri = '/api/v2/' + refEntityName + '?q=' + refEntityIdAttribute + '=in=(' + ids + ')'
        return restApi.getAsync(requestUri).then(function (refEntityItems) {
            var labels = {}
            $.each(refEntityItems.items, function () {
                var id = this[refEntityIdAttribute]
                var label = this[refEntityLabelAttribute]
                labels[id] = label
            })
            return labels
        })
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