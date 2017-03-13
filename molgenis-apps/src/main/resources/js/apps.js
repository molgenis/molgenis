(function ($, molgenis) {
    'use strict';

    $(function () {
        $('#create-app-btn').on('click', function () {
            React.render(molgenis.ui.Form({
                entity: 'sys_App',
                modal: true,
                mode: 'create',
                onSubmitSuccess: function () {
                    window.location = molgenis.contextUrl;
                }
            }), $('#form-modal-container')[0]).setState({showModal: true});
        });

        $('.edit-app-btn').on('click', function () {
            var appId = $(this).data('app-id')
            React.render(molgenis.ui.Form({
                entity: 'sys_App',
                entityInstance: appId,
                modal: true,
                mode: 'edit',
                onSubmitSuccess: function () {
                    window.location = molgenis.contextUrl;
                }
            }), $('#form-modal-container')[0]).setState({showModal: true});
        });

        $('.activate-app-btn').on('click', function () {
            var appId = $(this).data('app-id')
            $.post(molgenis.contextUrl + '/' + appId + '/activate').then(function () {
                window.location = molgenis.contextUrl;
            })
        });

        $('.deactivate-app-btn').on('click', function () {
            var appId = $(this).data('app-id')
            $.post(molgenis.contextUrl + '/' + appId + '/deactivate').then(function () {
                window.location = molgenis.contextUrl;
            })
        });
    })
}($, window.top.molgenis = window.top.molgenis || {}));