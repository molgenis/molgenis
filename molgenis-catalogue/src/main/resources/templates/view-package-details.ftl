<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['ui.fancytree.min.css', 'jquery-ui-1.9.2.custom.min.css', 'catalogue.css']>
<#assign js=['jquery-ui-1.9.2.custom.min.js', 'jquery.fancytree.min.js', 'jquery.molgenis.tree.js', 'jquery.molgenis.attributemetadata.table.js', 'jquery.molgenis.entitymetadata.table.js','catalogue.js', 'handlebars.min.js']>

<@header css js/>

<div id="entity-class" class="well clearfix">
	<h3 id="entity-class-name"></h3>
	<span id="entity-class-description"></span>

	<#if showPackageSelect?string('true', 'false') == 'true'>
		<div class="dropdown pull-right">
  			<button class="btn btn-default dropdown-toggle" type="button" id="dropdown-menu-packages" data-toggle="dropdown">
  				Choose a Package <span class="caret"></span>
  			</button>
  			<ul class="dropdown-menu scrollable-menu" role="menu" aria-labelledby="dropdown-menu-packages">
    			<#list packages as package>
                	<li role="presentation">
                		<a role="menuitem" tabindex="-1" href="#" id="/api/v1/${package.name}" class="package-dropdown-item">${package.simpleName}</a>
                	</li>
            	</#list>
    		</ul>
		</div>
	</#if>
</div>

<div class="row">
	<div class="col-md-12">
		<ul class="nav nav-tabs" role="tablist">
		  <li class="active"><a href="#tree" role="tab" data-toggle="tab">Tree</a></li>
		  <li><a href="#uml" role="tab" data-toggle="tab">UML</a></li>
		</ul>
	</div>
</div>

<div class="tab-content">
	<div class="tab-pane active" id="tree">
		<div class="row">	
			<div class="col-md-3">
				<div class="well">
					<div class="panel">
		    			<div class="panel-heading">
		        			<h4 class="panel-title clearfix">
		        				Data item selection
		        				<button type="button" title="Show shoppingcard" class="pull-right btn btn-default btn-sm" id="cart-button"><span class="glyphicon glyphicon-shopping-cart"></span></button>				
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
	</div>
	
	<div class="tab-pane" id="uml">
		JUHA SPUL
	</div>
</div>



<div class="modal" id="cart-modal"></div>

<#if selectedPackageName??>
<script>var selectedPackageName='${selectedPackageName}';</script>
</#if>

<@footer/>