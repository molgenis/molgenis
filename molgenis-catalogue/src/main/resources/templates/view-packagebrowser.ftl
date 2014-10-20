<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['ui.fancytree.min.css', 'jquery-ui-1.9.2.custom.min.css', 'catalogue.css']>
<#assign js=['jquery-ui-1.9.2.custom.min.js', 'jquery.fancytree.min.js', 'jquery.molgenis.tree.js', 'jquery.molgenis.attributemetadata.table.js', 'catalogue.js', 'handlebars.min.js']>

<@header css js/>

<div class="row">
	<div class="col-md-6">
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
</div>

<div class="row">
	<div class="col-md-12">
		<div id="package-search-results"></div>
	</div>
</div>

<script id="count-template" type="text/x-handlebars-template">
    <div class="row">
		<div class="col-md-1">
			<span>
    			Found {{count}} models
			</span>
		</div>	
	</div>
</script>

<script id="model-template" type="text/x-handlebars-template">
    <div class="row">
		<div class="col-md-12 well">
			<div class="package" data-id="{{package.name}}">
				<div class="col-md-4">
					Package name: {{package.name}}
					Description: {{package.description}}
					Type: Not yet implemented
					Tags: Not yet implemented
					Homepage: www.google.com
				</div>
				<div class="col-md-4 col-md-offset-4">
					<button class="btn btn-default details-btn" type="button">View details</button>
					<button class="btn btn-default dataexplorer-btn" type="button">View in dataexplorer</button>
					<button class="btn btn-default import-btn" type="button">Import data</button>
				</div>
			</div>
		</div>
	</div>
</script>

<@footer />
