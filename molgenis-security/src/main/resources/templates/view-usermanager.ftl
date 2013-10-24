<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["usermanager.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "usermanager.js"]>
  <link rel="stylesheet" href="http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css" />
<@header css js/>
<form class="form-horizontal" id="form-usermanager" method="post" action="${context_url}">
	<div class="container-fluid"> 
		<div class="row-fluid">
			<div id="userView" class="span6">
					<label class="control-label" for="user-select">Select User:</label>
					<select id="user-select" name="userId" >
						<#if users?has_content>
							<#list users as user>
								<option value="${user.id?c}" <#if (user.id?string == user_selected_id?string)> selected</#if>>${user.username}</option>
							</#list>
						</#if>
					</select>
					
					<#if groupsWhereUserIsMember?has_content>
						<ol id="groupsWhereUserIsMember">
							<#list groupsWhereUserIsMember as group>
								<li value="${group.id?c}" class="ui-widget-content">${group.name}</li>
							</#list>
						</ol>
					</#if>
					
					<#if groupsWhereUserIsNotMember?has_content>
						<label class="control-label" for="dropDownOfGroupsToAdd">Select A group to add to user:</label>
						<select id="dropDownOfGroupsToAdd" name="groupToAddId">
							<#list groupsWhereUserIsNotMember as group>
								<option value="${group.id?c}"<#if group_index == 0> selected</#if>>${group.name}</option>
							</#list>
						</select>
					</#if>
			</div>
				
			<div id="groupView" class="span6">
				<label class="control-label" for="group-select">Select Group:</label>
				<select id="group-select" name="groupId" >
					<#list groups as group>
						<option value="${group.id?c}"<#if group_index == 0> selected</#if>>${group.name}</option>
					</#list>
				</select>
				<ol id="usersMemberOfGroup"></ol>
			</div>
		</div>
	</div>
</form>
<@footer/>