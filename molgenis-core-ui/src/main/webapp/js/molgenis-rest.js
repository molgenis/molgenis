/* contains helper script for the REST API wizard */
function generateRestUrl(fields)
{	
	url = window.location.href.substring(0,window.location.href.indexOf('?')) + '?';
	//skip the last element because that is submit button
	for(i = 0 ; i < fields.length - 1; i++)
	{
		if(i > 0 && i % 3 == 0) url += '&';
		url += fields[i].value;
	}
	
	alert(url);
	window.open(url);
}