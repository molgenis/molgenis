$(document).ready(function () {
	
	//-------------------------------------------------//
	//Global functions of the publication part of GOAD.//
	//-------------------------------------------------//
	
	// When clicking upon a study within the studyTable.
	$("body").on("click", ".studyTable", function(){
	
		// Creates a tooltip when hovering on the studyTable	
		$('.studyTable').tooltip(); 
	
		// The information of the given studies are obtained.
		var uniqueConditions = [];
		var studyConditions = [];
		var studyInformation = [];
		$.get("/api/v2/Test_studies?q=GEOD_NR=="+$(this).attr("id")).done(function(data){
			var data = data["items"];
			// The variables studyTitle and GEODOnPage are cleared.
			var studyTitle = [];
			GEODOnPage = [];
			organismOnPage = [];
			// For each known sample the data is obtained.
			$.each(data, function(i, item){
				// The data is saved when the sample is not known yet.
				if ($.inArray(data[i]["Tissue"] + " " + data[i]["Celltype"] + " " + data[i]["Strain"] + " " + data[i]["Age"] + " " + data[i]["RepNumber"], uniqueConditions) === -1 ){
					uniqueConditions.push(data[i]["Tissue"] + " " + data[i]["Celltype"] + " " + data[i]["Strain"] + " " + data[i]["Age"] + " " + data[i]["RepNumber"]);
					// Variable sampleInfo is made to make sure that one sample is noted as option.	
					var sampleInfo = data[i]["Tissue"] + " " + data[i]["Celltype"] + " " + data[i]["Strain"] + " " + data[i]["Age"] + " " + data[i]["RepNumber"];
					// Information is pushed to the variable studyConditions.
					studyConditions.push('<option value="' + data[i]["SRA"] + '">' + sampleInfo + '</option>');
				}
				// The code below is used to show global information like the title, author and publication year above the QE/DE part.
				// Only if the title is not yet known in studyTitle, this information is added to the variable studyInformation.
				if ($.inArray(data[i]["Title"], studyTitle)== -1) {
					studyTitle.push(data[i]["Title"]);
					studyInformation.push(
						"<b>Title:</b> " + data[i]["Title"] + "<br/>"
						+ "<b>Author:</b> " + data[i]["Author"] + "<br/>"
						+ "<b>Publication year:</b> " + data[i]["Year"]);
					// The GEOD number is pushed so it can be used for calling the R API's.
					GEODOnPage.push(data[i]["GEOD_NR"]);
					organismOnPage.push(data[i]["Organism"]);
 			}});
		// Conditions are sorted alphabetically.
		studyConditions.sort();
		// The different conditions are added into the select selectConditions.
		$("#selectConditions").html(studyConditions);
		// Global information is added into the div informationStudy.
		$("#informationStudy").html(studyInformation);
		});
		// The according showing the studies is hidden and the publicationPart with the options QE and DE is shown.
		$("#accordion").hide();
		$(".row.DE").hide();
		$("#publicationPart").show();
	});

	//---------------------------------------------//
	//DE functions of the publication part of GOAD.//
	//---------------------------------------------//

	//When clicking upon the DE button.
	$("body").on("click", "#DEbutton", function(){
		// QE content that might be visible is hidden.
		$("#QE_info").hide();
		$("#QE_content").hide();
		$("#searchBar_QE").hide();
		$("#submitQEbutton").hide();
		// The select2 bar is shown.
		$("#s2id_selectConditions").show();
		$("#selectBar").show();
		// Makes sure that there are two conditions chosen within the select bar.
		if ($('#selectConditions option').size() !== 1) {
			$("#selectBar").toggle();
			$("#selectConditions").select2({
				placeholder: "Select two conditions",
				maximumSelectionSize:2,
				allowClear: true
			});
		} else {
		//An error is raised when none or one condition is chosen.
			$("#selectBarMessage").show();
		}
		hideQE();
		$("#s2id_selectConditions").show();
		$("#selectBar").show();
		$("#DE_info").show();
	});

	$("body").on("click", "#submitDEbutton", function(){
		// The table-scroll is emptied when clicking the DE submit button
		// and the for-control is reset.
		$("#DE_info").hide();
		$(".table-scroll").empty();
		$(".form-control").trigger("reset");
		// When there are two conditions chosen the API for the scatterplot and the DE table is called.
		var count = $("#selectConditions :selected").length;
		if (count === 2) {
			var tableContent = [];
			$.get("/scripts/New_DE_ScatterPlot/run?entityName="+GEODOnPage[0].replace(/-/g,'')+"&condition1="+$("#selectConditions").val()[0]+"&condition2="+$("#selectConditions").val()[1]+"&targetFile="+GEODOnPage[0].replace(/-/g,'')+"_targets&organism="+organismOnPage[0].replace(/ /g, "+")).done(
				function(data){
				// The necessary information from the scatterD3 plot is obtained
		   			var regexData = /(\<div id="htmlwidget_container"\>\n[A-z0-9 \n\<\=\"\-\:\;\>\/\!\.]+)\<script type="application\/json" data-for="htmlwidget.+\>(\{.+\})/g;
					match = regexData.exec(data);
					if (match !== null) {
						var obj = JSON.parse(match[2]);	
						// The necessary information for the scatterplot is written to the div scatterplot.
						$("#scatterplot").html(match[1]);
						// The render function creates the interactive scatterplot.
						//render('#scatterplot', obj.x);
						// The extra div that is created by the render function is removed.
						$('div[id^="htmlwidget"]').remove()
						// The column name which is used for the legend is replaced bij 'p-value'.
						$('text.color-legend-label').text('p-value')
					}
				});
			
			$.get("/scripts/New_DE_table/run?entityName="+GEODOnPage[0].replace(/-/g,'')+"&condition1="+$("#selectConditions").val()[0]+"&condition2="+$("#selectConditions").val()[1]+"&targetFile="+GEODOnPage[0].replace(/-/g,'')+"_targets&organism="+organismOnPage[0].replace(/ /g, "+")).done(
					function(data){
						if (data.startsWith('Login success[1] "No differentially expressed genes where found')) {
							$("#NoDEGMessage").show();
							$(".row.DE").show();
							$("#DETable").hide();
							$("#DETableContent").hide();
							$("#scatterplot").hide();
						} else { 
							// The DE table is added to the div DETable.
							$("#DETable").append(
							'<table id="countTable" class="table table-striped table-hover table-condensed table-responsive header-fixed sortable">' +
								'<thead><tr><th>Genesymbol</th><th>LogFC</th><th>FDR</th></tr></thead>' +
								'<tbody id="DEcontent" class="searchable"></tbody>' +
							'</table>');
							// The data information obtained from the API is splitted by new lines and spaces.
							var stringArray = data.split(/[ ,\n"]+/);
							// Starting from the 5th element the data is saved into the variable tableContent.
							tableContent = stringArray.slice(4,stringArray.length)
							var counter = 0;
							var stringToAppend = '';
							// For each element within the variable tableContent
							$.each(tableContent, function(i, content){
								if (counter ===0){
									// The beginning of the row for the table is made starting with the first element.
									stringToAppend += '<tr><td>'+content+'</td>';
									counter += 1;
								} else if (counter < 2){
									// Adding elements. 
									stringToAppend += '<td>'+content+'</td>';
									counter += 1;
								} else {
									// The last element end to row and appends the information to the div DEcontent.
									stringToAppend += '<td>'+parseFloat(content).toFixed(7)+'</td></tr>';
									$('#DEcontent').append(stringToAppend);
									stringToAppend = '';
									counter = 0;
								};

							});
							// Shows all of the necessary content used for the DE analysis.
							$("#DETable").show();
							$("#DEcontent").show();
							$("#searchBar_DE").show()
							$("#DETableContent").show();
							$("#scatterplot").show();
							$(".row.DE").show();
							searchBar("#DEsearch");	
					}
				});
			} else {
				// An error is given when there is only one condition to choose from.
				$("#errorLengthForSubmit").show();
				$('#errorLengthForSubmit').delay(3000).fadeOut('slow');
			}
		});
	
	//---------------------------------------------//
	//QE functions of the publication part of GOAD.//
	//---------------------------------------------//
	
	// When clicking upon the QE button
	$("body").on("click", "#QEbutton", function(){
		$("#QE_info").show();
		$("#submitQEbutton").show();
		$("#DE_info").hide();
		$("#QE_content").hide();
		$("#selectBar").hide();
		$(".row.DE").hide();
	});

	$("#DownloadQE").click(function (e) {
		// Obtaining information from the header and the table body.
		var tableHeader = $('#hiddenQETable>table>thead').find('tr:has(th)');
	    var tableRows = $('#hiddenQETable>table').find('tr:has(td)');

	    // Temporary delimiter characters unlikely to be typed by keyboard
	    // This is to avoid accidentally splitting the actual contents
	    tmpColDelim = String.fromCharCode(11); // vertical tab character
	    tmpRowDelim = String.fromCharCode(0); // null character

	    // Actual delimiter characters for CSV format
	    colDelim = '","';
	    rowDelim = '"\r\n"';

	    // The function exportTableInfo is used to obtain the necessary information
		csv = exportTableInfo(tableHeader, "th").concat(rowDelim, exportTableInfo(tableRows, "td"));

		// Making sure that the content is downloaded with the given name "QE_content"
    	var downloadLink = document.createElement("a");
    	downloadLink.download="QE_content";
    	downloadLink.href = 'data:application/csv;charset=utf-8,' + encodeURIComponent(csv.replace(/"\"/g, '"'));
    	downloadLink.click();
	});
	
	// When clicking upon the submit QE button
	$("body").on("click", "#submitQEbutton", function(){
		// The table-scroll is emptied when clicking the DE submit button
		// and the for-control is reset.
		$("#QE_info").hide();
		$("#submitQEbutton").hide();
		$(".table-scroll").empty();
		$(".form-control").trigger("reset");
		var attrNames = [];
		var availableGenes = [];
		// Creating a table that is shown on the website.
		var tableContent = '<table id=tpmTable" class="table table-striped table-hover table-condensed table-responsive header-fixed"><thead><tr>';
		$.get("/api/v2/TPM_"+GEODOnPage[0].replace(/-/g,'')).done(function(data){
			// Attributes from the meta data is obtained to determine the different cell types that are known.
			var attr = data.meta["attributes"];
			var data = data["items"];
			$.each(attr, function(t, types){
				// The name of the cell type is obtained.
				if(!(attr[t]["name"].endsWith("low") || attr[t]["name"].endsWith("high") || attr[t]["name"].endsWith("percentile"))){
					// console.log(attr[t]["name"])
					attrNames.push(attr[t]["name"]);
					if (attr[t]["name"].length < 4){
						// Cell types with a length of 4 are seen as an abbreviation and therefore written in uppercase.
						tableContent += '<th>'+ attr[t]["name"].replace(/_/g, " ").toUpperCase()+'</th>';	
					} else {
						// The rest of the cell type names are capitalized.
						tableContent += '<th>'+ capitalizeEachWord(attr[t]["name"].replace(/_/g, " "))+'</th>';
					}
				}
			 });
			// The rest of the header of the table is added together with the body.
			tableContent += '</tr></thead><tbody id="tpmContent" class="searchable"></tbody></table>'
			// This information is added into the div with the id "QETable"
			$("#QETable").append(tableContent);
			
			var stringToAppend = '';
			$.each(data, function(i, content){
				if (i === 0) {
					// The line below is used to create a bargraph of the first found gene on the website (bar graph part).
					obtainTPMofGenes(GEODOnPage[0].replace(/-/g,''), content["external_gene_name"])
				}
				// These genes are saved into the array availableGenes, where it can be used with the search function.
				availableGenes.push(content["external_gene_name"]);
				// For each cell type name.
				$.each(attrNames, function(n, names){
					if (n === 0) {
						// The name of the cell type is added as the first column and identifier of the row.
						stringToAppend += '<tr class="TpmVals" id="'+ data[i][names] +'"><td><b>' + data[i][names] + '</b></td>';
					} else if (n === attrNames.length - 1) {
						// The last item is added and the row is closed.
						stringToAppend += '<td>' + data[i][names] + '</td></tr>';
						// Information is added onto the div with id "tpmContent".
						$('#tpmContent').append(stringToAppend);
						// The string is emptied again.
						stringToAppend = '';
					} else {
						// All items in between the first and last column are added.
						stringToAppend += '<td>'+ data[i][names] +'</td>';
					}
				});
			});
			// The autocomplete function of the searchbar for both the bar graph as the table is defined.
			$(".genelist").autocomplete({
				minLength:2,   
               	delay:0,
				source: availableGenes, 
				});
			// Shows all of the necessary content used for the QE analysis.
			$("#QETable").show();
			$("#QE_content").show();
			$("#searchBar_QE").show();
			$("#QEsearch").show();
			$(".row.DE").show();
			$("#DownloadQE").show();
			$("#selectBarQE").show();
			searchBar("#QEsearch");
		});
		hideDE();
	});


	$("body").on("click", ".TpmVals", function(){
		obtainTPMofGenes(GEODOnPage[0].replace(/-/g,''), $(this).attr("id"));
		});

	$("body").on("click", "#searchGeneBarGraph", function(){
		if (organismOnPage[0] === "Homo sapiens") {
			obtainTPMofGenes(GEODOnPage[0].replace(/-/g,''), $("#geneBarGraph").val().toUpperCase());
		} else if (organismOnPage[0] === "Mus musculus") {
			obtainTPMofGenes(GEODOnPage[0].replace(/-/g,''), capitalizeEachWord($("#geneBarGraph").val()));
		}
	});

	$("body").on("click", "#searchGeneTable", function(){
		var rex = new RegExp("^"+$("#geneTableQE").val(), 'i');
		$('.searchable tr').hide();
		$('.searchable tr').filter(function () {
			return rex.test($(this).text());
		}).show();

	});
	
	$("body").on("click", "#returnToTPMTable", function(){
		$("#QETable").show()
		$("#QEsearch").show();
		$("#returnToTPMTable").hide()
		$('#TPMdiv').empty();
	});
	
});

function hideDE(){
	// DE data is hidden.
	$("#selectBar").hide();
	$("#selectConditions").hide();
	$("#scatterplot").hide();
	$("#searchBar_DE").hide();
	$("#DETable").hide();
}

function hideQE(){
	// QE data is hidden.
	$("#QEsearch").hide();
	$("#QETable").hide();
	$("#QE_content").hide();
}

function exportTableInfo(rowTable, columnRecog) {
	// Grab text from table into CSV formatted string
	// Obtain the different columns within the row
	return '"' + rowTable.map(function (i, row) {
			var $row = $(row);
			$cols = $row.find(columnRecog);

			// Obtains the text from the column of the row
			return $cols.map(function (j, col) {
				var $col = $(col);
					text = $col.text();

				return text.replace(/"/g, ''); // escape double quotes

		}).get().join(tmpColDelim)
		}).get().join(tmpRowDelim)
		.split(tmpRowDelim).join(rowDelim)
		.split(tmpColDelim).join(colDelim) + '"';
}

function obtainTPMofGenes(study, genes) {
	// This function is used to preprocess the data for the creation of the bar graph.
	var dict = {};
	// The gene is searched within the given study.
	$.get("/api/v2/TPM_" + study + "?q=external_gene_name==" + genes).done(function(data){
		// The div with id "TPMdiv" is empied.
		$('#TPMdiv').empty();
		// The attributes are obtained from the meta data and the items are obtained.
		var attr = data.meta["attributes"];
		var data = data["items"];
		$.each(data, function(i, content){
			$.each(attr, function(t, types){
				if (t === 0) {
					// The gene name is obtained and written into the div with id "TPMdiv"
					$("#TPMdiv").html("<h4>"+data[i][attr[t]["name"]]+"</h4>")
				} else if (t % 4 === 1 && t !== 0) {
					// The cell type is obtained.
					if (attr[t]["name"].length > 4){
						// Cell type names with a length > 4 are capitalized.
						dict["Gene"] = capitalizeEachWord(attr[t]["name"].replace(/_/g, " "))
					} else {
						// Cell type names with a length < 4 are seen as an abbreviation and therefore written in uppercase.
						dict["Gene"] = attr[t]["name"].replace(/_/g, " ").toUpperCase()
					}
					// The 'normal' TPM value is added into the dict 'dict'.
					dict["TPM"] = data[i][attr[t]["name"]]
				} else if (t % 4 === 2 && t !== 0) {
					// Information on position 2 when performing t % 4 are saved as the low TPM values.
					dict["Low_TPM"] = data[i][attr[t]["name"]]
				} else if (t % 4 === 3 && t !== 0) {
					// Information on position 3 when performing t % 4 are saved as the high TPM values.
					dict["High_TPM"] = data[i][attr[t]["name"]]
				} else if (t % 4 === 0 && t != 0) {
					// Information on position 0 when performing t % 4 are saved as the percentiles.
					dict["Percentile"] = data[i][attr[t]["name"]]
					// All of the information is pushed to the array bargraphData
					bargraphData.push(dict)
					// The dict 'dict' is empied when in the end.
					if (t !== attr.length) {
						dict = {};
					} 
				}
			});
		});
		// The function createBarGraph is called to create the bargraph
		createBarGraph();
		// The tab with the bargraph is opened when the bargraph is made.
		$('#QE_tabs a[href="#barGraph"]').tab('show')
		// Emptying the array bargraphData in the end. 
		bargraphData = [];
		});
}

// The function render is a function from scatterD3.js
// It contains some small adjustments to work on the GOAD website.
function render(el, obj) {
	var width = 600;
	var height = 400;
	if (width < 0) width = 0;
    if (height < 0) height = 0;
    // Create root svg element
    var svg = d3.select(el).append("svg");
    svg
    .attr("width", width)
    .attr("height", height)
    .attr("class", "scatterD3")
    .append("style")
    .text(".scatterD3 {font: 10px sans-serif;}" +
    ".scatterD3 .axis line, .axis path { stroke: #000; fill: none; shape-rendering: CrispEdges;} " +
    ".scatterD3 .axis .tick line { stroke: #ddd;} " +
    ".scatterD3 .axis text { fill: #000; } " +
    ".scatterD3 .zeroline { stroke-width: 1; stroke: #444; stroke-dasharray: 5,5;} ");

    // Create tooltip content div
    var tooltip = d3.select(".scatterD3-tooltip");
    if (tooltip.empty()) {
        tooltip = d3.select("body")
        .append("div")
        .style("visibility", "hidden")
        .attr("class", "scatterD3-tooltip");
    }

    // Create scatterD3 instance
    scatter = scatterD3().width(width).height(height).svg(svg);
	
    // Check if update or redraw
    var first_draw = (Object.keys(scatter.settings()).length === 0);
    var redraw = first_draw || !obj.settings.transitions;
    var svg = d3.select(el).select("svg").attr("id", "scatterD3-svg-" + obj.settings.html_id);
    scatter = scatter.svg(svg);

    // convert data to d3 format
    data = HTMLWidgets.dataframeToD3(obj.data);

    // If no transitions, remove chart and redraw it
    if (!obj.settings.transitions) {
        svg.selectAll("*:not(style)").remove();
    }

    // Complete draw
    if (redraw) {
        scatter = scatter.data(data, redraw);
        obj.settings.redraw = true;
        scatter = scatter.settings(obj.settings);
        // add controls handlers and global listeners for shiny apps
        scatter.add_controls_handlers();
        scatter.add_global_listeners();
        // draw chart
        d3.select(el)
          .call(scatter);
    }
    // Update only
    else {
        // Check what did change
        obj.settings.has_legend_changed = scatter.settings().has_legend != obj.settings.has_legend;
        obj.settings.has_labels_changed = scatter.settings().has_labels != obj.settings.has_labels;
        obj.settings.size_range_changed = scatter.settings().size_range != obj.settings.size_range;
        obj.settings.ellipses_changed = scatter.settings().ellipses != obj.settings.ellipses;
        function changed(varname) {
            return obj.settings.hashes[varname] != scatter.settings().hashes[varname];
        };
        obj.settings.x_changed = changed("x");
        obj.settings.y_changed = changed("y");
        obj.settings.lab_changed = changed("lab");
        obj.settings.legend_changed = changed("col_var") || changed("symbol_var") ||
                                      changed("size_var") || obj.settings.size_range_changed;
        obj.settings.data_changed = obj.settings.x_changed || obj.settings.y_changed ||
                                    obj.settings.lab_changed || obj.settings.legend_changed ||
                                    obj.settings.has_labels_changed || changed("ellipses_data") ||
                                    obj.settings.ellipses_changed;
        obj.settings.opacity_changed = changed("point_opacity");
        obj.settings.subset_changed = changed("key_var");
        scatter = scatter.settings(obj.settings);
        // Update data only if needed
        if (obj.settings.data_changed) scatter = scatter.data(data, redraw);
    }
}