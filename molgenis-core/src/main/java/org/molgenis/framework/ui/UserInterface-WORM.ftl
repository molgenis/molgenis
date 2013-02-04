<#macro molgenis_header>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
"http://www.w3.org/TR/html4/strict.dtd">
<html>
	<head>
		<title>${application.getLabel()}</title>
		<link rel="stylesheet" style="text/css" href="css/colors.css" />
		<link rel="stylesheet" style="text/css" href="css/main.css" />
		<link rel="stylesheet" style="text/css" href="css/data.css" />
		<link rel="stylesheet" style="text/css" href="css/dateinput.css" />
		<link rel="stylesheet" style="text/css" href="css/xrefinput.css" />		
		<link rel="stylesheet" style="text/css" href="css/menu.css" />			
		<script language="JavaScript" type="text/javascript" src="js/all.js"></script>	
		<script src="js/popup.js" language="javascript"></script>	
		<script src="js/datetimeinput.js" language="javascript"></script>
		<script src="js/xrefinput.js" language="javascript"></script>
		<script src="js/mrefinput.js" language="javascript"></script>					
		<script src="js/menu.js" language="javascript"></script>	
		${application.getCustomHtmlHeaders()}
	</head>
	<body onload="document.getElementById('searchField').focus()">
</#macro>

<#macro molgenis_footer>
	</body>
</html>
</#macro>

<#macro UserInterface screen>
<@molgenis_header/>
		<table id="main">
		<!--header-->
		<tr>
			<!-- navigation menu-->
			<td id="navigation">

<#if username != "">
			<span id="logout">
				Logged in as <b>${username}</b> [<a href="" onClick="logout.submit();return false;">logout</a>]
				<form method="post" name="logout">
					<input type="hidden" name="logout" value="logout"/>
				</form>
			</span>
</#if>		
				<form name="navigationForm" method="get">
					<input type="hidden" name="__target" value="">
					<input type="hidden" name="select" value="">
				<!--table class="navigation">
<#--@Navigation screen=screen.getSelected() submenu="false" /-->
				</table-->
				</form>				
			</td>
			<!-- body -->
			<td class="information" valign="top" >
				<@layout screen.getSelected() />
			</td>
		</tr>
		<tr>
			<td></td>
			<td id="footer">
				<i>This database was generated using the open source <a href="http://www.molgenis.org">MOLGENIS database generator</a> version ${application.getVersion()}.
				Please cite <a href="http://www.ncbi.nlm.nih.gov/pubmed/15059831">Swertz et al (2004)</a> or <a href="http://www.ncbi.nlm.nih.gov/pubmed/17297480">Swertz & Jansen (2007)</a> on use. For-profit users should apply for a commercial license.</i>
			</td>
		</tr>
	</table>
<@molgenis_footer />	
</#macro>

<#macro Navigation screen submenu>
	<#assign selectedItem = screen.getSelected()/>
    <#list screen.getVisibleChildren() as item>
		<#assign __target = screen.getName() />
		<#assign select = item.getName() />
		<#if submenu == "true" && item.getParent().getClass().getSuperclass().getSimpleName() == "FormScreen" && item.getClass().getSuperclass().getSimpleName() == "MenuScreen">
		<#elseif item == selectedItem>
<tr><td class="leftNavigationSelected" name="${item.getLabel()}" onClick="document.forms.navigationForm.__target.value='${__target}';document.forms.navigationForm.select.value='${select}';document.forms.navigationForm.submit();">${item.getLabel()}</td></tr>
		<#if item.getChildren()?size &gt; 1>
<tr><td class="navigation"><table class="navigation">
				<#if selectedItem.getClass().getSuperclass().getSimpleName() == "MenuScreen"><@Navigation screen=item submenu="true" /></#if>
</table></td></tr>
		</#if>
		<#else>
<tr><td class="leftNavigationNotSelected" name="${item.getLabel()}" onClick="document.forms.navigationForm.__target.value='${__target}';document.forms.navigationForm.select.value='${select}';document.forms.navigationForm.submit();">${item.getLabel()}</td></tr>
		</#if>
	</#list>
</#macro>