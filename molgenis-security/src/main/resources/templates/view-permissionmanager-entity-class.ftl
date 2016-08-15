<div class="well">
    <ul class="nav nav-pills">
        <li class="active"><a href="#entity-class-group-permission-manager" data-toggle="tab">Groups</a></li>
        <li><a href="#entity-class-user-permission-manager" data-toggle="tab">Users</a></li>
    </ul>
    <div class="tab-content">
        <div class="tab-pane active" id="entity-class-group-permission-manager">
            <form class="form-horizontal" id="entity-class-group-permission-form" method="post"
                  action="${context_url?html}/update/entityclass/group" role="form">
                <div class="form-group">
                    <label class="col-md-3 control-label" for="entity-class-group-select">Select Group:</label>
                    <div class="col-md-4">
                        <select class="form-control" name="groupId" id="entity-class-group-select">
                        <#list groups as group>
                            <option value="${group.id?html}"<#if group_index == 0>
                                    selected</#if>>${group.name?html}</option>
                        </#list>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <div class="permission-table-container">
                        <table class="table table-condensed table-borderless" id="entity-class-group-permission-table">
                            <thead>
                            <tr>
                                <th>Entity Class</th>
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
        <div class="tab-pane" id="entity-class-user-permission-manager">
            <form class="form-horizontal" id="entity-class-user-permission-form" method="post"
                  action="${context_url?html}/update/entityclass/user" role="form">
                <div class="form-group">
                    <label class="col-md-3 control-label" for="entity-class-user-select">Select User:</label>
                    <div class="col-md-4">
                        <select class="form-control" name="userId" id="entity-class-user-select">
                        <#list users as user>
                            <option value="${user.id?html}"<#if user_index == 0>
                                    selected</#if>>${user.username?html}</option>
                        </#list>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <div class="permission-table-container">
                        <table class="table table-condensed table-borderless" id="entity-class-user-permission-table">
                            <thead>
                            <tr>
                                <th>Entity Class</th>
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
</div>