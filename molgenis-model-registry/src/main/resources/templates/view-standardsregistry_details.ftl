<#include "view-standardsregistry_docs-macros.ftl">
<div class="row">
    <div class="col-md-12">
        <button id="search-results-back-btn" class="btn btn-default btn-xs"><span class="glyphicon glyphicon-chevron-left"></span> Back to search results</button>
    </div>
</div>

<div class="row">
	<div class="col-md-3" >
		<div class="well">
			<div id="package-tree-container" class="panel">
		    	<div class="panel-heading">
		        	<h4 class="panel-title clearfix">Data item selection</h4>
		        </div>
		        <div class="panel-body">
		        	<div id="attribute-selection"></div>
		        </div>
		    </div>
		</div>
	</div>
	<div class="col-md-9">
   		<div id="package-doc-container">
    		<div id="package-index"><#-- for back-to-top -->
       			<@renderPackage package/>
        	</div>
    	</div>
   </div>               
</div>

