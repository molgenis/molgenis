(function($, w) {
	"use strict";
	
	$(function() {
		
		function createListOfUsersFromGroup(data) {
			var listItems = [];
			$.each(data, function (index) {
				listItems.push('<tr>' 
						+ '<td>' + data[index].id + '</td>' 
						+ '<td>' + data[index].username + '</td></tr>');
			});
			return "<tbody>" + listItems.join('') + "</tbody>";
		};
		
		
		/**
		 * Change event is fired when user is selected
		 */
		$('#user-select').change(function(){
			this.form.submit();
		});
		
		
		/**
		 * Change event is fired when group to add is selected
		 */
		$('#drop-down-groups-to-add').change(function(){
			var groupToAddId = $(this).val();
			$.get('/menu/admin/usermanager/addusertogroup/' + groupToAddId, function(data) {
				$("#form-usermanager").submit();
			});
		});
		
		
		/**
		 * Remove user from group
		 */
		$('#groupsWhereUserIsMember a[data-remove-group-id]').click(function(){
			var groupToRemoveId = $(this).attr("data-remove-group-id");
			$.get('/menu/admin/usermanager/removeuserfromgroup/' + groupToRemoveId, function(data) {
				$("#form-usermanager").submit();
			});
		});
		
		
		/**
		 * Drop down with groups to select
		 */
		$('#group-select').change(function() {
			$.get('/plugin/usermanager/users/' + $(this).val(), function(data) {
				$('#users-of-group').html(createListOfUsersFromGroup(data));
			});
		});
		
		
		/**
		 * Groups where user is member
		 */
		$.each($('#groupsWhereUserIsMember a[data-group-id]'), function(){
			$(this).click(function(){
				$('#group-select').val($(this).attr('data-group-id'));
				$('#group-select').change();
				$('#group-select').trigger('liszt:updated');
			});
		});
		
		
		$('#group-select').chosen();
		$('#user-select').chosen();
		$('#drop-down-groups-to-add').chosen();
		
		//Init groups select
		$('#group-select').change();
		$('#group-select').trigger('liszt:updated');
	});

}($, window.top));