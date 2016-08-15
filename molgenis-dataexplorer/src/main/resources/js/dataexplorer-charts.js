/**
 * This file will not work properly without loading first the dataexplorer.js file
 *
 * @param $
 * @param molgenis
 */
(function ($, molgenis) {
    "use strict";
    molgenis.charts = molgenis.charts || {};
    var ns = molgenis.charts.dataexplorer = molgenis.charts.dataexplorer || {};
    var restApi = new molgenis.RestClient();

    function createScatterPlotChartRequestPayLoad(entity,
                                                  x,
                                                  y,
                                                  xAxisLabel,
                                                  yAxisLabel,
                                                  width,
                                                  height,
                                                  title,
                                                  query,
                                                  splitFeature) {

        return {
            "entity": entity,
            "width": width,
            "height": height,
            "title": title,
            "type": "SCATTER_CHART",
            "query": query,
            "x": x,
            "y": y,
            "xAxisLabel": xAxisLabel,
            "yAxisLabel": yAxisLabel,
            "split": splitFeature
        };
    }

    function createBoxPlotChartRequestPayLoad(entity,
                                              width,
                                              height,
                                              title,
                                              featureIdentifier,
                                              splitIdentifier,
                                              query,
                                              scale) {

        return {
            "entity": entity,
            "width": width,
            "height": height,
            "title": title,
            "type": "BOXPLOT_CHART",
            "observableFeature": featureIdentifier,
            "split": splitIdentifier,
            "query": query,
            "scale": scale
        };
    }

    /**
     * create string of option elements for a select
     *
     * @param attributes
     */
    function createAttributeSelectOptions(attributes, selectedVal) {
        var items = [];
        items.push('<option value=' + '-1' + '>select</option>');
        $.each(attributes, function () {
            items.push('<option value=' + this.href + (selectedVal === this.href ? ' selected ' : '') + '>' + this.name + '</option>');
        });
        return items.join('');
    }

    /**
     * make the scatter plot
     */
    ns.createScatterPlot = function (entity, entityQuery, xAxisFeature, yAxisFeature, splitFeature) {
        var width = 1024;
        var height = 576;
        var title = $('#scatterplot-title').val();
        var x, y, xAxisLabel, yAxisLabel, xAxisDataType, split;

        if (xAxisFeature) {
            x = xAxisFeature.name;
            xAxisLabel = xAxisFeature.label;
            xAxisDataType = xAxisFeature.fieldType;
        }

        if (yAxisFeature) {
            y = yAxisFeature.name;
            yAxisLabel = yAxisFeature.label;
        }

        if (splitFeature) {
            split = splitFeature.name;
        }

        $.ajax({
            type: "POST",
            url: "/charts/xydatachart",
            data: JSON.stringify(createScatterPlotChartRequestPayLoad(
                entity.name,
                x,
                y,
                xAxisLabel,
                yAxisLabel,
                width,
                height,
                title,
                entityQuery,
                split
            )),
            contentType: "application/json; charset=utf-8",
            cache: false,
            async: true,
            success: function (options) {
                $('#tabs a:last').tab('show');
                if (xAxisDataType === 'DATE' || xAxisDataType === 'DATE_TIME') {
                    $('#chart-view').highcharts('StockChart', options);
                } else {
                    $('#chart-view').highcharts(options);
                }
            }
        });

    };

    //Box Plot
    ns.createBoxPlot = function (entity, entityQuery, attribute, splitAttribute) {
        var title = $('#boxplot-title').val();
        var width = 1024;
        var height = 576;
        var featureIdentifier, splitIdentifier;
        var scale;

        if ($('#boxplot-scale').val() === "") {
            scale = 1.5; // Default value
        } else {
            scale = new Number($('#boxplot-scale').val());
        }

        if (attribute) {
            featureIdentifier = attribute.name;
        }

        if (splitAttribute) {
            splitIdentifier = splitAttribute.name;
        }

        $.ajax({
            type: "POST",
            url: "/charts/boxplot",
            data: JSON.stringify(createBoxPlotChartRequestPayLoad(
                entity.name,
                width,
                height,
                title,
                featureIdentifier,
                splitIdentifier,
                entityQuery,
                scale
            )),
            contentType: "application/json; charset=utf-8",
            cache: false,
            async: true,
            success: function (options) {
                $('#tabs a:last').tab('show');
                $('#chart-view').highcharts(options);
            }
        });

    };

    function activateDesignerSubmitButtonScatterPlot() {
        var disabled = true;
        var valueOne = $('#scatterplot-select-yaxis-feature').val();
        var valueTwo = $('#scatterplot-select-xaxis-feature').val();

        if (valueOne && (valueOne !== "-1") && valueTwo && (valueTwo !== "-1")) {
            disabled = false;
        }

        $("#scatterplot-designer-modal-create-button").prop('disabled', disabled);
    }

    function activateDesignerSubmitButtonBoxPlot() {
        var disabled = true;
        var valueOne = $('#boxplot-select-feature').val();

        if (valueOne && (valueOne !== "-1")) {
            disabled = false;
        }

        $('#boxplot-designer-modal-create-button').prop('disabled', disabled);
    }

    /**
     * Returns the selected attributes from the data explorer
     */
    function getAttributes() {
        var attributes = molgenis.dataexplorer.getSelectedAttributes();
        return molgenis.getAtomicAttributes(attributes, restApi);
    }

    /**
     * Returns the selected entity from the data explorer
     */
    function getEntity() {
        return molgenis.dataexplorer.getSelectedEntityMeta();
    }

    /**
     * Returns the selected entity query from the data explorer
     */
    function getEntityQuery() {
        var query = molgenis.dataexplorer.getEntityQuery();
        return {'rules': [query.q]};
    }

    /**
     * Returns attributes map with attribute.href as key
     */
    function toAttributeMap(attributes) {
        var attributeMap = {};
        $.each(attributes, function () {
            attributeMap[this.href] = this;
        });
        return attributeMap;
    }

    $(function () {
        // bind event handlers with namespace

        // scatter plot modal
        $(document).on('show.bs.modal.charts', '#chart-designer-modal-scatterplot', function () {
            $("#scatterplot-designer-modal-create-button").prop('disabled', true); // reset

            var attributes = getAttributes();
            var xaxisAttributes = $.grep(attributes, function (attribute) {
                return $.inArray(attribute.fieldType, ['DECIMAL', 'LONG', 'INT', 'DATE', 'DATE_TIME']) !== -1;
            });
            var yaxisAttributes = $.grep(attributes, function (attribute) {
                return $.inArray(attribute.fieldType, ['DECIMAL', 'LONG', 'INT', 'DATE', 'DATE_TIME']) !== -1;
            });

            $('#scatterplot-select-xaxis-feature').html(createAttributeSelectOptions(xaxisAttributes, $('#scatterplot-select-xaxis-feature').val()));
            $('#scatterplot-select-yaxis-feature').html(createAttributeSelectOptions(yaxisAttributes, $('#scatterplot-select-yaxis-feature').val()));
            $('#scatterplot-select-split-feature').html(createAttributeSelectOptions(getAttributes(), $('#scatterplot-select-split-feature').val()));
            $('#scatterplot-designer-modal-create-button').data('attributes', attributes);

            activateDesignerSubmitButtonScatterPlot();
        });

        $('#scatterplot-select-xaxis-feature').change(activateDesignerSubmitButtonScatterPlot);
        $('#scatterplot-select-yaxis-feature').change(activateDesignerSubmitButtonScatterPlot);

        // box plot modal
        $(document).on('show.bs.modal.charts', '#chart-designer-modal-boxplot', function () {
            $("#boxplot-designer-modal-create-button").prop('disabled', true); // reset

            var attributes = getAttributes();
            var attributeAttributes = $.grep(attributes, function (attribute) {
                return $.inArray(attribute.fieldType, ['DECIMAL', 'LONG', 'INT']) !== -1;
            });

            $('#boxplot-select-feature').html(createAttributeSelectOptions(attributeAttributes, $('#boxplot-select-feature').val()));
            $('#boxplot-select-split-feature').html(createAttributeSelectOptions(attributes, $('#boxplot-select-split-feature').val()));
            $('#boxplot-designer-modal-create-button').data('attributes', attributes);

            activateDesignerSubmitButtonBoxPlot();
        });

        $('#scatterplot-designer-modal-create-button').click(function () {
            var attributeMap = toAttributeMap($('#scatterplot-designer-modal-create-button').data('attributes'));
            var xaxisAttribute = attributeMap[$('#scatterplot-select-xaxis-feature').val()];
            var yaxisAttribute = attributeMap[$('#scatterplot-select-yaxis-feature').val()];
            var splitAttribute = attributeMap[$('#scatterplot-select-split-feature').val()];
            ns.createScatterPlot(getEntity(), getEntityQuery(), xaxisAttribute, yaxisAttribute, splitAttribute);
            $('#scatterplot-designer-modal-create-button').removeData();
        });

        $('#boxplot-designer-modal-create-button').click(function () {
            var attributeMap = toAttributeMap($('#boxplot-designer-modal-create-button').data('attributes'));
            var attribute = attributeMap[$('#boxplot-select-feature').val()];
            var splitAttribute = attributeMap[$('#boxplot-select-split-feature').val()];
            ns.createBoxPlot(getEntity(), getEntityQuery(), attribute, splitAttribute);
            $('#boxplot-designer-modal-create-button').removeData();
        });

        $('#boxplot-select-feature').change(activateDesignerSubmitButtonBoxPlot);
    });
})($, window.top.molgenis = window.top.molgenis || {});