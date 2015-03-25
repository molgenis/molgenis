(function($, molgenis) {
	"use strict";
	var restApi = new molgenis.RestClient();
	
	$(function() {
		var script;
		
		$('.execute').click(function() {
			script = $(this).parent().siblings(".name").text();
			var hasAttributes = $(this).attr("data-hasAttributes");
			if (hasAttributes == 'false') {
				window.open("/scripts/" + encodeURIComponent(script) + "/run");
			} else {
				var parametersTemplate = Handlebars.compile($("#parameters-template").html());
				restApi.getAsync('/api/v1/script', {q:{q:[{field:'name', operator:'EQUALS', value:script}]}, expand:['parameters']}, function(result) {
					$('#parametersForm').html(parametersTemplate({parameters:result.items[0].parameters.items}));
					$('#parametersModal').modal('show');
				});
			}	
		});
		
		$('#runWithParametersButton').click(function(){
			var form = $('#parametersForm');
			form.validate();
			if (form.valid()) {
				window.open("/scripts/" + encodeURIComponent(script) + "/run?" + form.serialize());
			}
		});
		
		$('#create-script-btn').click(function(e) {
			$('#formModalTitle').html('Add Script');
			React.render(molgenis.ui.Form({
				entity : 'script',
				mode: 'create',
				onSubmitSuccess : function() {
					$('#formModal').modal('hide');
					location.reload();
				},
				cancelBtn: true,
				onCancel: function() {
					$('#formModal').modal('hide');
				}
			}), $('#controlGroups')[0]);
			$('#formModal').modal('show');
		});
		
		$('.edit-script-btn').click(function() {
			var scriptToEdit = $(this).parent().siblings(".name").text();
			
			$('#formModalTitle').html('Edit ' + scriptToEdit);
			React.render(molgenis.ui.Form({
				entity : 'script',
				entityInstance: encodeURIComponent(scriptToEdit),
				mode: 'edit',
				onSubmitSuccess : function() {
					$('#formModal').modal('hide');
				},
				cancelBtn: true,
				onCancel: function() {
					$('#formModal').modal('hide');
				}
			}), $('#controlGroups')[0]);
			$('#formModal').modal('show');
		});
		
		$('.delete-script-btn').click(function() {
			var scriptToDelete = $(this).parent().siblings(".name").text();
			
			if (confirm("Delete script named " + scriptToDelete + " ?")){
				restApi.remove("/api/v1/script/" + encodeURIComponent(scriptToDelete),{
					success:function(){},
					error:function(){}
				});
				location.reload();
			};
		});
		
		$('#create-scriptparameter-btn').click(function() {
			$('#formModalTitle').html('Add Scriptparameter');
			React.render(molgenis.ui.Form({
				entity : 'scriptparameter',
				mode: 'create',
				onSubmitSuccess : function() {
					$('#formModal').modal('hide');
					location.reload();
				},
				cancelBtn: true,
				onCancel: function() {
					$('#formModal').modal('hide');
				}
			}), $('#controlGroups')[0]);
			$('#formModal').modal('show');
		});
	})
	
})($, window.top.molgenis = window.top.molgenis || {});