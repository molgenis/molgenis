var i18nDeferred = $.Deferred();
window.top.molgenis.I18nStrings(
    function(i18n){
        i18nDeferred.resolve(i18n);
    }
);
$.when($,
    window.top.molgenis,
    i18nDeferred.promise()
).then(
    function ($, molgenis, i18n) {
    "use strict";

    molgenis.dataexplorer = molgenis.dataexplorer || {};
    var restApi = new molgenis.RestClient();

    /**
     * Retrieves the collectionID and biobankID of all entities remaining after filtering
     * Sends request to server, which posts to the negotiator
     */
    function sendNegotiatorRequest() {
        var entityName = molgenis.dataexplorer.getSelectedEntityMeta().name
        var rsql = molgenis.dataexplorer.getRSQL()
        var uri = '/api/v2/' + entityName + '?q=' + rsql

        var collections = []

        restApi.getAsync(uri).then(function (response) {
            var collectionId, biobank, biobankId
            $.each(response.items, function () {
                var item = this

                collectionId = item.id
                biobank = item.biobank
                biobankId = biobank ? biobank.id : null

                collections.push({
                    collectionId: collectionId,
                    biobankId: biobankId
                })
            })

            if (collections.length === 0) {
                molgenis.createAlert([{message: 'Please make sure your filters result in at least 1 row'}], 'warning');
            } else {
                // Remove the nToken from the URL to prevent duplication on the negotiator side
                // when a query is edited more than once
                var url = window.location.href.replace(/&nToken=\w{32}/, '')

                var request = {
                    URL: url,
                    collections: collections,
                    humanReadable: molgenis.rsql.getHumanReadable(rsql),
                    nToken: molgenis.dataexplorer.getnToken()
                }

                $.ajax({
                    method: 'POST',
                    dataType: 'json',
                    url: '/plugin/directory/export',
                    data: JSON.stringify(request),
                    contentType: 'application/json',
                    success: function (response) {
                        window.location.href = response
                    }
                })
            }
        })
    }

    $(function () {
        $('#directory-export-button').on('click', function () {
            if( !molgenis.dataexplorer.getRSQL() ) {
                // no filters selected yet
                bootbox.alert(i18n.dataexplorer_directory_export_no_filters);
                return;
            }

            bootbox.confirm({
                title:  i18n.dataexplorer_directory_export_dialog_title,
                message:  i18n.dataexplorer_directory_export_dialog_message,
                buttons: {
                    confirm: {
                        label:  i18n.dataexplorer_directory_export_dialog_yes
                    },
                    cancel: {
                        label: i18n.dataexplorer_directory_export_dialog_no
                    }
                },
                callback: function (result) {
                    if (result) {
                        sendNegotiatorRequest();
                    }
                }
            })
        })
    })
});
