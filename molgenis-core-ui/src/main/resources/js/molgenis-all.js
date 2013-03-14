function toggleDiv(div, image)
{
	if (document.getElementById(div).style.display == "block")
	{
		document.getElementById(image).src = "res/img/open.png";
		document.getElementById(div).style.display = "none";
	}
	else
	{
		document.getElementById(image).src = "res/img/close.png";
		document.getElementById(div).style.display = "block";
	}
}

function moveDivHorizontal()
{
	x = 0;
	w = "100%";
	if ( typeof(window.pageXOffset) == 'number'){x = window.pageXOffset;}
	else if (typeof(document.body.scrollLeft) == 'number'){x = document.body.scrollLeft; w = "auto";}
	else if (typeof(document.documentElement.scrollLeft) == 'number'){x = document.documentElement.scrollLeft;}
		
	for (i = 0; i < headersArray.length; i++)
	{
		document.getElementById(headersArray[i]).style.marginLeft = x;
		document.getElementById(headersArray[i]).style.width = w;
	}
}


//check form input
function validateForm(form, fields) 
{
	alertstring = "";
	
	for (i = 0; i < fields.length; i++) {
		if (fields[i].value == "")
		{
			alertstring += fields[i].name + "\n";
		}
	}
	if (alertstring == "")
	{
		return true;
	}
	else
	{
		alert("Fields marked with * are required. Please provide: \n" + alertstring);
		return false;
	}
}

//alter form input
function setInput(form, targetv, actionv, __targetv, __actionv, __showv)
{
	document.getElementById(form).target = targetv;
	document.getElementById(form).action = actionv;
	document.getElementById(form).__target.value = __targetv;
	document.getElementById(form).__action.value = __actionv;
	document.getElementById(form).__show.value = __showv;
}

function checkAll(formname, inputname)
{
	forminputs = document.getElementById(formname).getElementsByTagName('input');
	for (i = 0; i < forminputs.length; i++) 
	{
		if (forminputs[i].name == inputname && !forminputs[i].disabled) 
		{
			forminputs[i].checked = document.getElementById(formname).checkall.checked;
		}
	}
}

function toggleCssClass(cssClass) 
{
	var cssRules = new Array();
	var ff = true;
	if (document.styleSheets[0].cssRules) 
	{
		cssRules = document.styleSheets[0].cssRules;
	} 
	else if (document.styleSheets[0].rules) 
	{
		ff = false;
		cssRules = document.styleSheets[0].rules;
	}
	
	missing = true;

	for (i=0; i< cssRules.length; i++)
	{
		if(cssRules[i].selectorText.toLowerCase() == "."+cssClass.toLowerCase())
		{ 

			if(document.styleSheets[0].deleteRule)
				document.styleSheets[0].deleteRule(i);
			else 
				document.styleSheets[0].removeRule(i);
			missing = false;
			break;
		}
	}
	if(missing)
	{
		if(ff) document.styleSheets[0].insertRule("."+cssClass+"{display: none} ", cssRules.length - 1);
		else  document.styleSheets[0].addRule("."+cssClass,"display: none", cssRules.length - 1);
	}	
}
