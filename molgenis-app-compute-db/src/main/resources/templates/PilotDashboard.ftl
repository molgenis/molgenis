<!DOCTYPE html>
<html>
	<head>
		<title>Pilot plugin</title>
		<meta charset="utf-8">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
		<link rel="stylesheet" href="/css/pilot-dashboard.css" type="text/css">
		<script type="text/javascript" src="/js/jquery-1.8.3.min.js"></script>
		
		<script type="text/javascript">
			function updateHostStatus(hostId) {
				$.ajax({
					type : 'POST',
					url : '/api/v1/computetask?_method=GET',
					data : JSON.stringify({
						q : [ {
							"field" : "computeHost",
							"operator" : "EQUALS",
							"value" : hostId
						} ]
					}),
					contentType : 'application/json',
					async : true,
					success : function(response) {
						console.log(JSON.stringify(response));
						updateHostStatusTable(hostId, response);
					}
					
				});
			}
			
			function updateHostStatusTable(hostId, response) {
				var generated = 0;
				var ready = 0;
				var running = 0;
				var failed = 0;
				var done = 0;
					
				$.each(response.items, function(key, val) {	
					if (val.statusCode == 'generated') {
						generated++;
					} else if (val.statusCode == 'ready') {
						ready++;
					} else if (val.statusCode == 'running') {
						running++;
					} else if (val.statusCode == 'failed') {
						failed++;
					} else if (val.statusCode == 'done') {
						done++;
					} 
				});
				
				$('#' + hostId + ' td.generated').html(generated);
				$('#' + hostId + ' td.ready').html(ready);
				$('#' + hostId + ' td.running').html(running);
				$('#' + hostId + ' td.failed').html(failed);
				$('#' + hostId + ' td.done').html(done);
			}
			
			function updateStatus() {
				console.log('updateStatus');
				<#list hosts as host>
					updateHostStatus(${host.id});
				</#list>
			}
			
			// on document ready
			$(function() {
				setInterval(function(){updateStatus()}, 5000);
			});
		</script>
	</head>
	<body>
		<div class="container-fluid">
			<#if error??>
				<div class="alert alert-error">${error}</div>
			</#if>
							
			<#list hosts as host>
				<div class="well">
					<div class="row-fluid">
						<div class="span12">
							<span class="host-name">${host.name}</span>
							Status:
								<#if host.running>
  									<span class="text-success">Running</span>
  								<#else>
  									<span class="text-error">Not running</span>
  								</#if>
						</div>
					</div>
					<div class="row-fluid">
						<div class="span6 dashboard">
							<#if host.running>
  								<form action="/plugin/dashboard/stop" class="form-horizontal" method="post">		
  									<input type="hidden" name="id" value="${host.id}" />
  									<div class="control-group">
    									<div class="controls">
      										<button type="submit" class="btn">Stop</button>
    									</div>
  									</div>
  								</form>
  							<#else>
  								<form action="/plugin/dashboard/start" class="form-horizontal" method="post">
  									<input type="hidden" name="id" value="${host.id}" />
  									<div class="control-group">
    									<label class="control-label" for="inputPassword">Password</label>
    									<div class="controls">
      										<input type="password" name="password" id="inputPassword" placeholder="Password">
    									</div>
  									</div>
  									<div class="control-group">
    									<div class="controls">
      										<button type="submit" class="btn">Start</button>
    									</div>
  									</div>
  								</form>
  							</#if>
  							
  							<form action="/plugin/dashboard/generate" class="form-horizontal" method="post">
  								<input type="hidden" name="hostName" value="${host.name}" />
  								<div class="control-group">
    								<label class="control-label" for="inputParametersFile">Parameters file</label>
    								<div class="controls">
      									<input type="text" name="parametersFile" id="inputParametersFile" placeholder="File path">
    								</div>
  								</div>
  								<div class="control-group">
    								<div class="controls">
      									<button type="submit" class="btn">Generate</button>
    								</div>
  								</div>
  							</form>
  							
						</div>
    					<div class="span6">
    						<table id="${host.id}" class="table">
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
    		</#list>					
		</div>
		
	</body>
</html>