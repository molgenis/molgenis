<#macro listTasks>
<div class="row">
	<div class="col-md-offset-2 col-md-8">
		<div class="row" style="min-height:300px;">
			<#if existingTasks?? & (existingTasks?size > 0)>
			<div class="col-md-12">
				<br><span class="font-size-medium-center">There are ${existingTasks?size} existing matching tasks available, you can retrieve them by clicking <strong>Retrieve</strong> button</span><br>
				<table class="table">
					<tr><th>Name</th><th>Date created</th><th>Code system</th><th>Retrieve</th></tr>
					<#list existingTasks as task>
					<tr><td>${task.Identifier}</td><td>${task.Data_created}</td><td>${task.Code_system}</td><td><button type="button" class="btn">Retrieve</button></td></tr>
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
	});
</script>
</#macro>