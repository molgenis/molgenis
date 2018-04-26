<div class="well">
    <ul class="nav nav-pills" role="tablist">
        <li class="active"><a href="#plugin-group-permission-manager" role="tab" data-toggle="tab">Groups</a></li>
        <li><a href="#plugin-user-permission-manager" role="tab" data-toggle="tab">Users</a></li>
    </ul>
    <div class="tab-content">
        <div class="tab-pane active" id="plugin-group-permission-manager">
            <form class="form-horizontal" id="plugin-group-permission-form" method="post"
                  action="${context_url?html}/update/plugin/group" role="form">
                <div class="form-group">
                    <label class="col-md-3 control-label" for="plugin-group-select">Select Group:</label>
                    <div class="col-md-4">
                        <select class="form-control" name="groupId" id="plugin-group-select">
                        <#list groups as group>
                            <option value="${group.id?html}"<#if group_index == 0>
                                    selected</#if>>${group.name?html}</option>
                        </#list>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <div class="permission-table-container">
                        <table class="table table-condensed table-borderless" id="plugin-group-permission-table">
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
                        <select class="form-control" name="userId" id="plugin-user-select">
                        <#list users as user>
                            <option value="${user.id?html}"<#if user_index == 0>
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