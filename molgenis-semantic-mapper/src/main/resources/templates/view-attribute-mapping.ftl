<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=['mapping-service.css']>
<#assign js=[
'attribute-mapping.js',
'd3.min.js',
'vega.min.js',
'jstat.min.js',
'biobankconnect-graph.js',
'jquery/scrollTableBody/jquery.scrollTableBody-1.0.0.js',
'bootbox.min.js',
'jquery.ace.js',
'jquery.highlight.js'
]>

<@header css js/>

<div class="row">
    <div class="col-md-12">
    <#-- Hidden fields containing information needed for ajax requests -->
        <input id="mappingProjectId" type="hidden" name="mappingProjectId" value="${mappingProject.identifier?html}"/>
        <input id="target" type="hidden" name="target" value="${entityMapping.targetEntityType.id?html}"/>
        <input id="source" type="hidden" name="source" value="${entityMapping.sourceEntityType.id?html}"/>
        <input id="targetAttribute" type="hidden" name="targetAttribute"
               value="${attributeMapping.targetAttribute.name?html}"/>
        <input id="targetAttributeType" type="hidden" name="targetAttributeType"
               value="${attributeMapping.targetAttribute.dataType?html}"/>
        <input id="sourceAttributeSize" type="hidden" value="${sourceAttributesSize?html}"/>
        <input id="dataExplorerUri" type="hidden" value="${dataExplorerUri?html}"/>
    </div>
</div>
<div class="row">
    <div id="attribute-mapping-toolbar" class="col-md-12 col-lg-12">
        <a href="/menu/main/mappingservice/mappingproject/${mappingProject.identifier?html}" type="btn"
           class="btn btn-default btn-xs">
            <span class="glyphicon glyphicon-chevron-left"></span>
            Cancel and go back
        </a>
        <button id="save-mapping-btn" type="btn" class="btn btn-primary btn-xs">
            <span class="glyphicon glyphicon-floppy-save"></span>
            Save
        </button>
        <button id="save-discuss-mapping-btn" type="btn" class="btn btn-danger btn-xs">
            <span class="glyphicon glyphicon-floppy-save"></span>
            Save to discuss
        </button>
    </div>
</div>
<div class="row">
    <div class="col-md-12 col-lg-12">
        <center><h4>Mapping to <i>${entityMapping.targetEntityType.id}
            .${attributeMapping.targetAttribute.name}</i> from <i>${entityMapping.sourceEntityType.id}</i>
        </h4></center>
    </div>
</div>
<div class="row">
    <div class="col-md-5 col-lg-5">
        <table class="table-borderless">
            <tr>
                <td class="td-align-top"><strong>Algorithm state</strong></td>
                <td id="algorithmState"
                    class="td-align-top"><#if attributeMapping.algorithmState??>${attributeMapping.algorithmState?html}<#else>
                    N/A</#if></td>
            </tr>
            <tr>
                <td class="td-align-top"><strong>Name</strong></td>
                <td class="td-align-top">${attributeMapping.targetAttribute.name?html}
                    (${attributeMapping.targetAttribute.dataType})
                </td>
            </tr>
            <tr>
                <td class="td-align-top"><strong>Label</strong></td>
                <td class="td-align-top"><#if attributeMapping.targetAttribute.label??>
                ${attributeMapping.targetAttribute.label?html}
                <#else>
                    N/A
                </#if>
                </td>
            </tr>
            <tr>
                <td class="td-align-top"><strong>Description</strong></td>
                <td class="td-align-top">
                <#if attributeMapping.targetAttribute.description??>
                ${attributeMapping.targetAttribute.description?html}
                <#else>
                    N/A
                </#if>
                </td>
            </tr>
            <tr>
                <td class="td-align-top"><strong>OntologyTerms</strong></td>
                <td class="td-align-top">
                <#if tags ?? && tags?size == 0>
                    N/A
                <#else>
                    <#list tags as tag>
                        <#assign synonyms = tag.synonyms?join("</br>")>
                        <span class="label label-info ontologytag-tooltip" data-toggle="popover"
                              title="<strong>Synonyms</strong>" data-content="${synonyms}">${tag.label?html}</span>
                    </#list>
                </#if>
                </td>
            </tr>
            <tr>
                <td class="td-align-top"><strong>Categories</strong></td>
                <td class="td-align-top">
                <#if attributeMapping.targetAttribute.dataType == "CATEGORICAL" || attributeMapping.targetAttribute.dataType == "CATEGORICAL_MREF" && (categories)?has_content>
                    <#assign refEntityType = attributeMapping.targetAttribute.refEntity>
                    <#assign idAttr = refEntityType.getIdAttribute()>
                    <#assign labelAttr = refEntityType.getLabelAttribute()>
                    <#list categories as category>
                        <#if (category[idAttr.name])??>
                            <#assign value = category[idAttr.name] />
                            <#assign dataType = idAttr.dataType />
                            <#if dataType == "DATE_TIME" || dataType == "DATE">
                            ${value.format()}
                            <#else>
                            ${value?string}
                            </#if>
                        </#if>

                        <#if labelAttr?? && labelAttr.name != idAttr.name>
                            <#if (category[labelAttr.name])??>
                                =
                                <#assign value = category[labelAttr.name] />
                                <#assign dataType = labelAttr.dataType />
                                <#if dataType == "DATE_TIME" || dataType == "DATE">
                                ${value.format()}
                                <#else>
                                ${value?string}
                                </#if>
                            </#if>
                        </#if>

                        </br>
                    </#list>
                <#else>
                    N/A
                </#if>
                </td>
            </tr>
        </table>
    </div>
</div>

<div class="row"> <#-- Start: Master row -->

    <div class="col-md-6 col-lg-4"> <#-- Start: Attribute table column -->
        <div id="attribute-mapping-table-container"> <#-- Start: Attribute table container -->

            <div class="row">
                <div class="col-md-12">
                    <legend>
                        Attributes
                        <i class="glyphicon glyphicon-question-sign" rel="tooltip" title="Select attribute(s) to map to
						${attributeMapping.targetAttribute.name?html}. By checking one of the attributes below,
						an algorithm will be generated and the result of your selection will be shown."></i>
                    </legend>
                    <form>
                        <div class="form-group">
                            <div class="input-group">
                                <input id="attribute-search-field" type="text" class="form-control"
                                       placeholder="Search all ${sourceAttributesSize?html} attributes from ${entityMapping.sourceEntityType.id?html}">
                                <span class="input-group-btn">
									<button id="attribute-search-field-button" type="button"
                                            class="btn btn-default"><span
                                            class="glyphicon glyphicon-search"></span></button>
								</span>
                            </div>
                        </div>
                    </form>
                </div>
            </div>

            <div class="row">
                <div class="col-md-12">
                    <p id="attribute-search-result-message"></p>
                    <table id="attribute-mapping-table" class="table table-bordered scroll"></table>
                </div>
            </div>

        </div> <#-- End: Attribute table container -->
    </div> <#-- End: Attribute table column -->

    <div class="col-md-6 col-lg-4"> <#-- Start: Mapping column -->
        <div id="attribute-mapping-container">  <#-- Start: Mapping container -->

            <div class="row">
                <div class="col-md-12">
                    <legend>
                        Mapping
                        <i class="glyphicon glyphicon-question-sign" rel="tooltip" title="Use one of the methods below to map the values of the
						selected attribute(s) to the target attribute. The script editor offers large control over your algorithm, but javascript knowledge is needed.
						<#if attributeMapping.targetAttribute.dataType == "XREF" || attributeMapping.targetAttribute.dataType == "CATEGORICAL" ||
                        attributeMapping.targetAttribute.dataType == "STRING">
				    		The Map tab allows you to map the various categorical values or strings to the categorical values of the target attribute.
				    	</#if>"></i>
                    </legend>
                </div>
            </div>

            <div class="row">
                <div class="col-md-12">
                    <ul class="nav nav-tabs" role="tablist">
                        <li id="script-tab" role="presentation" class="active"><a href="#script" aria-controls="script"
                                                                                  role="tab"
                                                                                  data-toggle="tab">Script</a></li>

                    <#if attributeMapping.targetAttribute.dataType == "XREF" || attributeMapping.targetAttribute.dataType == "CATEGORICAL">
                        <li id="map-tab" role="presentation"><a href="#map" aria-controls="map" role="tab"
                                                                data-toggle="tab">Map</a></li>
                    </#if>
                    </ul>
                </div>
            </div>

            <div class="row">
                <div class="col-md-12">
                    <div class="tab-content">
                        <div role="tabpanel" class="tab-pane active" id="script"><@script /></div>
                        <div role="tabpanel" class="tab-pane" id="map"><@map /></div>
                    </div>
                    <br/>
                </div>
            </div>
        </div> <#-- End: Mapping container -->
    </div>  <#-- End: Mapping column -->

    <div class="col-md-6 col-lg-4"> <#-- Start Result column -->
        <div id="result-container"> <#-- Start: Result container -->

            <div class="row">
                <div class="col-md-12">
                    <legend>
                        Result
                        <i class="glyphicon glyphicon-question-sign" rel="tooltip" title="The most right column contains the results
						of applying the algorithm over the values of the selected source attributes."></i>
                    </legend>
                    <p>

                    </p>
                    <h4>Validation</h4>
                    <p>Algorithm validation starts automatically when the algorithm is updated. In case of errors, click
                        the error label for more details</p>
                    <div id="mapping-validation-container"></div>
                    <h4>Preview</h4>
                    <div id="result-table-container"></div>
                </div>
            </div>

        </div> <#-- End: Result container -->
    </div> <#-- End: Result column -->

</div> <#-- End: Master row -->


<#-- map tab -->
<#macro map>
<div class="row">
    <div class="col-md-12">
        <div id="advanced-mapping-table"></div>
    </div>
</div>
</#macro>

<#-- algorithm editor tab -->
<#macro script>
<div class="row">
    <div class="col-md-12">
        <button class="btn btn-success pull-right" id="validate-algorithm-btn">Validate algorithm</button>
        <div class="ace-editor-container">
            <h4>Algorithm</h4>

            <p>
                Use the script editor to determine how the values of selected attributes are processed.
                See the <a id="js-function-modal-btn" href="#">list of available functions</a> for more information.
            </p>
            <textarea id="ace-editor-text-area" name="algorithm" rows="15" style="width:100%;">
                ${(attributeMapping.algorithm!"")?html}
            </textarea>
        </div>
    </div>
</div>
</#macro>


<div id="js-function-modal" class="modal fade">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title">Javascript function examples</h4>
            </div>

            <div class="modal-body">
                <div class="row">
                    <div class="col-md-2"
                    <strong>.map()</strong>
                </div>
                <div class="col-md-10"
                <p>can be used to map multiple values to eachother. Example: <b>$('GENDER').map({"0":"0","1":"1",}).value()</b>
                </p>
            </div>
        </div>

        <div class="row">
            <div class="col-md-2"
            <strong>.date()</strong>
        </div>
        <div class="col-md-10"
        <p>Can be used to calculate the date. Example: <b>$('DATE').date().value()</b></p>
    </div>
</div>
</div>
</div>
</div>
</div>
<div class="modal" id="validation-error-messages-modal" tabindex="-1" role="dialog"
     aria-labelledby="validation-error-messages-label" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="validation-error-messages-label">Validation Errors</h4>
            </div>
            <div class="modal-body">
                <table class="table table-bordered validation-error-messages-table">
                    <thead>
                    <th>Source Entity</th>
                    <th>Error message</th>
                    </thead>
                    <tbody id="validation-error-messages-table-body">
                    </tbody>
                </table>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>
<@footer/>