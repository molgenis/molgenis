<#include "resource-macros.ftl">
<#if annotationRun?? && (annotationRun.status == 'RUNNING')>
<div class="row">
    <div class="col-md-12">
        This entity is currently being annotated, details listed below.
    </div>
    <div class="col-md-12">
        <div id="annotateRun"></div>
    </div>
</div>
</div>
<script>
    $(function () {
        React.render(molgenis.ui.jobs.JobContainer({
            'jobHref': '/api/v1/sys' + molgenis.packageSeparator + 'job' + molgenis.packageSeparator + 'AnnotationJobExecution/${annotationRun.identifier}'
        }), $('#annotateRun')[0]);
    });
</script>
<#else>

<div class="row">
    <div class="col-md-12" id="annotator-select-container">
        <form id="annotate-dataset-form" role="form" class="well">
            <div class="row">
                <div class="col-md-6">
                    <div class="form-group">
                        <legend>Available annotators</legend>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="form-group">
                        <legend>Unavailable annotators
                            <a id="disabled-tooltip" data-toggle="tooltip"
                               title="These annotations are not available for the selected data set because:
					            1) The annotation data is not available on the server, 2) A webservice might be offline or 3) Your data set does not contain the correct columns">
                                <span class="glyphicon glyphicon-question-sign"></span>
                            </a>
                        </legend>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-md-6">
                    <a href="#" class="btn btn-link pull-left select-all-btn">Select all</a>
                    <a href="#" class="btn btn-link pull-left deselect-all-btn">Deselect all</a>
                </div>
            </div>
            <div class="row">
                <div class="col-md-12">
                    <hr style="margin:0px">
                </div>
            </div>
            <div class="row">
                <div class="col-md-6">
                    <div class="form-group">
                        <div id="enabled-annotator-selection-container"></div>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="form-group">
                        <div id="disabled-annotator-selection-container"></div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-md-12">
                    <div class="form-group">
                        <input type="hidden" value="" id="dataset-identifier" name="dataset-identifier">
                        <button id="annotate-dataset-button" class="btn btn-default">Annotate</button>
                    </div>
                </div>
            </div>
        </form>
    </div>
</div>

<div class="modal large" id="annotatorDescription" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-xxl">
        <div class="modal-content">
            <div class="modal-header">
                <button class="close" data-dismiss="modal">×</button>
                <h3 name="annotator" id="annotator"></h3>
            </div>
            <div class="modal-body">
                <h3 style="margin-bottom: 2px">Description</h3>
                <div name="description" id="description" class="well"></div>
                <h3>Required input attributes</h3>
                <div name="inputmetadata" id="inputmetadata" class="well"></div>
                <h3>Output attributes</h3>
                <div name="outputmetadata" id="outputmetadata" class="well"></div>
            </div>
        </div>
    </div>
</div>
</#if>

<script id="annotator-template" type="text/x-handlebars-template">
    {{#equal this.enabled 'true'}}
    <div class="checkbox">
        <label>
            <input type="checkbox" class="checkbox" name="annotatorNames" value="{{this.annotatorName}}">
            <a data-toggle="modal"
               data-annotator="{{this.annotatorName}}"
               data-description="{{this.description}}"
               data-inputmetadata="{{this.inputMetaData}}"
               data-outputmetadata="{{this.outputMetaData}}"
               class="open-annotatorDescription"
               href="#annotatorDescription">
                {{this.annotatorName}}
            </a>
        </label>
        {{#if this.showSettingsButton}}
        <span id="{{this.annotatorName}}-settings-btn" class="glyphicon glyphicon-cog" aria-hidden="true"
              style="cursor: pointer; margin-left: 5px;"></span>
        {{/if}}
    </div>
    {{/equal}}

    {{#notequal this.enabled 'true'}}
    <div class="checkbox">
        <label>
            <a data-toggle="modal"
               data-annotator="{{this.annotatorName}}"
               data-description="{{this.description}}"
               data-inputmetadata="{{this.inputMetaData}}"
               data-outputmetadata="{{this.outputMetaData}}"
               class="open-annotatorDescription"
               href="#annotatorDescription">
                {{this.annotatorName}}<i style="color: red"> ({{this.enabled}})</i>
            </a>
        </label>
        {{#if this.showSettingsButton}}
        <span id="{{this.annotatorName}}-settings-btn" class="glyphicon glyphicon-cog" aria-hidden="true"
              style="cursor: pointer; margin-left: 5px;"></span>
        {{/if}}
    </div>
    {{/notequal}}

    {{#if this.showSettingsButton}}
    <div id="{{this.annotatorName}}-settings-container"></div>
    {{/if}}
</script>

<script id="attributes-template" type="text/x-handlebars-template">
    <div class="row">
        <div class="col-sm-12">
            <div class="row">
                <div class="col-sm-3">
                    <b>Attribute name</b>
                </div>
                <div class="col-md-2">
                    <b>Datatype</b>
                </div>
                <div class="col-sm-7">
                    <b>Description</b>
                </div>
            </div>

            {{#inputParams}}
            <hr style="margin: 0px;">
            <div class="row">
                <div class="col-sm-3">
                    {{name}}
                </div>
                <div class="col-sm-2">
                    {{type}}
                </div>
                <div class="col-sm-7">
                    {{desc}}
                </div>
            </div>
            {{/inputParams}}
        </div>
    </div>
</script>

<#if annotationRun??>
<script>
    if ('${annotationRun.status}' === "SUCCESS") {
        molgenis.createAlert([{'message': 'This entity has most recently been annotated with: ${annotationRun.annotators}'}], 'info');
    }
    if ('${annotationRun.status}' === "FAILED") {
        molgenis.createAlert([{'message': 'The last annotation run for this entity has failed'}], 'warning');
    }
</script>
</#if>
<script>
    $.when($.ajax("<@resource_href "/js/dataexplorer-annotators.js"/>", {'cache': true}))
            .then(function () {
                molgenis.dataexplorer.annotators.getAnnotatorSelectBoxes();
            });

    $('.select-all-btn').click(function (e) {
        $("input[name='annotatorNames']").each(function () {
            this.checked = true;
        });
    });

    $('.deselect-all-btn').click(function (e) {
        $("input[name='annotatorNames']").each(function () {
            this.checked = false;
        });
    });

    $(document).on("click", ".open-annotatorDescription", function () {
        $(".modal-header #annotator").html($(this).data('annotator'));
        $(".modal-body #description").html($(this).data('description'));
        $(".modal-body #inputmetadata").html($(this).data('inputmetadata'));
        $(".modal-body #outputmetadata").html($(this).data('outputmetadata'));
    });
</script>