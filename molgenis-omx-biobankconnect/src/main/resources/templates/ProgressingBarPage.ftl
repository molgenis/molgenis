<form id="wizardForm" name="wizardForm" method="post" class="form-horizontal" action="">
	<div class="row-fluid">
		<div class="span12">
			<legend><div><strong>Please be patient</strong> : this will take up to 5 mins</div></legend>
		</div>
	</div>
	<div id="progress-bar-div" class="row-fluid">
		<div class="offset2 span8">
			When finished, click next!
		</div>
		<div class="progress progress-striped active offset2 span8">
			<div class="bar text-align-center"></div>
		</div>
	</div>
	<div id="existing-mapping-div" class="row-fluid">
	</div>
	<script type="text/javascript">
		$(document).ready(function(){
			var molgenis = window.top.molgenis;
			molgenis.checkMatchingStatus('${context_url}', $('#progress-bar-div').find('div.bar:eq(0)'));
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