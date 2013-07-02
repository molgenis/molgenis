<#macro HarmonizationPlugin screen>
<#assign model = screen.myModel>
	<script>
		$(function(){
			$('#protocol-id').change(function(){
				molgenis.changeDataSet($(this).val());
			});
			molgenis.changeDataSet($('#protocol-id').val());
		});
	</script>
<form method="post" id="${screen.name}" name="${screen.name}" enctype="multipart/form-data" action="molgenis.do">
	<!--needed in every form: to redirect the request to the right screen-->
	<input type="hidden" name="__target" value="${screen.name}">
	<input type="hidden" name="__action">
	<div class="formscreen">
		<div class="screenbody">
			<div class="container-fluid">
				<div class="row-fluid">
					<div class="span12">
						<h1>Harmonization</h1>
						<div class="row-fluid">
							<div class="span3">
								<select id="protocol-id">
									<#list model.dataSets as dataset>
											<option value="${dataset.id?c}"<#if dataset_index == 0> selected</#if>>${dataset.name}</option>
									</#list>
								</select>
							</div>
							<div class="span9">
								<div class="row-fluid data-table-container">
									<table id="dataitem-table" class="table table-striped table-condensed">
									</table>
								</div>
								<div class="pagination pagination-centered">
									<ul>
									</ul>
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