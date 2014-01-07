				<div class="well">
					<ul class="nav nav-pills">
						<li class="active"><a href="#entity-class-group-permission-manager" data-toggle="tab">Groups</a></li>
						<li><a href="#entity-class-user-permission-manager" data-toggle="tab">Users</a></li>
					</ul>
					<div class="tab-content">
					    <div class="tab-pane active" id="entity-class-group-permission-manager">
							<form class="form-horizontal" id="entity-class-group-permission-form" method="post" action="${context_url}/update/entityclass/group">
								<label class="control-label" for="entity-class-group-select">Select Group:</label>
				    			<div class="controls">
									<select name="groupId" id="entity-class-group-select">
								<#list groups as group>
										<option value="${group.id?c}"<#if group_index == 0> selected</#if>>${group.name}</option>
								</#list>
									</select>
								</div>
								<div class="permission-table-container">
							  		<table class="table table-condensed table-borderless" id="entity-class-group-permission-table">
							  			<thead>
							  				<tr>
							  					<th>Entity Class</th>
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
					    <div class="tab-pane" id="entity-class-user-permission-manager">
							<form class="form-horizontal" id="entity-class-user-permission-form" method="post" action="${context_url}/update/entityclass/user">
								<label class="control-label" for="entity-class-user-select">Select User:</label>
				    			<div class="controls">
									<select name="userId" id="entity-class-user-select">
								<#list users as user>
										<option value="${user.id?c}"<#if user_index == 0> selected</#if>>${user.username}</option>
								</#list>
									</select>
								</div>
								<div class="permission-table-container">
							  		<table class="table table-condensed table-borderless" id="entity-class-user-permission-table">
							  			<thead>
							  				<tr>
							  					<th>Entity Class</th>
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