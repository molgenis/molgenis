(function ($, molgenis) {
  'use strict'

  var self = molgenis.usermanager = molgenis.usermanager || {}
  var api = new molgenis.RestClient()

  /**
   * @memberOf molgenis.usermanager
   */
  function getCreateForm (type) {
    React.render(molgenis.ui.Form({
      mode: 'create',
      entity: 'sys' + molgenis.packageSeparator + 'sec' + molgenis.packageSeparator + type,
      modal: true,
      onSubmitSuccess: function (e) {

        api.getAsync(e.location, null, function () {
          location.reload()
        })
      }
    }), $('<div>')[0])
  }

  /**
   * @memberOf molgenis.usermanager
   */
  function getEditForm (id, type) {
    React.render(molgenis.ui.Form({
      entity: 'sys_sec_' + type,
      entityInstance: id,
      mode: 'edit',
      modal: true,
      onSubmitSuccess: function () {
        location.reload()
      }
    }), $('<div>')[0])
  }

  /**
   * @memberOf molgenis.usermanager
   */
  function setActivation (type, id, checkbox) {
    var active = checkbox.checked
    $.ajax({
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      type: 'PUT',
      dataType: 'json',
      data: JSON.stringify({
        type: type,
        id: id,
        active: active
      }),
      url: molgenis.getContextUrl() + '/activation',
      success: function (data) {
        var styleClass = data.success ? 'success' : 'warning'

        $('#userRow' + data.id).addClass(styleClass)
        setTimeout(function () {
          $('#userRow' + data.id).removeClass('success')
        }, 1000)
      }
    })
  }

  $(function () {

    $('#create-user-btn').click(function (e) {
      e.preventDefault()
      getCreateForm('User')
    })

    $('.edit-user-btn').click(function (e) {
      e.preventDefault()
      getEditForm($(this).data('id'), 'User')
    })

    $('.activate-user-checkbox').click(function (e) {
      setActivation('user', $(this).data('id'), this)
    })
  })
}($, window.top.molgenis = window.top.molgenis || {}))