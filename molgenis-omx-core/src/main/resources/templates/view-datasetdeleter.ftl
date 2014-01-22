<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["chosen.css"]>
<#assign js=["chosen.jquery.min.js", "datasetdeleter.js"]>
<@header css js/>
	<div class="row-fluid">
		<div class="span9">
			<h4>Delete dataset:</h4>
			<div class="well" style="width: 400px; max-height:400px; overflow:auto">
				<form id="deletedataset-form" enctype="multipart/form-data">
				<label class="control-label" for="dataset-select">Choose a dataset:</label>
				<select data-placeholder="Choose a Dataset to delete" id="dataset-select" name="dataset">
				</select><br>
				<input type="checkbox" name="deletemetadata"> Delete the metadata of this dataset.
			</form>
			</div>
			<a id="delete-button" class="btn">Delete</a>
		</div>
	</div>
	<script type="text/javascript">
		$(function() {
			window.top.molgenis.fillDataSetSelect(function() {
				<#-- select first dataset -->
				$('#dataset-select option:first').val();
				<#-- fire event handler -->
				$('#dataset-select').change();
				<#-- use chosen plugin for dataset select -->
				$('#dataset-select').chosen();
			});
			
			var submitBtn = $('#delete-button');
			var form = $('#deletedataset-form');
			form.submit(function(e){
				window.top.molgenis.deleteDataSet(e);
			});
			
			submitBtn.click(function(e) {
				e.preventDefault();
				e.stopPropagation();
				form.submit();
			});
		});		
	</script>
<@footer/>