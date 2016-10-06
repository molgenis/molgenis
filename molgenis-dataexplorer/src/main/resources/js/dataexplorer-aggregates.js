/**
 * Aggregates module
 *
 * Dependencies: dataexplorer.js
 *
 * @param $
 * @param molgenis
 */
(function ($, molgenis) {
    "use strict";

    var AGGREGATE_ANONYMIZATION_VALUE = -1;

    molgenis.dataexplorer = molgenis.dataexplorer || {};
    var self = molgenis.dataexplorer.aggregates = molgenis.dataexplorer.aggregates || {};

    // module api
    self.createAggregatesTable = createAggregatesTable;
    self.updateAggregatesTable = updateAggregatesTable;

    var restApi = new molgenis.RestClient();
    var restApiV2 = new molgenis.RestClientV2();

    /**
     * @memberOf molgenis.dataexplorer.aggregates
     */
    function createAggregatesTable() {
        var attributes = getAttributes();
        var aggregableAttributes = $.grep(attributes, function (attribute) {
            if (attribute.isAggregatable) {
                if (attribute.nillable) {
                    return attribute.fieldType !== 'CATEGORICAL' && attribute.fieldType !== 'XREF' && attribute.fieldType !== 'MREF' && attribute.fieldType !== 'CATEGORICAL_MREF' && attribute.fieldType !== 'ONE_TO_MANY';
                }
                return true;
            }
            return false;
        });

        if (aggregableAttributes.length > 0) {
            createAttributeDropdown($('#x-aggr-div'), aggregableAttributes, 'x-aggr-attribute', aggregableAttributes[0], true);
            createAttributeDropdown($('#y-aggr-div'), aggregableAttributes, 'y-aggr-attribute', aggregableAttributes.length > 1 ? aggregableAttributes[1] : false);
            $('#distinct-attr-select').empty();
            if (molgenis.dataexplorer.settings['agg_distinct'] === false) {
                $('#distinct-attr').hide();
            } else {
                $('#distinct-attr').show();
                if (molgenis.dataexplorer.settings['agg_distinct_overrides'] && JSON.parse(molgenis.dataexplorer.settings['agg_distinct_overrides'])[getEntity().name]) {
                    // show fixed value for this entity
                    var distinctAttr = JSON.parse(molgenis.dataexplorer.settings['agg_distinct_overrides'])[getEntity().name];
                    var distinctAttrLabel = getEntity().attributes[distinctAttr].label;
                    $('#distinct-attr-select').append($('<p>').addClass('form-control-static').text(distinctAttrLabel));
                } else {
                    var distinctAttributes = $.grep(attributes, function (attribute) {
                        // see: https://github.com/molgenis/molgenis/issues/1938
                        return attribute.nillable !== true;
                    });
                    createAttributeDropdown($('#distinct-attr-select'), distinctAttributes, 'distinct-aggr-attribute', false);
                }
            }

            $('#feature-select-container').show();
            $('#aggregate-table-container').empty();

            $('.attribute-dropdown').on('change', function () {
                updateAggregatesTable($('#x-aggr-attribute').val(), $('#y-aggr-attribute').val(), $('#distinct-aggr-attribute').val());
            });

            //render first results
            updateAggregatesTable($('#x-aggr-attribute').val(), $('#y-aggr-attribute').val(), $('#distinct-aggr-attribute').val());
        } else {
            $('#feature-select-container').hide();
            $('#aggregate-table-container').html('<p>No aggregable items</p>');
        }
    }

    function createAttributeDropdown(parent, aggregableAttributes, id, defaultValue, hasDefault) {
        parent.empty();
        if (defaultValue && hasDefault) {
            var attributeSelect = $('<select id="' + id + '" class="attribute-dropdown"/>');
        }
        else {
            var attributeSelect = $('<select id="' + id + '" class="attribute-dropdown" data-placeholder="Select ..." />');
            attributeSelect.append('<option value="">Select ...</option>');
        }
        $.each(aggregableAttributes, function () {
            if (this == defaultValue) attributeSelect.append('<option selected value="' + this.name + '">' + this.label + '</option>');
            else attributeSelect.append('<option value="' + this.name + '">' + this.label + '</option>');
        });

        parent.append(attributeSelect);
        attributeSelect.select2({width: '100%'});
    }

    /**
     * @memberOf molgenis.dataexplorer.aggregates
     */
    function updateAggregatesTable(xAttributeName, yAttributeName, distinctAttributeName) {
        if ($('#aggregate-table-container').length > 0) {
            React.unmountComponentAtNode($('#aggregate-table-container')[0]);

            React.render(molgenis.ui.AggregateTable({
                entity: getEntity().name,
                x: xAttributeName,
                y: yAttributeName,
                distinct: distinctAttributeName,
                query: getEntityQuery()
            }), $('#aggregate-table-container')[0]);
        }
    }

    /**
     * Returns the selected attributes from the data explorer
     *
     * @memberOf molgenis.dataexplorer.aggregates
     */
    function getAttributes() {
        var attributes = molgenis.dataexplorer.getSelectedAttributes();
        var selectedEntityMeta = getEntity();

        //No 'nested' mref attributes
        attributes = $.grep(attributes, function (attribute) {
            return selectedEntityMeta.attributes[attribute.name] !== undefined;
        });

        return molgenis.getAtomicAttributes(attributes, restApi);
    }

    /**
     * Returns the selected entity from the data explorer
     *
     * @memberOf molgenis.dataexplorer.aggregates
     */
    function getEntity() {
        return molgenis.dataexplorer.getSelectedEntityMeta();
    }

    /**
     * Returns the selected entity query from the data explorer
     *
     * @memberOf molgenis.dataexplorer.aggregates
     */
    function getEntityQuery() {
        return molgenis.dataexplorer.getEntityQuery().q;
    }

    $(function () {
        $(document).off('.aggregates');

        // bind event handlers with namespace
        $(document).on('changeAttributeSelection.aggregates', function (e, data) {
            if (molgenis.dataexplorer.getSelectedModule() === 'aggregates') {
                molgenis.dataexplorer.aggregates.createAggregatesTable();
            }
        });

        $(document).on('changeQuery.aggregates', function (e, entitySearchQuery) {
            if (molgenis.dataexplorer.getSelectedModule() === 'aggregates') {
                var xAttribute = $('#x-aggr-attribute').val();
                var yAttribute = $('#y-aggr-attribute').val();
                var distinctAttributeName = $('#distinct-aggr-attribute').val();

                molgenis.dataexplorer.aggregates.updateAggregatesTable(xAttribute, yAttribute, distinctAttributeName);
            }
        });

        $(document).on('changeModule.aggregates', function (e, mod) {
            if (mod === 'aggregates') {
                molgenis.dataexplorer.aggregates.createAggregatesTable();
            }
        });
    });

})($, window.top.molgenis = window.top.molgenis || {});