function createBarGraph(){
	// The size of the bargraph is defined.
	var margin = {top: 20, right: 20, bottom: 30, left: 40},
		width = ($("#QE_content").width() * 0.85) - margin.left - margin.right,
	    height = 500 - margin.top - margin.bottom;

	var x0 = d3.scale.ordinal()
	    .rangeRoundBands([0, width], .1);

	var x1 = d3.scale.ordinal();

	var y = d3.scale.linear()
	    .range([height, 0]);

	// Colors for the columns is defined.
	var color = d3.scale.ordinal()
		.domain(["0-5 percentile: very high expression", 
				"5-10 percentile: high expression",
				"10-20 percentile: moderately high expression",
				"20-30 percentile: moderately high expression",
				"30-40 percentile: moderate expression",
				"40-50 percentile: moderate expression",
				"50-60 percentile: moderate expression",
				"60-70 percentile: low expression",
				"70-80 percentile: low expression",
				"80-90 percentile: low expression",
				"90-100 percentile: very low expression"
	  			])
	    .range(["#ff0000", 		//Red	
				"#ff6600",		//Orange
				"#ffff00", 		//Yellow
				"#ccff33", 		//Greenyellow 	
				"#66ff33", 		//Green
				"#66ffff", 		//Turqoise
				"#33ccff", 		//Darkturqoise
				"#0000ff", 		//Blue
				"#000033", 		//Darkblue
				"#0000gg", 		//Midnightblue	    		
				"#000000" 		//Black	    		
				]); 	

	    	//"#ff6600", "#ffff00", "#ccff33", "#66ff33", "#66ffff", "#33ccff", "#0000ff", "#0000gg", "#000033", "#000000"]);

	// X axis is defined.
	var xAxis = d3.svg.axis()
	    .scale(x0)
	    .orient("bottom");

	// Y axis is defined.
	var yAxis = d3.svg.axis()
	    .scale(y)
	    .orient("left")
	    .tickFormat(d3.format(".2s"));

	// The tooltip is defined.
	var tip = d3.tip()
	  .attr('class', 'd3-tip')
	  .offset([-10, 0])
	  .html(function(d) {
	  	var percentileInfo = d.Percentile.split(": ")
	    return "TPM value : " + d.TPM + "<br/><br/>TPM Low value : " + d.Low_TPM +"<br/><br/>TPM High value : " + d.High_TPM + "<br/><br/>" + capitalizeEachWord(percentileInfo[1])
	})

	// The svg is drawn and added into the div with the id "TPMdiv"
	var svg = d3.select("#TPMdiv").append("svg")
	    .attr("width", width + margin.left + margin.right)
	    .attr("height", height + margin.top + margin.bottom)
	    .attr("class", "tpmValsPlot")
	  	.append("g")
	    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

	  // The type of information shown (also in the legend) are defined.
	  svg.call(tip);
	  
	  // Defining the tpmTypes (these types will be drawn).
	  var tpmTypes = ["TPM"];
	  // var tpmTypes = ["Percentile"];
	  var percentileTypes = ["0-5 percentile: very high expression", 
	  						"5-10 percentile: high expression",
	  						"10-20 percentile: moderately high expression",
	  						"20-30 percentile: moderately high expression",
	  						"30-40 percentile: moderate expression",
	  						"40-50 percentile: moderate expression",
	  						"50-60 percentile: moderate expression",
	  						"60-70 percentile: low expression",
	  						"70-80 percentile: low expression",
	  						"80-90 percentile: low expression",
	  						"90-100 percentile: very low expression"];

	  // The tpmVals are obtained.
	  bargraphData.forEach(function(d) {
	    d.tpmVal = tpmTypes.map(function(name) { return {name: name, value: +d[name]}; });
	  });

	  x0.domain(bargraphData.map(function(d) { return d.Gene; }));
	  x1.domain(tpmTypes).rangeRoundBands([0, x0.rangeBand()]);
	  y.domain([0, d3.max(bargraphData, function(d) { return d3.max(d.tpmVal, function(d) { return d.value; }); })]);

	  // Adds the X axis to the svg.
	  svg.append("g")
	      .attr("class", "x axis")
	      .attr("transform", "translate(0," + height + ")")
	      .call(xAxis);

	  // Adds the Y axis to the svg.
	  svg.append("g")
	      .attr("class", "y axis")
	      .call(yAxis)
	      .append("text")
	      .attr("transform", "rotate(-90)")
	      .attr("y", 6)
	      .attr("dy", ".71em")
	      .style("text-anchor", "end")
	      .text("TPM value");

	  // Defines state (used to draw all of the bars).
	  var state = svg.selectAll(".state")
	      .data(bargraphData)
	      .enter().append("g")
	      .attr("class", "state")
	      .attr("transform", function(d) { return "translate(" + x0(d.Gene) + ",0)"; })
	      .attr("fill", function(d) { return color(d.Percentile); })
	      .on('mouseover', tip.show)
	      .on('mouseout', tip.hide);

	  // Adds each bar to the bargraph.
	  state.selectAll("rect")
	      .data(function(d) { return d.tpmVal; })
	      .enter().append("rect")
	      // .attr("width", x1.rangeBand())
	      .attr("width", d3.min([x1.rangeBand(), 100]))
	      // .attr("x", function(d) { return x1(d.name); })
	      .attr("x", function(d) { return x1(d.name) + (x1.rangeBand() - d3.min([x1.rangeBand(), 100]))/2; })
	      .attr("y", function(d) { return y(d.value); })
	      .attr("height", function(d) { return height - y(d.value); })
	      // .attr("fill", function(d) { return color(d.Percentile); });

	  // Adds the legend to the svg.
	  var legend = svg.selectAll(".legend")
	      // .data(tpmTypes.slice().reverse())
	      .data(percentileTypes.slice().reverse())
	      .enter().append("g")
	      .attr("class", "legend")
	      .attr("transform", function(d, i) { return "translate(0," + i * 20 + ")"; });

	  // Adds the color to the legend.
	  legend.append("rect")
	      .attr("x", width + 13)
	      .attr("width", 18)
	      .attr("height", 18)
	      .style("fill", color);

	  // Adds the names of the legend.
	  legend.append("text")
	      .attr("x", width + 7)
	      .attr("y", 9)
	      .attr("dy", ".35em")
	      .style("text-anchor", "end")
	      .text(function(d) {var percentileNumber = d.split(": "); return capitalizeEachWord(percentileNumber[0]); });
}