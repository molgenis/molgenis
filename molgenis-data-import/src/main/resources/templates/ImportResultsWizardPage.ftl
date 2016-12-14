<div class="row">
    <div class="col-md-8">
        <form method="post" id="wizardForm" name="wizardForm" action="">
            <div id="message-panel" class="panel panel-primary">
                <div class="panel-heading">
                    <h3 class="panel-title">Importing...</h3>
                </div>

                <div id="message" class="panel-body">
                    <div class="text-center"><img src="/img/waiting-spinner.gif"/></div>
                </div>
            </div>
        </form>
    </div>
</div>
<div class="row">
    <div class="col-md-8">
        <div id="permission-panel" class="hidden panel panel-primary">
            <div class="panel-heading"><h4 class="panel-title">Permissions</h4></div>
            <div class="panel-body">
                <div class="tab-content">
                    <div class="tab-pane active" id="entity-class-group-permission-manager">
                        <form class="form-horizontal" id="entity-class-group-permission-form" method="post"
                              action="${context_url?html}/add/entityclass/group" role="form">
                            <div class="form-group col-md-12 ">
                                <label class="col-md-3 control-label" for="entity-class-group-select">Select
                                    Group:</label>
                                <div class="col-md-4">
                                    <select class="form-control" name="groupId" id="entity-class-group-select">
                                    <#list wizard.groups as group>
                                        <option value="${group.id?html}"<#if group_index == 0>
                                                selected</#if>>${group.name?html}</option>
                                    </#list>
                                    </select>
                                </div>
                            </div>
                            <div class="form-group col-md-12">
                                <div class="permission-table-container">
                                    <table class="table table-condensed table-borderless"
                                           id="entity-class-group-permission-table">
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
                            <div class="form-group col-md-12">
                                <hr></hr>
                                <button type="submit" class="btn btn-primary pull-right">Save</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script type="text/javascript">
        $(function () {
            var restApi = new molgenis.RestClient();
            var timer;
            var importedEntities;

            $('.next').addClass('disabled');
            $('.cancel').addClass('hidden');
            $('.previous').addClass('disabled');

            checkImportResult();

            function checkImportResult() {
                restApi.getAsync('/api/v1/sys' + molgenis.packageSeparator + 'ImportRun/${wizard.importRunId?js_string}', {}, function (importRun) {
                    if (timer) {
                        clearTimeout(timer);
                    }

                    if (importRun.status !== 'RUNNING') {
                        importedEntities = importRun.importedEntities;
                        $('#message-panel').removeClass('panel-info');
                        $('#message-panel').addClass(importRun.status == 'FINISHED' ? 'panel-success' : 'panel-danger');
                        $('#message-panel .panel-heading').text(importRun.status == 'FINISHED' ? 'Import success' : 'Import failed');

                        if (importRun.message !== null) {
                            $('#message').html(importRun.message);
                        }

                        $('.next').removeClass('disabled');
                        var groupcount = "${wizard.groups?size}";
                        var allow = "${wizard.allowPermissions?c}";
                        if (importRun.importedEntities !== undefined) {
                            if (importRun.importedEntities && importRun.importedEntities.length > 0 && groupcount > 0 && allow === "true") {
                                $('#permission-panel').removeClass("hidden");
                                $.ajax({
                                    url: "${context_url?html}/entityclass/group/" + $('#entity-class-group-select').val(),
                                    type: 'GET',
                                    data: {entityIds: importRun.importedEntities},
                                    success: function (data) {
                                        $('.permission-table-container tbody').empty().html(createGroupPermissionTable(data));
                                    }
                                });
                            }
                        }
                    } else {
                        timer = setTimeout(checkImportResult, 3000);
                    }
                });
            }

            function createGroupPermissionTable(data) {
                var items = [];
                $.each(data.entityIds, function (entityId, entityName) {
                    if (data.groupPermissionMap && data.groupPermissionMap[entityId.toLowerCase()]) {
                        $.each(data.groupPermissionMap[entityId.toLowerCase()], function (idx, perm) {
                            items.push('<tr>');
                            items.push('<td>' + (idx == 0 ? entityName : '') + '</td>');
                            items.push('<td><input type="radio" name="radio-' + entityId + '" value="writemeta"' + (perm.type === "writemeta" ? ' checked' : '') + '></td>');
                            items.push('<td><input type="radio" name="radio-' + entityId + '" value="write"' + (perm.type === "write" ? ' checked' : '') + '></td>');
                            items.push('<td><input type="radio" name="radio-' + entityId + '" value="read"' + (perm.type === "read" ? ' checked' : '') + '></td>');
                            items.push('<td><input type="radio" name="radio-' + entityId + '" value="count"' + (perm.type === "count" ? ' checked' : '') + '></td>');
                            items.push('<td><input type="radio" name="radio-' + entityId + '" value="none"' + (perm.type ? '' : ' checked') + '></td>');
                            items.push('</tr>');
                        });
                    } else {
                        items.push('<tr>');
                        items.push('<td>' + entityName + '</td>');
                        items.push('<td><input type="radio" name="radio-' + entityId + '" value="writemeta"></td>');
                        items.push('<td><input type="radio" name="radio-' + entityId + '" value="write"></td>');
                        items.push('<td><input type="radio" name="radio-' + entityId + '" value="read"></td>');
                        items.push('<td><input type="radio" name="radio-' + entityId + '" value="count"></td>');
                        items.push('<td><input type="radio" name="radio-' + entityId + '" value="none" checked></td>');
                        items.push('</tr>');
                    }
                    if (data.hierarchyPermissionMap && data.hierarchyPermissionMap[entityId.toLowerCase()]) {
                        $.each(data.hierarchyPermissionMap[entityId.toLowerCase()], function (idx, perm) {
                            items.push('<tr>');
                            items.push('<td><span class="muted inherited-permission">inherited from hierarchy</span></td>');
                            items.push('<td><input type="radio"' + (perm.type === "writemeta" ? ' checked' : '') + ' disabled></td>');
                            items.push('<td><input type="radio"' + (perm.type === "write" ? ' checked' : '') + ' disabled></td>');
                            items.push('<td><input type="radio"' + (perm.type === "read" ? ' checked' : '') + ' disabled></td>');
                            items.push('<td><input type="radio"' + (perm.type === "count" ? ' checked' : '') + ' disabled></td>');
                            items.push('<td><input type="radio"' + (perm.type ? '' : ' checked') + ' disabled></td>');
                            items.push('</tr>');
                        });
                    }
                });
                return items.join('');
            }

            $('#entity-class-group-permission-form').submit(function (e) {
                e.preventDefault();
                $.ajax({
                    type: $(this).attr('method'),
                    url: $(this).attr('action'),
                    data: $(this).serialize(),
                    success: function (data) {
                        $('#plugin-container .alert').remove();
                        $('#plugin-container').prepend('<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Success!</strong> Updated entity permissions</div>');
                    },
                    error: function (xhr, textStatus, errorThrown) {
                        var errorMessage = JSON.parse(xhr.responseText).errorMessage;
                        $('#plugin-container .alert').remove();
                        $('#plugin-container').prepend('<div class="alert alert-danger"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Error!</strong> ' + errorMessage + '</div>');
                    }
                });
            });

            $('#entity-class-group-select').change(function () {

                $.ajax({
                    type: 'GET',
                    url: molgenis.getContextUrl() + '/entityclass/group/' + $(this).val(),
                    data: {entityIds: importedEntities},
                    success: function (data) {
                        $('.permission-table-container tbody').empty().html(createGroupPermissionTable(data));
                    },
                    error: function (xhr, textStatus, errorThrown) {
                        var errorMessage = JSON.parse(xhr.responseText).errorMessage;
                        $('#plugin-container .alert').remove();
                        $('#plugin-container').prepend('<div class="alert alert-danger"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Error!</strong> ' + errorMessage + '</div>');
                    }
                });
            });

        });
    </script>