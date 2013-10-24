(function($, w) {
	"use strict";
	
	$(document).ready(function() {
		  		//TODO JQUERY UI
		// Waarschijnlijk uiteindelijk niet gebruiken
//		$('#groupsWhereUserIsMember').selectable();
//		$('#usersMemberOfGroup').selectable();
		
		function createListOfUsersFromGroup(data) {
			var listItems = [];
			$.each(data, function (index) {
				listItems.push('<li value="' + data[index].id + '" class="ui-widget-content">' + data[index].username + '</li>');
			});
			return listItems.join('');
		};
		
		/**
		 * Change event is fired when user is selected
		 */
		$('#user-select').change(function(){
			this.form.submit();
			alert("submit user");
		});
		
		/**
		 * Change event is fired when group to add is selected
		 */
		$('#dropDownOfGroupsToAdd').change(function(){
			$("#form-usermanager").attr("action", "/plugin/usermanager/addgroup/");
			$("#form-usermanager").submit();
			alert("submit group to add");
		});
		
		/**
		 * Drop down with groups to select
		 */
		$('#group-select').change(function() {
			$.get('/plugin/usermanager/users/' + $(this).val(), function(data) {
				$('#usersMemberOfGroup').empty().html(createListOfUsersFromGroup(data));
			});
		});
		
		/**
		 * Groups where user is member
		 */
		$.each($('#groupsWhereUserIsMember li'), function(key, value){
			$(this).click(function(){
				$('#group-select').val($(this).val());
				$('#group-select').change();
			});
		});
		
		
		/**
		 * Drop down with groups to add
		 */
		$.each($('#dropDownOfGroupsToAdd li'), function(key, value){
			$(this).click(function(){
				alert("dropDownOfGroupsToAdd");
			});
		});
		
		
//		//Drop down with groups to add
//		//
//		$("#dropDownOfGroupsToAdd").submit(function(event) {
//			alert( "Handler for .submit() called." );
//			event.preventDefault();
//		});
		
		console.log("de code is met succes geladen");
	});

}($, window.top));