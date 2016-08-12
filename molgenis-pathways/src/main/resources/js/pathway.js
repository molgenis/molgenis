(function ($, molgenis) {

    "use strict";
    var select2_items = [];
    var select2_items_vcf = [];
    var pathway_info = [];
    var pathwayId = "";
    var selectedVcf;

    var fillPathwaySelect = function (data) {
        select2_items = [];
        for (var i = 0; i < data.length; i++) {
            select2_items.push({
                text: data[i].name,
                id: data[i].id
            });
        }
    }

    var fillPathwaySelectVcf = function (data) {
        select2_items_vcf = [];
        for (var i = 0; i < data.length; i++) {
            select2_items_vcf.push({
                text: data[i].name,
                id: data[i].id
            });
        }
    }

    function getPathwaysForGene(submittedGene) {
        $("#pathway-select").select2("val", "");

        $.ajax({
            type: 'POST',
            url: molgenis.getContextUrl() + "/filteredPathways",
            contentType: 'application/json',
            data: JSON.stringify(submittedGene),
            success: fillPathwaySelect
        });
    }

    function getPathways() {

        $.ajax({
            type: 'POST',
            url: molgenis.getContextUrl() + "/allPathways",
            contentType: 'application/json',
            success: fillPathwaySelect
        });
    }

    function getPathwayImage(pathwayId) {
        $.ajax({
            type: 'GET',
            url: molgenis.getContextUrl() + "/pathwayViewer/" + pathwayId,
            success: function (data) {
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
            type: 'POST',
            url: molgenis.getContextUrl() + "/pathwaysByGenes",
            contentType: 'application/json',
            data: selectedVcf,
            success: fillPathwaySelectVcf
        });
    }

    function getColoredPathwayImage(selectedVcf, pathwayId) {
        $.ajax({
            type: 'GET',
            url: molgenis.getContextUrl() + "/getColoredPathway/" + selectedVcf + "/" + pathwayId,
            contentType: 'application/json',
            success: function (data) {
                $("#colored-pathway-svg-image").empty();
                $('#colored-pathway-svg-image').append(data);
                SvgZoomColoredPathway();
            }
        });
    }

    function SvgZoomColoredPathway() {
        svgPanZoom(document.getElementById('colored-pathway-svg-image').getElementsByTagName('svg')[0]);
    }

    $(function () {
        $('#hiding-select2').hide();

        $('#tabs').click(function () {
            $(this).tab('show');
            return false;
        })

        $('#pathway-select').select2({
            placeholder: "Select a pathway",
            width: '100%',
            data: function () {
                return {
                    results: select2_items
                };
            },
        }).on("select2-selecting", function (event) {
            pathwayId = event.val;
            getPathwayImage(pathwayId);
        });

        $('#submit-genename-btn').on('click', function () {
            var submittedGene = $('#gene-search').val();
            getPathwaysForGene(submittedGene);
            return false;
        });

        $('#dataset-select').select2({
            placeholder: "Select a VCF file",
            width: '100%'
        });

        $('#submit-vcfFile-btn').on('click', function () {
            selectedVcf = $('#dataset-select').val();
            $('#hiding-select2').show();
            getPathwaysByGenes(selectedVcf);
            return false;
        });

        $('#pathway-select2').select2({
            placeholder: "Select a pathway",
            width: '100%',
            data: function () {
                return {
                    results: select2_items_vcf
                };
            },
        }).on("select2-selecting", function (event) {
            pathwayId = event.val;
            getColoredPathwayImage(selectedVcf, pathwayId);
        });

        getPathways();
    });

}($, window.top.molgenis = window.top.molgenis || {}));