(function ($, molgenis) {
  'use strict'

  $(function () {
    function createGroupPermissionTable (data, permissions) {
      var items = []
      $.each(data.entityIds, function (entityId, entityTypeId) {
        if (data.groupPermissionMap && data.groupPermissionMap[entityId]) {
          $.each(data.groupPermissionMap[entityId], function (idx, perm) {
            items.push('<tr>')
            items.push('<td>' + (idx == 0 ? entityTypeId : '') + '</td>')
            for (var i = 0; i < permissions.length; i++) {
              items.push('<td><input type="radio" name="radio-' + entityId + '" value="' + permissions[i] + '"' + (perm.type === permissions[i] ? ' checked' : '') + '></td>')
            }
            items.push('<td><input type="radio" name="radio-' + entityId + '" value="none"' + (perm.type ? '' : ' checked') + '></td>')
            items.push('</tr>')
          })
        } else {
          items.push('<tr>')
          items.push('<td>' + entityTypeId + '</td>')
          for (var i = 0; i < permissions.length; i++) {
            items.push('<td><input type="radio" name="radio-' + entityId + '" value="' + permissions[i] + '"></td>')
          }
          items.push('<td><input type="radio" name="radio-' + entityId + '" value="none" checked></td>')
          items.push('</tr>')
        }
        if (data.hierarchyPermissionMap && data.hierarchyPermissionMap[entityId]) {
          $.each(data.hierarchyPermissionMap[entityId], function (idx, perm) {
            items.push('<tr>')
            items.push('<td><span class="muted inherited-permission">inherited from hierarchy</span></td>')
            for (var i = 0; i < permissions.length; i++) {
              items.push('<td><input type="radio"' + (perm.type === permissions[i] ? ' checked' : '') + ' disabled></td>')
            }
            items.push('<td><input type="radio"' + (perm.type ? '' : ' checked') + ' disabled></td>')
            items.push('</tr>')
          })
        }
      })
      return items.join('')
    }

    function createUserPermissionTable (data, permissions) {
      var items = []
      $.each(data.entityIds, function (entityId, entityTypeId) {
        if (data.userPermissionMap && data.userPermissionMap[entityId]) {
          $.each(data.userPermissionMap[entityId], function (idx, perm) {
            items.push('<tr>')
            items.push('<td>' + (idx == 0 ? entityTypeId : '') + '</td>')
            for (var i = 0; i < permissions.length; i++) {
              items.push('<td><input type="radio" name="radio-' + entityId + '" value="' + permissions[i] + '"' + (perm.type === permissions[i] ? ' checked' : '') + '></td>')
            }
            items.push('<td><input type="radio" name="radio-' + entityId + '" value="none"' + (perm.type ? '' : ' checked') + '></td>')
          })
        } else {
          items.push('<tr>')
          items.push('<td>' + entityTypeId + '</td>')
          for (var i = 0; i < permissions.length; i++) {
            items.push('<td><input type="radio" name="radio-' + entityId + '" value="' + permissions[i] + '"></td>')
          }
          items.push('<td><input type="radio" name="radio-' + entityId + '" value="none" checked></td>')
          items.push('</tr>')
        }
        if (data.hierarchyPermissionMap && data.hierarchyPermissionMap[entityId]) {
          $.each(data.hierarchyPermissionMap[entityId], function (idx, perm) {
            items.push('<tr>')
            items.push('<td><span class="muted inherited-permission">inherited from hierarchy</span></td>')
            for (var i = 0; i < permissions.length; i++) {
              items.push('<td><input type="radio"' + (perm.type === permissions[i] ? ' checked' : '') + ' disabled></td>')
            }
            items.push('<td><input type="radio"' + (perm.type ? '' : ' checked') + ' disabled></td>')
            items.push('</tr>')
          })
        }
        if (data.groupPermissionMap && data.groupPermissionMap[entityId]) {
          $.each(data.groupPermissionMap[entityId], function (idx, perm) {
            items.push('<tr>')
            items.push('<td><span class="muted inherited-permission">inherited from group: ' + perm.group + '</span></td>')
            for (var i = 0; i < permissions.length; i++) {
              items.push('<td><input type="radio"' + (perm.type === permissions[i] ? ' checked' : '') + ' disabled></td>')
            }
            items.push('<td><input type="radio"' + (perm.type ? '' : ' checked') + ' disabled></td>')
            items.push('</tr>')
          })
        }
      })
      return items.join('')
    }

      function createPermissionTable(data, nrPermissions) {
          var items = []
          $.each(data.permissions, function (idx, permission) {
              items.push('<tr data-id="' + permission.resource.id + '" data-mask="' + permission.mask + '">')
              items.push('<td>' + permission.resource.label + '</td>')
              for (var i = nrPermissions - 1; i >= 0; --i) {
                  var checked = (permission.mask >> i) & 1 === 1
                  items.push('<td><input type="checkbox"' + (checked ? ' checked' : '') + ' data-index="' + i + '"></td>')
              }
              items.push('</tr>')
          })
          return items.join('')
      }

    $('#plugin-group-select').change(function () {
      $.get(molgenis.getContextUrl() + '/plugin/group/' + $(this).val(), function (data) {
        $('#plugin-group-permission-table tbody').empty().html(createGroupPermissionTable(data, ['read']))
      })

    })
    $('#plugin-user-select').change(function () {
      $.get(molgenis.getContextUrl() + '/plugin/user/' + $(this).val(), function (data) {
        $('#plugin-user-permission-table tbody').empty().html(createUserPermissionTable(data, ['read']))
      })
    })
    $('#package-group-select').change(function () {
        var sid = $(this).val()
        $.get(molgenis.getContextUrl() + '/package/group/' + sid, function (data) {
            var tbody = $('#package-group-permission-table tbody')
            tbody.empty().html(createPermissionTable(data, 5))
            tbody.data('type', 'group')
            tbody.data('sid', sid)
        })
    })
    $('#package-user-select').change(function () {
        var sid = $(this).val()
        $.get(molgenis.getContextUrl() + '/package/group/' + sid, function (data) {
            var tbody = $('#package-group-permission-table tbody')
            tbody.empty().html(createPermissionTable(data, 5))
            tbody.data('type', 'group')
            tbody.data('sid', sid)
        })
    })
    $('#entity-class-group-select').change(function () {
        var sid = $(this).val()
        $.get(molgenis.getContextUrl() + '/repository/group/' + sid, function (data) {
            var tbody = $('#entity-class-group-permission-table tbody')
            tbody.empty().html(createPermissionTable(data, 5))
            tbody.data('type', 'group')
            tbody.data('sid', sid)
      })
    })
    $('#entity-class-user-select').change(function () {
        var sid = $(this).val()
        $.get(molgenis.getContextUrl() + '/repository/user/' + sid, function (data) {
            var tbody = $('#entity-class-user-permission-table tbody')
            tbody.empty().html(createPermissionTable(data, 5))
            tbody.data('type', 'user')
            tbody.data('sid', sid)
      })
    })
      $('input:checkbox', '#entity-type-rls-table').change(function () {
          var id = $(this).attr('id');
          var rlsEnabled = this.checked;
          $.ajax({
              url: molgenis.getContextUrl() + "/update/entityclass/rls",
              type: "POST",
              data: JSON.stringify({
                  id: id,
                  rlsEnabled: rlsEnabled
              }),
              contentType: "application/json; charset=utf-8"
          })
      })

      $('#entity-class-group-permission-table,#entity-class-user-permission-table').on('change', 'input:checkbox', function () {
          var col = $(this)
          var row = col.closest('tr')
          var table = row.closest('tbody')

          var id = row.data('id')
          var type = table.data('type')
          var sid = table.data('sid')
          var mask = row.data('mask') ^ 1 << col.data('index')

          row.data('mask', mask)

          $.post(molgenis.getContextUrl() + "/repository/" + id + "/" + type + "/" + sid + "/" + mask)
      });

      $('#package-group-permission-table,#package-user-permission-table').on('change', 'input:checkbox', function () {
          var col = $(this)
          var row = col.closest('tr')
          var table = row.closest('tbody')

          var id = row.data('id')
          var type = table.data('type')
          var sid = table.data('sid')
          var mask = row.data('mask') ^ 1 << col.data('index')

          row.data('mask', mask)

          $.post(molgenis.getContextUrl() + "/package/" + id + "/" + type + "/" + sid + "/" + mask)
      });

    $('#plugin-group-permission-form,' +
      '#plugin-user-permission-form,' +
      '#package-group-permission-form,' +
        '#package-user-permission-form').submit(function (e) {
      e.preventDefault()
      $.ajax({
        type: $(this).attr('method'),
        url: $(this).attr('action'),
        data: $(this).serialize(),
        success: function (data) {
          $('#plugin-container .alert').remove()
          $('#plugin-container').prepend('<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Success!</strong> Updated plugin permissions</div>')

          //Refresh
          $('#plugin-group-select').change()
          $('#package-group-select').change()
          $('#entity-class-group-select').change()
          $('#plugin-user-select').change()
          $('#package-user-select').change()
          $('#entity-class-user-select').change()
        },
        error: function (xhr, textStatus, errorThrown) {
          var errorMessage = JSON.parse(xhr.responseText).errorMessage
          $('#plugin-container .alert').remove()
          $('#plugin-container').prepend('<div class="alert alert-danger"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Error!</strong> ' + errorMessage + '</div>')
        }
      })
    })

    $('a[data-toggle="tab"][href="#plugin-group-permission-manager"]').on('show.bs.tab', function (e) {
      $('#plugin-group-select').change()
    })
    $('a[data-toggle="tab"][href="#plugin-user-permission-manager"]').on('show.bs.tab', function (e) {
      $('#plugin-user-select').change()
    })
    $('a[data-toggle="tab"][href="#package-group-permission-manager"]').on('show.bs.tab', function (e) {
      $('#package-group-select').change()
    })
    $('a[data-toggle="tab"][href="#package-user-permission-manager"]').on('show.bs.tab', function (e) {
      $('#package-user-select').change()
    })
    $('a[data-toggle="tab"][href="#entity-class-group-permission-manager"]').on('show.bs.tab', function (e) {
      $('#entity-class-group-select').change()
    })
    $('a[data-toggle="tab"][href="#entity-class-user-permission-manager"]').on('show.bs.tab', function (e) {
      $('#entity-class-user-select').change()
    })

    $('#plugin-group-select').change()
    $('#package-group-select').change()
    $('#entity-class-group-select').change()
  })
}($, window.top.molgenis = window.top.molgenis || {}))