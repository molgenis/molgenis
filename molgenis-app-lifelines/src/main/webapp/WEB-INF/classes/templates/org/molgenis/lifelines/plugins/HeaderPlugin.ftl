<#macro plugins_header_Header screen>
<#assign model = screen.myModel>
<div id="header" style="margin-top: 5px; margin-bottom: 10px;">
	<a href="/">
		<img src="${model.hrefLogo?html}">
	</a>
</div>
<div id="login-modal-container-header"></div>
<div class="login-header">
	<#assign login = screen.login/>
	<#if !login.authenticated>
		<div><a class="modal-href" href="/account/login" data-target="login-modal-container-header">login/register</a></div>
	</#if>
</div>
</#macro>
