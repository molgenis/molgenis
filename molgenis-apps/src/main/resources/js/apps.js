$(function() {
    $('#create-app-btn').on('click', function () {
        React.render(molgenis.ui.Form({
            entity: 'sys_App',
            modal: true,
            mode: 'create',
            onSubmitSuccess: function () {
                location.reload();
            }
        }), $('#create-app-form')[0]).setState({ showModal : true});
    });

    $('.activate-app-btn').on('click', function (){
        var appname = $(this).data('appname')
        $.post(molgenis.contextUrl + "/" + appname + "/activate").then(function() {
            location.reload()
        })
    });

    $('.deactivate-app-btn').on('click', function (){
        var appname = $(this).data('appname')
        $.post(molgenis.contextUrl + "/" + appname + "/activate").then(function() {
            location.reload()
        })
    });
})