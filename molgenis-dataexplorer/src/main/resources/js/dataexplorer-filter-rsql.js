(function ($, molgenis) {

    molgenis.dataexplorer = molgenis.dataexplorer || {};
    var self = molgenis.dataexplorer.rsql = molgenis.dataexplorer.rsql || {};

    /**
     * Transform an RSQL query string to a set of Simple and Complex filters
     *
     * @param rsql
     * @param restApi
     * @param entityTypeId
     */
    self.createFiltersFromRsql = function createFilters(rsql, restApi, entityTypeId) {
        fetchModelParts(rsql, entityTypeId, restApi).then(function (modelParts) {
            var filters = []
            $.each(Object.keys(modelParts), function () {
                var modelPart = modelParts[this]
                filters.push(createFilter(modelPart.attribute, modelPart.model))
            })
            $(document).trigger('updateAttributeFilters', {'filters': filters})
        })
    }

    /**
     *
     * Uses a model part to create the correct type of filter
     *
     * @param attribute
     * @param model
     *
     */
    function createFilter(attribute, model) {
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
        lines.push(lines[1])

        var complexFilter = new molgenis.dataexplorer.filter.ComplexFilter(attribute)
        for (var index = 0; index < lines.length; index += 2) {
            var line = lines[index]

            var simpleFilter = new molgenis.dataexplorer.filter.SimpleFilter(attribute, undefined, undefined)
            $.each(line.values, function () {
                if (this.value.constructor === Array) {
                    $.each(this.value, function () {
                        simpleFilter.getValues().push(this)
                    })
                } else {
                    simpleFilter.getValues().push(this.value)
                }
                simpleFilter.getLabels().push(this.label)
            })
            simpleFilter.operator = line.operator

            var complexFilterElement = new molgenis.dataexplorer.filter.ComplexFilterElement(attribute)
            complexFilterElement.simpleFilter = simpleFilter
            //every complex filter element gets an operator to state how it relates to the previous one
            //so the operator for the first complexfilterelement is not meaningfull since there is no previous element
            //so if the index is 0 we can skip adding the operator
            if (index != 0) {
                complexFilterElement.operator = lines[index - 1]
            }
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
     * Uses the RSQL to create model parts which can be used to create complex and simple filters
     *
     * @param rsql
     * @param entityTypeId
     * @param restApi
     * @returns {Promise.modelParts}
     */
    function fetchModelParts(rsql, entityTypeId, restApi) {
        var tree = molgenis.rsql.parser.parse(rsql)
        var constraintsBySelector = molgenis.rsql.transformer.groupBySelector(tree)

        var modelParts = {}
        var promises = []

        $.each(Object.keys(constraintsBySelector), function () { // per attribute in RSQL
            var attributeName = this
            var constraint = constraintsBySelector[attributeName]

            // If attribute name contains '.' we need to query the refEntity
            var seperatorPosition = attributeName.indexOf('.')
            if (seperatorPosition !== -1) {
                var referringAttributeName = attributeName.slice(0, seperatorPosition)
                var referredAttributeName = attributeName.slice(seperatorPosition + 1, attributeName.length)

                promises.push(getAttribute(entityTypeId, referringAttributeName, restApi).then(function (attribute) {
                    var referedEntityName = attribute.refEntity.name
                    return getAttribute(referedEntityName, referredAttributeName, restApi).then(function (referredAttribute) {
                        var model = molgenis.rsql.transformer.transformModelPart(referredAttribute.fieldType, [], constraint)

                        // getAttributeLabel uses an attribute.parent.label to create the complete label,
                        // and attribute.parent.name to do the request to the server
                        //
                        // add these entries here
                        referredAttribute.parent = {
                            'name': referringAttributeName,
                            'label': referringAttributeName
                        }

                        modelParts[attributeName] = {
                            'attribute': referredAttribute,
                            'model': model
                        }
                    })
                }, function (error) {
                    throw 'An error has occurred: ' + attributeName + ' ' + error.statusText;
                }))
            } else {
                // Retrieve V1 metadata for every attribute so we support legacy javascript
                promises.push(getAttribute(entityTypeId, attributeName, restApi).then(function (attribute) {

                    // Only retrieve labels if the attribute has a refEntity
                    if (attribute.refEntity) {
                        var values = molgenis.rsql.transformer.getArguments(constraint)

                        // Retrieve the items you want with an 'IN' query
                        return getLabelValues(attribute.refEntity.name, attribute.refEntity.idAttribute, attribute.refEntity.labelAttribute, values, restApi).then(function (labels) {
                            var model = molgenis.rsql.transformer.transformModelPart(attribute.fieldType, labels, constraint)
                            modelParts[attributeName] = {
                                'attribute': attribute,
                                'model': model
                            }
                        })
                    } else {
                        var model = molgenis.rsql.transformer.transformModelPart(attribute.fieldType, [], constraint)
                        modelParts[attributeName] = {
                            'attribute': attribute,
                            'model': model
                        }
                    }
                }, function (error) {
                    throw 'An error has occurred: ' + attributeName + ' ' + error.statusText;
                }))
            }
        })

        return Promise.all(promises).then(function () {
            return modelParts
        })
    }

    /**
     * Fetches metadata for an attribute from the V1 RestClient
     */
    function getAttribute(entityTypeId, attributeName, restApi) {
        return restApi.getAsync('/api/v1/' + encodeURIComponent(entityTypeId) + '/meta/' + encodeURIComponent(attributeName) + '?expand=refEntity')
    }

    /**
     * Fetches labels for identifiers from the V2 RestClient
     */
    function getLabelValues(refEntityName, refEntityIdAttribute, refEntityLabelAttribute, values, restApi) {
        var rsqlQuery = molgenis.rsql.encodeRsqlValue(molgenis.rsql.createRsqlQuery([{
            field: refEntityIdAttribute,
            operator: 'IN',
            value: Array.from(values.values())
        }]))
        var requestUri = '/api/v2/' + refEntityName + '?q=' + rsqlQuery

        return restApi.getAsync(requestUri).then(function (refEntityItems) {
            var labels = {}
            $.each(refEntityItems.items, function () {
                var id = this[refEntityIdAttribute]
                var label = this[refEntityLabelAttribute]
                labels[id] = label
            })
            return labels
        }, function (error) {
            throw 'An error has occurred: ' + error.statusText;
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

        // Ensures that xref.value is deleted from map
        if (attribute in existingModel) {
            delete existingModel[attribute]
        }
        else {
            $.each(Object.keys(existingModel), function () {
                var key = this
                if (key.indexOf('.' + attribute) !== -1) {
                    delete existingModel[key]
                }
            })
        }

        return molgenis.rsql.transformer.transformToRSQL(existingModel)
    }

}($, window.top.molgenis = window.top.molgenis || {}));