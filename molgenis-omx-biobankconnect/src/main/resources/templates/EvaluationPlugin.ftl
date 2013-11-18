<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["jquery-ui-1.9.2.custom.min.css", "bootstrap-fileupload.min.css", "biobank-connect.css", "mapping-manager.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "bootstrap-fileupload.min.js", "common-component.js", "catalogue-chooser.js", "ontology-annotator.js", "mapping-manager.js", "biobank-connect.js", "simple_statistics.js"]>
<@header css js/>
<form id="evaluationForm" class="form-horizontal" enctype="multipart/form-data">
	<div class="row-fluid">
		<div class="span12 well">
			<div class="row-fluid">
				<div class="span12"><legend class="legend-mapping-manager">
					System performance evaluation
				</div>
			</div>
			<div class="row-fluid">
				<div class="offset2 span8">
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
			<div class="row-fluid">
				<div class="offset2 span8">
					<#if (dataSets?size > 0)>
					<table class="table">
						<tr align="center">
							<th>Biobank</th>
							<th>Mapped</th>
						</tr>
						<#list dataSets as dataset>
							<#if dataset.id?c != selectedDataSet>
								<#if mappedDataSets?seq_contains(dataset.id?c)>
									<tr>
										<td>${dataset.name}</td>
										<td><i class="icon-ok"></i></td>
									</tr>
								<#else>
									<tr align="center">
										<td>${dataset.name}</td>
										<td><i class="icon-remove"></i></td>
									</tr>
								</#if>
							</#if>
						</#list>
					</table>
					</#if>
				</div>
			</div>
			<div class="row-fluid">
				<div class="offset2 span8">
					<div class="fileupload fileupload-new" data-provides="fileupload">
						<div class="input-append">
							<div class="uneditable-input">
								<i class="icon-file fileupload-exists"></i>
								<span class="fileupload-preview"></span>
							</div>
							<span class="btn btn-file btn-info">
								<span class="fileupload-new">Select file</span>
								<span class="fileupload-exists">Change</span>
								<input type="file" id="file" name="file" required/>
							</span>
							<a href="#" class="btn btn-danger fileupload-exists" data-dismiss="fileupload">Remove</a>
							<button class="btn btn-primary" id="verify-button" type="button">Verify mapping</button>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</form>
	<script type="text/javascript">
		$(document).ready(function(){
			var molgenis = window.top.molgenis;
			molgenis.setContextURL('${context_url}');
			$('#selectedDataSet').change(function(){
				$('#evaluationForm').attr({
					'action' : molgenis.getContextUrl(),
					'method' : 'GET'
				}).submit();
			});
			
			$('#verify-button').click(function(){
				$('#evaluationForm').attr({
					'action' : molgenis.getContextUrl() + '/verify',
					'method' : 'POST'
				}).submit();
			});
		});
	</script>
<@footer/>	