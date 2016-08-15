(function ($, molgenis) {
    "use strict";

    $(function () {
        React.render(molgenis.ui.FileIngestPlugin({}), document.getElementById('file-ingest-plugin'));
    });

}($, window.top.molgenis = window.top.molgenis || {}));
