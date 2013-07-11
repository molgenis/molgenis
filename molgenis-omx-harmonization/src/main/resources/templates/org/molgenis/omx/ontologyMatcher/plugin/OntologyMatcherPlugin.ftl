<#macro OntologyMatcherPlugin screen>
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
		<div id="alertMessage">
		</div>
		<div class="screenbody">
			<div class="container-fluid">
				<div>
					<h1>Matching result</h1>
				</div>
				<div class="row-fluid">
					<div class="span12">
						<div class="row-fluid">
							<div class="span3">
								<select id="protocol-id" name="selectedDataSet">
									<#list model.dataSets as dataset>
											<option value="${dataset.id?c}"<#if dataset_index == 0> selected</#if>>${dataset.name}</option>
									</#list>
								</select>
							</div>
							<div class="offset6 span3">
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
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</form>
</#macro>