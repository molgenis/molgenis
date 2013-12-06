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

$.ajax({
	url: '/charts/get/${fileName}.svg',
	async: false
}).done(function (svgDoc){
	//import contents of the svg document into this document
	var importedSVGRootElement = document.importNode(svgDoc.documentElement,true);
	//append the imported SVG root element to the appropriate HTML element
	$("#container").append(importedSVGRootElement);
	annotateHeatMap();
}
);



/*
	FUNCTIONS
*/

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
	size = nCol * nCol;

	// node where matrix part of plot starts
	matrixStart = 0;

	// ANNOTATE THE HEAT MAP:
	
	startPoint = nCol;
	// do we need to add column annotations?
	if (typeof colAnnotations.length == 0){
		console.log("tipping the column annotations")
		// col groups, temp for now: 
		$("svg > g > g > path").slice(0, nCol)
		.each(function(index){
			$(this).qtip({content: colAnnotations[index]})
		});

		
	}else{
		startPoint = 0;
	}

	
	// first col, first row:
	var rowsLeft = nCol;
	var thisCol = 1;
	test = nCol + nCol + nRow;
	console.log("tipping <g> " + startPoint + " to " + test);
	$("svg > g > g > path").slice(startPoint, startPoint + nCol + nRow)
	.each(function(){
		if(rowsLeft == 1){
			$(this).qtip(getTipConfig(rowsLeft, thisCol));
			thisCol++;
		}else{
			$(this).qtip(getTipConfig(rowsLeft, thisCol));
			rowsLeft--;
		}
	});

	//the rest:
	var thisRow = nCol;
	var thisCol = 2;
	console.log("tipping <path> 0 to " + (nCol-1)*(nCol-1));
	$("svg > g > path").slice(0, (nCol-1)*(nCol-1)).each(function(index){
		if(thisRow == 2){
			$(this).qtip(getTipConfig(thisRow, thisCol));
			thisRow = nCol;
			thisCol++;
		}else{
			$(this).qtip(getTipConfig(thisRow, thisCol));
			thisRow--;
		}
	});

}