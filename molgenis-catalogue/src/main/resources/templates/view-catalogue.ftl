<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['ui.fancytree.min.css', 'catalogue.css']>
<#assign js=['jquery.fancytree.min.js', 'jquery.molgenis.tree.js', 'jquery.molgenis.attributemetadata.table.js', 'catalogue.js']>

<@header css js/>

<#if selectedEntityName??>
<div id="entity-class" class="well clearfix">
    <h3 id="entity-class-name"></h3>
    <span id="entity-class-description"></span>

    <#if showEntitySelect?string('true', 'false') == 'true'>
        <div class="dropdown pull-right">
            <button class="btn btn-default dropdown-toggle" type="button" id="dropdown-menu-entities"
                    data-toggle="dropdown">
                Choose an entity <span class="caret"></span>
            </button>
            <ul class="dropdown-menu scrollable-menu" role="menu" aria-labelledby="dropdown-menu-entities">
                <#list entitiesMeta as entityMeta>
                    <li role="presentation">
                        <a role="menuitem" tabindex="-1" href="#" id="/api/v1/${entityMeta.id?html}"
                           class="entity-dropdown-item">${entityMeta.label?html}</a>
                    </li>
                </#list>
            </ul>
        </div>
    </#if>
</div>

<div class="row">
    <div class="col-md-3">
        <div class="well">
            <div class="panel">
                <div class="panel-heading">
                    <h4 class="panel-title clearfix">
                        Data item selection
                    </h4>
                </div>
                <div class="panel-body">
                    <div id="attribute-selection"></div>
                </div>
            </div>
        </div>
    </div>

    <div class="pull-right col-md-9">
        <div class="well">
            <div id="attributes-table"></div>
        </div>
    </div>

</div>

<div class="modal" id="cart-modal"></div>

<script>var selectedEntityName = '${selectedEntityName?js_string}';</script>
<#else>
<span>No available catalogues.</span>
<script>var selectedEntityName = undefined;</script>
</#if>
<@footer/>