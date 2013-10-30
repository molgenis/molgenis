<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["chosen.css"]>
<#assign js=["chosen.jquery.min.js", "usermanager.js"]>
<@header css js/>
<form class="form-horizontal" id="form-usermanager" method="post" action="${context_url}">
	<div class="container-fluid"> 
		<div class="row-fluid">
			<div id="userView" class="span6">
				<div class="control-group">
					<label class="control-label" for="user-select">Select User:</label>
					<div class="controls">
						<select id="user-select" data-placeholder="Choose an user" name="userId" class="chosen-select">
							<option value="-1"></option>
							<#if users?has_content>
								<#list users as user>
									<option value="${user.id?c}" <#if (user.id?string == user_selected_id?string)> selected</#if>>${user.username}</option>
								</#list>
							</#if>
						</select>
					</div>
				</div>	
				
				<div class="control-group">	
					<#if groupsWhereUserIsMember?has_content>
						<div class="controls">
							<table id="groupsWhereUserIsMember" class="table table-striped table-hover">
								<tbody>
									<#list groupsWhereUserIsMember as group>
										<tr data-group-id="${group.id?c}" style="cursor: pointer;">
											<td>${group.name}</td>
											<td>
												<div class="controls">
													<a class="btn btn-small" data-remove-group-id="${group.id?c}"><i class="icon-remove"></i></a>
												</div>
											</td>
										</tr>
									</#list>
								</tbody>
							</table>
						</div>
					</#if>
				</div>	
					
				<div class="control-group">	
					<#if groupsWhereUserIsNotMember?has_content>
						<label class="control-label" for="drop-down-groups-to-add">Add an user to this group:</label>
						<div class="controls">
							<select id="drop-down-groups-to-add" data-placeholder="Choose a group" name="groupToAddId">
								<option></option>
								<#list groupsWhereUserIsNotMember as group>
									<option value="${group.id?c}">${group.name}</option>
								</#list>
							</select>
						</div>
					</#if>
				</div>	
			</div>
				
			<div id="groupView" class="span6">
				<div class="control-group">
					<label class="control-label" for="group-select">Select Group:</label>
					<div class="controls">
						<select id="group-select" data-placeholder="Choose a group" name="groupId">
							<#if groups?has_content>
								<option value="-1"></option>
								<#list groups as group>
									<option value="${group.id?c}" <#if (group.id?string == group_selected_id?string)> selected</#if>>${group.name}</option>
								</#list>
							</#if>
						</select>
					</div>
				</div>	
				
				<div class="control-group">	
					<div class="controls">
						<table id="users-of-group" class="table table-striped table-hover">
						</table>
					</div>
				<div>	
			</div>
		</div>
	</div>
</form>
<@footer/>