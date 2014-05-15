function isViewState(viewState) {
	// viewState: "users" | "groups"
	var response = false;
	$.ajax({ type: 'GET',
			 async: false,
	         url: '/menu/admin/usermanager' + '/isViewState/' + viewState,   
	         success : function(bool)
	         {
	            response = bool;
	         }
	});
	
	return response;
}

function setViewState(viewState) {
	// viewState: "users" | "groups"
	$.ajax({ type: "PUT",
	         url: '/menu/admin/usermanager' + '/setViewState/' + viewState,   
	         success : function(bool)
	         {
	            response = bool;
	         }
	});
}

function getCreateForm(type) {
	$.ajax({ type: 'GET',   
	         url: 'http://localhost:8080/api/v1/molgenis' + type + '/create',   
	         success : function(text)
	         {
	         	$('#managerModalTitle').html('Add ' + type);
	            $('#controlGroups').html(text);
	         }
	});
}

function getEditForm(id, type) {
	$.ajax({ type: 'GET',   
	         url: 'http://localhost:8080/api/v1/molgenis' + type + '/' + id + '/edit',   
	         success : function(text)
	         {
	         	$('#managerModalTitle').html('Edit ' + type);
	             $('#controlGroups').html(text);
	         }
	});
}

function setActivation(type, id, checkbox) {
	// type: "user" | "group"
	var active = checkbox.checked;
	$.ajax({ type: 'PUT',   
	         url: '/menu/admin/usermanager' + '/setActivation/' + type + '/' + id + '/' + active,   
	         success : function(text)
	         {
	            $('#groupRow' + id).addClass('success')
	            $('#userRow' + id).addClass('success');
	            setTimeout(function () {
	            	$('#groupRow' + id).removeClass('success');
	            	location.reload();
	            }, 1000);
	         }
	});
}

function changeGroupMembership(userId,groupId,checkbox)
{
	var member = checkbox.checked;
	$.ajax({ type: "PUT",   
	         url: "/menu/admin/usermanager" + "/changeGroupMembership/" + userId + "/" + groupId + "/" + member, 
	         success : function(text)
	         {
	             //$('#controlGroups').html(text);
				$('#userRow' + userId).addClass('success');
	            setTimeout(function () {
	            	$('#userRow' + userId).removeClass('success');
	            	location.reload();
	            }, 1000);
	         }
	});
}

// prevent modal being submitted if one presses enter
function ignoreEnter(event)
{
  if (event.keyCode == 13) {
    return false;
  }
}

$(function() {
	// Initialize view:
	if (isViewState('users'))
	{
		$('#usersTab').addClass('active');
		$('#user-manager').addClass('active');
	} else if (isViewState('groups'))
	{
		$('#groupsTab').addClass('active');
		$('#group-manager').addClass('active');
	}
	
	var submitBtn = $('#submitFormButton');
	submitBtn.click(function(e) {
    	e.preventDefault();
    	e.stopPropagation();
    	$('#entity-form').submit();
    	$('#usermanagerModal').modal('toggle');
    	location.reload();
    });
});