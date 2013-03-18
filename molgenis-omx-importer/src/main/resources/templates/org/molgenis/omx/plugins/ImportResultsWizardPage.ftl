<#if wizard.importResult??>
	<div style="height: 230px; overflow: auto; width: 600px;background-color:white;font-family:Courier New, Courier, monospace" class="well">
		<ul class="unstyled">
			<#list wizard.importResult.messages?values as message>
				<li>${message}</li>
			</#list>
		</ul>
	</div>
</#if>