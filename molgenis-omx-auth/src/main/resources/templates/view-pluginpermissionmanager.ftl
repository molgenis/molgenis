<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=[]>
<#assign js=["pluginpermissionmanager.js"]>
<@header css js/>
	<div class="row-fluid">
		<div class="span3"></div>
		<div class="span6">
			<div class="row-fluid">	
				<div class="well">
					<ul class="nav nav-pills">
						<li class="active"><a href="#group-plugin-permission-manager" data-toggle="tab">Groups</a></li>
						<li><a href="#user-plugin-permission-manager" data-toggle="tab">Users</a></li>
					</ul>
					<div class="tab-content">
					    <div class="tab-pane active" id="group-plugin-permission-manager">
							<form class="form-horizontal" id="group-plugin-permission-form" method="post" action="${context_url}/update/group">
								<label class="control-label" for="group-select">Select Group</label>
				    			<div class="controls">
									<select name="groupId" id="group-select">
								<#list groups as group>
										<option value="${group.id?c}"<#if group_index == 0> selected</#if>>${group.name}</option>
								</#list>
									</select>
								</div>
						  		<table class="table table-condensed table-borderless" id="group-plugin-permission-table">
						  			<thead>
						  				<tr>
						  					<th>Plugin</th>
						  					<th>Write</th>
						  					<th>Read</th>
						  					<th>None</th>
						  				</tr>
						  			</thead>
						  			<tbody>
						  			</tbody>
						  		</table>
						  		<button type="submit" class="btn pull-right">Save</button>
							</form>
					    </div>
					    <div class="tab-pane" id="user-plugin-permission-manager">
							<form class="form-horizontal" id="user-plugin-permission-form" method="post" action="${context_url}/update/user">
								<label class="control-label" for="user-select">Select User</label>
				    			<div class="controls">
									<select name="userId" id="user-select">
								<#list users as user>
										<option value="${user.id?c}"<#if user_index == 0> selected</#if>>${user.username}</option>
								</#list>
									</select>
								</div>
						  		<table class="table table-condensed table-borderless" id="user-plugin-permission-table">
						  			<thead>
						  				<tr>
						  					<th>Plugin</th>
						  					<th>Write</th>
						  					<th>Read</th>
						  					<th>None</th>
						  				</tr>
						  			</thead>
						  			<tbody>
						  			</tbody>
						  		</table>
						  		<button type="submit" class="btn pull-right">Save</button>
							</form>
					    </div>
					</div>
				</div>
			</div>
		</div>
		<div class="span3"></div>
	</div>
<@footer/>