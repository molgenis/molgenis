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
					$('#resubmitFailedTasksForm_' + run).show();
				} else {
					$('#resubmitFailedTasksForm_' + run).hide();
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
				<h4>No Runs found.</h4>
			</#if>
								
			<#list runs as run>
				<div class="well well-small">
					<div class="row-fluid">
						<div class="span6">
							<div class="host-header">
								<span class="host-name">${run.name}@${run.backendUrl}</span>
								<span class="creation-time">(${run.creationTime})</span>
								<#if run.running>
  									<div class="text-success">Active</div>
                                    <form id="resubmitFailedTasksForm_${run.name}" action="/plugin/dashboard/inactivate" class="form-inline" method="post">
                                        <input type="hidden" name="run" value="${run.name}" />
                                        <button type="submit" class="btn resubmit-btn">Inactivate</button>
                                    </form>
  								<#else>
  									<div class="text-error">Not active</div>
                                    <form id="resubmitFailedTasksForm_${run.name}" action="/plugin/dashboard/activate" class="form-inline" method="post">
                                        <input type="hidden" name="run" value="${run.name}" />
                                        <button type="submit" class="btn resubmit-btn">Activate</button>
                                    </form>
                                </#if>
  								
  							</div>	
							<#if run.running>
  								<form action="/plugin/dashboard/stop" class="form-inline" method="post">		
  									<input type="hidden" name="run" value="${run.name}" />
  									<button type="submit" class="btn">Stop Submitting Pilots</button>
  								</form>
  							<#else>
  								<form action="/plugin/dashboard/start" class="form-inline" method="post">
  									<input type="hidden" name="run" value="${run.name}" />
  									<input type="text" name="username" id="inputUsername" placeholder="Username"  />
  									<input type="password" name="password" id="inputPassword" placeholder="Password"  />
    								<button type="submit" class="btn">Submit Pilots</button>
    							</form>		
  							</#if>
  							
  							<form id="resubmitFailedTasksForm_${run.name}" action="/plugin/dashboard/resubmit" class="form-inline" method="post">
  								<input type="hidden" name="run" value="${run.name}" />
  								<button type="submit" class="btn resubmit-btn">Resubmit failed jobs</button>
  							</form>
  							
						</div>
    					<div class="span5 status">
    						<div class="status-table">
    							<table id="${run.name}" class="table table-condensed table-hover">
    								<tr>
    									<td>Jobs generated</td>
    									<td class="generated"></td>
    									<td class="text-success">Jobs done</td>
    									<td class="done text-success"></td>
    								</tr>
    								<tr>
    									<td class="text-info">Jobs ready</td>
    									<td class="ready text-info"></td>
    									<td class="text-error">Jobs failed</td>
    									<td class="failed text-error"></td>
    								</tr>
    								<tr>
    									<td class="text-warning">Jobs running</td>
    									<td class="running text-warning"></td>
    									<td></td>
    									<td></td>
    								</tr>
                                    <tr>
                                        <td class="text-warning">Pilots submitted</td>
                                        <td class="running text-warning"></td>
                                        <td class="text-warning">Pilots started</td>
                                        <td class="running text-warning"></td>
                                    </tr>
    							</table>
    						</div>
    					</div>
    					<div class="span1">
    						<a href="/plugin/dashboard/close?run=${run.name}" title="Remove from dashboard" class="close" >&times;</a>
    					</div>
    				</div>
    			</div>
    		</#list>					
		</div>
		
	</body>
</html>