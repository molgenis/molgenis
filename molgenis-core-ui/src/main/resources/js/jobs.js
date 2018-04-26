$(function () {

    React.render(molgenis.ui.jobs.JobsContainer({
            'url': '/plugin/jobs/latest',
        }, molgenis.ui.jobs.Jobs({},
        molgenis.ui.jobs.JobTable({
            customColumns: [
                {
                    th: 'Type', td: function (job) {
                    return job.type
                }
                },
                {
                    th: 'Message', td: function (job) {
                    return job.progressMessage
                }
                }]
        }))),
        $('#job-container')[0]);
});
