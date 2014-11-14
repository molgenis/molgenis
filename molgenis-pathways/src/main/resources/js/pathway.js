(function($, molgenis) {

	"use strict";
	var select2_items = [];
	var pathway_info = [];
	var pathwayId = "";

	function getPathwaysForGene(submittedGene, event) {
		event.preventDefault();
				
		$("#pathway-select").select2("val", "");

		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + "/geneName",
			contentType : 'application/json',
			data : JSON.stringify(submittedGene),
			success : function(data) {
				select2_items = [];
				for ( var item in data) {
					select2_items.push({
						text : data[item], // value (pathway name)
						id : item // key (pathway id)
					});
				}
			}
		});
	}

	function getPathways(event) {
//		event.preventDefault();

		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + "/allPathways",
			contentType : 'application/json',
			success : function(data) {
				select2_items = [];
				for ( var item in data) {
					select2_items.push({
						text : data[item],
						id : item
					});
				}
			}
		});
	}

	function getPathwayImage(pathwayId, event) {
//		event.preventDefault();
		
		$.ajax({
			type : 'GET',
			url : molgenis.getContextUrl() + "/pathwayViewer/"+pathwayId,
			success : function(data) {
//				console.log(data);
				$('#pathway-svg-image').append(data);
			}
		});
	}	

	$(function() {
		
		$('#pathway-select').select2({
			placeholder : "Select a pathway",
			width : '500px',
			data : function() {
				return {
					results : select2_items
				};
			},
		}).on("select2-selecting", function(event) {
//			console.log(event.val);
			pathwayId = event.val;
			getPathwayImage(pathwayId,  event);
		});
		$('#submit-genename-btn').on('click', function(event) {
			var submittedGene = $('#gene-search').val();
			getPathwaysForGene(submittedGene, event);
		});
		getPathways(event);
	});

}($, window.top.molgenis = window.top.molgenis || {}));