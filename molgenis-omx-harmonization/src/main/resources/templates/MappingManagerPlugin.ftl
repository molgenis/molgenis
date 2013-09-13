<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["mapping-manager.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "common-component.js", "mapping-manager.js"]>
<@header css js/>
	<div class="row-fluid">
		<div class="span12">
			<div class="row-fluid">
				<div class="span4">
					<h3>Step3 : View and select mappings</h3>
				</div>
				<div id="alert-message" class="offset1 span7">
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
							<div id="div-index-ontology" class="span8 well upper-header">
								<div class="row-fluid">
									<div class="span3">
										<dl>
											<dt>Selected catalogue :</dt>
											<dd id="catalogue-name"><br></dd>
											<dt>Number of data items :</dt>
											<dd id="dataitem-number"><br></dd>
										</dl>
									</div>
									<div class="offset5 span4">
										<dl>
											<dt>Choose a catalogue :</dt>
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
											<dt>Action :</dt>
											<dd>
												<div class="btn-group">
													<button id="downloadButton" class="btn btn-primary">Download</button>
													<button id="refresh-button" class="btn">Refresh</button>
												</div>
											</dd>
										</dl>
									</div>
								</div>
							</div>
							<div class="span4">
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
															Select catalogue from <strong>'dropdown'</strong> to view its mappings. 
															<br>All mappings to other catalogues are shown in the table.
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
			molgenis.changeDataSet($('#selectedDataSet').val());
			$('#selectedDataSet').change(function(){
				molgenis.changeDataSet($(this).val());
			});
			$('#downloadButton').click(function(){
				molgenis.downloadMappings();
				return false;
			});
		});
	</script>
<@footer/>	