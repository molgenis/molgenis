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
			type : 'PUT',
			url : molgenis.getContextUrl() + '/setActivation/' + type + '/' + id + '/' + active,
			success : function(text) {
				$('#groupRow' + id).addClass('success')
				$('#userRow' + id).addClass('success');
				setTimeout(function() {
					$('#groupRow' + id).removeClass('success');
					location.reload();
				}, 1000);
			}
		});
	}

	/**
	 * @memberOf molgenis.usermanager
	 */
	function changeGroupMembership(userId, groupId, checkbox) {
		var member = checkbox.checked;
		$.ajax({
			type : 'PUT',
			url : molgenis.getContextUrl() + '/changeGroupMembership/' + userId + '/' + groupId + '/' + member,
			success : function(text) {
				// $('#controlGroups').html(text);
				$('#userRow' + userId).addClass('success');
				setTimeout(function() {
					$('#userRow' + userId).removeClass('success');
					location.reload();
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
			e.preventDefault();
			e.stopPropagation();
			$('#entity-form').submit();
		});

		$(document).on('onFormSubmitSuccess', function() {
			$('#usermanagerModal').modal('toggle');
			location.reload();
		});
		
		$('#managerModal').keydown(function(e) {
			// prevent modal being submitted if one presses enter
			if (event.keyCode === 13) {
				e.preventDefault();
				e.stopPropagation();
			}
		});
	});
}($, window.top.molgenis = window.top.molgenis || {}));