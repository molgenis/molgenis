<#include "view-standardsregistry_docs-macros.ftl">
<div class="row">
    <div class="col-md-12 hidden-print">
        <button id="search-results-back-btn" class="btn btn-default btn-md pull-left"><span class="glyphicon glyphicon-chevron-left"></span> Back to search results</button>
        <button id="print-btn" class="btn btn-default btn-md pull-right"><span class="glyphicon glyphicon-print"></span></button>
    </div>
</div>
<div class="row">
	<div class="col-md-12 hidden-print">
		<ul class="nav nav-tabs" role="tablist">
		  <li class="active"><a href="#tree" id="tree-tab" role="tab" data-toggle="tab">Tree</a></li>
		  <li><a href="#uml" id="uml-tab" role="tab" data-toggle="tab">UML</a></li>
		</ul>
	</div>
</div>
<div class="tab-content">
	<div class="tab-pane active" id="tree">
		<div class="row">
			<div class="col-md-3 hidden-print" >
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
    					<#if package??>
       						<@renderPackage package/>
       					<#else>
							<script language="javascript"> 
								window.location = molgenis.getContextUrl() + '?showPackageNotFound=true'; 
							</script>        						
       					</#if>
        			</div>
    			</div>
   			</div>               
		</div>
	</div>
	<div class="tab-pane" id="uml">
		<div id="zoom-in-out" class="btn-group hidden-print">
			<button type="button" id="zoom-in" class="btn btn-default btn-sm" data-toggle="tooltip" data-placement="bottom" title="Zoom in">
				<span class="glyphicon glyphicon-zoom-in"></span>
			</button>
			<!-- Automatic scaling -->
			<div class="btn-group">
				<button id="reset" type="button" class="btn btn-default btn-sm" aria-haspopup="true" aria-expanded="false" data-toggle="tooltip" data-placement="top" title="Reset">
					Reset </span>
				</button>
				<button type="button" class="btn btn-sm dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
					<span class="caret"></span>
					<span class="sr-only">Toggle Dropdown</span>
				</button>
				<ul class="dropdown-menu">
				<li>
					<a id="a4-horizontal" data-toggle="tooltip" data-placement="top" title="A4 horizontal">
						A4 <span class="glyphicon glyphicon-resize-horizontal"></span>
					</a>
				</li>
				<li>
					<a id="a4-vertical" data-toggle="tooltip" data-placement="right"  title="A4 vertical">
						A4 <span class="glyphicon glyphicon-resize-vertical"></span>
					</a>
				</li>
				<li>
					<a id="a3-horizontal" data-toggle="tooltip"  data-placement="right" title="A3 horizontal">
						A3 <span class="glyphicon glyphicon-resize-horizontal"></span>
					</a>
				</li>
				<li>
					<a  id="a3-vertical" data-toggle="tooltip" data-placement="right" title="A3 vertical">
						A3 <span class="glyphicon glyphicon-resize-vertical"></span>
					</a>
				</li>
			  </ul>
			</div>
  			<button type="button" id="zoom-out" class="btn btn-default btn-sm" data-toggle="tooltip" data-placement="bottom" title="Zoom out">
  				<span class="glyphicon glyphicon-zoom-out"></span>
  			</button>
		</div>
			
		<div id="paper-holder">
			<div id="paper"></div>
		</div>
	
		<div id="dpi"></div>
	</div>
</div>


