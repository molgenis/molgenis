<div id="dataexplorer-grid-data">
	<div class="accordion" id="genomebrowser">
	    <div class="accordion-group">
	        <div class="accordion-heading">
	            <a class="accordion-toggle" data-toggle="collapse" href="#dalliance"><i class="icon-chevron-down"></i> Genome Browser</a>
	        </div>
	        <div id="dalliance" class="accordion-body collapse in">
	            <div class="accordion-inner">
	            	<#-- dalliance default id to print browser -->
	                <div id="svgHolder"></div>
	                <div class="pull-right"><a id="genomebrowser-filter-button" class="btn btn-small"><img src="/img/filter-bw.png"> Apply filter</a></div>
	            </div>
	        </div>
	    </div>
	</div>
	<div class="row-fluid data-table-container" id="data-table-container"></div>
		<div class="row-fluid data-table-pager-container">
			<a id="download-modal-button" class="btn" data-toggle="modal" data-target="#downloadModal"> Download as csv</a>
		</div>
	</div>
</div>

<!-- Download Modal -->
<div class="modal fade medium" id="downloadModal" tabindex="-1" role="dialog" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">				
	      	<div class="modal-header">
	        	<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
	        	<h4 class="modal-title">Download as csv</h4>
	     	</div>
	      	<div class="modal-body">
	      		<div class="control-group form-horizontal">
					<label class="control-label">As column names i want</label>
	    			<div class="controls">
	    				<label><input type="radio" name="ColNames" value ="ATTRIBUTE_LABELS" checked="true"> Attribute labels</label>
	    				<label><input type="radio" name="ColNames" value ="ATTRIBUTE_NAMES"> Attribute names</label>
					</div>
	      		</div>
			</div>
	      	<div class="modal-footer">
	        	<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
	        	<button id="download-button" class="btn btn-primary">Download</button>
	      	</div>
	    </div>
	</div>
</div>
<script>
	<#-- load css dependencies -->
	if (!$('link[href="/css/jquery.molgenis.table.css"]').length)
		$('head').append('<link rel="stylesheet" href="/css/jquery.molgenis.table.css" type="text/css" />');
	<#-- load js dependencies -->
	$.when(
		$.ajax("/js/jquery.bootstrap.pager.js", {'cache': true}),
		$.ajax("/js/jquery.molgenis.table.js", {'cache': true}),
		$.ajax("/js/dalliance-compiled.js", {'cache': true}),
		$.ajax("/js/dataexplorer-data.js", {'cache': true}))
		.done(function() {
			<#-- create genome browser -->
		    molgenis.dataexplorer.data.createGenomeBrowser({
				${initLocation},
	            coordSystem: ${coordSystem},
	            chains: ${chains},
	            sources: ${sources},
	            browserLinks: ${browserLinks}
			}, [<#list genomeEntities?keys as entityName>{'name': '${entityName}', 'label': '${genomeEntities[entityName]}'}<#if entityName_has_next>,</#if></#list>]);
			<#-- create data table -->
		    molgenis.dataexplorer.data.createDataTable(${tableEditable?string('true', 'false')});    	
		})
		.fail(function() {
			molgenis.createAlert([{'message': 'An error occured. Please contact the administrator.'}], 'error');
		});
</script>