(function ($, molgenis) {
    "use strict";

    $(function () {
        $('#reindex-form').submit(function (e) {
            e.preventDefault();
            $.ajax({
                type: $(this).attr('method'),
                url: $(this).attr('action'),
                data: $(this).serialize(),
                success: function (data) {
                    molgenis.createAlert([{'message': 'Reindexing completed.'}], 'success');
                }
            });
        });
    });
}($, window.top.molgenis = window.top.molgenis || {}));