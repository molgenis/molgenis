$(function () {
    var $job = $('#gavin-job');

    if ($job.length) {
        React.render(molgenis.ui.jobs.JobContainer({
            jobHref: molgenis.getContextUrl() + '/job/' + $job.data('execution-id'),
            refreshTimeoutMillis: 20000,
            onCompletion: function () {
                location.reload();
            }
        }), $job[0]);
    }
});