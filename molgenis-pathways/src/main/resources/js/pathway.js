(function($, molgenis) {

	"use strict";
	var select2_items = [];
	var select2_items2 = [];
	var pathway_info = [];
	var pathwayId = "";
	var pathwayId2 = "";

	function getPathwaysForGene(submittedGene, event) {
		event.preventDefault(); // otherwise, the <form> will be displayed.

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

	function getVcfComponents(selectedVcf, event) {
		event.preventDefault();
		// console.log(JSON.stringify(selectedVcf));
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + "/vcfFile",
			dataType : 'json',
			contentType : 'application/json',
			data : JSON.stringify(selectedVcf),
			success : function(data) {
				// console.log(data);
			}
		});
	}

	function getPathways(event) {

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
		$.ajax({
			type : 'GET',
			url : molgenis.getContextUrl() + "/pathwayViewer/" + pathwayId,
			success : function(data) {
				$("#pathway-svg-image").empty();
				$('#pathway-svg-image').append(data);
			}
		});
	}

	function getPathwaysByGenes(event) {

		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + "/pathwaysByGenes",
			contentType : 'application/json',
			success : function(data) {
				select2_items2 = [];
				// console.log(data);
				for ( var item in data) {
					select2_items2.push({
						text : data[item],
						id : item
					});
				}
			}
		});
	}

	function getGPML(pathwayId, event) {

		$.ajax({
			type : 'GET',
			url : molgenis.getContextUrl() + "/getGPML/" + pathwayId,
			contentType : 'application/json',
			success : function(data) {
				var gpml = data;
			}

		});
	}

	$(function() {
		$('#hiding-select2').hide();
		$('#pathway-select').select2({
			placeholder : "Select a pathway",
			width : '500px',
			data : function() {
				return {
					results : select2_items
				};
			},
		}).on("select2-selecting", function(event) {
			pathwayId = event.val;
			getPathwayImage(pathwayId, event);
			getGPML(pathwayId, event);
		});
		$('#submit-genename-btn').on('click', function(event) {
			var submittedGene = $('#gene-search').val();
			getPathwaysForGene(submittedGene, event);
		});
		$('#submit-vcfFile-btn').on('click', function(event) {
			var selectedVcf = $('#dataset-select').val();
			getVcfComponents(selectedVcf, event);
			$('#hiding-select2').show();
			getPathwaysByGenes(event);
		});
		$('#pathway-select2').select2({
			placeholder : "Select a pathway",
			width : '500px',
			data : function() {
				return {
					results : select2_items2
				};
			},
		}).on("select2-selecting", function(event) {
			pathwayId2 = event.val;
			getPathwayImage(pathwayId2, event);
			getGPML(pathwayId2, event);
		});
		getPathways(event);
	});

}($, window.top.molgenis = window.top.molgenis || {}));