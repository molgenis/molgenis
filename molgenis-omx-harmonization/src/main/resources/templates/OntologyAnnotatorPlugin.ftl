<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["bootstrap-fileupload.min.css", "ontology-annotator.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "common-component.js", "ontology-annotator.js"]>
<@header css js/>
		<div class="row-fluid">
			<div class="span12">
				<div id="alert-message"></div>
				<div>
					<h1>Harmonization</h1>
				</div>
				<div>
					<select id="selectedDataSet" name="selectedDataSet">
						<#list dataSets as dataset>
								<option value="${dataset.id?c}"<#if dataset_index == 0> selected</#if>>${dataset.name}</option>
						</#list>
					</select>
				</div>
				<div class="row-fluid">
					<div class="span8">
						<div class="row-fluid">
							<div class="input-append span4">
								<input id="search-text" type="text" title="Enter your search term" />
								<button class="btn" type="button" id="search-button"><i class="icon-large icon-search"></i></button>
							</div>
						</div>
						<div class="row-fluid data-table-container">
							<table id="dataitem-table" class="table table-striped table-condensed">
							</table>
							<div class="pagination pagination-centered">
								<ul></ul>
							</div>
							<div>
								<button id="annotate-dataitems" class="btn">Go annotation</button>
								<button id="refresh-button" class="btn">Refresh</button>
								<button id="match-catalogue" class="btn">Match catalogue</button>
							</div>
						</div>
					</div>
					<div class="span4">
						<div class="accordion-group">
						    <div class="accordion-heading">
								<h5 class="text-left text-info">Help!</h5>	
							</div>
							<div class="accordion-body in">
								<p class="justify-text">Ontologies are used to expand semantics of data items so that those data items annotated with 
								ontologies terms would have more synonyms and relevent terms that would be more likely matched with 
								other catalogue data items. Please index the ontologies in advance. It will largely speed up the matching process.
								</p>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	<script type="text/javascript">
		$(document).ready(function(){
			var molgenis = window.top.molgenis;
			$('#selectedDataSet').change(function(){
				molgenis.changeDataSet($(this).val());
			});
			molgenis.changeDataSet($('#selectedDataSet').val());
		});
	</script>
<@footer/>