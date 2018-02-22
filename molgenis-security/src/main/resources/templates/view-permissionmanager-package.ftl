<div class="well">
    <ul class="nav nav-pills" role="tablist">
        <li class="active"><a href="#package-group-permission-manager" role="tab" data-toggle="tab">Groups</a></li>
        <li><a href="#package-user-permission-manager" role="tab" data-toggle="tab">Users</a></li>
        <div class="tab-content">
            <div class="tab-pane active" id="package-group-permission-manager">
                <form class="form-horizontal" id="package-group-permission-form" method="post"
                      action="${context_url?html}/update/package/group" role="form">
                    <div class="form-group">
                        <label class="col-md-3 control-label" for="package-group-select">Select Group:</label>
                        <div class="col-md-4">
                            <select class="form-control" name="groupId" id="package-group-select">
                        <#list groups as group>
                            <option value="${group.id?html}"<#if group_index == 0>
                                    selected</#if>>${group.name?html}</option>
                        </#list>
                            </select>
                        </div>
                    </div>
                    <span>Package permissions determine the permission on entity types within the package and its child packages.<br/>
                    They do <b>not</b> effect permissions on the package metadata itself.</span>
                    <div class="form-group">
                        <div class="permission-table-container">
                            <table class="table table-condensed table-borderless" id="package-group-permission-table">
                                <thead>
                                <tr>
                                    <th>Package</th>
                                    <th>Edit metadata</th>
                                    <th>Edit</th>
                                    <th>View</th>
                                    <th>Count</th>
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
                            <select class="form-control" name="userId" id="package-user-select">
                        <#list users as user>
                            <option value="${user.id?html}"<#if user_index == 0>
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
    </ul>
</div>