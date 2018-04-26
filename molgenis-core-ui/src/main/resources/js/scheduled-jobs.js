(function ($, molgenis) {
    "use strict";

    $(function () {
        React.render(molgenis.ui.ScheduledJobsPlugin({}), document.getElementById('scheduled-jobs-plugin'));
    });

}($, window.top.molgenis = window.top.molgenis || {}));
