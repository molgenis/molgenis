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
		
		$('#create-script-btn').click(function() {
			$.ajax({
				type : 'GET',
				url : '/api/v1/script/create',
				success : function(text) {
					$('#formModalTitle').html('Add Script');
					$('#controlGroups').html(text);
					$('#formModal').modal('show');
				}
			});
		});
		
		$('.edit-script-btn').click(function() {
			var scriptToEdit = $(this).parent().siblings(".name").text();
			$.ajax({
				type : 'GET',
				url : '/api/v1/script/' + encodeURIComponent(scriptToEdit) +  '/edit',
				success : function(text) {
					$('#formModalTitle').html('Edit ' + scriptToEdit);
					$('#controlGroups').html(text);
					$('#formModal').modal('show');
				}
			});
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
		
		$('#submitFormButton').click(function() {
			$('#entity-form').submit();
		});
		
		$('#create-scriptparameter-btn').click(function() {
			$.ajax({
				type : 'GET',
				url : '/api/v1/scriptparameter/create',
				success : function(text) {
					$('#formModalTitle').html('Add Scriptparameter');
					$('#controlGroups').html(text);
					$('#formModal').modal('show');
				}
			});
		});
		
		$(document).on('onFormSubmitSuccess', function() {
			location.reload();
		});

	})
	
})($, window.top.molgenis = window.top.molgenis || {});