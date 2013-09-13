<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=[]>
<#assign js=["pluginpermissionmanager.js"]>
<@header css js/>
	<div class="row-fluid">
		<div class="span3"></div>
		<div class="span6">
			<form class="form-horizontal" id="pluginpermission-form" method="post" action="${context_url}/update">
				<label class="control-label" for="user-select">User</label>
    			<div class="controls">
					<select name="userId" id="user-select">
				<#list users as user>
						<option value="${user.id?c}"<#if user_index == 0> selected</#if>>${user.name}</option>
				</#list>
					</select>
				</div>
		  		<table class="table table-condensed table-borderless" id="plugin-permission-table">
		  			<thead>
		  				<tr>
		  					<th>Plugin</th>
		  					<th>Read</th>
		  					<th>Write</th>
		  					<th>None</th>
		  				</tr>
		  			</thead>
		  			<tbody>
		  			</tbody>
		  		</table>
		  		<button type="submit" class="btn pull-right">Save</button>
			</form>
		</div>
		<div class="span3"></div>
	</div>
<@footer/>