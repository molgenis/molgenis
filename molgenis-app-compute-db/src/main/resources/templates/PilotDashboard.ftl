<!DOCTYPE html>
<html>
	<head>
		<title>Pilot plugin</title>
		<meta charset="utf-8">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
		<link rel="stylesheet" href="/css/pilot-dashboard.css" type="text/css">
		<script type="text/javascript" src="/js/jquery-1.8.3.min.js"></script>
		<script type="text/javascript" src="/js/bootstrap.min.js"></script>
		
		<script type="text/javascript">
			function updateRunStatus(run) {
				$.ajax({
					type : 'GET',
					url : '/plugin/dashboard/status?run=' + run,
					contentType : 'application/json',
					async : true,
					success : function(response) {
						console.log(JSON.stringify(response));
						updateHostStatusTable(run, response);
					}
					
				});
			}
			
			function updateHostStatusTable(run, response) {
				$('#' + run + ' td.generated').html(response.generated);
				$('#' + run + ' td.ready').html(response.ready);
				$('#' + run + ' td.running').html(response.running);
				$('#' + run + ' td.failed').html(response.failed);
				$('#' + run + ' td.done').html(response.done);
				
				if (response.failed > 0) {
					$('#renerateFailedTasksForm_' + run).show();
				} else {
					$('#renerateFailedTasksForm_' + run).hide();
				}
			}
			
			function updateStatus() {
				console.log('updateStatus');
				<#list runs as run>
					updateRunStatus('${run.name}');
				</#list>
				setTimeout(function(){updateStatus()}, 5000);
			}
			
			// on document ready
			$(function() {
				updateStatus();
			});
		</script>
	</head>
	<body>
		<div class="container-fluid">
			<#if error??>
				<div class="alert alert-error">
					<button type="button" class="close" data-dismiss="alert">&times;</button>
					${error}
				</div>
			</#if>
			
			<#if message??>
				<div class="alert alert-success">
					<button type="button" class="close" data-dismiss="alert">&times;</button>
					${message}
				</div>
			</#if>			
		
			<#if runs?size == 0>
				<h4>No Runs found. Please add a host on the 'Runs' tab</h4>
			</#if>
								
			<#list runs as run>
				<div class="well">
					<div class="row-fluid">
						<div class="span6">
							<div class="host-header">
								<span class="host-name">${run.name}@${run.backendUrl}</span>
								<#if run.running>
  									<span class="text-success">Running</span>
  								<#else>
  									<span class="text-error">Not running</span>
  								</#if>
  							</div>	
							<#if run.running>
  								<form action="/plugin/dashboard/stop" class="form-inline" method="post">		
  									<input type="hidden" name="run" value="${run.name}" />
  									<button type="submit" class="btn">Stop</button>	
  								</form>
  							<#else>
  								<form action="/plugin/dashboard/start" class="form-inline" method="post">
  									<input type="hidden" name="run" value="${run.name}" />
  									<input type="text" name="username" id="inputUsername" placeholder="Username"  />
  									<input type="password" name="password" id="inputPassword" placeholder="Password"  />
    								<button type="submit" class="btn">Start</button>
    							</form>		
  							</#if>
  							<#--
  							<form id="renerateFailedTasksForm_${run.name}" action="/plugin/dashboard/regenerate" class="form-inline" method="post">
  								<input type="hidden" name="run" value="${run.name}" />
  								<button type="submit" class="btn">Resubmit failed jobs</button>
  							</form>
  							-->
						</div>
    					<div class="span6 status">
    						<div class="status-table">
    							<table id="${run.name}" class="table table-condensed table-hover">
    								<tr>
    									<td>Jobs generated</td>
    									<td class="generated"></td>
    								</tr>
    								<tr>
    									<td>Jobs ready</td>
    									<td class="ready"></td>
    								</tr>
    								<tr>
    									<td>Jobs running</td>
    									<td class="running"></td>
    								</tr>
    								<tr>
    									<td>Jobs failed</td>
    									<td class="failed"></td>
    								</tr>
    								<tr>
    									<td>Jobs done</td>
    									<td class="done"></td>
    								</tr>
    							</table>
    						</div>
    					</div>
    				</div>
    			</div>
    		</#list>					
		</div>
		
	</body>
</html>