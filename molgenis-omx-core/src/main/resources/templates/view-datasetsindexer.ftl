<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=[]>
<#assign js=["datasetsindexer.js"]>
<@header css js/>
	<div class="row-fluid">
		<div class="span9">
		<h4>Index datasets:</h4>
		<div style="width:400px">
			<a href="#" id="deselect-all" style="float:right;margin-left:10px">Deselect all</a>
			<a href="#" id="select-all" style="float:right">Select all</a>
		</div>
		<div class="well" style="width: 400px; max-height:400px; overflow:auto">
			<#list dataSets as dataSet>
				<label style="display: block; padding-left: 15px;">
					<input id="d${dataSet.id?c}" class="dataset-chk" type="checkbox" name="dataset" value="${dataSet.id?c}" /> ${dataSet.name}
				</label> 
			</#list>
		</div>
		<input type="submit" value="Start indexing" class="btn" style="margin-top: 20px" />
		</div>
	</div>
	<script type="text/javascript">
		alert("test");
		console.log("stap 1");
		alert("test2");
		$(document).ready(function(){
			$('#select-all').click(function() {
				$('.dataset-chk').attr('checked', true);
				return false;
			});
			
			$('#deselect-all').click(function() {
				$('.dataset-chk').attr('checked', false);
				return false;
			});
			
			$('input[type=submit]').click(function(){
				var selectedDataSets = [];
				$('.dataset-chk').each(function(){
					if($(this).is(':checked')){
						selectedDataSets.push($(this).val());
					} 
				});
				var indexRequest = {
					'selectedDataSets' : selectedDataSets
				};
				$.ajax({
					type : 'POST',
					url : '${context_url}/index',
					async : false,
					data : JSON.stringify(indexRequest),
					contentType : 'application/json',
				}).done(function(response){
					molgenis.createAlert([{'message': response.message}], response.isRunning ? 'success' : 'error');
					$('.dataset-chk').attr('checked', false);
				});
				
				return false;
			});
			console.log("stap 2");
		});
	</script>
<@footer/>	