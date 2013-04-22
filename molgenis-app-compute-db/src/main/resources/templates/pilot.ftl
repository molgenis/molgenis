<!DOCTYPE html>
<html>
	<head>
		<title>Pilot plugin</title>
		<meta charset="utf-8">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
		<link rel="stylesheet" href="/css/pilot.css" type="text/css">
		<script type="text/javascript" src="/js/jquery-1.8.3.min.js"></script>
	</head>
	<body>
		<div class="container-fluid">
			<div class="row-fluid">
				
				<form style="height: 100%" id="pilotForm" name="pilotForm" <#if pilotForm.running>action="/plugin/pilot/stop"<#else>action="/plugin/pilot/start"</#if> class="form-horizontal">
  					<#if errorMessage??>
						<div class="alert alert-error">${errorMessage}</div>
					</#if>
  					<div class="control-group">
  						<label class="control-label">Status</label>
  						<div class="controls">
  							<#if pilotForm.running>
  								<h4 class="text-success">Running</h4>
  							<#else>
  								<h4 class="text-error">Not running</h4>
  							</#if>
    					</div>
  					</div>
  					<div class="control-group">
  						<label class="control-label" for="inputComputeHost">Compute host</label>
    					<div class="controls">
    						<select id="inputComputeHost" name="computeHostId">
    							<#list computeHosts as computeHost>
    								<option value="${computeHost.id}" <#if pilotForm.computeHostId?? && (pilotForm.computeHostId == computeHost.id) >selected</#if> >
    									${computeHost.name}
    								</option>
    							</#list>
    						</select>
    					</div>
 					 </div>
  					<div class="control-group">
    					<label class="control-label" for="inputPassword">Password</label>
    					<div class="controls">
      						<input type="password" id="inputPassword" placeholder="Password" name="password">
    					</div>
  					</div>
  					<div class="control-group">
   			 			<div class="controls">
   							<button type="submit" class="btn" <#if computeHosts?size == 0>disabled=true</#if> ><#if pilotForm.running>Stop<#else>Start</#if></button>
    					</div>
  					</div>
				</form>
			</div>
			
		</div>
		
		<#if pilotForm.running>
			<script type="text/javascript">
				$("#pilotForm input").attr("disabled", true);
				$("#pilotForm select").attr("disabled", true);
			</script>
		</#if>
		
	</body>
</html>