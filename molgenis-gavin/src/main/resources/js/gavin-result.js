$(function () {
    var $job = $('#gavin-job');

    if ($job.length) {
        React.render(molgenis.ui.jobs.JobContainer({
            jobHref : '/api/v2/sys_idx_GavinJobExecution/' + $job.data('execution-id'),
            onCompletion : function () {
                location.reload();
            }
        }), $job[0]);
    }
});