<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['ui.fancytree.min.css', 'jquery-ui-1.9.2.custom.min.css', 'catalogue.css']>
<#assign js=['jquery-ui-1.9.2.custom.min.js', 'jquery.fancytree.min.js', 'jquery.molgenis.tree.js', 'jquery.molgenis.attributemetadata.table.js', 'catalogue.js']>

<@header css js/>

<div id="entity-class" class="well">
	<h3 id="entity-class-name"></h3>
	<span id="entity-class-description"></span>
	<button type="button" class="pull-right btn btn-default"><span class="glyphicon glyphicon-shopping-cart"></span></button>				
</div>


<div class="row">
	<div id="entity-select-holder" class="pull-right col-md-5" <#if showEntitySelect?string('true', 'false') == 'false'> style="display:none"</#if>>
   		<div class="form-horizontal form-group">
        	<label class="col-md-4 control-label" for="dataset-select">Choose an entity:</label>
        	<div class="col-md-8">
        		<select class="form-control" id="entity-select" data-placeholder="Choose an Entity (example: dataset, protocol..." id="dataset-select">
            		<#list entitiesMeta as entityMeta>
                		<option value="/api/v1/${entityMeta.name}" <#if entityMeta.name == selectedEntityName> selected</#if>><#if entityMeta.label?has_content>${entityMeta.label}<#else>${entityMeta.name}</#if></option>
               		</#list>
            	</select>
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
	
	<div class="pull-right col-md-9">
		<div class="well">
			<div id="attributes-table"></div>
		</div>
	</div>
	
</div>

<@footer/>