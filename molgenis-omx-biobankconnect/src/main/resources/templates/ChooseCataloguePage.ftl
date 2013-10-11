<form id="wizardForm" name="wizardForm" method="post" class="form-horizontal" action="">
	<div class="row-fluid">
		<div class="span12">
			<div class="row-fluid">
				<div id="div-info" class="span12 well">	
					<div class="row-fluid">
						<div class="span9"><legend class="legend">
							Browse catalogue : 
							<select id="selectedDataSetId" name="selectedDataSetId">
								<#if wizard.selectedDataSet??>
									<#list wizard.dataSets as dataset>
										<option value="${dataset.id?c}"<#if dataset.id?c == wizard.selectedDataSet.id?c> selected</#if>>${dataset.name}</option>
									</#list>
								<#else>
									<#list wizard.dataSets as dataset>
										<option value="${dataset.id?c}"<#if dataset_index == 0> selected</#if>>${dataset.name}</option>
									</#list>
								</#if>
							</select>
							<button id="import-data-button" class="btn btn-primary" type="btn">Import data</button>
						</div>
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
	<script type="text/javascript">
		$(document).ready(function(){
			var molgenis = window.top.molgenis;
			molgenis.getCatalogueChooser().changeDataSet($('#selectedDataSetId').val());
			$('#selectedDataSetId').change(function(){
				molgenis.getCatalogueChooser().changeDataSet($('#selectedDataSetId').val());
			});
			if($('#selectedDataSetId option').length === 0){
				$('.pager li.next').addClass('disabled');
			}
			$('#import-data-button').click(function(){
				$('#wizardForm').attr({
					'action' : '/menu/main/importwizard',
					'method' : 'GET'
				}).submit();
			});
		});
	</script>
</form>