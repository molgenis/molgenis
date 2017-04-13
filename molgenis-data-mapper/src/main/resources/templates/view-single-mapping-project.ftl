<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['mapping-service.css']>
<#assign js=['single-mapping-project.js', 'bootbox.min.js', 'jquery/scrollTableBody/jquery.scrollTableBody-1.0.0.js']>

<@header css js/>

<div class="row">
    <div class="col-md-12">
        <a href="${context_url}" class="btn btn-default btn-xs">
            <span class="glyphicon glyphicon-chevron-left"></span> Back to mapping project overview
        </a>
    </div>
</div>

<div class="row">
    <div class="col-md-6">
        <h3>Mappings for the ${mappingProject.name?html} project</h3>
        <p>Create and view mappings.</p>
    </div>
    <div class="col-md-4">
        <div class="row">
            <div class="col-md-12">
                <p class="bg-success text-center pull-right algorithm-color-legend">Curated algorithms</p>
            </div>
        </div>
        <div class="row">
            <div class="col-md-12">
                <p class="bg-info text-center pull-right algorithm-color-legend">Generated algorithms with high
                    quality</p>
            </div>
        </div>
        <div class="row">
            <div class="col-md-12">
                <p class="bg-warning text-center pull-right algorithm-color-legend">Generated algorithms with low
                    quality</p>
            </div>
        </div>
        <div class="row">
            <div class="col-md-12">
                <p class="bg-danger text-center pull-right algorithm-color-legend">Algorithms to discuss</p>
            </div>
        </div>
    </div>
</div>

<!--Table for Target and Source attribute metadata-->
<div class="row">
    <div class="col-md-10">
        <table id="attribute-mapping-table" class="table table-bordered">
            <thead>
            <tr>
                <th>Target model: ${selectedTarget?html}</th>
            <#list mappingProject.getMappingTarget(selectedTarget).entityMappings as source>
                <th>Source: ${source.name?html}
                    <#if hasWritePermission>
                        <div class="pull-right">
                            <form method="post" action="${context_url}/removeEntityMapping" class="verify">
                                <input type="hidden" name="mappingProjectId" value="${mappingProject.identifier}"/>
                                <input type="hidden" name="target" value="${selectedTarget}"/>
                                <input type="hidden" name="source" value="${source.name}"/>
                                <button type="submit" class="btn btn-danger btn-xs pull-right"><span
                                        class="glyphicon glyphicon-trash"></span></button>
                            </form>
                        </div>
                    </#if>
                </th>
            </#list>
            </tr>
            </thead>
            <tbody>
            <#list mappingProject.getMappingTarget(selectedTarget).target.getAtomicAttributes() as attribute>
            <tr>
                <td>
                    <b>${attribute.label?html}</b> (${attribute.dataType})
                    <#if !attribute.nillable> <span class="label label-default">required</span></#if>
                    <#if attribute.unique> <span class="label label-default">unique</span></#if>
                    <#if attribute.description??><br/>${attribute.description?html}</#if>
                    <#if attribute.tags??><br/><#list attribute.tags as tag>${tag.label?html}<#sep>
                        , </#list></#if>
                    <#if attributeTagMap[attribute.name]??>
                        <br/>
                        <#list attributeTagMap[attribute.name] as tag>
                            <span class="label label-danger"> ${tag.label}</span>
                        </#list>
                    </#if>
                </td>
                <#list mappingProject.getMappingTarget(selectedTarget).entityMappings as source>
                    <td <#if source.getAttributeMapping(attribute.name)??>
                        <#assign attributeMapping = source.getAttributeMapping(attribute.name)>
                        <#if attributeMapping.algorithmState??>
                            <#if attributeMapping.algorithmState == "GENERATED_HIGH">
                                    class="bg-info"
                            <#elseif attributeMapping.algorithmState == "GENERATED_LOW">
                                    class="bg-warning"
                            <#elseif attributeMapping.algorithmState == "CURATED">
                                    class="bg-success"
                            <#elseif attributeMapping.algorithmState == "DISCUSS">
                                    class="bg-danger"
                            </#if>
                        </#if>
                    </#if>>
                        <div class="pull-right">
                            <form method="get" action="${context_url}/attributeMapping" class="pull-right">
                                <button type="submit" class="btn btn-default btn-xs">
                                    <span class="glyphicon glyphicon-pencil"></span>
                                </button>
                                <input type="hidden" name="mappingProjectId" value="${mappingProject.identifier}"/>
                                <input type="hidden" name="target" value="${selectedTarget}"/>
                                <input type="hidden" name="source" value="${source.name}"/>
                                <input type="hidden" name="targetAttribute" value="${attribute.name}"/>
                            </form>
                            <#if hasWritePermission && source.getAttributeMapping(attribute.name)??>
                                <form method="post" action="${context_url}/removeAttributeMapping"
                                      class="pull-right verify">
                                    <button type="submit" class="btn btn-default btn-xs">
                                        <span class="glyphicon glyphicon-remove"></span>
                                    </button>
                                    <input type="hidden" name="mappingProjectId" value="${mappingProject.identifier}"/>
                                    <input type="hidden" name="target" value="${selectedTarget}"/>
                                    <input type="hidden" name="source" value="${source.name}"/>
                                    <input type="hidden" name="attribute" value="${attribute.name}"/>
                                </form>
                            </#if>
                        </div>
                        <div>
                            <#if source.getAttributeMapping(attribute.name)??>
                                <#assign attributeMapping = source.getAttributeMapping(attribute.name)>
                                <#list attributeMapping.sourceAttributes as mappedSourceAttribute>
                                <#if mappedSourceAttribute??>${mappedSourceAttribute.label?html}<#if mappedSourceAttribute_has_next>, </#if></#if>
                                    <#if attributeMapping.algorithmState??></#if>
                                </#list>
                            <#elseif !attribute.nillable>
                                <span class="label label-danger">missing</span>
                            </#if>
                        </div>
                    </td>
                </#list>
            </tr>
            </#list>
            </tbody>
        </table>

    </div>
<#if entityTypes?has_content && hasWritePermission>
    <div class="col-md-2">
        <a id="add-new-attr-mapping-btn" href="#" class="btn btn-primary btn-xs" data-toggle="modal"
           data-target="#create-new-source-column-modal"><span class="glyphicon glyphicon-plus"></span>Add source</a>
    </div>
</#if>
</div>

<div class="row">
<#if mappingProject.getMappingTarget(selectedTarget).entityMappings?has_content>
    <div class="col-md-8">
        <a id="create-integrated-entity-open-modal-btn" href="#" class="btn btn-success pull-right" data-toggle="modal"
           data-target="#create-integrated-entity-modal">
            <span class="glyphicon glyphicon-play"></span> Create integrated dataset
        </a>
    </div>
</#if>
</div>

<!--Create new source dialog-->
<div class="modal" id="create-new-source-column-modal" tabindex="-1" role="dialog">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title" id="create-new-source-column-modal-label">Add new source</h4>
            </div>
            <div class="modal-body">
                <form id="create-new-source-form" method="post" action="${context_url}/addEntityMapping">
                    <div class="form-group">
                        <label>Select a new source to map against the target attribute</label>
                        <select name="source" id="source-entity-select" class="form-control" required="required"
                                placeholder="Select source entity">
                        <#list entityTypes as entityType>
                            <option value="${entityType.id?html}">${entityType.id?html}</option>
                        </#list>
                        </select>
                    </div>
                    <input type="hidden" name="mappingProjectId" value="${mappingProject.identifier}">
                    <input type="hidden" name="target" value="${selectedTarget}">
                </form>
            </div>

            <div class="modal-footer">
                <button type="button" id="submit-new-source-column-btn" class="btn btn-primary">Add source</button>
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
            </div>
        </div>
    </div>
</div>

<!--Create integrated entity dialog-->
<div class="modal" id="create-integrated-entity-modal" tabindex="-1" role="dialog">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title" id="create-integrated-entity-modal-label">Create integrated dataset</h4>
            </div>
            <div class="modal-body">
                <form id="create-integrated-entity-form" method="post" action="${context_url}/createIntegratedEntity">

                    <div>
                        <label>Enter a name for the integrated dataset.</label>
                        <input name="newEntityName" type="text" value="" required/>
                        <p></p>
                        <small>The mapped entity will be placed in the same package as the target.</small>
                        </p>
                    </div>
                    <div>
                        <label>Add source attribute</label>
                        <input name="addSourceAttribute" type="checkbox"/>
                    </div>

                    <input type="hidden" name="mappingProjectId" value="${mappingProject.identifier}">
                    <input type="hidden" name="target" value="${selectedTarget}">
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" id="create-integrated-entity-btn" class="btn btn-primary">Create integrated
                    dataset
                </button>
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
            </div>
        </div>
    </div>
</div>
<@footer/>