 function parseDate(str) {
 	if(!/^(\d){8}$/.test(str)) return "invalid date";
 	var y = str.substr(0,4),
 	m = str.substr(4,2),
 	d = str.substr(6,2);
 	return new Date(y,m,d);
 }
 
 $('#container').highcharts({
 	chart: {
 		zoomType: 'x',
 		spacingRight: 20	
 	},
 	title: {
 		text: '${chart.title}'
 	},
 	yAxis: {
 		title: {
 			text: '${chart.yLabel}'
 		}
 	},
 	xAxis: {
 		type: 'datetime',
		maxZoom: 5 * 24 * 3600000, // fourteen days
		title: {
			text: '${chart.xLabel}'
		}
	},
	navigator:{
		enabled: true
	},
	scrollbar: {
	    enabled: true
	},
	exporting: {
         enabled: true
    },
	plotOptions: {
		line: {		
			marker: {
				enabled: false
			}
		}
	},
	tooltip: {
    	valueSuffix: 'C'
    },
    credits: {
    	enabled: false
  	},
	series: [{
		<#list chart.data as series>
		name: '${series.name}',
		pointInterval: 24 * 3600 * 1000,
		pointStart: Date.UTC(2013, 00, 01),
		data: [
			<#list series.data as point>
			${point.yvalue!"null"} <#if point_has_next>,</#if>
			</#list>
		]
			<#if series_has_next> 
				}, { 
			<#else> 
				}]
			</#if>
		</#list>
	
});