<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['ui.fancytree.min.css', 'jquery-ui-1.9.2.custom.min.css', 'select2.css', 'joint.min.css','standardsregistry.css']>
<#assign js=['jquery-ui-1.9.2.custom.min.js', 'jquery.fancytree.min.js', 'select2.min.js', 'jquery.molgenis.entitymetadata.table.js', 'jquery.molgenis.attributemetadata.table.js', 'jquery.bootstrap.pager.js', 'lodash.js', 'backbone-min.js', 'geometry.min.js', 'vectorizer.min.js', 'joint.clean.min.js','joint.shapes.uml.min.js', 'joint.layout.DirectedGraph.min.js', 'standardsregistry.js', 'handlebars.min.js']>

<@header css js/>
<#-- Search box and search results -->
<div id="standards-registry-search">
    <div class="row">
    	<div class="col-md-4">
    		<form class="form-horizontal" name="search-form" action="${context_url}/search" method="post">
    			<div class="form-group">
                	<div class="col-md-12">
                    	<div class="input-group">
                        	<input type="text" class="form-control" name="packageSearch" id="package-search" placeholder="Search models" autofocus="autofocus">
                        	<span class="input-group-btn">
                            	<button id="search-button" class="btn btn-default" type="submit"><span class="glyphicon glyphicon-search"></span></button>
                            	<button id="search-clear-button" class="btn btn-default" type="button"><span class="glyphicon glyphicon-remove"></span></button>
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
    		<div id="package-search-results"></div>  
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
  		<h3 style="margin-top: 0px;">{{package.name}}</h3>
  		<p>{{package.description}}</p>
  		<p>
  			{{#each tags}}
  				{{#equal this.relation 'link'}}
  					<span class="label label-primary"><a href='{{this.label}}' target="_blank">{{this.label}}</a></span>
  				{{/equal}}
  				{{#notequal this.relation 'link'}}
  					<span class="label label-primary">{{this.label}}</span>
  				{{/notequal}}
  			{{/each}}
  		</p>
  		{{#if package.matchDescription}}
        	<p><span class="label label-default">{{package.matchDescription}}</span></p>
        {{/if}}
        <form class="form-inline">
            <div class="form-group">
        	   <a class="btn btn-primary details-btn" href="#" role="button">View Model Details</a>
        	</div>
            <div class="form-group{{#unless entities.length}} hidden{{/unless}}">
                <div class="input-group select2-bootstrap-append entity-select-control">
                    <select id="select2-input-group-append" class="form-control select2 entity-select-dropdown" data-placeholder="Select an entity">
                        <option></option>
        		    	{{#each entities}}
        	        		<option value="{{this.name}}">{{this.label}}</option>
        	    		{{/each}}
        			</select>
        			<span class="input-group-btn">
                        <button class="btn btn-default dataexplorer-btn" type="button" data-select2-open="select2-input-group-append">View Model Data</button>
                    </span>
    			</div>
			</div>
        </form>
  	</div>
</script>
<@footer />