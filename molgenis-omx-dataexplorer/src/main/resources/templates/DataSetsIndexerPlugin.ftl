<#macro DataSetsIndexerPlugin screen>

	<form method="post" id="datasets-form" name="${screen.name}" enctype="multipart/form-data" action="molgenis.do">
		<!--needed in every form: to redirect the request to the right screen-->
		<input type="hidden" name="__target" value="${screen.name}">
		<input type="hidden" value="${screen.name}" name="select">
		<!--need to be set to "true" in order to force a download-->
		<input type="hidden" name="__show">
		
		<div class="formscreen">
			
			<div class="form_header" id="${screen.name}">${screen.label}</div>
			<#--messages-->
			<#list screen.getMessages() as message>
				<#if message.text??>
					<#if message.success>
						<p class="successmessage">${message.text}</p>
					<#else>
						<p class="errormessage">${message.text}</p>
					</#if>
				</#if>
			</#list>
			
			<div class="screenbody">
				<div class="screenpadding">
					<h4>Index datasets:</h4>
					<div style="width:400px">
						<a href="#" id="deselect-all" style="float:right;margin-left:10px">Deselect all</a>
						<a href="#" id="select-all" style="float:right">Select all</a>
					</div>
					<div class="well" style="width: 400px; max-height:400px; overflow:auto">
						<#list screen.dataSets as dataSet>
							<label style="display: block; padding-left: 15px;">
								<input id="d${dataSet.id}" class="dataset-chk" type="checkbox" name="dataset" value="${dataSet.id}" /> ${dataSet.name}
							</label> 
						</#list>
					</div>
					<input type="submit" value="Start indexing" class="btn" style="margin-top: 20px" />
				</div>
			</div>
			
		</div>
		
	</form>
	
	<script type="text/javascript">
		$('#select-all').click(function() {
			$('.dataset-chk').attr('checked', true);
			return false;
		});
		
		$('#deselect-all').click(function() {
			$('.dataset-chk').attr('checked', false);
			return false;
		});
		
		$('#datasets-form').submit(function() {
			$('input[type=submit]').hide();
			$('#spinner').modal('show');
			return true;	
		});
	</script>
</#macro>