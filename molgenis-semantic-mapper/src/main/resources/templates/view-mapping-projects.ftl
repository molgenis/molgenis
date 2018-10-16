<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['mapping-service.css']>
<#assign js=['mapping-projects.js', 'bootbox.min.js', 'jquery/scrollTableBody/jquery.scrollTableBody-1.0.0.js']>

<@header css js/>
<@createNewMappingProjectModal />

<!--Table containing mapping projects-->
<div class="row">
    <div class="col-md-6">
        <h1>Mapping projects overview</h1>
        <p>Create and view mapping projects. <#if importerUri??>Upload additional target entities and mapped sources <a
                href="${importerUri?html}">here</a>.</#if></p>

    <#if entityTypes?has_content>
        <div class="btn-group" role="group">
            <button type="button" id="add-mapping-project-btn" class="btn btn-primary" data-toggle="modal"
                    data-target="#create-new-mapping-project-modal"><span class="glyphicon glyphicon-plus"></span>&nbsp;Add
                Mapping Project
            </button>
        </div>
    </#if>
        <hr/>
    </div>
</div>
<div class="row">
    <div class="col-md-6">
    <#if mappingProjects?has_content>
        <table class="table table-bordered" id="mapping-projects-tbl">
            <thead>
            <tr>
                <th></th>
                <th>Mapping name</th>
                <th>Max depth</th>
                <th>Target entities</th>
                <th>Mapped sources</th>
            </tr>
            </thead>
            <tbody>
                <#list mappingProjects as project>
                    <tr>
                        <td>
                            <form method="post" action="${context_url}/removeMappingProject"
                                  class="pull-left verify">
                                <input type="hidden" name="mappingProjectId" value="${project.identifier}"/>
                                <button type="submit" class="btn btn-danger btn-xs"><span
                                        class="glyphicon glyphicon-trash"></span></button>
                            </form>
                            <form method="post" action="${context_url}/mappingproject/clone" class="pull-left">
                                <input type="hidden" name="mappingProjectId" value="${project.identifier?html}"/>
                                <button type="submit" class="btn btn-default btn-xs clone-btn"><span
                                        class="glyphicon glyphicon-duplicate"></span></button>
                            </form>
                        </td>
                        <td>
                          <a href="${context_url}/mappingproject/${project.identifier}">${project.name?html}</a>
                        </td>
                        <td class="text-center">
                          <#if project.depth gt 1 >
                          <form method="post" action="${context_url}/mappingproject/setDepth" class="pull-left">
                            <input type="hidden" name="mappingProjectId" value="${project.identifier?html}"/>
                            <input type="hidden" name="depth" value="${project.depth - 1}"/>
                            <button type="submit" class="btn btn-default btn-xs plus-btn"><span
                                class="glyphicon glyphicon-minus"></span></button>
                          </form>
                          </#if>
                          ${project.depth}
                          <form method="post" action="${context_url}/mappingproject/setDepth" class="pull-right">
                            <input type="hidden" name="mappingProjectId" value="${project.identifier?html}"/>
                            <input type="hidden" name="depth" value="${project.depth + 1}"/>
                            <button type="submit" class="btn btn-default btn-xs minus-btn"><span
                                class="glyphicon glyphicon-plus"></span></button>
                          </form>
                        </td>
                      <td>
                          <#if project.mappingTargets?size != 0>
                            <#list project.mappingTargets as target>
                              <#if target??>${target.name?html}<#else><span class="bg-danger text-white">Target undefined or unknown</span></#if><#if target_has_next>, </#if>
                            </#list>
                          <#else>
                            <span class="bg-danger text-white">Targets undefined or unknown</span>
                          </#if>
                        </td>
                        <td>
                          <#list project.mappingTargets as target>
                            <#if target??>
                              <#list target.entityMappings as mapping>
                                <#if mapping.name??>${mapping.name?html}<#else><span class="bg-danger text-white">Source undefined or unknown</span></#if><#if mapping_has_next>, </#if>
                              </#list>
                            </#if>
                            <#break>
                          </#list>
                        </td>
                    </tr>
                </#list>
            </tbody>
        </table>
    </#if>
    </div>
</div>

<@footer/>

<#macro createNewMappingProjectModal>
<div class="modal fade" id="create-new-mapping-project-modal" tabindex="-1" role="dialog"
     aria-labelledby="create-new-mapping-project-modal" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="create-new-mapping-project-label">Create a new mapping project</h4>
            </div>
            <div class="modal-body">
                <form id="create-new-mapping-project-form" method="post" action="${context_url}/addMappingProject">
                    <div class="form-group">
                        <label>Mapping project name</label>
                        <input name="mapping-project-name" type="text" class="form-control" placeholder="Mapping name"
                               required="required">
                    </div>

                    <div class="form-group">
                        <label>Max Depth</label>
                        <input name="depth" type="number" class="form-control"
                               placeholder="Maximum mapping depth for references"
                               required="required"
                               value="3" min="1">
                      <span class="help-block">Maximum depth for evaluation of nested reference attributes.</span>
                    </div>

                    <hr/>

                    <div class="form-group">
                        <label>Select the Target entity</label>
                        <select name="target-entity" id="target-entity-select" class="form-control" required="required"
                                placeholder="Select a target entity">
                            <#list entityTypes as entityType>
                                <option value="${entityType.id?html}">${entityType.id?html}</option>
                            </#list>
                        </select>
                    </div>
                </form>
            </div>

            <div class="modal-footer">
                <button type="button" id="submit-new-mapping-project-btn" class="btn btn-primary">Create project
                </button>
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
            </div>
        </div>
    </div>
</div>
</#macro>	