var i18nDeferred = $.Deferred()
window.top.molgenis.I18nStrings(
    function (i18n) {
        i18nDeferred.resolve(i18n)
    }
)
$.when($,
    window.top.molgenis,
    i18nDeferred.promise()
).then(
    function ($, molgenis, i18n) {
        'use strict'

        molgenis.dataexplorer = molgenis.dataexplorer || {}
        var restApi = new molgenis.RestClient()

    /**
     * Retrieves the collectionID and biobankID of all entities remaining after filtering
     * Sends request to server, which posts to the negotiator
     */
    function sendNegotiatorRequest () {
        var entityTypeId = molgenis.dataexplorer.getSelectedEntityMeta().name
        var searchQuery = molgenis.dataexplorer.getSearchQuery()
        var filter = molgenis.dataexplorer.getRSQL()

        var rsqlParts = []
        var humanReadableParts = []
        if (searchQuery) {
            rsqlParts.push('*=q=' + molgenis.rsql.toRsqlValue(searchQuery))
            humanReadableParts.push('Free text search contains ' + searchQuery)
        }
        if (filter) {
            rsqlParts.push(filter)
            humanReadableParts.push(molgenis.rsql.getHumanReadable(filter))
        }
        var rsql = molgenis.rsql.encodeRsqlValue(rsqlParts.join(';'))
        var humanReadable = humanReadableParts.join(' and ')

        // Remove the nToken from the URL to prevent duplication on the negotiator side
        // when a query is edited more than once
        var url = window.location.href.replace(/&nToken=\w{32}/, '')
        var request = {
            URL: url,
            entityId: entityTypeId,
            rsql: rsql,
            humanReadable: humanReadable,
            nToken: molgenis.dataexplorer.getnToken()
        }

        $.ajax({
            method: 'POST',
            dataType: 'json',
            url: '/plugin/directory/export',
            data: JSON.stringify(request),
            contentType: 'application/json',
            success: function (response) {
                if (response.success) {
                    window.location.href = response.redirectUrl
                } else {
                    molgenis.createAlert([{message: response.warning}], 'warning')
                }
            }
        })
    }


    $(function () {
        $('#directory-export-button').on('click', function () {
            if (!molgenis.dataexplorer.getRSQL() && !molgenis.dataexplorer.getSearchQuery()) {
                // no filters selected yet
                bootbox.alert(i18n.dataexplorer_directory_export_no_filters)
                return
            }

            bootbox.confirm({
                title: i18n.dataexplorer_directory_export_dialog_title,
                message: i18n.dataexplorer_directory_export_dialog_message,
                buttons: {
                    confirm: {
                        label: i18n.dataexplorer_directory_export_dialog_yes
                    },
                    cancel: {
                        label: i18n.dataexplorer_directory_export_dialog_no
                    }
                },
                callback: function (result) {
                    if (result) {
                        sendNegotiatorRequest()
                    }
                }
            })
        })
    })
    })
