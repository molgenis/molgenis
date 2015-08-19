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
			React.render(molgenis.ui.Form({
				mode: 'create',
				entity : 'Script',
				modal: true,
				onSubmitSuccess : function() {
					location.reload();
				}
			}), $('<div>')[0]);
		});
		
		$('.edit-script-btn').click(function() {
			var scriptToEdit = $(this).parent().siblings(".name").text();
			
			React.render(molgenis.ui.Form({
				mode: 'edit',
				entity : 'script',
				entityInstance: encodeURIComponent(scriptToEdit),
				modal: true,
				onSubmitSuccess : function() {
					location.reload();
				}
			}), $('<div>')[0]);
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
			React.render(molgenis.ui.Form({
				mode: 'create',
				entity : 'scriptparameter',
				modal: true,
				onSubmitSuccess : function() {
					location.reload();
				}
			}), $('<div>')[0]);
		});
	})
	
})($, window.top.molgenis = window.top.molgenis || {});