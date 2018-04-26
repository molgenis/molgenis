<#macro listTasks>
<div class="row">
    <div class="col-md-offset-1 col-md-10">
        <div class="row" style="min-height:300px;">
            <div id="job-container"></div>
        </div>
        <div class="row" style="min-height:100px;">
            <div class="col-md-12">
                <button id="new-task-button" type="button" class="btn btn-primary">New task</button>
            </div>
        </div>
    </div>
</div>
<script>
    $(document).ready(function () {
        var customColumns = [{
            th: 'Job name', td: function (job) {
                return job.name
            }
        }, {
            th: 'Ontology url', td: function (job) {
                return job.ontologyIri
            }
        }, {
            th: 'Delete', td: function (job) {
                return job.deleteUrl && molgenis.ui.ConfirmClick({
                                    confirmMessage: 'Are you sure you want to delete this SORTA task?',
                                    onClick: function () {
                                        $.post(job.deleteUrl).done(
                                                function () {
                                                    molgenis.createAlert([{message: 'Task successfully deleted.'}], 'success');
                                                }
                                        );
                                    }
                                }, molgenis.ui.Button({
                                    icon: 'trash',
                                    size: 'xsmall',
                                    style: 'danger'
                                })
                        )
            }
        }];

        React.render(molgenis.ui.jobs.JobsContainer({
                    url: molgenis.getContextUrl() + '/jobs'
                }, molgenis.ui.jobs.Jobs({},
                molgenis.ui.jobs.JobTable({customColumns: customColumns}))),
                $('#job-container')[0]);

        $('#new-task-button').click(function () {
            $('#ontology-match').attr({
                'action': molgenis.getContextUrl() + '/newtask',
                'method': 'GET'
            }).submit();
        });
        $.each($('.retrieve-button-class'), function (index, button) {
            $(button).click(function () {
                $('#ontology-match').attr({
                    'action': molgenis.getContextUrl() + '/result/' + $(this).parents('tr:eq(0)').children('td:eq(0)').html(),
                    'method': 'GET'
                }).submit();
            });
        });
    });
</script>
</#macro>