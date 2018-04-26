<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['mapping-service.css']>
<#assign js=['bootbox.min.js', 'jquery/scrollTableBody/jquery.scrollTableBody-1.0.0.js', 'tag-wizard.js']>

<@header css js/>

<div class="row">
    <div class="col-md-12">
        <input type="hidden" id="global-information" data-entity="${entity.id}"></input>
        <h3>Tag Wizard</h3>
        <p>
            Tag attributes with ontology terms from the selected ontologies either manually or automatically.
        </p>
        </br>
    </div>
</div>

<div class="row">
    <div class="col-md-12">
        <label>Select an entity to tag</label>
    </div>
</div>
<div class="row">
    <div class="col-md-3">
        <form id="change-entity-form" method="GET" action="${context_url}">
            <select id="select-target" name="selectedTarget" class="form-control">
            <#list entityTypeIds as entityTypeId>
                <option
                    <#if entityTypeId == entity.id>selected="selected"</#if>value="${entityTypeId?html}">${entityTypeId?html}</option>
            </#list>
            </select>
        </form>
    </div>
</div>

<div class="row">
    <div class="col=md-12">
        <hr></hr>
    </div>
</div>

<div class="row">
    <div class="col-md-12">
        <label>Select an ontology:</label>
    </div>
</div>

<div class="row">
    <div class="col-md-3">
        <select multiple class="form-control " name="ontology-select" id="ontology-select"
                data-placeholder="Select an ontology to use">
            <option value=""></option>
        <#if selectedOntologies??>
            <#list selectedOntologies as selectedOntology>
                <#if selectedOntology??>
                    <option selected="selected" data-iri="${selectedOntology.IRI} "
                            value="${selectedOntology.id}">${selectedOntology.name?html}</option>
                </#if>
            </#list>
        </#if>
        <#list ontologies as ontology>
            <option selected="selected" data-iri="${ontology.IRI}" value="${ontology.id}">${ontology.name?html}</option>
        </#list>
        </select>
    </div>
    <div class="col-md-9">
        <button type="button" class="btn btn-primary" id="automatic-tag-btn"><span
                class="glyphicon glyphicon-flash"></span> Run Automatic Tagging
        </button>
        <button type="button" class="btn btn-danger" id="clear-all-tags-btn"><span
                class="glyphicon glyphicon-trash"></span> Clear tags
        </button>
    </div>
</div>

<div class="row">
    <div class="col-md-12">
        <br></br>
    </div>
</div>

<div class="row">
    <div class="col-md-6">
        <table class="table table-bordered" id="tag-mapping-table">
            <thead>
            <th>Target Attribute</th>
            <th style="width:500px;">Tags</th>
            <th></th>
            </thead>
            <tbody>
            <#list attributes as attribute>
            <tr>
                <td>
                    <b>${attribute.name}</b>
                    <p><i>${attribute.label!""}</i></p>
                    <p><i>${attribute.description!""}</i></p>
                </td>
                <#assign relationsAndTagsMap = taggedAttributes[attribute.name]>
                <#if relationsAndTagsMap.keySet()?has_content>
                    <#list relationsAndTagsMap.keySet() as relation>
                        <td class="tag-column" id="${attribute.name}-tag-column">
                            <#list relationsAndTagsMap.get(relation) as tag>
                                <button type="btn" class="btn btn-primary btn-xs remove-tag-btn"
                                        data-relation="${relation.IRI}" data-attribute="${attribute.name}"
                                        data-tag="${tag.IRI}">
                                ${tag.label} <span class="glyphicon glyphicon-remove"></span>
                                </button>
                            </#list>
                        </td>
                    </#list>
                    <td>
                        <button type="btn" class="btn btn-default btn-xs edit-attribute-tags-btn pull-right"
                                data-relation="http://molgenis.org#isAssociatedWith"
                                data-attribute="${attribute.name}" data-toggle="modal"
                                data-target="#edit-ontology-modal">
                            Edit <span class="glyphicon glyphicon-pencil"></span>
                        </button>
                    </td>
                <#else>
                    <td class="tag-column" id="${attribute.name}-tag-column"></td>
                    <td style="width:10px">
                        <button type="btn" class="btn btn-default btn-xs edit-attribute-tags-btn pull-right"
                                data-relation="http://molgenis.org#isAssociatedWith"
                                data-attribute="${attribute.name}" data-toggle="modal"
                                data-target="#edit-ontology-modal">
                            Edit <span class="glyphicon glyphicon-pencil"></span>
                        </button>
                    </td>
                </#if>
            </tr>
            </#list>
            </tbody>
        </table>
    </div>
    <div id="edit-ontology-container" class="col-md-6"></div>
</div>

<div class="modal fade" id="edit-ontology-modal">
    <div class="modal-dialog">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h4 class="modal-title">Select ontology terms to add as tag</h4>
            </div>

            <div class="modal-body">
                <div class="row">
                    <div class="col-md-12">
                        <input id="tag-dropdown" type="hidden"></input>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                <button id="save-tag-selection-btn" data-dismiss="modal" type="button" class="btn btn-primary">Save
                    changes
                </button>
            </div>
        </div>
    </div>
</div>

<@footer/>