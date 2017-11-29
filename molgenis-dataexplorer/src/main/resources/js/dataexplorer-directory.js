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

    /**
     * Creates a negotiator request object
     */
    function createNegotiatorRequest () {
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
      return {
        URL: url,
        entityId: entityTypeId,
        rsql: rsql,
        humanReadable: humanReadable,
        nToken: molgenis.dataexplorer.getnToken()
      }
    }

    /**
     * Retrieves the collectionID and biobankID of all entities remaining after filtering
     * Sends request to server, which posts to the negotiator
     */
    function sendNegotiatorRequest (request) {
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

    /**
     * Validates the negotiator request
     */
    function validateNegotiatorRequest () {
      var request = createNegotiatorRequest()

      $.ajax({
        method: 'POST',
        dataType: 'json',
        url: '/plugin/directory/validate',
        data: JSON.stringify(request),
        contentType: 'application/json',
        success: function (response) {
          if (response.isValid) {
            if (!response.message) {
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
                    sendNegotiatorRequest(request)
                  }
                }
              })
            } else {
                var negotiatorModal = $('#negotiator-modal')

                // on show modal; fill the template and add event listeners
                negotiatorModal.on('show.bs.modal', function (event) {
                    var modal = $(this)
                    modal.find('#negotiator-message').text(response.message)

                    var enabledList = modal.find('#enabled-collections-list')
                    var disabledList = modal.find('#disabled-collections-list')

                    $.each(response.enabledCollections, function (index, label) {
                        enabledList.append($('<li>').text(label))
                    })

                    $.each(response.disabledCollections, function (index, label) {
                        disabledList.append($('<li>').text(label))
                    })

                    modal.find('#negotiator-apply-btn').click(function() {
                        sendNegotiatorRequest(request)
                    })
                })

                // on hide modal; clear the template and remove the event listeners
                negotiatorModal.on('hidden.bs.modal', function (event) {
                    var modal = $(this)
                    modal.find('#enabled-collections-list').empty();
                    modal.find('#disabled-collections-list').empty();
                    modal.find('#negotiator-apply-btn').off('click');
                    negotiatorModal.off('show.bs.modal')
                    negotiatorModal.off('hidden.bs.modal')
                })

                negotiatorModal.modal('show')
            }
          } else {
              bootbox.alert({
                  title: i18n.dataexplorer_directory_export_dialog_warning_title,
                  message: response.message
              })
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
        validateNegotiatorRequest()
      })
    })
  })
