<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#-- Some of the css/js may be omitted, here...   -->
  
<#assign css=['select2.css', 'bootstrap-datetimepicker.min.css', 'molgenis-form.css', "chosen.css"]>
<#assign js=['jquery.validate.min.js', 'select2.min.js', 'bootstrap-datetimepicker.min.js', <#--'molgenis-form-edit.js',--> "chosen.jquery.min.js", "usermanager.js"]>

<@header css js/>
<div class="container-fluid">
	<div class="row-fluid">
		<ul class="nav nav-pills">
			<li id="usersTab"><a href="#user-manager" data-toggle="tab" onclick="setViewState('users');">Users</a></li>
			<li id="groupsTab"><a href="#group-manager" data-toggle="tab" onclick="setViewState('groups');">Groups</a></li>
		</ul>
		<div class="tab-content">
			<div class="tab-pane" id="user-manager">
				<h3>User management <a href="#" style="margin:30px 10px" data-toggle="modal" data-target="#managerModal" onclick="getCreateForm('user')"><img src="/img/new.png"></a></h3>
				<table class="table">
				<thead>
				<tr>
					<th>Edit</th>
					<th>Active</th>
					<th>Username</th>
					<th>Full name</th>
					<#list groups as g><#if g.active><th>${g.name}</th></#if></#list>
				</tr>
				</thead>
				
				<tbody>
				<#if users?has_content>
				<#list users as user>
				<tr id="userRow${user.id}">
					<td><a href="#" data-toggle="modal" data-target="#managerModal" onclick="getEditForm(${user.id}, 'user')"><img src="/img/editview.gif"></a></td>
					<td><#if 1 < user.id><input type="checkbox" onclick="setActivation('user', ${user.id}, this)" <#if user.isActive()>checked</#if>></#if></td>
					<td>${user.getUsername()?if_exists}</td>
					<td>${user.getFullName()?if_exists}</td>
					<#list groups as g><#if g.active><td><input type="checkbox" onclick="changeGroupMembership(${user.id},${g.id},this)" <#if user.isGroupMember(g.id)>checked</#if>></td></#if></#list>
				</tr>
				</#list>
				</#if>
			 	</tbody>
				</table>
			</div>
			<div class="tab-pane" id="group-manager">
				<h3>Group management <a href="#" style="margin:30px 10px" data-toggle="modal" data-target="#managerModal" onclick="getCreateForm('group')"><img src="/img/new.png"></a></h3>
				<table class="table">
				<thead>
				<tr>
					<th>Edit</th>
					<th>Active</th>
					<th>Group name</th>
					<th>Usernames in group</th>
				</tr>
				</thead>
				<tbody>
				<#if groups?has_content>
				<#list groups as g>
				<tr id="groupRow${g.id}">
					<td><a href="#" data-toggle="modal" data-target="#managerModal" onclick="getEditForm(${g.id},'group')"><img src="/img/editview.gif"></a></td>
					<td><input type="checkbox" onclick="setActivation('group', ${g.id}, this)" <#if g.active>checked</#if>></td>
					<td>${g.getName()?if_exists}</td>
					<td>
					<#if users?has_content>
					<#assign setComma = false>
					<#list users as user><#if user.isGroupMember(g.id)><#if setComma>, </#if>${user.getUsername()?if_exists}<#assign setComma = true></#if></#list>
					</#if>
					</td>
				</tr>				
				</#list>
				</#if>
				</tbody>
				</table>
			</div>
		</div>
	</div>
</div>


<!-- Modal -->
<div class="modal fade medium" id="managerModal" tabindex="-1" role="dialog" aria-hidden="true" onkeydown="return ignoreEnter(event);">
	<div class="modal-dialog">
		<div class="modal-content">				
	      	<div class="modal-header">
	        	<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
	        	<h4 class="modal-title" id="managerModalTitle"></h4>
	     	</div>
	      	<div class="modal-body">
				<DIV id="controlGroups"></DIV>
	      	</div>
	      	<div class="modal-footer">
	        	<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
	        	<button id="submitFormButton" type="submit" class="btn btn-primary">Save</button>
	      	</div>
	    </div>
	</div>
</div>

<@footer/>