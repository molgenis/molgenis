(function ($, molgenis) {
    "use strict";

    function getCreateForm(type) {
        React.render(molgenis.ui.Form({
            mode: 'create',
            entity: 'sys' + molgenis.packageSeparator + 'sec' + molgenis.packageSeparator + type,
            modal: true,
            onSubmitSuccess: function (e) {
                location.reload()
            }
        }), $('<div>')[0]);
    }

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

    function setActivation (id, checkbox) {
        var active = checkbox.checked;
        $.ajax({
            type: 'post',
            dataType: 'json',
            data: {
                id: id,
                active: active
            },
            url: molgenis.getContextUrl() + '/activation',
            success: function (data) {
                var styleClass = data.success ? 'success' : 'warning'
                $('#userRow' + data.id).addClass(styleClass)
                setTimeout(function () {
                    $('#userRow' + data.id).removeClass(styleClass)
                }, 1000)
            }
        });
    }

    $(function () {
        $('#create-user-btn').click(function (e) {
            e.preventDefault();
            getCreateForm('User');
        });

        $('.edit-user-btn').click(function (e) {
            e.preventDefault();
            getEditForm($(this).data('id'), 'User');
        });

        $('.activate-user-checkbox').click(function (e) {
            setActivation($(this).data('id'), this)
        });
    });
}($, window.top.molgenis = window.top.molgenis || {}));