/*
LOAD REQUIREMENTS
*/


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
url: '/charts/get/${fileName?js_string}.csv',
async: false
}).done(function(csv){
data = $.csv.toArrays(csv);
console.log(data);
});

<#compress>

var colAnnotations = [
    <#if colAnnotations??>
        <#list colAnnotations as ca>
            <#t> "${ca?js_string}"<#if ca_has_next>,</#if>
        </#list>
    </#if>
];

</#compress>

var nCol = ${nCol?js_string};
var nRow = ${nRow?js_string};
var nRowAnnotations = 0;
var nColAnnotations = 0;

$.ajax({
url: '/charts/get/${fileName?js_string}_annotated.svg',
async: false
}).done(function (svgDoc){
//import contents of the svg document into this document
var importedSVGRootElement = document.importNode(svgDoc.documentElement,true);
//append the imported SVG root element to the appropriate HTML element
$("#container").append(importedSVGRootElement);

}
);


/*
FUNCTIONS
*/


$(document).ready(function(){
isMouseDown = false;

$('svg').mousedown(function() {
isMouseDown = true;
})
.mouseup(function() {
isMouseDown = false;
}).mouseleave(function(){
isMouseDown = false;
});

$('path#matrix').mouseenter(function() {
if(isMouseDown){
var row = $(this).attr('row');
$('path[row='+row+']').css('fill', 'red');
}

}).click(function(e) {
var row = $(this).attr('row');
$('path[row='+row+']').css('fill','red');

});


$('path#matrix').qtip({
content: function() {
var row = $(this).attr('row');
var col = $(this).attr('col');
var rowName = data[row][0];
var colName = data[0][col];
var val = data[row][col];
return ("<b>" + rowName + "</b><br/>" + colName + "<br/>" + val);
},
position: {
my: 'bottom center',
at: 'top center',
adjust: {
screen: true,
resize: true
}
},
style: {
tip:true,

classes: 'ui-tooltip-blue ui-tooltip-shadow'
},
show: {
effect: false
},
hide: {
fixed: true
}
});


});
