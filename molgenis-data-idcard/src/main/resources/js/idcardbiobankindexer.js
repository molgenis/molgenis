(function ($, molgenis) {
    "use strict";

    var IdcardBiobankIndexerComponent;

    $(function () {
        var onButtonClick = function () {
            $.post(molgenis.getContextUrl() + '/reindex').done(function (job) {
                updateJobStatus(job);
            });
        }

        IdcardBiobankIndexerComponent = React.render(molgenis.ui.IdcardBiobankIndexerComponent({
            biobankEntity: 'rdconnect_regbb',
            indexEntity: 'sys_idc_IdCardIndexingEvent',
            buttonDisabled: false,
            onButtonClick: onButtonClick
        }), $('#idCardBiobankIndexer-container')[0]);
    });

    function updateJobStatus(job) {
        if (job.triggerStatus === 'NONE' || job.triggerStatus === 'SUCCESS' || job.triggerStatus === 'ERROR') {
            IdcardBiobankIndexerComponent.setProps({buttonDisabled: false});
            molgenis.createAlert([{'message': 'Reindexing ID-Card completed'}], 'success');
        }
        else {
            IdcardBiobankIndexerComponent.setProps({buttonDisabled: true});
            molgenis.createAlert([{'message': 'Reindexing ID-Card biobanks in progress ...'}], 'info');
            setTimeout(function () {
                $.get(molgenis.getContextUrl() + '/status/' + job.triggerGroup + '/' + job.triggerName).done(function (job) {
                    updateJobStatus(job);
                });
            }, 1000);
        }
    }

}($, window.top.molgenis = window.top.molgenis || {}));