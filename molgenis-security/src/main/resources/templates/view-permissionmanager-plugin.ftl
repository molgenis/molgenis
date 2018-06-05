<div class="well">
    <ul class="nav nav-pills" role="tablist">
        <li class="active"><a href="#plugin-role-permission-manager" role="tab" data-toggle="tab">Roles</a></li>
        <li><a href="#plugin-user-permission-manager" role="tab" data-toggle="tab">Users</a></li>
    </ul>
    <div class="tab-content">
        <div class="tab-pane active" id="plugin-role-permission-manager">
            <form class="form-horizontal" id="plugin-role-permission-form" method="post"
                  action="${context_url?html}/update/plugin/role" role="form">
                <div class="form-group">
                    <label class="col-md-3 control-label" for="plugin-role-select">Select Role:</label>
                    <div class="col-md-4">
                        <select class="form-control" name="rolename" id="plugin-role-select">
                        <#list roles as role>
                            <option value="${role.name?html}"<#if role_index == 0>
                                    selected</#if>>${role.name?html}</option>
                        </#list>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <div class="permission-table-container">
                        <table class="table table-condensed table-borderless" id="plugin-role-permission-table">
                            <thead>
                            <tr>
                                <th>Plugin</th>
                                <th>View</th>
                                <th>None</th>
                            </tr>
                            </thead>
                            <tbody>
                            </tbody>
                        </table>
                    </div>
                </div>
                <div class="form-group">
                    <button type="submit" class="btn btn-default pull-right">Save</button>
                </div>
            </form>
        </div>
        <div class="tab-pane" id="plugin-user-permission-manager">
            <form class="form-horizontal" id="plugin-user-permission-form" method="post"
                  action="${context_url?html}/update/plugin/user" role="form">
                <div class="form-group">
                    <label class="col-md-3 control-label" for="plugin-user-select">Select User:</label>
                    <div class="col-md-4">
                        <select class="form-control" name="username" id="plugin-user-select">
                        <#list users as user>
                            <option value="${user.username?html}"<#if user_index == 0>
                                    selected</#if>>${user.username?html}</option>
                        </#list>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <div class="permission-table-container">
                        <table class="table table-condensed table-borderless" id="plugin-user-permission-table">
                            <thead>
                            <tr>
                                <th>Plugin</th>
                                <th>View</th>
                                <th>None</th>
                            </tr>
                            </thead>
                            <tbody>
                            </tbody>
                        </table>
                    </div>
                </div>
                <div class="form-group">
                    <button type="submit" class="btn pull-right">Save</button>
                </div>
            </form>
        </div>
    </div>
</div>