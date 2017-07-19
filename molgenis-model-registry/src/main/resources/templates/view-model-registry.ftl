<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['ui.fancytree.min.css', 'joint.min.css','model-registry.css']>
<#assign js=['jquery.fancytree.min.js', 'jquery.bootstrap.pager.js', 'lodash.js', 'backbone-min.js', 'chrome-getTransformToElement-polyfill.js', 'geometry.min.js', 'vectorizer.min.js', 'joint.clean.min.js','joint.shapes.uml.min.js', 'joint.layout.DirectedGraph.min.js', 'jquery.scrollTo.min.js', 'model-registry.js']>

<@header css js/>
<#-- Search box and search results -->
<div id="standards-registry-search">
    <div class="row">
        <div class="col-md-4">
            <form class="form-horizontal" name="search-form" action="${context_url?html}/search" method="post">
                <div class="form-group">
                    <div class="col-md-12">
                        <div class="input-group">
                            <input type="text" class="form-control" name="packageSearch" id="package-search"
                                   placeholder="Search models" autofocus="autofocus">
                            <span class="input-group-btn">
                            	<button id="search-button" class="btn btn-default" type="submit"><span
                                        class="glyphicon glyphicon-search"></span></button>
                            	<button id="search-clear-button" class="btn btn-default" type="button"><span
                                        class="glyphicon glyphicon-remove"></span></button>
                        	</span>
                        </div>
                    </div>
                </div>
            </form>
        </div>
        <div class="col-md-4">
            <div id="package-search-results-pager"></div>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
            <div id="package-search-results"
                 <#if packageSearchResponse?has_content>data-package-search-results='${packageSearchResponse?html}'</#if>></div>
        </div>
    </div>
</div>

<#-- Search result details: tree etc. -->
<div id="standards-registry-details" class="hidden"></div>

<#-- Handlebar templates -->
<script id="count-template" type="text/x-handlebars-template">
    <div class="row">
        <div class="col-md-12">
            <em class="pull-right">Found {{count}} models</em>
        </div>
    </div>
</script>

<script id="model-template" type="text/x-handlebars-template">
    <div class="well package" data-id="{{package.name}}">
        <h3 style="margin-top: 0px;">{{#if
            package.label}}{{package.label}}{{else}}{{package.name}}{{/if}}</h3>
        <p>{{package.description}}</p>
        <p>
            {{#each tags}}
            {{#if this.iri}}
            <span class="label label-primary"><a href='{{this.iri}}' target="_blank">{{this.label}}</a></span>
            {{else}}
            <span class="label label-primary">{{this.label}}</span>
            {{/if}}
            {{/each}}
        </p>
        {{#if package.matchDescription}}
        <p><span class="label label-default">{{package.matchDescription}}</span></p>
        {{/if}}
        <form class="form-inline">
            <div class="form-group">
                <a class="btn btn-primary details-btn" href="?package={{package.name}}#" role="button">View
                    Model
                    Details</a>
            </div>
            <div class="form-group{{#unless entities.length}} hidden{{/unless}}">
                <div class="input-group select2-bootstrap-append entity-select-control">
                    <select id="select2-input-group-append" class="form-control select2 entity-select-dropdown"
                            data-placeholder="Select an entity">
                        <option></option>
                        {{#each entities}}
                        <option value="{{this.name}}">{{this.label}}</option>
                        {{/each}}
                    </select>
                    <span class="input-group-btn">
                        <button class="btn btn-default dataexplorer-btn" type="button"
                                data-select2-open="select2-input-group-append">View Data</button>
                    </span>
                </div>
            </div>
        </form>
    </div>
</script>
<@footer />