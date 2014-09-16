<#-- Contents of the shopping cart modal -->
<#if !attributes?has_content>
	<p>Cart is empty</p>
<#else>
	<table class="table">
	<tr><th/><th>name</th><th>type</th><th>nillable</th><th>description</th></tr>
	<#list attributes as attribute>
		<tr>
		<td><a href="remove?attributeName=${attribute.name?html}"><span class="glyphicon glyphicon-remove"></span></a></td>
		<th>${attribute.label}</th>
		<td>${attribute.dataType}</td>
		<td><input disabled='disabled' type='checkbox'<#if attribute.nillable> checked = 'checked' </#if>/></td>
		<td>${attribute.description}</td>
		</tr>
	</#list>
	</table>
</#if>