/**
 * Annotators module
 *
 * Dependencies: dataexplorer.js
 *
 * @param $
 * @param molgenis
 */
(function ($, molgenis) {
    "use strict";

    molgenis.dataexplorer = molgenis.dataexplorer || {};

    var annotatorTemplate;
    var attributesTemplate;
    var self = molgenis.dataexplorer.annotators = molgenis.dataexplorer.annotators || {};

    // module api
    self.getAnnotatorSelectBoxes = getAnnotatorSelectBoxes;

    var restApi = new molgenis.RestClient();

    function getAnnotatorSelectBoxes() {
        // reset
        var entity = getEntity();
        var enabledAnnotatorContainer = $('#enabled-annotator-selection-container');
        var disabledAnnotatorContainer = $('#disabled-annotator-selection-container');

        restApi.getAsync(entity.href, null, function (dataset) {
            $.ajax({
                type: 'POST',
                url: '/annotators/get-available-annotators',
                data: JSON.stringify(dataset.name),
                contentType: 'application/json',
                success: function (resultMap) {
                    for (var key in resultMap) {
                        var enabled = resultMap[key]['canAnnotate'];
                        var desc = resultMap[key]["description"].toString();
                        var inputAttributes = resultMap[key]["inputAttributes"];
                        var inputAttributeTypes = resultMap[key]["inputAttributeTypes"];

                        var outputAttributes = resultMap[key]["outputAttributes"];
                        var outputAttributeTypes = resultMap[key]["outputAttributeTypes"];

                        var showSettingsButton = resultMap[key]["showSettingsButton"];

                        var inputmetadataString = createAttributeHtml(inputAttributes, inputAttributeTypes);
                        var outputmetadataString = createAttributeHtml(outputAttributes, outputAttributeTypes);

                        if (enabled === 'true') {
                            enabledAnnotatorContainer.append(annotatorTemplate({
                                'enabled': enabled,
                                'annotatorName': key,
                                'description': desc,
                                'inputMetaData': inputmetadataString,
                                'outputMetaData': outputmetadataString,
                                'showSettingsButton': showSettingsButton
                            }));
                        } else {
                            disabledAnnotatorContainer.append(annotatorTemplate({
                                'enabled': enabled,
                                'annotatorName': key,
                                'description': desc,
                                'inputMetaData': inputmetadataString,
                                'outputMetaData': outputmetadataString,
                                'showSettingsButton': showSettingsButton
                            }));
                        }

                        (function (key) {
                            $(document).on('click', '#' + key + '-settings-btn', function () {
                                React.unmountComponentAtNode($('#' + key + '-settings-container')[0]); // fix https://github.com/molgenis/molgenis/issues/3587
                                React.render(molgenis.ui.Form({
                                    entity: 'sys' + molgenis.packageSeparator + 'set' + molgenis.packageSeparator + key,
                                    entityInstance: key,
                                    mode: 'edit',
                                    modal: true,
                                    enableOptionalFilter: false,
                                    enableFormIndex: false,
                                    onSubmitSuccess: function () {
                                        location.reload();
                                    }
                                }), $('#' + key + '-settings-container')[0]);
                            });
                        })(key);
                    }

                    $('#selected-dataset-name').html(dataset.name);
                    $('#dataset-identifier').val(dataset.name);
                    $('.darktooltip').tooltip({placement: 'right'});
                }
            });
        });
    }

    function createAttributeHtml(inputAttributes, inputAttributeTypes) {
        var inputParams = [];
        for (var attr in inputAttributes) {
            var input = new Object();
            input.name = inputAttributes[attr].name;
            input.type = inputAttributeTypes[input.name];
            input.desc = inputAttributes[attr].description;
            inputParams.push(input);
        }
        return attributesTemplate({
            'inputParams': inputParams
        });
    }

    /**
     * Returns the selected attributes from the data explorer
     *
     * @memberOf molgenis.dataexplorer.annotators
     */
    function getAttributes() {
        var attributes = molgenis.dataexplorer.getSelectedAttributes();
        return molgenis.getAtomicAttributes(attributes, restApi);
    }

    /**
     * Returns the selected entity from the data explorer
     *
     * @memberOf molgenis.dataexplorer.annotators
     */
    function getEntity() {
        return molgenis.dataexplorer.getSelectedEntityMeta();
    }

    /**
     * Returns the selected entity query from the data explorer
     *
     * @memberOf molgenis.dataexplorer.annotators
     */
    function getEntityQuery() {
        return molgenis.dataexplorer.getEntityQuery().q;
    }

    // on document ready
    $(function () {
        var submitBtn = $('#annotate-dataset-button');
        var form = $('#annotate-dataset-form');

        $("#disabled-tooltip").tooltip();

        $('#annotate-dataset-form').click(function () {
            if ($('#annotate-dataset-form input:checkbox[name=annotatorNames]:checked').size() > 0) {
                submitBtn.removeAttr("disabled", "disabled");
            } else {
                submitBtn.attr("disabled", "disabled");
            }
        });

        annotatorTemplate = Handlebars.compile($("#annotator-template").html());
        attributesTemplate = Handlebars.compile($("#attributes-template").html());

        submitBtn.click(function (e) {
            e.preventDefault();
            e.stopPropagation();
            form.submit();
        });

        form.submit(function (e) {
            e.preventDefault();
            e.stopPropagation();
            if (form.valid()) {
                $.ajax({
                    type: 'POST',
                    url: '/annotators/annotate-data/',
                    data: form.serialize(),
                    contentType: 'application/x-www-form-urlencoded',
                    success: function (name) {
                        window.location.replace("?mod=annotators&entity=" + name);
                    }
                });
            }
        });

        submitBtn.attr("disabled", "disabled");
    });
}($, window.top.molgenis = window.top.molgenis || {}));	