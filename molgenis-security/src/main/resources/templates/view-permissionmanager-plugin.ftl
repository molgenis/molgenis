				<div class="well">
					<ul class="nav nav-pills">
						<li class="active"><a href="#plugin-group-permission-manager" data-toggle="tab">Groups</a></li>
						<li><a href="#plugin-user-permission-manager" data-toggle="tab">Users</a></li>
					</ul>
					<div class="tab-content">
					    <div class="tab-pane active" id="plugin-group-permission-manager">
							<form class="form-horizontal" id="plugin-group-permission-form" method="post" action="${context_url}/update/plugin/group">
								<label class="control-label" for="plugin-group-select">Select Group:</label>
				    			<div class="controls">
									<select name="groupId" id="plugin-group-select">
								<#list groups as group>
										<option value="${group.id?c}"<#if group_index == 0> selected</#if>>${group.name}</option>
								</#list>
									</select>
								</div>
								<div class="permission-table-container">
							  		<table class="table table-condensed table-borderless" id="plugin-group-permission-table">
							  			<thead>
							  				<tr>
							  					<th>Plugin</th>
							  					<th>Edit</th>
							  					<th>View</th>
							  					<th>None</th>
							  				</tr>
							  			</thead>
							  			<tbody>
							  			</tbody>
							  		</table>
						  		</div>
						  		<button type="submit" class="btn pull-right">Save</button>
							</form>
					    </div>
					    <div class="tab-pane" id="plugin-user-permission-manager">
							<form class="form-horizontal" id="plugin-user-permission-form" method="post" action="${context_url}/update/plugin/user">
								<label class="control-label" for="plugin-user-select">Select User:</label>
				    			<div class="controls">
									<select name="userId" id="plugin-user-select">
								<#list users as user>
										<option value="${user.id?c}"<#if user_index == 0> selected</#if>>${user.username}</option>
								</#list>
									</select>
								</div>
								<div class="permission-table-container">
							  		<table class="table table-condensed table-borderless" id="plugin-user-permission-table">
							  			<thead>
							  				<tr>
							  					<th>Plugin</th>
							  					<th>Edit</th>
							  					<th>View</th>
							  					<th>None</th>
							  				</tr>
							  			</thead>
							  			<tbody>
							  			</tbody>
							  		</table>
						  		</div>
						  		<button type="submit" class="btn pull-right">Save</button>
							</form>
					    </div>
					</div>
				</div>