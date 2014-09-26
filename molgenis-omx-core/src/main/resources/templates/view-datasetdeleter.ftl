<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["select2.css"]>
<#assign js=["select2.min.js", "datasetdeleter.js"]>
<@header css js/>
<div class="row">
	<div class="col-md-12">
		<h4>Delete dataset</h4>
		
		<div class="well" style="width: 400px; max-height:400px; overflow:auto">
			<form role="form" id="deletedataset-form" enctype="multipart/form-data">
				<div class="form-group">
					<label class="control-label" for="dataset-select">Choose a dataset:</label>
					<select class="form-control" data-placeholder="Choose a Dataset to delete" id="dataset-select" name="dataset"></select>
				</div>
				
				<div class="checkbox">
					<label>
						<input type="checkbox" name="deletemetadata"> Delete the metadata of this dataset.
					</label>
				</div>
				
			</form>
		</div>
		
		<a id="delete-button" class="btn btn-default">Delete</a>
	</div>
</div>
<@footer/>