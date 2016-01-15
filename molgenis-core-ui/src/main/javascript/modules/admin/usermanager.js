import api from '../RestClientV1';
import $ from 'jquery';
import molgenis from '../MolgenisGlobalObject';
import Form from 'react-components/Form';
import React from 'react';

function setViewState(viewState) {
	// viewState: "users" | "groups"
	$.ajax({
		type : 'PUT',
		url : molgenis.contextUrl + '/setViewState/' + viewState,
	});
}

function getCreateForm(type) {
	React.render(Form({
		mode : 'create',
		entity : 'molgenis' + type,
		modal : true,
		onSubmitSuccess : function(e) {

			// Put user in 'All users' group if not SuperUser
			api.getAsync(e.location, null, function(user) {
				if (user.superuser === false) {
					addUserToAllUsersGroup(api.getPrimaryKeyFromHref(e.location), function() {
						location.reload();
					});
				} else {
					location.reload();
				}
			});
		}
	}), $('<div>')[0]);
}

function getEditForm(id, type) {
	React.render(Form({
		entity : 'molgenis' + type,
		entityInstance : id,
		mode : 'edit',
		modal : true,
		onSubmitSuccess : function() {
			location.reload();
		}
	}), $('<div>')[0]);
}

function setActivation(type, id, checkbox) {
	// type: "user" | "group"
	var active = checkbox.checked;
	$.ajax({
		headers : {
			'Accept' : 'application/json',
			'Content-Type' : 'application/json'
		},
		type : 'PUT',
		dataType : 'json',
		data : JSON.stringify({
			type : type,
			id : id,
			active : active
		}),
		url : molgenis.contextUrl + '/activation',
		success : function(data) {
			var styleClass = data.success ? 'success' : 'warning'
			if (data.type === "group") {
				$('#groupRow' + data.id).addClass(styleClass);
				setTimeout(function() {
					$('#groupRow' + data.id).removeClass('success');
				}, 1000);
			}

			if (data.type === "user") {
				$('#userRow' + data.id).addClass(styleClass);
				setTimeout(function() {
					$('#userRow' + data.id).removeClass('success');
				}, 1000);
			}
		}
	});
}

function changeGroupMembership(userId, groupId, checkbox) {
	var member = checkbox.checked;
	$.ajax({
		headers : {
			'Accept' : 'application/json',
			'Content-Type' : 'application/json'
		},
		type : 'PUT',
		dataType : 'json',
		url : molgenis.contextUrl + '/changeGroupMembership',
		data : JSON.stringify({
			userId : userId,
			groupId : groupId,
			member : member
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

function addUserToAllUsersGroup(userId, callback) {
	getAllUsersGroup(function(groupId) {
		if (groupId === null) {
			callback();
		} else {
			$.ajax({
				headers : {
					'Accept' : 'application/json',
					'Content-Type' : 'application/json'
				},
				type : 'PUT',
				dataType : 'json',
				url : molgenis.contextUrl + '/changeGroupMembership',
				data : JSON.stringify({
					userId : userId,
					groupId : groupId,
					member : true
				}),
				success : function() {
					callback();
				}
			});
		}
	});
}

function getAllUsersGroup(callback) {
	api.getAsync('/api/v1/MolgenisGroup', {
		q : [ {
			field : 'name',
			operator : 'EQUALS',
			value : 'All Users'
		} ]
	}, function(result) {
		var groupId = null;
		if (result.total > 0) {
			groupId = result.items[0].id;
		}
		callback(groupId);
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
});

window.$ = window.jquery = $;
