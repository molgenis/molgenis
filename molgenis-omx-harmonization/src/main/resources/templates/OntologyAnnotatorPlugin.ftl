<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["bootstrap-fileupload.min.css", "ontology-annotator.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "common-component.js", "ontology-annotator.js"]>
<@header css js/>
	<div class="row-fluid">
				<div class="span3">
					<h3>Step1 : Annotate data items</h3>
				</div>
				<div id="alert-message" class="offset1 span8">
				</div>
			</div>
	<div class="row-fluid">
		<div class="span12">
			<form id="ontologyannotator-form" class="form-horizontal" enctype="multipart/form-data">
				<div class="row-fluid">
					<div class="well span8">
						<div class="row-fluid">
							<div class="span12">
								<div class="row-fluid">
									<div class="span3">
										<dl>
											<dt>Selected catalogue :</dt>
											<dd id="catalogue-name"></dd>
											<dt>Number of data items :</dt>
											<dd id="dataitem-number"></dd>
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
													<button id="annotate-dataitems" class="btn">Annotate</button>
													<button id="refresh-button" class="btn">Refresh</button>
												</div>
											</dd>
										</dl>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
				<div id="div-info" class="row-fluid well">
					<div id="div-search" class="row-fluid">
						<div>Search data items :</div>
						<div class="input-append">
							<input id="search-dataitem" type="text" title="Enter your search term" />
							<button class="btn" type="button" id="search-button"><i class="icon-large icon-search"></i></button>
							<button class="btn" type="button" id="clear-button"><i class="icon-large icon-trash"></i></button>
						</div>
					</div>
					<div class="row-fluid">
						<div class="span8">
							<div class="row-fluid data-table-container">
								<table id="dataitem-table" class="table table-striped table-condensed">
								</table>
								<div class="pagination pagination-centered">
									<ul></ul>
								</div>
							</div>
						</div>
						<div class="span4">
							<div class="accordion-group">
							    <div class="accordion-heading">
									<h5 class="text-left text-info">Actions</h5>	
								</div>
								<div class="accordion-body in">
									<ol class="action-list">
										<li>
											<strong>Automatical annotation</strong> <br> click on <strong>'Annotate'</strong> button, BiobankConnect will annotate potential ontology terms to data items.
										</li>
										<li>
											<strong>Manual annotation</strong> <br> click on <strong>'Name'</strong> of data items, a popup window will show up where you can add ontology terms by hand.
										</li>
										<li>
											<strong>Hint</strong> <br> Ontology term annotation is not neccessary for the rest of steps.
										</li>
									</ol>
								</div>
							</div>
						</div>
					</div>
				</div>
			</form>
		</div>
	</div>
	<script type="text/javascript">
		$(document).ready(function(){
			var molgenis = window.top.molgenis;
			molgenis.setContextURL('${context_url}');
			<#if isRunning?? && isRunning>
				$('#annotate-dataitems').attr('disabled', 'disabled');
			</#if>
			<#if message??>
				molgenis.showMessageDialog('${message}');
			</#if>
			$('#selectedDataSet').change(function(){
				molgenis.changeDataSet($(this).val());
			});
			molgenis.changeDataSet($('#selectedDataSet').val());
		});
	</script>
<@footer/>