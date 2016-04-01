<form method="post" id="wizardForm" name="wizardForm" enctype="multipart/form-data" action="" role="form">
    <div class="row">
	    <div class="col-md-12">
	    	<h4>Upload a file</h4>
    		<input type="file" name="upload" data-filename-placement="inside" title="Select a file...">
	    	<hr></hr>
	    </div>
    </div>
</form>

<div class="row">
	<div class="col-md-6">
        <div class="panel panel-primary" id="instant-import">
            <div class="panel-heading">
                <h4 class="panel-title">
                    <a data-toggle="collapse" data-target="#instant-import-collapse" href="#instant-import-collapse">Advanced</a>
                </h4>
            </div>
            <div id="instant-import-collapse" class="panel-collapse collapse out">
                <div class="panel-body">
                	<div id="instant-import-alert"></div>
                	<h4>Instant import. Warning: validation is skipped</h4>
					<div id="import-form"></div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
React.render(molgenis.ui.UploadContainer({
		'id' : 'file-upload-test',
		'url' : '/plugin/importwizard/importFile',
		'type' : 'file',
		'name' : 'file-uploader-mania',
		'width' : '6'
	}), $('#import-form')[0]);
</script>