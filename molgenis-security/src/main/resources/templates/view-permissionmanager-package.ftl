<div class="well">
    <ul class="nav nav-pills" role="tablist">
        <li class="active"><a href="#package-role-permission-manager" role="tab" data-toggle="tab">Roles</a></li>
        <li><a href="#package-user-permission-manager" role="tab" data-toggle="tab">Users</a></li>
    </ul>
    <div class="tab-content">
        <div class="tab-pane active" id="package-role-permission-manager">
            <form class="form-horizontal" id="package-role-permission-form" method="post"
                  action="${context_url?html}/update/package/role" role="form">
                <div class="form-group">
                    <label class="col-md-3 control-label" for="package-role-select">Select Role:</label>
                    <div class="col-md-4">
                        <select class="form-control" name="rolename" id="package-role-select">
                    <#list roles as role>
                        <option value="${role.name?html}"<#if role_index == 0>
                                selected</#if>>${role.name?html}</option>
                    </#list>
                        </select>
                    </div>
                </div>
                <span>Package permissions determine the permission on entity types within the package and its child packages.<br/>
                Additionally WRITEMETA permission on a package means that the user can create packages and Entity Types in this package and its children.</span>
                They do <b>not</b> effect permissions on the package metadata itself.<br/>
                <div class="form-group">
                    <div class="permission-table-container">
                        <table class="table table-condensed table-borderless" id="package-role-permission-table">
                            <thead>
                            <tr>
                                <th>Package</th>
                                <th>Edit metadata</th>
                                <th>Edit</th>
                                <th>View</th>
                                <th>Count</th>
                                <th>View metadata</th>
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
        <div class="tab-pane" id="package-user-permission-manager">
            <form class="form-horizontal" id="package-user-permission-form" method="post"
                  action="${context_url?html}/update/package/user" role="form">
                <div class="form-group">
                    <label class="col-md-3 control-label" for="package-user-select">Select User:</label>
                    <div class="col-md-4">
                        <select class="form-control" name="username" id="package-user-select">
                    <#list users as user>
                        <option value="${user.username?html}"<#if user_index == 0>
                                selected</#if>>${user.username?html}</option>
                    </#list>
                        </select>
                    </div>
                </div>
                <span>Package permissions determine the permission on entity types within the package and its child packages.<br/>
                They do <b>not</b> effect permissions on the package metadata itself.<br/></span>
                <div class="form-group">
                    <div class="permission-table-container">
                        <table class="table table-condensed table-borderless" id="package-user-permission-table">
                            <thead>
                            <tr>
                                <th>Package</th>
                                <th>Edit metadata</th>
                                <th>Edit</th>
                                <th>View</th>
                                <th>Count</th>
                                <th>View metadata</th>
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