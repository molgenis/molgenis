<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["mapping-manager.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "common-component.js", "mapping-manager.js"]>
<@header css js/>
	<div id="alertMessage">
	</div>
	<div>
		<h1>Matching result</h1>
	</div>
	<#if isRunning>
		The matching is still running! Please be patient!
	<#else>
	<div class="row-fluid">
		<div class="span12">
			<div class="row-fluid">
				<div class="span3">
					<select id="protocol-id" name="selectedDataSet">
						<#list dataSets as dataset>
								<option value="${dataset.id?c}"<#if dataset_index == 0> selected</#if>>${dataset.name}</option>
						</#list>
					</select>
				</div>
				<div class="offset5 span4">
					<div class="accordion" id="feature-filters-container">
						<div class="accordion-group">
						    <div class="accordion-heading">
								<span class="accordion-toggle" data-toggle="false" data-parent="#feature-filters-container">Icon meanings</span>
							</div>
							<div class="accordion-body collapse in">
								<div class="accordion-inner" id="feature-filters">
									<div>
										<i class="icon-ok"></i>
										<span class="float-right text-success">Mappings have been selected</span>
									</div>
									<div>
										<i class="icon-pencil"></i>
										<span class="float-right text-info">Select the mappings</span>
									</div>
									<div>
										<i class="icon-trash"></i>
										<span class="float-right text-warning">Delete all mappings</span>
									</div>
									<div>
										<i class="icon-ban-circle"></i>
										<span class="float-right text-error">No candidate available</span>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid">
				<div class="span12">
					<div class="row-fluid data-table-container">
						<table id="dataitem-table" class="table table-striped table-condensed show-border">
						</table>
						<div class="pagination pagination-centered">
							<ul></ul>
						</div>
					</div>
					<button id="downloadButton" class="btn">Download</button>
				</div>
			</div>
		</div>
	</div>
	</#if>
	<script type="text/javascript">
		$(document).ready(function(){
			var molgenis = window.top.molgenis;
			$('#protocol-id').change(function(){
				molgenis.changeDataSet($(this).val());
			});
			molgenis.changeDataSet($('#protocol-id').val());
			$('#downloadButton').click(function(){
				molgenis.downloadMappings();
				return false;
			});
		});
	</script>
<@footer/>	