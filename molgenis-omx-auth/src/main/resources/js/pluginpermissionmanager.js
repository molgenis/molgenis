(function($, w) {
	"use strict";
	
	$(function() {
		$('#user-select').change(function() {
			$.get('/plugin/pluginpermissionmanager/user/' + $(this).val(), function(data) {
				$('#plugin-permission-table tbody').empty();
				var items = [];
				$.each(data.pluginPermissions, function(idx, perm) {
					var id = perm.pluginId;
					var read = perm.canRead;
					var write = perm.canWrite;
					
					items.push('<tr>');
					items.push('<td>' + id + '</td>');
					items.push('<td><input type="radio" name="radio-' + id + '" value="read"' + (read ? ' checked' : '') + '></td>');
					items.push('<td><input type="radio" name="radio-' + id + '" value="write"' + (write ? ' checked' : '') + '></td>');
					items.push('<td><input type="radio" name="radio-' + id + '" value="none"' + (!(read || write) ? ' checked' : '') + '></td>');
					items.push('</tr>');
				});
				$('#plugin-permission-table tbody').html(items.join(''));
			});
			$('#pluginpermission-form').submit(function(e) {
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
		});
		
		$('#user-select').change();
	});
}($, window.top));