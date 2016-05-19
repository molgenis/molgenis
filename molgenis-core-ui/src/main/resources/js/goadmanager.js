var uniqueStudies = []
var uniqueTitle = []
var uniqueGEOD = []
var organismOnPage = []
var GEODOnPage = []
var bargraphData = []

$(document).ready(function () {

	obtainStudies();	
	
	// The code below is used to connect the GOAD Facebook page to the website.
	if ($('#fb-root').length > 0) {
        $(function (d, s, id) {
            var js, fjs = d.getElementsByTagName(s)[0];
            if (d.getElementById(id))
                return;
            js = d.createElement(s);
            js.id = id;
            // Connecting to the appID that links to the GOAD facebook page
            js.src = "//connect.facebook.net/en_US/sdk.js#xfbml=1&version=v2.3&appId=654692364667032";
            fjs.parentNode.insertBefore(js, fjs);
        }(document, 'script', 'facebook-jssdk'));
    }  
	
	// When the user clicks upon the contact button, all other divs are hidden and contactInfo is shown.
	$("body").on("click", "#contactButton", function(){
		returnHome();
		$("#accordion").hide();
		$("#contactInfo").show();
	});

	// When clicking upon the return button at the publication part, the homepage is shown again. 
	$("body").on("click", ".returnButton", function(){
		returnHome();
	});
	
	$("body").on("click", "#geneSearch", function(){
		var geneName = [];
		var freqData = [];
		var allInfoFreqData = {};
		var tpmFreqData = {};
		// The filled in value of the input with id geneText is used.
		if ($('#geneText').val() != "") {
			$.each(uniqueGEOD, function(s, studies){
				// The first step is to determine which organism is checked.
				// The information of that organism is used to look up the gene and the link to the ensembl page.
				// An error bar is shown when the gene couldn't be found.
				
				// Checking for the organism 'mice'
				if ($("input[type='radio']:checked").val() === "mice") {
					geneName = capitalizeEachWord($('#geneText').val());
					$.get("/api/v2/miceGenes?q=Associated_Gene_Name=="+geneName).done(function(data){
						if (data["total"] !== 0) {
							var data = data["items"];
							$("#geneInformation").html("<a href='http://www.ensembl.org/Mus_musculus/Gene/Summary?g="+data[0]['Ensembl_Gene_ID']+"' target='_blank'><h4>"+ geneName + "</a>" +
								"<br/>" + capitalizeEachWord(data[0]["Description"].split("[")[0]) + "</h4>");
						} else {
							$("#geneInformation").html('<div class="alert alert-danger" role="alert"><strong>Oops!</strong> The gene: "'+ capitalizeEachWord(geneName) +'" is unknown<br/>Please return and fill in another gene!</div>');
						}
					});
					
				// Checking for the organism 'human'
				} else if ($("input[type='radio']:checked").val() === "human") {
					geneName = $('#geneText').val().toUpperCase();
					$.get("/api/v2/humanGenes?q=Associated_Gene_Name=="+geneName).done(function(data){
						if (data["total"] !== 0) {
							var data = data["items"];
							$("#geneInformation").html("<a href='http://www.ensembl.org/Home_sapiens/Gene/Summary?g="+data[0]['Ensembl_Gene_ID']+"' target='_blank'><h4>"+ geneName + "</a>" +
								"<br/><h5>" + capitalizeEachWord(data[0]["Description"].split("[")[0]) + "</h4>");
						} else {
							$("#geneInformation").html('<div class="alert alert-danger" role="alert"><strong>Oops!</strong> The gene: "'+ capitalizeEachWord(geneName) +'" is unknown<br/>Please return and fill in another gene!</div>');
						}
					});
				}
				
				//The given gene is searched within all of the studies.
				$.get("/api/v2/TPM_"+studies.replace(/-/g,"")+"?q=external_gene_name=="+geneName).done(function(data){
					// The process continues when information is found.)
					if (data["total"] !== 0) {
					// The div with id dashboard is shown (necessary otherwise the image wont be created).
					$("#dashboard").show();
						// Attributes are obtained from the meta data
						// Items are obtained.
						var attr = data.meta["attributes"];
						var data = data["items"];
						
						//Each data item and attribute is walked through.
						$.each(data, function(i, content){
							$.each(attr, function(t, types){
								// The 1st position when performing item % 4 and is used to obtain the cell type name.
								if (t % 4 === 1 && t !== 0) {
									if (attr[t]["name"].length > 4){
										// The names of the cell types are capitalized when the length > 4.
										allInfoFreqData["Gene"] = capitalizeEachWord(attr[t]["name"].replace(/_/g, " "))
									} else {
										// Cell types with a length of 4 are seen as an abbreviation and therefore written in Uppercase.
										allInfoFreqData["Gene"] = attr[t]["name"].replace(/_/g, " ").toUpperCase()
									}
									// The 'normal' TPM values are obtained as well
									tpmFreqData["TPM"] = data[i][attr[t]["name"]]
								} else if (t % 4 === 2 && t !== 0) {
									// The TPM low values are obtained
									// Found on the 2th position when performing item % 4.
									tpmFreqData["TPM Low"] = data[i][attr[t]["name"]]
								} else if (t % 4 === 3 && t !== 0) {
									// Found on the 3th position when performing item % 4.
									tpmFreqData["TPM High"] = data[i][attr[t]["name"]]
									// All of the obtained info about the cell type is saved into allInfoFreqData
									allInfoFreqData["TPMvals"] = tpmFreqData
									// The dict tpmFreqData is cleared for the next cell type (only TPM values)
									tpmFreqData = {};
									// The dict allInfoFreData is pushed into the dict freData so it can be used by the
									// function dashboard in the end.
									freqData.push(allInfoFreqData);
									// The dict allInfoFreqData is cleared when all of the celltypes within a study are walked through.
									if (t !== attr.length) {
										allInfoFreqData = {};
									}	 
								}
							});
						});
						
						// A div is created for each study (Making sure that the page doesn't look messed up in the end).
						$("#dashboard").append("<div id='" + studies.replace(/-/g,"") + "'>")
						// The title, author and research link are obtained from the global study information.
						$.get('/api/v2/Test_studies?attr=Title&q=GEOD_NR=="'+studies+'"').done(function(data){
								var data = data["items"];
								// This information is added into the div, making sure that it is clear which dashboard belongs to which study.
								if (data[1]['Research_link'] === "Unknown") {
									
									$("#"+studies.replace(/-/g,"")).append("<p class='svgTitle'><b>"+ data[1]["Title"] +"</b><br/>By "+ data[1]["Author"] + "</a>"+ "</p> <br/>");
								} else {
									$("#"+studies.replace(/-/g,"")).append("<p class='svgTitle'><b>"+ data[1]["Title"] +"</b><br/>By <a href='"+data[1]['Research_link']+"' target='_blank'>"+ data[1]["Author"] + "</a>"+ "</p> <br/>");
								}
								// This information is added into the div, making sure that it is clear which doashboard belongs to which study.
							});
						// The dashboard is created for the study.
						dashboard('#'+studies.replace(/-/g,""),freqData);
						// freqData is cleared.
						freqData = [];
					} 
				});
			});
		}

	// The accordion is resetted to the 'normal' state.
	$('#collapseOne').collapse("show");
	$('#collapseTwo').collapse("hide");
	// A return button is shown.
	$("#returnTPM").show();
	// The accordion is hidden.
	$("#accordion").hide();
	// The gene part is shown (part with the dashboards).
	$("#genePart").show();
	// The search value is cleared.
	$('#geneText').val("");
	});
	
	// When clicking upon the conditionSearch button
	$("body").on("click", "#conditionSearch", function(){
		// The tableContent is emptied
		$("#tableContent").empty();
		if ($('#conditionText').val() != "") {
			// Studies that contain the condition given in the searchbar are returned.
			$.get("/api/v2/Test_studies?q=Abstract=q="+$("#conditionText").val().replace(/ /g,'_')).done(function(data){
			var data = data["items"];
			var tdstart = "<td>";
			var tdend = "</td>";
			uniqueTitle = [];
			uniqueStudies = [];
			// The found studies are loaded into the studyTable.
			$.each(data, function(i, item){
				if ($.inArray(data[i]["Title"], uniqueTitle)== -1) {
					uniqueTitle.push(data[i]["Title"]);
					uniqueStudies.push(
						"<tr class='studyTable' id='" + data[i]["GEOD_NR"] + "' >" 
						+ tdstart + data[i]["Title"] + tdend
						+ tdstart + data[i]["Author"] + tdend 
						+ tdstart + data[i]["Organism"] + tdend 
						+ tdstart + data[i]["Year"] + tdend 
						+ "</tr>" );
		 			}
			});
		// These studies are sorted alphabetically on the GEOD number.
		uniqueStudies.sort()
		// Studies are joined with <br/> and written into the div tableContent.
		$("#tableContent").html(uniqueStudies.join("<br/>"));
			});
	// The accordion with the studies will open and the condition search part will close.
	$('#collapseOne').collapse("show");
	$('#collapseThree').collapse("hide");
	// A refresh button is showed to refresh all of the studies again.
	$("#refreshPublications").show();
	// The condition searchbar is emptied again.
	$('#conditionText').val("");
	}});

	// When clicking upon the refresh button 
	$("body").on("click", "#refreshPublications", function(){
	// All of the known studies are obtained. 
	$.get("/api/v2/Test_studies?attrs=Unique_ID,GEOD_NR,Title,Author,Organism,Year,Research_link&num=10000").done(function(data){
		var data = data["items"];
		var tdstart = "<td>";
		var tdend = "</td>";
		uniqueTitle = [];
		uniqueStudies = [];
		// Each of the known studies is added into the variable uniqueStudies.
		$.each(data, function(i, item){
			if ($.inArray(data[i]["Title"], uniqueTitle)== -1) {
				uniqueTitle.push(data[i]["Title"]);
				uniqueStudies.push(
					"<tr class='studyTable' id='" + data[i]["GEOD_NR"] + "' >" 
					+ tdstart + data[i]["Title"] + tdend
					+ tdstart + data[i]["Author"] + tdend 
					+ tdstart + data[i]["Organism"] + tdend 
					+ tdstart + data[i]["Year"] + tdend 
					+ "</tr>" );
	 			}
			});
		// These studies are sorted alphabetically on the GEOD number.
		uniqueStudies.sort()
		// Studies are joined with <br/> and written into the div tableContent.
		$("#tableContent").html(uniqueStudies.join("<br/>"));
	});
	// The refresh button is hidden again.
	$("#refreshPublications").hide();
	});

	//----------------------//
	//Tutorial part of GOAD.//
	//----------------------//
	
	// When clicking upon the close button of the tutorial pop up
	$("body").on("click", "#closeModal", function(){
		// Information about the QE and DE analysis is hidden.
		$('#QEanalysis').collapse('hide');
		$('#DEanalysis').collapse('hide');
	});
	
	// When clicking upon the DE information in the tutorial button the QE information is hidden.
	$("body").on("click", "#tutorialDEinfo", function(){
		$('#QEanalysis').collapse('hide');
	});

	// When clicking upon the QE information in the tutorial button the DE information is hidden.
	$("body").on("click", "#tutorialQEinfo", function(){
		$('#DEanalysis').collapse('hide');
	});

	// The input id filter is called (this input is used to filter the studies on the homepage).
    searchBar("#filter");
    
});

//----------------------//
// 	  Functions GOAD 	//
//----------------------//

function searchBar(inputName){
	$(inputName).keyup(function () {
    var rex = new RegExp($(this).val(), 'i');
    $('.searchable tr').hide();
    $('.searchable tr').filter(function () {
        return rex.test($(this).text());
    }).show();
});
}

function returnHome(){
	$("#accordion").show();
	$(function() {
	  $('#selectConditions').select2('data', null)
	})
	hideDE();
	hideQE();
	$(".alert-danger").hide();
	$("#publicationPart").hide();
	$("#contactInfo").hide();
	$("#genePart").hide();
	$("#noQEFound").remove();
	$("#refreshPublications").hide();
	$("#dashboard").empty().hide();
	$("#TPMdiv").empty();
	$("#DE_info").hide();
	$("#QE_info").hide();
	obtainStudies();
}

function obtainStudies() {
	// The studies known in the studies database are collected
	$.get("/api/v2/Test_studies?attrs=Unique_ID,GEOD_NR,Title,Author,Organism,Year,Research_link&num=10000").done(function(data){
		// The items within the data are collected and saved as variable data.
		var data = data["items"];
		var tdstart = "<td>";
		var tdend = "</td>";
		// For each element in the variable data
		$.each(data, function(i, item){
			// If the title is unknown in the variable uniqueTitle
			if ($.inArray(data[i]["Title"], uniqueTitle)== -1) {
				// The title of the given study is pushed to the variable uniqueTitle
				uniqueTitle.push(data[i]["Title"]);
				// The EGEOD number of the given study is pushed to the variable uniqueGEOD
				uniqueGEOD.push(data[i]["GEOD_NR"]);
				// Information like the GEOD number, title, author, organism and year are pushed to the
				// variable uniqueStudies in the form of a row of a table.
				uniqueStudies.push(
					"<tr class='studyTable' id='" + data[i]["GEOD_NR"] + "' >" 
					+ tdstart + data[i]["Title"] + tdend
					+ tdstart + data[i]["Author"] + tdend 
					+ tdstart + data[i]["Organism"] + tdend 
					+ tdstart + data[i]["Year"] + tdend 
					+ "</tr>" );
	 			}
			});
		// The uniqueStudies are sorted, leading to a table where the studies are sorted on the GEOD number.
		uniqueStudies.sort()
		// The sorted table is joined with <br/> and added to the div tableContent.
		$("#tableContent").html(uniqueStudies.join("<br/>"));
	});
}

function capitalizeEachWord(str) {
	// Each word ending with a space is used within the function.
	// This function makes sure that each word within the string is capitalized.
    return str.replace(/\w\S*/g, function(txt) {
        return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
    });
}

function dashboard(id, fData){
    var barColor = '#002080';
    function segColor(c){ return {"TPM High":"#0080ff", "TPM Low":"#999999","TPM":"#ffcc00"}[c]; }
    
    // compute total values that can be seen without hovering over the pie chart
    fData.forEach(function(d){d.total=d.TPMvals.TPM;});
    
    // function to handle histogram.
    function histoGram(fD){
        var hG={},    hGDim = {t: 60, r: 0, b: 30, l: 0};
        hGDim.w = 700 - hGDim.l - hGDim.r, 
        hGDim.h = 300 - hGDim.t - hGDim.b;
            
        var tip = d3.tip()
		  .attr('class', 'd3-tip')
		  .offset([-10, 0])
		  .html(function(d) {
		    return "<strong>TPM value : </strong>" + d[1]
		  });

        //create svg for histogram.
        var hGsvg = d3.select(id).append("svg")
            .attr("width", hGDim.w + hGDim.l + hGDim.r)
            .attr("height", hGDim.h + hGDim.t + hGDim.b).append("g")
            .attr("transform", "translate(" + hGDim.l + "," + hGDim.t + ")");

        hGsvg.call(tip);

        // create function for x-axis mapping.
        var x = d3.scale.ordinal().rangeRoundBands([0, hGDim.w], 0.1)
                .domain(fD.map(function(d) { return d[0]; }));

        // Add x-axis to the histogram svg.
        hGsvg.append("g").attr("class", "x axis")
            .attr("transform", "translate(0," + hGDim.h + ")")
            .call(d3.svg.axis().scale(x).orient("bottom"));

        // Create function for y-axis map.
        var y = d3.scale.linear().range([hGDim.h, 0])
                .domain([0, d3.max(fD, function(d) { return d[1]; })]);

        // Create bars for histogram to contain rectangles and freq labels.
        var bars = hGsvg.selectAll(".bar").data(fD).enter()
                .append("g").attr("class", "bar");
        
        //create the rectangles.
        bars.append("rect")
            // .attr("x", function(d) { return x(d[0]); })
            .attr("x", function(d) { return x(d[0]) + (x.rangeBand() - d3.min([x.rangeBand(), 100]))/2; })
            .attr("y", function(d) { return y(d[1]); })
            // .attr("width", x.rangeBand())
            .attr("width", d3.min([x.rangeBand(), 100]))
            .attr("height", function(d) { return hGDim.h - y(d[1]); })
            .style('fill',barColor)
            .on("mouseover",tip.show)
            .on("mouseout",tip.hide)

           	// .attr("width", d3.min([x1.rangeBand(), 100]))
	    	// .attr("x", function(d) { return x1(d.name) + (x1.rangeBand() - d3.min([x1.rangeBand(), 100]))/2; })
            
        //Create the frequency labels above the rectangles.
        bars.append("text").text(function(d){return parseFloat(d3.format(",")(d[1])).toFixed(2)})
            .attr("x", function(d) { return x(d[0])+x.rangeBand()/2; })
            .attr("y", function(d) { return y(d[1])-5; })
            .attr("text-anchor", "Middle");
        
        function mouseover(d){  // utility function to be called on mouseover.
            // filter for selected Gene.
            var st = fData.filter(function(s){ return s.Gene == d[0];})[0],
                nD = d3.keys(st.TPMvals).map(function(s){ return {type:s, TPMvals:st.TPMvals[s]};});
        }
        
        // create function to update the bars. This will be used by pie-chart.
        hG.update = function(nD, color){
            // update the domain of the y-axis map to reflect change in frequencies.
            y.domain([0, d3.max(nD, function(d) {return d[1]; })]);
            
            // Attach the new data to the bars.
            var bars = hGsvg.selectAll(".bar").data(nD);
            
            // transition the height and color of rectangles.
            bars.select("rect").transition().duration(500)
                .attr("y", function(d) {return y(d[1]); })
                .attr("height", function(d) { return hGDim.h - y(d[1]); })
                .style("fill", color);

            // transition the frequency labels location and change value.
            bars.select("text").transition().duration(500)
                .text(function(d){ return parseFloat(d3.format(",")(d[1])).toFixed(2)})
                .attr("y", function(d) {return y(d[1])-5; });            
        }        
        return hG;
    }
    
    // function to handle pieChart.
    function pieChart(pD){
        var pC ={},    pieDim ={w:250, h: 250};
        pieDim.r = Math.min(pieDim.w, pieDim.h) / 2;
                
        // create svg for pie chart.
        var piesvg = d3.select(id).append("svg")
            .attr("width", pieDim.w).attr("height", pieDim.h).append("g")
            .attr("transform", "translate("+pieDim.w/2+","+pieDim.h/2+")");
        
        // create function to draw the arcs of the pie slices.
        var arc = d3.svg.arc().outerRadius(pieDim.r - 10).innerRadius(0);

        // create a function to compute the pie slice angles.
        var pie = d3.layout.pie().sort(null).value(function(d) { return d.TPMvals; });

        // Draw the pie slices.
        piesvg.selectAll("path").data(pie(pD)).enter().append("path").attr("d", arc)
            .each(function(d) { this._current = d; })
            .style("fill", function(d) { return segColor(d.data.type); })
            .on("mouseover",mouseover).on("mouseout",mouseout);
    
        // Utility function to be called on mouseover a pie slice.
        function mouseover(d){
            // call the update function of histogram with new data.
            hG.update(fData.map(function(v){ 
            	return [v.Gene, v.TPMvals[d.data.type]];}),segColor(d.data.type));
        }
        //Utility function to be called on mouseout a pie slice.
        function mouseout(d){
            // call the update function of histogram with all data.
            hG.update(fData.map(function(v){
                return [v.Gene,v.total];}), barColor);
        }
        // Animating the pie-slice requiring a custom function which specifies
        // how the intermediate paths should be drawn.
        function arcTween(a) {
            var i = d3.interpolate(this._current, a);
            this._current = i(0);
            return function(t) { return arc(i(t));    };
        }    
        return pC;
    }
    
    // function to handle legend.
    function legend(lD){
        // var leg = {};
            
        // create table for legend.
        var legend = d3.select(id).append("table").attr('class','legend');
        
        // create one row per segment.
        var tr = legend.append("tbody").selectAll("tr").data(lD).enter().append("tr");
            
        // create the first column for each segment.
        tr.append("td").append("svg").attr("width", '16').attr("height", '16').append("rect")
            .attr("width", '16').attr("height", '16')
			.style("fill",function(d){ return segColor(d.type); });
            
        // create the second column for each segment.
        tr.append("td").text(function(d){ return d.type;});

    }
    
    // calculate total frequency by segment for all Gene.
    var tF = ["TPM High","TPM Low","TPM"].map(function(d){ 
        return {type:d, TPMvals: d3.sum(fData.map(function(t){ return t.TPMvals[d];}))}; 
    });    
    
    // calculate total frequency by Gene for all segment.
    var sF = fData.map(function(d){return [d.Gene,d.total];});

    var hG = histoGram(sF), // create the histogram.
        pC = pieChart(tF), // create the pie-chart.
        leg= legend(tF);  // create the legend.
}