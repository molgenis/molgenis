(function($, w) {
	"use strict";
	
	$(function() {
		function createGroupPermissionTable(data) {
			var items = [];
			$.each(data.pluginIds, function(idx, pluginId) {
				var perms = data.groupPluginPermissionMap[pluginId];
				if(perms) {
					$.each(perms, function(idx, perm) {
						items.push('<tr>');
						items.push('<td>' + (idx == 0 ? pluginId : '') + '</td>');
						items.push('<td><input type="radio" name="radio-' + pluginId + '" value="write"' + (perm.type === "write" ? ' checked' : '') + '></td>');
						items.push('<td><input type="radio" name="radio-' + pluginId + '" value="read"' + (perm.type === "read" ? ' checked' : '') + '></td>');
						items.push('<td><input type="radio" name="radio-' + pluginId + '" value="none"' + (perm.type ? '' : ' checked') + '></td>');
						items.push('</tr>');						
					});
				} else {
					items.push('<tr>');
					items.push('<td>' + pluginId + '</td>');
					items.push('<td><input type="radio" name="radio-' + pluginId + '" value="write"></td>');
					items.push('<td><input type="radio" name="radio-' + pluginId + '" value="read"></td>');
					items.push('<td><input type="radio" name="radio-' + pluginId + '" value="none" checked></td>');
					items.push('</tr>');
				}
			});
			return items.join('');
		}
		
		function createUserPermissionTable(data) {
			console.log(data);
			var items = [];
			$.each(data.pluginIds, function(idx, pluginId) {
				var userPerms = data.userPluginPermissionMap[pluginId];
				var groupPerms = data.groupPluginPermissionMap[pluginId];
				if(userPerms) {
					$.each(userPerms, function(idx, perm) {
						items.push('<tr>');
						items.push('<td>' + (idx == 0 ? pluginId : '') + '</td>');
						items.push('<td><input type="radio" name="radio-' + pluginId + '" value="write"' + (perm.type === "write" ? ' checked' : '') + '></td>');
						items.push('<td><input type="radio" name="radio-' + pluginId + '" value="read"' + (perm.type === "read" ? ' checked' : '') + '></td>');
						items.push('<td><input type="radio" name="radio-' + pluginId + '" value="none"' + (perm.type ? '' : ' checked') + '></td>');
						items.push('<td></td>');
					});
				} else {
					items.push('<tr>');
					items.push('<td>' + pluginId + '</td>');
					items.push('<td><input type="radio" name="radio-' + pluginId + '" value="write"></td>');
					items.push('<td><input type="radio" name="radio-' + pluginId + '" value="read"></td>');
					items.push('<td><input type="radio" name="radio-' + pluginId + '" value="none" checked></td>');
					items.push('<td></td>');
					items.push('</tr>');
				}
				if(groupPerms) {
					$.each(groupPerms, function(idx, perm) {
						items.push('<tr>');
						items.push('<td></td>');
						items.push('<td><input type="radio"' + (perm.type === "write" ? ' checked' : '') + ' disabled></td>');
						items.push('<td><input type="radio"' + (perm.type === "read" ? ' checked' : '') + ' disabled></td>');
						items.push('<td><input type="radio"' + (perm.type ? '' : ' checked') + ' disabled></td>');
						items.push('<td>' + perm.group + '</td>');
						items.push('</tr>');						
					});
				}
			});
			return items.join('');
		}
		
		$('#group-select').change(function() {
			$.get('/plugin/pluginpermissionmanager/group/' + $(this).val(), function(data) {
				$('#group-plugin-permission-table tbody').empty().html(createGroupPermissionTable(data, false));
				$('#group-plugin-permission-table tbody');
			});
			
		});
		$('#user-select').change(function() {
			$.get('/plugin/pluginpermissionmanager/user/' + $(this).val(), function(data) {
				$('#user-plugin-permission-table tbody').empty().html(createUserPermissionTable(data, true));
			});
		});
		
		$('#group-plugin-permission-form,#user-plugin-permission-form').submit(function(e) {
			e.preventDefault();
			$.ajax({
				type : $(this).attr('method'),
				url : $(this).attr('action'),
				data : $(this).serialize(),
				success : function(data) {
					$('#plugin-container .alert').remove();
					$('#plugin-container').prepend('<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Success!</strong> Updated plugin permissions</div>');
				},
				error: function (xhr, textStatus, errorThrown) {
					var errorMessage = JSON.parse(xhr.responseText).errorMessage;
					$('#plugin-container .alert').remove();
					$('#plugin-container').prepend('<div class="alert alert-error"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Error!</strong> ' + errorMessage + '</div>');
				}
			});
		});

		$('a[data-toggle="tab"][href="#group-plugin-permission-manager"]').on('show', function (e) {
			$('#group-select').change();
		});
		$('a[data-toggle="tab"][href="#user-plugin-permission-manager"]').on('show', function (e) {
			$('#user-select').change();
		});
		
		$('#group-select').change();
	});
}($, window.top));