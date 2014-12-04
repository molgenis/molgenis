<#macro listTasks>
<div class="row">
	<div class="col-md-offset-2 col-md-8">
		<div class="row" style="min-height:300px;">
			<#if existingTasks?? & (existingTasks?size > 0)>
			<div class="col-md-12">
				<br><span class="font-size-medium-center">There are ${existingTasks?size?html} existing matching tasks available, you can retrieve them by clicking <strong>Retrieve</strong> button</span><br>
				<table class="table">
					<tr><th>Name</th><th>Date created</th><th>Code system</th><th>Retrieve</th><th>Delete</th></tr>
					<#list existingTasks as task>
					<tr><td>${task.Identifier?html}</td><td>${task.Date_created?html}</td><td>${task.Code_system?html}</td>
						<td><button type="button" class="btn btn-default retrieve-button-class">Retrieve</button></td>
						<td><button type="button" class="btn btn-danger remove-button-class">Delete</button></td>
					</tr>
					</#list>
				</table>
			</div>
			<#else>
				<center>There are no existing matching jobs available! Please start a new one!</center>
			</#if>
		</div>
		<div class="row" style="min-height:100px;">
			<div class="col-md-12">
				<button id="new-task-button" type="button" class="btn btn-primary">New task</button>
			</div>
		</div>
	</div>
</div>
<script>
	$(document).ready(function(){
		$('#new-task-button').click(function(){
			$('#ontology-match').attr({
				'action' : molgenis.getContextUrl() + '/newtask',
				'method' : 'GET'
			}).submit();
		});
		$.each($('.retrieve-button-class'), function(index, button){
			$(button).click(function(){
				$('#ontology-match').attr({
					'action' : molgenis.getContextUrl() + '/result/' + $(this).parents('tr:eq(0)').children('td:eq(0)').html(),
					'method' : 'GET'
				}).submit();
			});
		});
		$.each($('.remove-button-class'), function(index, button){
			$(button).click(function(){
				var deleteButton = $('<button type="button" class="btn btn-primary">Confirm</button>').click(function(){
					var ontologyService = new molgenis.OntologyService();
					var entityName = $(button).parents('tr:eq(0)').children('td:eq(0)').html();					
					ontologyService.deleteMatchingTask(entityName, function(){
						location.reload();
					})
				});
				var modal = molgenis.createModalCallback('Delete task', {
					'body' : $('<div />').append('Are you sure you want to delete this task?'),
					'footer' : deleteButton
				});
				modal.modal('show');
			});
		});
	});
</script>
</#macro>