<form id="wizardForm" name="wizardForm" method="post" class="form-horizontal" action="">
	<div class="row-fluid">
		<div class="span12">
			<legend><div><strong>Please be patient</strong> : this will take up to 5 mins</div></legend>
		</div>
	</div>
	<div id="delete-mapping" class="row-fluid progress-bar-hidden">
		<div class="offset2 span10">
			Deleting existing mappings
		</div>
		<div class="progress progress-striped progress-warning active offset2 span8">
			<div class="bar text-align-center"></div>
		</div>
	</div>
	<div id="create-mapping" class="row-fluid progress-bar-hidden">
		<div class="offset2 span10">
			Create mappings
		</div>
		<div class="progress progress-striped active offset2 span8">
			<div class="bar text-align-center"></div>
		</div>
	</div>
	<div id="store-mapping" class="row-fluid progress-bar-hidden">
		<div class="offset2 span10">
			Store mappings
		</div>
		<div class="progress progress-striped progress-success active offset2 span8">
			<div class="bar text-align-center"></div>
		</div>
	</div>
	<script type="text/javascript">
		$(document).ready(function(){
			var molgenis = window.top.molgenis;
			var currentStatus = {};
			currentStatus['DeleteMapping'] = $('#delete-mapping');
			currentStatus['CreateMapping'] = $('#create-mapping');
			currentStatus['StoreMapping'] = $('#store-mapping');
			$('.progress-bar-hidden').hide();
			molgenis.checkMatchingStatus('${context_url}', $('#wizardForm'), currentStatus);
			//molgenis.checkMatchingStatus('${context_url}', $('#progress-bar-div').find('div.bar:eq(0)'));
			$('li.cancel').addClass('disabled').click(function(){
				if(!$(this).hasClass('disabled')){
					$('form').attr({
						'action' : '${context_url}/reset',
						'method' : 'GET'
					}).submit();
				}
				return false;
			});
			$('li.next').addClass('disabled').click(function(){
				if(!$(this).hasClass('disabled')){
					$('form').attr({
						'action' : '${context_url}/next',
						'method' : 'GET',
					}).submit();
				}
				return false;
			});
			$('li.previous').addClass('disabled').click(function(){
				if(!$(this).hasClass('disabled')){
					$('form').attr({
						'action' : '${context_url}/prev',
						'method' : 'GET'
					}).submit();
				}
				return false;
			});
		});
	</script>
</form>