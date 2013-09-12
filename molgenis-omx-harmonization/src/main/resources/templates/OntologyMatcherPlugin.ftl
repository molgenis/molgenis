<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["bootstrap-fileupload.min.css", "ontology-matcher.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "common-component.js", "ontology-matcher.js"]>
<@header css js/>
	<div class="row-fluid">
		<div class="span3">
			<h3>Step2 : Match biobanks</h3>
		</div>
		<div id="alert-message" class="offset1 span8">
		</div>
	</div>
	<div class="row-fluid">
		<div class="span12">
			<form id="ontologymatcher-form" class="form-horizontal" enctype="multipart/form-data">
				<div class="row-fluid">
					<div class="span8">
						<div class="row-fluid">
							<div class="span12 div-inputinfo well">
								<div class="row-fluid">		
									<div id="div-existing-mapping" class="span4">
										<dl>
											<dt>1.Selected source catalogue :</dt>
											<dd id="source-catalogue">Nothing selected</dd>
										</dl>
									</div>
									<div class="offset3 span5">
										<dl>
											<dt>Select a source catalogue :</dt>
											<dd>
												<button id="select-source-dataset" class="btn" type="button"><i class="icon-ok"></i></button>
												<select id="sourceDataSet" name="sourceDataSet">
													<#if selectedSourceDataSetId??>
														<#list dataSets as dataset>
															<option value="${dataset.id?c}"<#if dataset.id?c == selectedSourceDataSetId> selected</#if>>${dataset.name}</option>
														</#list>
													<#else>
														<#list dataSets as dataset>
															<option value="${dataset.id?c}"<#if dataset_index == 0> selected</#if>>${dataset.name}</option>
														</#list>
													</#if>
												</select>
											</dd>
										</dl>
									</div>
								</div>
							</div>
						</div>
						<div class="row-fluid">
							<div id="div-select-target-catalogue" class="span12 div-inputinfo well">
								<div class="row-fluid">		
									<div class="span12">
										<div class="span4">
											<dl>
												<dt>2.Selecte target catalogue :</dt>
												<dd id="target-catalogue">Nothing selected</dd>
											</dl>
										</div>
										<div class="offset3 span5">
											<dl>
												<dt>Select target catalogues :</dt>
												<dd>
													<button id="add-target-dataset" class="btn" type="btn"><i class="icon-plus"></i></button>
													<select id="targetDataSets" name="targetDataSets">
													</select>
												</dd>
											</dl>
										</div>
									</div>
									<div>
										<div class="btn-group">
											<button id="start-match" class="btn btn-info" type="button">Start matching</button>
											<button id="confirm-match" class="btn btn-warning" type="button">Confirm matching</button>
											<button id="reset-selection" class="btn" type="button">Reset</button>
										</div>
									</div>
								</div>
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
										Choose a <strong>source</strong> biobank catalogue <br> click on ' <i class="icon-ok"></i> '
										button to choose a catalogue for which data items will be matched.
									</li>
									<li>
										Choose <strong>target</strong> biobank catalogues <br> click on ' <i class="icon-plus"></i> ' 
										button to add catalogues that are to match with source catalogue. 
									</li>
									<li>
										Start match <br> click on '<strong>Start matching</strong>' button.click on '<strong>Reset</strong>' to remove all selections
									</li>
									<li>
										<strong>Hint</strong> <br> If mapping between any two biobanks already exists, existing mappings 
										will be lost once the button is clicked.
									</li>
								</ol>
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
				<#if percentage == 0.0>
					molgenis.showMessageDialog('Deleting existing mappings...');
				<#else>
					molgenis.showMessageDialog('Matching is running...' + ${percentage} + '% has completed!');
				</#if>
			</#if>
		});
	</script>
<@footer/>