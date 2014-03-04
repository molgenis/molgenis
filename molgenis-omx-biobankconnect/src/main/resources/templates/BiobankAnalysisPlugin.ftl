<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["jquery-ui-1.9.2.custom.min.css", "bootstrap-fileupload.min.css", "biobank-connect.css", "algorithm-editor.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "bootstrap-fileupload.min.js", "common-component.js", "algorithm-editor.js", "biobank-analysis.js"]>
<@header css js/>
<form id="evaluationForm" class="form-horizontal" method="GET">
	<div class="row-fluid">
		<div id="summary-table-div"  class="span12">
			<div class="row-fluid">
				<div class="span12 well">
						<div class="span5">
							<div class="row-fluid">
								<strong>Select an analysis</strong></br></br>
							</div>
							<div class="row-fluid"> 
								<select id="selectedDataSet" name="selectedDataSet">
									<#if selectedDataSet??>
										<#list dataSets as dataset>
											<option value="${dataset.id?c}"<#if dataset.id?c == selectedDataSet> selected</#if>>${dataset.name}</option>
										</#list>
									<#else>
										<#list dataSets as dataset>
											<option value="${dataset.id?c}"<#if dataset_index == 0> selected</#if>>${dataset.name}</option>
										</#list>
									</#if>
								</select>
								</br></br>
							</div>
						</div>
						<div class="offset1 span6">
							<div class="row-fluid">
								<strong>Add an analysis</strong></br></br>
							</div>
							<div class="row-fluid">
								<input name="newDataSet" type="text"/> <button id="add-new-dataset" class="btn btn-primary" type="button">Add</button>
							</div>
						</div>
				</div>
			</div>
			<div class="row-fluid">
				<div class="span12 well">
					<div class="row-fluid">
						<div><strong>Select a source dataset</strong></div></br>
					</div>
					<div class="row-fluid">
						<div class="span2">
							<select id="sourceDataSets" name="sourceDataSets">
								<#if sourceDataSets??>
									<#list sourceDataSets as dataset>
										<option value="${dataset.id?c}"<#if dataset_index == 0> selected</#if>>${dataset.name}</option>
									</#list>
								</#if>
							</select>
						</div>
						<div class="span2">
							<button id="selectSourceDataSet" class="btn btn-primary" type="button">Select</button>
						</div>
					</div></br>
				</div>
			</div>
		</div>
	</div>
</form>
<script src="/js/ace-min/ace.js" type="text/javascript" charset="utf-8"></script>
<script src="/js/ace-min/ext-language_tools.js" type="text/javascript" charset="utf-8"></script>
<script type="text/javascript">
	$(document).ready(function(){
		var molgenis = window.top.molgenis;
		var biobankAnalysis = new molgenis.BiobankAnalysis();
		$('#add-new-dataset').click(function(){
			if($('input[name="newDataSet"]').val() !== ''){
				$('#evaluationForm').attr({
					'action' : molgenis.getContextUrl() + '/newanalysis',
					'method' : 'POST'
				}).submit();
			}
		});

		if($('#selectedDataSet').val() !== ''){
			var request = {
				'dataSetId' :  $('#selectedDataSet').val()
			};
			biobankAnalysis.retrieveAnalyses(request, $('#summary-table-div'));
		}
		$('#selectedDataSet').change(function(){
			var request = {
				'dataSetId' :  $('#selectedDataSet').val()
			};
			biobankAnalysis.retrieveAnalyses(request, $('#summary-table-div'));
		});
		
		$('#selectSourceDataSet').click(function(){
			$('#evaluationForm').attr({
				'action' : molgenis.getContextUrl() + '/newsourcedata',
				'method' : 'POST'
			}).submit();
		});
	});
</script>
<@footer/>	