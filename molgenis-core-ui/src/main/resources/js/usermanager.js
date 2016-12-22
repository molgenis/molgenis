(function ($, molgenis) {
    "use strict";

    var self = molgenis.usermanager = molgenis.usermanager || {};
    var api = new molgenis.RestClient();

    /**
     * @memberOf molgenis.usermanager
     */
    function setViewState(viewState) {
        // viewState: "users" | "groups"
        $.ajax({
            type: 'PUT',
            url: molgenis.getContextUrl() + '/setViewState/' + viewState,
        });
    }

    /**
     * @memberOf molgenis.usermanager
     */
    function getCreateForm(type) {
        React.render(molgenis.ui.Form({
            mode: 'create',
            entity: 'sys' + molgenis.packageSeparator + 'sec' + molgenis.packageSeparator + type,
            modal: true,
            onSubmitSuccess: function (e) {

                //Put user in 'All users' group if not SuperUser
                api.getAsync(e.location, null, function (user) {
                    if (user.superuser === false) {
                        addUserToAllUsersGroup(api.getPrimaryKeyFromHref(e.location), function () {
                            location.reload();
                        });
                    } else {
                        location.reload();
                    }
                });
            }
        }), $('<div>')[0]);
    }

    /**
     * @memberOf molgenis.usermanager
     */
    function getEditForm(id, type) {
        React.render(molgenis.ui.Form({
            entity: 'sys_sec_' + type,
            entityInstance: id,
            mode: 'edit',
            modal: true,
            onSubmitSuccess: function () {
                location.reload();
            }
        }), $('<div>')[0]);
    }

    /**
     * @memberOf molgenis.usermanager
     */
    function setActivation(type, id, checkbox) {
        // type: "user" | "group"
        var active = checkbox.checked;
        $.ajax({
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            type: 'PUT',
            dataType: 'json',
            data: JSON.stringify({
                type: type,
                id: id,
                active: active
            }),
            url: molgenis.getContextUrl() + '/activation',
            success: function (data) {
                var styleClass = data.success ? 'success' : 'warning'
                if (data.type === "group") {
                    $('#groupRow' + data.id).addClass(styleClass);
                    setTimeout(function () {
                        $('#groupRow' + data.id).removeClass('success');
                    }, 1000);
                }

                if (data.type === "user") {
                    $('#userRow' + data.id).addClass(styleClass);
                    setTimeout(function () {
                        $('#userRow' + data.id).removeClass('success');
                    }, 1000);
                }
            }
        });
    }

    /**
     * @memberOf molgenis.usermanager
     */
    function changeGroupMembership(userId, groupId, checkbox) {
        var member = checkbox.checked;
        $.ajax({
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            type: 'PUT',
            dataType: 'json',
            url: molgenis.getContextUrl() + '/changeGroupMembership',
            data: JSON.stringify({
                userId: userId,
                groupId: groupId,
                member: member
            }),
            success: function (data) {
                var styleClass = data.success ? 'success' : 'warning'
                $('#userRow' + data.userId).addClass(styleClass);
                setTimeout(function () {
                    $('#userRow' + data.userId).removeClass(styleClass);
                }, 1000);
            }
        });
    }

    function addUserToAllUsersGroup(userId, callback) {
        getAllUsersGroup(function (groupId) {
            if (groupId === null) {
                callback();
            } else {
                $.ajax({
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    type: 'PUT',
                    dataType: 'json',
                    url: molgenis.getContextUrl() + '/changeGroupMembership',
                    data: JSON.stringify({
                        userId: userId,
                        groupId: groupId,
                        member: true
                    }),
                    success: function () {
                        callback();
                    }
                });
            }
        });
    }

    function getAllUsersGroup(callback) {
        api.getAsync('/api/v1/sys' + molgenis.packageSeparator + 'sec' + molgenis.packageSeparator + 'Group', {
            q: [{
                field: 'name',
                operator: 'EQUALS',
                value: 'All Users'
            }]
        }, function (result) {
            var groupId = null;
            if (result.total > 0) {
                groupId = result.items[0].id;
            }
            callback(groupId);
        });
    }

    $(function () {

        $('#usersTab a').click(function (e) {
            setViewState('users');
        });

        $('#groupsTab a').click(function (e) {
            setViewState('groups');
        });

        $('#create-user-btn').click(function (e) {
            e.preventDefault();
            getCreateForm('User');
        });

        $('#create-group-btn').click(function (e) {
            e.preventDefault();
            getCreateForm('Group');
        });

        $('.edit-user-btn').click(function (e) {
            e.preventDefault();
            getEditForm($(this).data('id'), 'User');
        });

        $('.edit-group-btn').click(function (e) {
            e.preventDefault();
            getEditForm($(this).data('id'), 'Group');
        });

        $('.activate-user-checkbox').click(function (e) {
            setActivation('user', $(this).data('id'), this);
        });

        $('.change-group-membership-checkbox').click(function (e) {
            changeGroupMembership($(this).data('uid'), $(this).data('gid'), this);
        });

        $('.activate-group-checkbox').click(function (e) {
            setActivation('group', $(this).data('id'), this);
        });
    });
}($, window.top.molgenis = window.top.molgenis || {}));