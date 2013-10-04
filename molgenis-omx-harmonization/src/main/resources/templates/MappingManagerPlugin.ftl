<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["jquery-ui-1.9.2.custom.min.css", "biobank-connect.css", "mapping-manager.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "common-component.js", "catalogue-chooser.js", "ontology-annotator.js", "mapping-manager.js", "biobank-connect.js", "simple_statistics.js"]>
<@header css js/>
	<div class="row-fluid">
		<div class="span12">
			<div class="row-fluid">
				<div class="span4">
					<h3>	View / select mappings</h3>
				</div>
				<div id="alert-message" class="span8">
				</div>
			</div>
		</div>
	</div>
	
	<div class="row-fluid">
		<div class="span12">
			<form id="mappingmanager-form" class="form-horizontal" enctype="multipart/form-data">
				<div class="row-fluid">
					<div class="span12">
						<div class="row-fluid">
							<div id="div-index-ontology" class="span5 well upper-header">
								<div class="row-fluid">
									<div class="span4">
										<dl>
											<dt>Choose catalogue :</dt>
											<dd>
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
											</dd>
											<dt>Number of data items :</dt>
											<dd id="dataitem-number"><br></dd>
										</dl>
									</div>
									<div class="offset4 span4">
										<dl>
											<dt>Action :</dt>
											<dd>
												<div class="btn-group">
													<button id="downloadButton" class="btn btn-primary">Download</button>
												</div>
											</dd>
										</dl>
									</div>
								</div>
							</div>
							<div class="span5">
								<div class="accordion">
									<div class="accordion-group upper-header">
									    <div class="accordion-heading">
									    	<ul class="nav nav-pills">
											 	<li id="accordion-action-click" class="active">
											    	<a href="#">Actions</a>
												</li>
												<li id="accordion-icon-meaning-click">
													<a href="#">Icon meanings</a>
												</li>
											</ul>
										</div>
										<div class="accordion-body collapse in">
											<div class="accordion-inner">
												<div id="accordion-action-content">
													<ol class="action-list">
														<li>
															Select catalogue from <strong>'dropdown'</strong> to view its mappings
															 to other catalogues.
														</li>
														<li>
															Edit, update and delete mappings. See details in <strong>'Icon meanings'</strong>.
														</li>
														<li>
															Click on <strong>'download'</strong> button to download all mappings
														</li>
													</ol>
												</div>
												<div id="accordion-icon-meaning-content">
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
						</div>
					</div>
				</div>
				<div class="row-fluid">
					<div class="span12">
						<div id="data-table-container" class="row-fluid data-table-container">
							<table id="dataitem-table" class="table table-striped table-condensed show-border">
							</table>
						</div>
						<div class="pagination pagination-centered">
							<ul id="table-papger"></ul>
						</div>
					</div>
				</div>
			</form>
		</div>
	</div>
	<script type="text/javascript">
		$(document).ready(function(){
			var molgenis = window.top.molgenis;
			molgenis.getMappingManager().changeDataSet($('#selectedDataSet').val());
			$('#selectedDataSet').change(function(){
				molgenis.getMappingManager().changeDataSet($(this).val());
			});
			$('#downloadButton').click(function(){
				molgenis.getMappingManager.downloadMappings();
				return false;
			});
		});
	</script>
<@footer/>	