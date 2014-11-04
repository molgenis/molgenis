(function($, molgenis) {
	"use strict";

	var self = molgenis.usermanager = molgenis.usermanager || {};

	/**
	 * @memberOf molgenis.usermanager
	 */
	function setViewState(viewState) {
		// viewState: "users" | "groups"
		$.ajax({
			type : 'PUT',
			url : molgenis.getContextUrl() + '/setViewState/' + viewState,
		});
	}

	/**
	 * @memberOf molgenis.usermanager
	 */
	function getCreateForm(type) {
		$.ajax({
			type : 'GET',
			url : '/api/v1/molgenis' + type + '/create',
			success : function(text) {
				$('#managerModalTitle').html('Add ' + type);
				$('#controlGroups').html(text);
				
			}
		});
	}

	/**
	 * @memberOf molgenis.usermanager
	 */
	function getEditForm(id, type) {
		$.ajax({
			type : 'GET',
			url : '/api/v1/molgenis' + type + '/' + id + '/edit',
			success : function(text) {
				$('#managerModalTitle').html('Edit ' + type);
				$('#controlGroups').html(text);
			}
		});
	}

	/**
	 * @memberOf molgenis.usermanager
	 */
	function setActivation(type, id, checkbox) {
		// type: "user" | "group"
		var active = checkbox.checked;
		$.ajax({
			headers: { 
		        'Accept': 'application/json',
		        'Content-Type': 'application/json' 
		    },
			type : 'PUT',
			dataType: 'json',
			data: JSON.stringify({
				type : type,
				id: id,
				active: active
			}),
			url : molgenis.getContextUrl() + '/activation',
			success : function(data) {
				var styleClass = data.success ? 'success' : 'warning'
				if(data.type === "group") {
					$('#groupRow' + data.id).addClass(styleClass);
					setTimeout(function() {$('#groupRow' + data.id).removeClass('success');}, 1000);
				}
				
				if(data.type === "user") {
					$('#userRow' + data.id).addClass(styleClass);
					setTimeout(function() {$('#userRow' + data.id).removeClass('success');}, 1000);
				}
		}
		});
	}
	
	/**
	 * @memberOf molgenis.usermanager
	 */
	function changeGroupMembership(userId, groupId, checkbox) {
		var member = checkbox.checked;
		$.ajax({
			headers: { 
		        'Accept': 'application/json',
		        'Content-Type': 'application/json' 
		    },
			type : 'PUT',
			dataType: 'json',
			url : molgenis.getContextUrl() + '/changeGroupMembership',
			data: JSON.stringify({
				userId: userId,
				groupId: groupId,
				member: member
			}),
			success : function(data) {
				var styleClass = data.success ? 'success' : 'warning'
				$('#userRow' + data.userId).addClass(styleClass);
				setTimeout(function() {
					$('#userRow' + data.userId).removeClass(styleClass);
				}, 1000);
			}
		});
	}

	$(function() {
		
		$('#usersTab a').click(function(e) {
			setViewState('users');
		});

		$('#groupsTab a').click(function(e) {
			setViewState('groups');
		});

		$('#create-user-btn').click(function(e) {
			e.preventDefault();
			getCreateForm('user');
		});

		$('#create-group-btn').click(function(e) {
			e.preventDefault();
			getCreateForm('group');
		});

		$('.edit-user-btn').click(function(e) {
			e.preventDefault();
			getEditForm($(this).data('id'), 'user');
		});

		$('.edit-group-btn').click(function(e) {
			e.preventDefault();
			getEditForm($(this).data('id'), 'group');
		});

		$('.activate-user-checkbox').click(function(e) {
			setActivation('user', $(this).data('id'), this);
		});

		$('.change-group-membership-checkbox').click(function(e) {
			changeGroupMembership($(this).data('uid'), $(this).data('gid'), this);
		});

		$('.activate-group-checkbox').click(function(e) {
			setActivation('group', $(this).data('id'), this);
		});

		var submitBtn = $('#submitFormButton');
		submitBtn.click(function(e) {
			$('#entity-form').submit();
			return false;
		});
		
		$( "#target" ).submit(function( event ) {
			alert( "Handler for .submit() called." );
			event.preventDefault();
		});

		$(document).on('onFormSubmitSuccess', function() {
			$('#usermanagerModal').modal('toggle');
			location.reload();
		});
		
		$('#managerModal').keydown(function(e) {
			// prevent modal being submitted if one presses enter
			if (event.keyCode === 13) {
				return false;
			}
		});
	});
}($, window.top.molgenis = window.top.molgenis || {}));