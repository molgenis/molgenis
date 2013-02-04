//Here the functions used in the mref 'div'
function mref_getLastSelect(container)
{
	var inputs = container.getElementsByTagName("select");
	var lastInput = null;
	for(i=0; i<inputs.length; i++)
	{
		if(inputs[i].tagName == "SELECT" && inputs[i].parentNode == container)
			lastInput = inputs[i];
	}
//	if(inputs.length > 0)
//	{
//		alert("getlastselect"+inputs[inputs.length-1].id);
//		return inputs[inputs.length -1];
//	}
	return lastInput;
}

function mref_addInput(name, xrefEntity, xrefField, xrefLabel, container)
{
	mref_addInput(name, xrefEntity, xrefField, xrefLabel, null, container);
}

function mref_addInput(name, xrefEntity, xrefField, xrefLabel, xrefFilters, container) {	
	//only add if the previous select has an option selected
	var lastSelect = mref_getLastSelect(container);	
	
	if(lastSelect == null || lastSelect.options[lastSelect.selectedIndex].value != "")
	{
		var uniqueId = Math.random();
			
		var selectInput = document.createElement('select');
		selectInput.appendChild(document.createElement("option"));
		selectInput.id = "select"+uniqueId;
		selectInput.name = name;
		selectInput.style.display = "block";	
			
		// add before the first [+] button
		buttons = container.getElementsByTagName('button');
		container.insertBefore(selectInput, buttons[0]);
		selectInput.onfocus = function(e)
		{
			showXrefInput(selectInput, xrefEntity, xrefField, xrefLabel, xrefFilters);
			return false;
		}
		selectInput.focus();
	
	}
	return false;
}
function mref_removeInput(container) {
	var lastSelect = mref_getLastSelect(container);	
	
	if(lastSelect != null)
	{	
		lastSelect.parentNode.removeChild(lastSelect);
		//alert(lastSelect.id);
	}
}

