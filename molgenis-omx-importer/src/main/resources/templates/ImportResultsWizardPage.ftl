<form method="post" id="wizardForm" name="wizardForm" action="">
<#if wizard.importResult??>
	<div style="height: 230px; overflow: auto; width: 600px;background-color:white;font-family:Courier New, Courier, monospace" class="well">
		<ul class="unstyled">
			<#list wizard.importResult.nrImportedEntitiesMap?keys as entityName>
				<li>imported ${wizard.importResult.nrImportedEntitiesMap[entityName]?c} ${entityName} entities</li>
			</#list>
		</ul>
	</div>
</#if>
</form>