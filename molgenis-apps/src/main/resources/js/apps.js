$(function () {
    $('#create-app-btn').on('click', function () {
        React.render(molgenis.ui.Form({
            entity: 'sys_App',
            modal: true,
            mode: 'create',
            onSubmitSuccess: function () {
                location.reload();
            }
        }), $('#create-app-form')[0]).setState({showModal: true});
    });

    $('.edit-app-btn').on('click', function () {
        var appName = $(this).data('appname')
        React.render(molgenis.ui.Form({
            entity: 'sys_App',
            entityInstance: appName,
            modal: true,
            mode: 'edit',
            onSubmitSuccess: function () {
                location.reload();
            }
        }), $('#create-app-form')[0]).setState({showModal: true});
    });

    $('.activate-app-btn').on('click', function () {
        var appName = $(this).data('appname')
        $.post(molgenis.contextUrl + "/" + appName + "/activate").then(function () {
            location.reload()
        })
    });

    $('.deactivate-app-btn').on('click', function () {
        var appName = $(this).data('appname')
        $.post(molgenis.contextUrl + "/" + appName + "/deactivate").then(function () {
            location.reload()
        })
    });
})