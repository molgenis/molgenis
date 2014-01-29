<form id="wizardForm" name="wizardForm" method="post" class="form-horizontal" action="">
	<div class="row-fluid">
		<div class="span12">
			<div class="row-fluid">
				<div id="div-info" class="span12 well">	
					<legend>Curate mappings for : <strong>${wizard.selectedDataSet.name}</strong></legend>
					<div class="row-fluid">
						<div  id="div-search" class="span3">
							<div><strong>Search data items :</strong></div>
							<div class="input-append">
								<input id="search-dataitem" type="text" title="Enter your search term" />
								<button class="btn" type="button" id="search-button"><i class="icon-large icon-search"></i></button>
							</div>
						</div>
					</div>
					<div class="row-fluid">
						<div class="span4">
							Number of data items : <span id="dataitem-number"></span>
						</div>
					</div>
					<div class="row-fluid">
						<div class="span12">
							<div class="data-table-container">
								<table id="dataitem-table" class="table table-striped table-condensed">
								</table>
								<div class="pagination pagination-centered">
									<ul></ul>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<script src="/js/ace-min/ace.js" type="text/javascript" charset="utf-8"></script>
	<script type="text/javascript">
		$(function(){
			var dataSetIds = [];
			<#list wizard.selectedBiobanks as dataSetId>
				dataSetIds.push('${dataSetId?c}');
			</#list>
			var molgenis = window.top.molgenis;
			molgenis.setContextURL('${context_url}');
			molgenis.getAlgorithmEditor().changeDataSet('${wizard.userName}', '${wizard.selectedDataSet.id?c}', dataSetIds);
			$('#selectedDataSetId').change(function(){
				molgenis.getAlgorithmEditor().changeDataSet('${wizard.userName}', '${wizard.selectedDataSet.id?c}', dataSetIds);
			});
		});
	</script>
</form>