<form id="wizardForm" name="wizardForm" method="post" class="form-horizontal" action="">
	<div class="row-fluid">
		<div class="span12">
			<legend><div><strong>Please be patient</strong> : this will take a while </div></legend>
		</div>
	</div>
	<script type="text/javascript">
		function isRunning(){
			$.ajax({
				type : 'GET',
				url : molgenis.getContextUrl() + '/progress',
				contentType : 'application/json',
				success : function(data, textStatus, request) {	
					console.log(data);
					if(data.isRunning === true){
						setTimeout(function(){
							isRunning();
						}, 3000);
					}
				},
				error : function(request, textStatus, error){
					console.log(error);
				}
			});
		}
		$(document).ready(function(){
			isRunning();
		});
	</script>
</form>