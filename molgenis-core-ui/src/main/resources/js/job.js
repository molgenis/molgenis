$(function () {
    const $jobContainer = $('#job-container');
    const jobHref = $jobContainer.data('href');
    const refreshTimeoutMillis = parseInt($jobContainer.data('timeout'));
    React.render(molgenis.ui.jobs.JobContainer({
        jobHref: jobHref,
        refreshTimeoutMillis: refreshTimeoutMillis
    }), $jobContainer[0]);
});
