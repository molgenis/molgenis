<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['ui.fancytree.min.css', 'jquery-ui-1.9.2.custom.min.css', 'catalogue.css']>
<#assign js=['jquery-ui-1.9.2.custom.min.js', 'jquery.fancytree.min.js', 'jquery.molgenis.tree.js','catalogue.js']>

<@header css js/>

<div id="entity-class" class="well">
	<h3 id="entity-class-name"></h3>
	<span id="entity-class-description"></span>
</div>

<div class="row">
	<div class="pull-right col-md-4" <#if hideDatasetSelect??> style="display:none"</#if>>
    	<div class="form-horizontal form-group">
        	<label class="col-md-4 control-label" for="dataset-select">Choose a dataset:</label>
        	<div class="col-md-8">
        		<select class="form-control" id="entity-select" data-placeholder="Choose an Entity (example: dataset, protocol..." id="dataset-select">
            	<#list entitiesMeta as entityMeta>
                	<option value="/api/v1/${entityMeta.name}" <#if entityMeta.name == selectedEntityName> selected</#if>><#if entityMeta.label?has_content>${entityMeta.label}<#else>${entityMeta.name}</#if></option>
               	</#list>
            	</select>
       		</div>
 		</div>
	</div>
</div>


<div class="col-md-3">
	<div class="well">
	<div class="panel">
    	<div class="panel-heading">
        	<h4 class="panel-title">Data item selection</h4>
        </div>
        <div class="panel-body">
        	<div id="attribute-selection"></div>
        </div>
    </div>
    </div>
</div>

<@footer/>