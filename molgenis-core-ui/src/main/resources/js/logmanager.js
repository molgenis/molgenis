(function ($, molgenis) {
    "use strict";

    function renderLoggerTable() {
        window.location = molgenis.getContextUrl();
    }

    $(function () {
        var container = $("#plugin-container");

        $('.log-level-select', container).select2();

        $(container).on('change', '.log-level-select', function (e) {
            var logger = $(this).closest('tr').data('logger');
            var level = $(this).val();
            $.post(molgenis.getContextUrl() + '/logger/' + logger + '/' + level);
        })

        $(container).on('click', '#create-logger-btn', function () {
            $.post(molgenis.getContextUrl() + '/loggers/reset', function () {
                window.location = molgenis.getContextUrl();
            });
        });

        $(container).on('click', '#reset-loggers-btn', function () {
            $.post(molgenis.getContextUrl() + '/loggers/reset').success(function () {
                renderLoggerTable();
            });
        });

        $('form[name="create-logger-form"]').submit(function (e) {
            e.preventDefault();
            if ($(this).valid()) {
                var name = $('#logger-name').val();
                $.post(molgenis.getContextUrl() + '/logger/' + name + '/DEBUG').success(function () {
                    renderLoggerTable();
                });
            }
        });
    });
}($, window.top.molgenis = window.top.molgenis || {}));