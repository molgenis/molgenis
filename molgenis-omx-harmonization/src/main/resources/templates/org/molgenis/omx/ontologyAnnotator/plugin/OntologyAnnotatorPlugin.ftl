<#macro OntologyAnnotatorPlugin screen>
<#assign model = screen.myModel>
	<script>
		$(function(){
			$('#protocol-id').change(function(){
				molgenis.changeDataSet($(this).val());
			});
			molgenis.changeDataSet($('#protocol-id').val());
		});
	</script>
<form method="post" id="harmonizationIndexer-form" name="${screen.name}" enctype="multipart/form-data" action="molgenis.do">
	<!--needed in every form: to redirect the request to the right screen-->
	<input type="hidden" name="__target" value="${screen.name}">
	<input type="hidden" name="__action">
	<div class="formscreen">
		<div class="screenbody">
			<div class="container-fluid">
				<div>
					<h1>Harmonization</h1>
				</div>
				<div class="row-fluid">
					<div class="span12">
						<div>
							<select id="protocol-id" name="selectedDataSet">
								<#list model.dataSets as dataset>
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
			</div>
		</div>
	</div>
</form>
</#macro>