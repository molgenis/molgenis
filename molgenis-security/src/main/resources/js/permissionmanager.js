(function ($, molgenis) {
    "use strict";

    $(function () {
        function createGroupPermissionTable(data) {
            var items = [];
            $.each(data.entityIds, function (entityId, entityTypeId) {
                if (data.groupPermissionMap && data.groupPermissionMap[entityId]) {
                    $.each(data.groupPermissionMap[entityId], function (idx, perm) {
                        items.push('<tr>');
                        items.push('<td>' + (idx == 0 ? entityTypeId : '') + '</td>');
                        items.push('<td><input type="radio" name="radio-' + entityId + '" value="writemeta"' + (perm.type === "writemeta" ? ' checked' : '') + '></td>');
                        items.push('<td><input type="radio" name="radio-' + entityId + '" value="write"' + (perm.type === "write" ? ' checked' : '') + '></td>');
                        items.push('<td><input type="radio" name="radio-' + entityId + '" value="read"' + (perm.type === "read" ? ' checked' : '') + '></td>');
                        items.push('<td><input type="radio" name="radio-' + entityId + '" value="count"' + (perm.type === "count" ? ' checked' : '') + '></td>');
                        items.push('<td><input type="radio" name="radio-' + entityId + '" value="none"' + (perm.type ? '' : ' checked') + '></td>');
                        items.push('</tr>');
                    });
                } else {
                    items.push('<tr>');
                    items.push('<td>' + entityTypeId + '</td>');
                    items.push('<td><input type="radio" name="radio-' + entityId + '" value="writemeta"></td>');
                    items.push('<td><input type="radio" name="radio-' + entityId + '" value="write"></td>');
                    items.push('<td><input type="radio" name="radio-' + entityId + '" value="read"></td>');
                    items.push('<td><input type="radio" name="radio-' + entityId + '" value="count"></td>');
                    items.push('<td><input type="radio" name="radio-' + entityId + '" value="none" checked></td>');
                    items.push('</tr>');
                }
                if (data.hierarchyPermissionMap && data.hierarchyPermissionMap[entityId]) {
                    $.each(data.hierarchyPermissionMap[entityId], function (idx, perm) {
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

        function createUserPermissionTable(data) {
            var items = [];
            $.each(data.entityIds, function (entityId, entityTypeId) {
                if (data.userPermissionMap && data.userPermissionMap[entityId]) {
                    $.each(data.userPermissionMap[entityId], function (idx, perm) {
                        items.push('<tr>');
                        items.push('<td>' + (idx == 0 ? entityTypeId : '') + '</td>');
                        items.push('<td><input type="radio" name="radio-' + entityId + '" value="writemeta"' + (perm.type === "writemeta" ? ' checked' : '') + '></td>');
                        items.push('<td><input type="radio" name="radio-' + entityId + '" value="write"' + (perm.type === "write" ? ' checked' : '') + '></td>');
                        items.push('<td><input type="radio" name="radio-' + entityId + '" value="read"' + (perm.type === "read" ? ' checked' : '') + '></td>');
                        items.push('<td><input type="radio" name="radio-' + entityId + '" value="count"' + (perm.type === "count" ? ' checked' : '') + '></td>');
                        items.push('<td><input type="radio" name="radio-' + entityId + '" value="none"' + (perm.type ? '' : ' checked') + '></td>');
                    });
                } else {
                    items.push('<tr>');
                    items.push('<td>' + entityTypeId + '</td>');
                    items.push('<td><input type="radio" name="radio-' + entityId + '" value="writemeta"></td>');
                    items.push('<td><input type="radio" name="radio-' + entityId + '" value="write"></td>');
                    items.push('<td><input type="radio" name="radio-' + entityId + '" value="read"></td>');
                    items.push('<td><input type="radio" name="radio-' + entityId + '" value="count"></td>');
                    items.push('<td><input type="radio" name="radio-' + entityId + '" value="none" checked></td>');
                    items.push('</tr>');
                }
                if (data.hierarchyPermissionMap && data.hierarchyPermissionMap[entityId]) {
                    $.each(data.hierarchyPermissionMap[entityId], function (idx, perm) {
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
                if (data.groupPermissionMap && data.groupPermissionMap[entityId]) {
                    $.each(data.groupPermissionMap[entityId], function (idx, perm) {
                        items.push('<tr>');
                        items.push('<td><span class="muted inherited-permission">inherited from group: ' + perm.group + '</span></td>');
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

        $('#plugin-group-select').change(function () {
            $.get(molgenis.getContextUrl() + '/plugin/group/' + $(this).val(), function (data) {
                $('#plugin-group-permission-table tbody').empty().html(createGroupPermissionTable(data));
            });

        });
        $('#plugin-user-select').change(function () {
            $.get(molgenis.getContextUrl() + '/plugin/user/' + $(this).val(), function (data) {
                $('#plugin-user-permission-table tbody').empty().html(createUserPermissionTable(data));
            });
        });
        $('#entity-class-group-select').change(function () {
            $.get(molgenis.getContextUrl() + '/entityclass/group/' + $(this).val(), function (data) {
                $('#entity-class-group-permission-table tbody').empty().html(createGroupPermissionTable(data));
            });
        });
        $('#entity-class-user-select').change(function () {
            $.get(molgenis.getContextUrl() + '/entityclass/user/' + $(this).val(), function (data) {
                $('#entity-class-user-permission-table tbody').empty().html(createUserPermissionTable(data));
            });
        });

        $('#plugin-group-permission-form,#plugin-user-permission-form,#entity-class-group-permission-form,#entity-class-user-permission-form').submit(function (e) {
            e.preventDefault();
            $.ajax({
                type: $(this).attr('method'),
                url: $(this).attr('action'),
                data: $(this).serialize(),
                success: function (data) {
                    $('#plugin-container .alert').remove();
                    $('#plugin-container').prepend('<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Success!</strong> Updated plugin permissions</div>');

                    //Refresh
                    $('#plugin-group-select').change();
                    $('#entity-class-group-select').change();
                    $('#plugin-user-select').change();
                    $('#entity-class-user-select').change();
                },
                error: function (xhr, textStatus, errorThrown) {
                    var errorMessage = JSON.parse(xhr.responseText).errorMessage;
                    $('#plugin-container .alert').remove();
                    $('#plugin-container').prepend('<div class="alert alert-danger"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Error!</strong> ' + errorMessage + '</div>');
                }
            });
        });

        $('a[data-toggle="tab"][href="#plugin-group-permission-manager"]').on('show.bs.tab', function (e) {
            $('#plugin-group-select').change();
        });
        $('a[data-toggle="tab"][href="#plugin-user-permission-manager"]').on('show.bs.tab', function (e) {
            $('#plugin-user-select').change();
        });
        $('a[data-toggle="tab"][href="#entity-class-group-permission-manager"]').on('show.bs.tab', function (e) {
            $('#entity-class-group-select').change();
        });
        $('a[data-toggle="tab"][href="#entity-class-user-permission-manager"]').on('show.bs.tab', function (e) {
            $('#entity-class-user-select').change();
        });

        $('#plugin-group-select').change();
        $('#entity-class-group-select').change();
    });
}($, window.top.molgenis = window.top.molgenis || {}));