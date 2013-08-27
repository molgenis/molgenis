<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN">

<html>
<head>
		<link rel="stylesheet" href="css/jquery-ui-1.9.2.custom.min.css" type="text/css">
		<link rel="stylesheet" href="css/bootstrap.min.css" type="text/css">
		<link rel="stylesheet" href="css/molgenis-colors.css" type="text/css">
		<link rel="stylesheet" href="css/molgenis-data.css" type="text/css">
    <title>Account</title>
</head>

<body>
    <div class="screenpadding">
        Logged in as <b>${user.name}</b>
		<form action="/account/updateuser" method="post">
	        <h4>Login information</h4>
	
	        <div style="clear:both; display:block" id="divoldpwd">
	            <label style="width:16em;float:left;" for="oldpwd">Old password</label><input type="password" class="text ui-widget-content ui-corner-all " id="oldpwd" name="oldpwd" value=""><script type="text/javascript">
	$('#oldpwd').autoGrowInput({comfortZone: 16, minWidth:512, maxWidth: 4080});
	            </script>
	        </div>
	
	        <div style="clear:both; display:block" id="divnewpwd">
	            <label style="width:16em;float:left;" for="newpwd">New password</label><input type="password" class="text ui-widget-content ui-corner-all " id="newpwd" name="newpwd" value=""><script type="text/javascript">
	$('#newpwd').autoGrowInput({comfortZone: 16, minWidth:512, maxWidth: 4080});
	            </script>
	        </div>
	
	        <div style="clear:both; display:block" id="divnewpwd2">
	            <label style="width:16em;float:left;" for="newpwd2">Repeat new password</label><input type="password" class="text ui-widget-content ui-corner-all " id="newpwd2" name="newpwd2" value=""><script type="text/javascript">
	$('#newpwd2').autoGrowInput({comfortZone: 16, minWidth:512, maxWidth: 4080});
	            </script>
	        </div>
	
	        <h4>Personal information</h4>
	
	        <div style="clear:both; display:block" id="divemailaddress">
	            <label style="width:16em;float:left;" for="emailaddress">Email</label><input type="text" id="emailaddress" class="" name="emailaddress" value="<#if user.email??>${user.email}</#if>"> *
	        </div>
	
	        <div style="clear:both; display:block" id="divphone">
	            <label style="width:16em;float:left;" for="phone">Phone</label><input type="text" id="phone" class="" name="phone" value="<#if user.phone??>${user.phone}</#if>">
	        </div>
	
	        <div style="clear:both; display:block" id="divfax">
	            <label style="width:16em;float:left;" for="fax">Fax</label><input type="text" id="fax" class="" name="fax"  value="<#if user.fax??>${user.fax}</#if>">
	        </div>
	
	        <div style="clear:both; display:block" id="divtollFreePhone">
	            <label style="width:16em;float:left;" for="tollFreePhone">TollFreePhone</label><input type="text" id="tollFreePhone" class="" name="tollFreePhone"  value="<#if user.tollFreePhone??>${user.tollFreePhone}</#if>">
	        </div>
	
	        <div style="clear:both; display:block" id="divaddress">
	            <label style="width:16em;float:left;" for="address">Address</label><input type="text" id="address" class="" name="address"  value="<#if user.adress??>${user.adress}</#if>">
	        </div>
	
	        <div style="clear:both; display:block" id="divtitle">
	            <label style="width:16em;float:left;" for="title">Title</label><input type="text" id="title" class="" name="title"  value="<#if user.title??>${user.title}</#if>">
	        </div>
	
	        <div style="clear:both; display:block" id="divfirstname">
	            <label style="width:16em;float:left;" for="firstname">First name</label><input type="text" id="firstname" class="" name="firstname"  value="<#if user.firstname??>${user.firstname}</#if>"> *
	        </div>
	
	        <div style="clear:both; display:block" id="divlastname">
	            <label style="width:16em;float:left;" for="lastname">Last name</label><input type="text" id="lastname" class="" name="lastname"  value="<#if user.lastname??>${user.lastname}</#if>"> *
	        </div>
	
	        <div style="clear:both; display:block" id="divinstitute">
	            <label style="width:16em;float:left;" for="institute">Institute</label><input type="text" id="institute" class="" name="institute"  value="<#if user.affiliation_name??>${user.affiliation_name}</#if>">
	        </div>
	
	        <div style="clear:both; display:block" id="divdepartment">
	            <label style="width:16em;float:left;" for="department">Department</label><input type="text" id="department" class="" name="department"  value="<#if user.department??>${user.department}</#if>">
	        </div>
	
	        <div style="clear:both; display:block" id="divposition">
	            <label style="width:16em;float:left;" for="position">Position</label><input type="text" id="position" class="" name="position"  value="<#if user.roles_Identifier??>${user.roles_Identifier}</#if>">
	        </div>
	
	        <div style="clear:both; display:block" id="divcity">
	            <label style="width:16em;float:left;" for="city">City</label><input type="text" id="city" class="" name="city"  value="<#if user.city??>${user.city}</#if>">
	        </div>
	
	        <div style="clear:both; display:block" id="divcountry">
	            <label style="width:16em;float:left;" for="country">Country</label><input type="text" id="country" class="" name="country"  value="<#if user.country??>${user.country}</#if>">
	        </div><button type="submit" class="btn btn-small" id="ChgUser">Apply changes</button>
		</form>
    </div>
</body>
</html>
