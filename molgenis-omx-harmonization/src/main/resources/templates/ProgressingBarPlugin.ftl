<form id="progressingbar-form" class="form-horizontal">
	<div class="row-fluid">
		<div class="span12">
			<legend><div class="text-align-center">Please be patient</div></legend>
		</div>
	</div>
	<div id="progress-bar-div" class="row-fluid">
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
		});
	</script>
</form>