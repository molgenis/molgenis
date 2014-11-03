<div class="row">
    <div class="col-md-12">
        <button id="search-results-back-btn" class="btn btn-default"><span class="glyphicon glyphicon-chevron-left"></span> Back to search results</button>
    </div>
</div>
<div class="row">
    <div class="col-md-12">        
        <div id="entity-class" class="well clearfix">
        	<h3 id="entity-class-name"></h3>
        	<span id="entity-class-description"></span>
        </div>
    </div>
</div>
<div class="row">
	<div class="col-md-12">
		<ul class="nav nav-tabs" role="tablist">
		  <li class="active"><a href="#tree" role="tab" data-toggle="tab">Tree</a></li>
		  <li><a href="#uml" id="uml-tab" role="tab" data-toggle="tab">UML</a></li>
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
		        			<h4 class="panel-title clearfix">Data item selection</h4>
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
					<div id="tags"></div>
				</div>
			</div>
		</div>
	</div>
	<div class="tab-pane" id="uml"><div id="paper"></div></div>
</div>