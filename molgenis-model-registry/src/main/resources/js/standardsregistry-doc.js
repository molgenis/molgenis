(function ($, molgenis) {
    "use strict";

    $(function () {
        $(document).on('click', '#print-doc-btn', function () {
            window.print();
        });
    });

}($, window.top.molgenis = window.top.molgenis || {}));
