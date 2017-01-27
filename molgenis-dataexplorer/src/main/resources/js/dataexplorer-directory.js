(function ($, molgenis) {
    "use strict";

    molgenis.dataexplorer = molgenis.dataexplorer || {};
    var restApi = new molgenis.RestClient();

    /**
     * Retrieves the collectionID and biobankID of all entities remaining after filtering
     * Sends request to server, which posts to the negotiator
     */
    function sendNegotiatorRequest() {
        var entityName = molgenis.dataexplorer.getSelectedEntityMeta().name
        var rsql = molgenis.createRsqlQuery(molgenis.dataexplorer.getFilterRules())
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
                // When a query is edited more then once
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
                    url: '/directory/export',
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
            console.log('test')
            bootbox.confirm({
                title: 'Send request to the BBMRI Negotiator?',
                message: "Your current selection of biobanks along with your filtering criteria will be sent to the BBMRI Negotiator. Are you sure?",
                buttons: {
                    confirm: {
                        label: 'Yes, Send to Negotiator'
                    },
                    cancel: {
                        label: 'No, I want to keep filtering'
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
}($, window.top.molgenis = window.top.molgenis || {}));
