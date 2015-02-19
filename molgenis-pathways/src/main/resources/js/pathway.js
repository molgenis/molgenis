(function($, molgenis) {

	"use strict";
	var select2_items = [];
	var select2_items2 = [];
	var pathway_info = [];
	var pathwayId = "";
	var selectedVcf;

	function getPathwaysForGene(submittedGene, event) {
		event.preventDefault(); // otherwise, the <form> will be displayed.

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
						text : data[item], // value (pathway name)
						id : item
					// key (pathway id)
					});
				}
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
				SvgZoomPathway()
			}
		});
	}

	function SvgZoomPathway() {
		var test = svgPanZoom(document.getElementById('pathway-svg-image')
				.getElementsByTagName('svg')[0]);

		// test.pan({x:10, y:0});
		// test.panBy({x:50, y:50});
		// test.zoomAtPointBy(1, {x:50, y:50})

		// var viewportGroupElement =
		// document.getElemenById('pathway-svg-image').querySelector('.svg-pan-zoom_viewport');
		// svgPanZoom('#pathway-svg-image').getElementsByTagName('svg')[0], {
		// viewportSelector: viewportGroupElement
		// });

		// test.fit();
		// document.getElementById('pathway-svg-image').querySelector('rect').setAttribute('width',
		// 1200);
		// document.getElementById('pathway-svg-image').querySelector('rect').setAttribute('height',
		// 700);
		// test.fit();
		// test.updateBBox();
		// test.fit();
	}
	
	function getPathwaysByGenes(selectedVcf) {
//		console.log("in function getPathwaysByGenes");

		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + "/pathwaysByGenes",
			contentType : 'application/json',
			data : selectedVcf,
			success : function(data) {
				select2_items2 = [];
//				 console.log(data);
				for ( var item in data) {
					select2_items2.push({
						text : data[item],
						id : item
					});
				}
			}
		});
	}

	function getGPML(selectedVcf, pathwayId, event) {
		$.ajax({
			type : 'GET',
			url : molgenis.getContextUrl() + "/getGPML/" + selectedVcf + "/" + pathwayId,
			contentType : 'application/json',
			success : function(data) {
				$("#colored-pathway-svg-image").empty();
				$('#colored-pathway-svg-image').append(data);
				SvgZoomColoredPathway();
			}
		});
	}

	function getColoredPathwayImage(pathwayId, event) {
		$.ajax({
			type : 'GET',
			url : molgenis.getContextUrl() + "/getColoredPathway/" + pathwayId,
			success : function(data) {
				$("#colored-pathway-svg-image").empty();
				$('#colored-pathway-svg-image').append(data);
			}
		});
	}

	function SvgZoomColoredPathway() {
		svgPanZoom(document.getElementById('colored-pathway-svg-image')
				.getElementsByTagName('svg')[0]);
	}

	$(function() {
		$('#hiding-select2').hide();

		$('#tabs').click(function(e) {
			e.preventDefault()
			$(this).tab('show')
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
			getPathwayImage(pathwayId, event);
		});
		$('#submit-genename-btn').on('click', function(event) {
			var submittedGene = $('#gene-search').val();
			getPathwaysForGene(submittedGene, event);
		});
		$('#dataset-select').select2({
			placeholder : "Select a vcf file",
			width : '400px'
		});
		$('#submit-vcfFile-btn').on('click', function(event) {
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
			getGPML(selectedVcf, pathwayId, event);
			return false;
		});
		getPathways(event);
	});

}($, window.top.molgenis = window.top.molgenis || {}));