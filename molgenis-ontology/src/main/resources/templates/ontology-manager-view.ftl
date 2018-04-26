<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["jasny-bootstrap.min.css", "ontology-indexer.css", "biobank-connect.css"]>
<#assign js=["jasny-bootstrap.min.js", "ontology-manager.js"]>
<@header css js/>
<#if removeSuccess??>
<div class="row">
    <div <#if removeSuccess>class="alert alert-info"<#else>class="alert alert-danger</#if>">
        <button type="button" class="close" data-dismiss="alert">&times;</button>
        <p><strong>Message : </strong> ${message?html}</p>
    </div>
</div>
</#if>
<form id="ontologymanager-form" class="form-horizontal" enctype="multipart/form-data">
    <br>
    <div class="row">
        <div class="col-md-12 well custom-white-well">
            <div class="row">
                <div class="col-md-offset-3 col-md-6 text-align-center">
                    <legend class="custom-purple-legend">
                        Ontology manager
                    </legend>
                </div>
            </div>
            <div class="row">
                <div class="col-md-offset-2 col-md-8 text-align-center">
                    <div class="row">
                        <div id="div-index-ontology" class="col-md-12">
                            <div class="row">
                                <div class="col-md-12">
                                <#if ontologies?? && ontologies?size!=0>
                                    <table id="ontology-table" class="table table-bordered">
                                        <tr>
                                            <th>Ontology</th>
                                            <th>Ontology uri</th>
                                            <th>Remove</th>
                                        </tr>
                                        <#list ontologies as ontology>
                                            <tr>
                                                <th>${ontology.name}</th>
                                                <th>${ontology.IRI}</th>
                                                <th>Remove</th>
                                            </tr>
                                        </#list>
                                    </table>
                                </#if>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <input type="hidden" name="ontologyUri"/>
        </div>
    </div>
</form>
<@footer/>