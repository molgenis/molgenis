function setContent(popup, text) 
{
	var content = ''
	content += '<table bgcolor="#FFFDD2" width="400px" >'
	content += '  <tr>'
	content += '    <td width="100%"><font size="2">' + text + '</font></td>'
	content += '    <td valign="top"> <img src="img/cancel.png" onclick="javascript:hidePopup(\'' + popup + '\')"> </td>'
	content += '  </tr>'
	content += '</table>'
  
	document.getElementById(popup).innerHTML = content; 
	//thanks to http://alistapart.com/d/footers/footer_variation1.html
}

function hidePopup(popup) 
{
	document.getElementById(popup).style.visibility = "hidden";
	document.getElementById(popup).innerHTML = ""; 
	document.getElementById(popup).style.left = -1000;
	document.getElementById(popup).style.top = -1000;  
}

function showPopup(event, popup, text) 
{
	if (document.all) // IE
	{
		document.getElementById(popup).style.left = event.clientX + document.body.scrollLeft;
		document.getElementById(popup).style.top = event.clientY + document.body.scrollTop;
	} 
	else //FF + NN
	{
		document.getElementById(popup).style.left = event.pageX;
		document.getElementById(popup).style.top = event.pageY;
	}
	setContent(popup, text);
	
	//correct position
	windowWidth = getWindowWidth();
	elementWidth = document.getElementById(popup).offsetWidth;
	if(document.getElementById(popup).offsetLeft + elementWidth > windowWidth)
	{
		document.getElementById(popup).style.left = windowWidth - elementWidth;
	}
		
	document.getElementById(popup).style.visibility = "visible";
}

function setPosition(e, popup) 
{
	document.getElementById(popup).style.left = e.pageX;
	document.getElementById(popup).style.top = e.pageY;
}

function getWindowWidth() {
	var windowWidth = 0;
	if (typeof(window.innerWidth) == 'number') {
		windowWidth = window.innerWidth;
	}
	else {
		if (document.documentElement && document.documentElement.clientWidth) {
			windowWidth = document.documentElement.clientWidth;
		}
		else {
			if (document.body && document.body.clientWidth) {
				windowWidth = document.body.clientWidth;
			}
		}
	}
	return windowWidth;
}