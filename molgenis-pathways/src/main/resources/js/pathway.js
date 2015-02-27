(function($, molgenis) {

	"use strict";
	var select2_items = [];
	var select2_items2 = [];
	var pathway_info = [];
	var pathwayId = "";
	var selectedVcf;

	function getPathwaysForGene(submittedGene) {
		$("#pathway-select").select2("val", "");

		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + "/filteredPathways",
			contentType : 'application/json',
			data : JSON.stringify(submittedGene),
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

	function getPathways() {

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

	function getPathwayImage(pathwayId) {
		$.ajax({
			type : 'GET',
			url : molgenis.getContextUrl() + "/pathwayViewer/" + pathwayId,
			success : function(data) {
				$("#pathway-svg-image").empty();
				$('#pathway-svg-image').append(data);
				SvgZoomPathway();
			}
		});
	}

	function SvgZoomPathway() {
		var test = svgPanZoom(document.getElementById('pathway-svg-image').getElementsByTagName('svg')[0]);
	}
	
	function getPathwaysByGenes(selectedVcf) {
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + "/pathwaysByGenes",
			contentType : 'application/json',
			data : selectedVcf,
			success : function(data) {
				select2_items2 = [];
				for ( var item in data) {
					select2_items2.push({
						text : data[item],
						id : item
					});
				}
			}
		});
	}

	function getColoredPathwayImage(selectedVcf, pathwayId) {
		$.ajax({
			type : 'GET',
			url : molgenis.getContextUrl() + "/getColoredPathway/" + selectedVcf + "/" + pathwayId,
			contentType : 'application/json',
			success : function(data) {
				$("#colored-pathway-svg-image").empty();
				$('#colored-pathway-svg-image').append(data);
				SvgZoomColoredPathway();
			}
		});
	}

	function SvgZoomColoredPathway() {
		svgPanZoom(document.getElementById('colored-pathway-svg-image').getElementsByTagName('svg')[0]);
	}

	$(function() {
		$('#hiding-select2').hide();

		$('#tabs').click(function() {
			$(this).tab('show');
			return false;
		})

		$('#pathway-select').select2({
			placeholder : "Select a pathway",
			width : '400px',
			data : function() {
				return {
					results : select2_items
				};
			},
		}).on("select2-selecting", function(event) {
			pathwayId = event.val;
			getPathwayImage(pathwayId);
		});
		
		$('#submit-genename-btn').on('click', function() {
			var submittedGene = $('#gene-search').val();
			getPathwaysForGene(submittedGene);
			return false;
		});
		
		$('#dataset-select').select2({
			placeholder : "Select a vcf file",
			width : '400px'
		});
		
		$('#submit-vcfFile-btn').on('click', function() {
			selectedVcf = $('#dataset-select').val();
			$('#hiding-select2').show();
			getPathwaysByGenes(selectedVcf);
			return false;
		});
		
		$('#pathway-select2').select2({
			placeholder : "Select a pathway",
			width : '400px',
			data : function() {
				return {
					results : select2_items2
				};
			},
		}).on("select2-selecting", function(event) {
			pathwayId = event.val;
			getColoredPathwayImage(selectedVcf, pathwayId);
		});
		
		getPathways();
	});

}($, window.top.molgenis = window.top.molgenis || {}));