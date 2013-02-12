<#assign form = model.authenticationForm>

<table width="300px">
<tr>
<td>
<table>
<tr><td>Username:</td><td>${form.username}</td></tr>
<tr><td>Password:</td><td>${form.password}</td></tr>
<tr><td align="center" colspan="2">${form.login}</td></tr>
<#if model.class.simpleName != "SimpleUserLoginModel">
<tr><td align="center" colspan="2"><a href="molgenis.do?__target=${screen.name}&__action=Register">Register</a></td></tr>
</#if>
<tr><td align="center" colspan="2"><a href="molgenis.do?__target=${screen.name}&__action=Forgot">Forgot password?</a></td></tr>
</table>
</td>
<td>
<table>
<#list form?keys as key>
<#if key == "google">
<tr><td>${form.google}</td></tr>
<#elseif key == "yahoo">
<tr><td>${form.yahoo}</td></tr>
</#if>
</#list>
</table>
</td>
</tr>
</table>