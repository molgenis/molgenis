<form method="post" id="wizardForm" name="wizardForm" action="">
	<div class="col-md-6">
		<div id="message-panel" class="panel">
    		<div class="panel-heading">
        		<h3 class="panel-title">Importing...</h3>
    		</div>
    		<div id="message" class="panel-body">
    			<div class="row text-center"><img src="/img/waiting-spinner.gif" /></div>
    		</div>
		</div>
	</div>
</form>

<script type="text/javascript">
	$(function() {
		var restApi = new molgenis.RestClient();
		var timer;
	
		$('.next').addClass('disabled');
		$('.cancel').addClass('disabled');
	   	$('.previous').addClass('disabled');
	   		
		checkImportResult();
	
		function checkImportResult() {
			restApi.getAsync('/api/v1/ImportRun/${wizard.importRunId?c}', {}, function(importRun) {
				if (timer) {
					clearTimeout(timer);	
				}
			
				if (importRun.message && importRun.message != '') {
					$('#message').html(importRun.message);
				}
			
				if (importRun.status != 'RUNNING') {
					$('#message-panel').removeClass('panel-info');
					$('#message-panel').addClass(importRun.status == 'FINISHED' ? 'panel-success' : 'panel-danger');
					$('#message-panel .panel-heading').text(importRun.status == 'FINISHED' ? 'Import success' : 'Import failed');
					$('.next').removeClass('disabled');
					$('.cancel').removeClass('disabled');
	 
				} else {
					timer = setTimeout(checkImportResult, 3000);
				}
			});
		}
	});
</script>