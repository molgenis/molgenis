/*
	LOAD REQUIREMENTS
*/

//load css for qtip
$('<link rel="stylesheet" type="text/css" href="/css/jquery.qtip.min.css" />').appendTo('head');

// load libraries:
$.ajax({
	url: '/js/jquery.csv-0.71.min.js',
	async: false
});

$.ajax({
	url: '/js/jquery.qtip.min.js',
	async: false
});


/*
	GET DATA FOR PLOT
*/

// read csv data
var data;
$.ajax({
	url: '/charts/get/${fileName}.csv',
	async: false
}).done(function(csv){
	data = $.csv.toArrays(csv);
	console.log(data);
})

<#compress>

var colAnnotations = [
	<#if colAnnotations??>
		<#list colAnnotations as ca>
			<#t> "${ca}"<#if ca_has_next>,</#if>
		</#list>
	</#if>
];

</#compress>

var nCol = ${nCol}; 
var nRow = ${nRow};
var nRowAnnotations = 0;
var nColAnnotations = 0;

$.ajax({
	url: '/charts/get/${fileName}_annotated.svg',
	async: false
}).done(function (svgDoc){
	//import contents of the svg document into this document
	var importedSVGRootElement = document.importNode(svgDoc.documentElement,true);
	//append the imported SVG root element to the appropriate HTML element
	$("#container").append(importedSVGRootElement);
	//annotateHeatMap();
}
);



/*
	FUNCTIONS
*/

$('path').click(function(e) {
    //console.log($(this));
    //console.log($(this).attr('row'));
});

$('g').click(function(e) {
	console.log("name lcick y'all");
  $(this).border('thin solid black');
 });
 
function getTipConfig(x, y){
	return {
		content: {
			title: "<b>" + data[x][0] + "</b>",
			text: ("<b>sample:</b> " + data[0][y] + "<br/><b>value:</b> " + data[x][y])             
		},
		position: { my: 'bottom center', at: 'top center' },
		show: {delay: 0}
	};
}

function annotateHeatMap(){
	// ORDER OF DRAWING IS:
	// 1. row annotations (if any)
	// 2. col annotations (if any)
	// 3. matrix
	// 4. column names
	// 5. row names
	// 6. the rest (dendrograms)

	// ORDER OF DRAWING FOR VALUES (step 1, 2 and 3) IS:
	// <g> elements:
	// first column from bottom to top
	// first row from left to right
	// <path> elements:
	// rest of the columns from top to bottom except first row
	
	var lastG = 0;		// set starting index
	var lastPath = 0;	// set starting index

	// counters for number of g or path elements per group
	var nG = 0;
	var nPath = 0;

	// store G and Path elements for efficiency:
	//var gElements = $("svg > g > g > path");
	var gElements = $("svg > g > g");
	var pathElements = $("svg > g > path");

	// 1. ROW ANNOTATIONS
	console.log(nRowAnnotations + " row annotations to add");
	
	if (nRowAnnotations > 0){
		// <g> elements:
		nG = (nRow + (nRowAnnotations - 1));

		gElements.slice(lastG, lastG + nG).each(function(){
 			//TODO
 		});

 		if (nRowAnnotations > 1){
 			// <path> elements:
 			nPath = (nRow - 1) * (nRowAnnotations - 1);

 			pathElements.slice(lastPath, lastPath + nPath).each(function(){
 				//TODO
 			});
 		}	

 		// set the indexes for the following step, and reset the counters
 		lastG += nG;
 		lastPath += nPath;
 		nG = 0;
 		nPath = 0;
	}


	// 2. COLUMN ANNOTATIONS
	console.log(nColAnnotations + " column annotations to add");

	if (nColAnnotations > 0){
		// <g> elements:
		nG = nCol + (nColAnnotations - 1);
		gElements.slice(lastG, lastG + nG).each(function(){
			//TODO
		});

		if (nColAnnotations > 1){
			// <path> elements:
			nPath = (nCol - 1) * (nColAnnotations - 1);
			pathElements.slice(lastPath, lastPath + nPath).each(function(){
				//TODO
			});
		}

		// set the indexes for the following step, and reset the counters
 		lastG += nG;
 		lastPath += nPath;
 		nG = 0;
 		nPath = 0;
	}


	// 3. MATRIX
	console.log("matrix with " + nCol + " columns and " + nRow + " rows to add (" + (nCol*nRow) + " values)");

	// <g> elements:
	nG = (nRow - 1) + nCol;
	gElements.slice(lastG, lastG + nG).each(function(){
		//TODO
		$("path", this).css('fill', 'LightSeaGreen');
	});

	// <path> elements:
	nPath = (nRow - 1)*(nCol - 1);
	pathElements.slice(lastPath, lastPath + nPath).each(function(){
		//TODO
		$(this).css('fill', 'LightSkyBlue');
	});

	lastG += nG;
	lastPath += nPath;
	nG = 0;
	nPath = 0;


	// 4. COLUMN NAMES
	console.log(nCol + " column names to add");

	gElements.slice(lastG, lastG + nCol).each(function(){
		$(this).css('fill', 'Gold');
	});
	lastG += nCol;

	// 5. ROW NAMES
	console.log(nRow + " row names to add");

	gElements.slice(lastG, lastG + nRow).each(function(){
		$(this).css('fill', 'GoldenRod');
	});
	lastG += nRow;

}

// function annotateHeatMap(){
// 	size = nCol * nCol;

// 	// node where matrix part of plot starts
// 	matrixStart = 0;

// 	// ANNOTATE THE HEAT MAP:
	
// 	startPoint = nCol;
// 	// do we need to add column annotations?
// 	if (typeof colAnnotations.length == 0){
// 		console.log("tipping the column annotations")
// 		// col groups, temp for now: 
// 		$("svg > g > g > path").slice(0, nCol)
// 		.each(function(index){
// 			$(this).qtip({content: colAnnotations[index]})
// 		});

		
// 	}else{
// 		startPoint = 0;
// 	}

	
// 	// first col, first row:
// 	var rowsLeft = nRow;
// 	var thisCol = 1;
// 	test = nCol + nCol + nRow;
// 	console.log("tipping <g> " + startPoint + " to " + test);
// 	$("svg > g > g > path").slice(startPoint, startPoint + nCol + nRow)
// 	.each(function(){
// 		if(rowsLeft == 1){
// 			$(this).qtip(getTipConfig(rowsLeft, thisCol));
// 			console.log(data[0][thisCol] + " " + data[rowsLeft][0]);
// 			thisCol++;
// 		}else{
// 			$(this).qtip(getTipConfig(rowsLeft, thisCol));
// 			console.log(data[0][thisCol] + " " + data[rowsLeft][0]);
// 			rowsLeft--;
// 		}
// 	});

// 	//the rest:
// 	var thisRow = nRow;
// 	var thisCol = 2;
// 	console.log("tipping <path> 0 to " + (nCol-1)*(nCol-1));
// 	$("svg > g > path").slice(0, (nCol-1)*(nRow-1)).each(function(index){
// 		if(thisRow == 2){
// 			$(this).qtip(getTipConfig(thisRow, thisCol));
// 			console.log(thisRow + " " + data[0][thisCol] + " " + data[thisRow][0]);
// 			thisRow = nCol;
// 			thisCol++;

// 		}else{
// 			$(this).qtip(getTipConfig(thisRow, thisCol));
// 			console.log(thisRow + " " + data[0][thisCol] + " " + data[thisRow][0]);
// 			thisRow--;
// 		}
// 	});

// }