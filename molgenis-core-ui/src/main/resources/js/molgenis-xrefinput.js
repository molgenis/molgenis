function showXrefInput(input, xref_entity, xref_field, xref_labels){
	showXrefInput(input, xref_entity, xref_field, xref_labels, null);
}

//decorates the select box with the XrefInput logic
function showXrefInput(input, xref_entity, xref_field, xref_labels, xref_filters) {
	var id = input.id + "_" + input.form.name;

	//hide previously open div
	if (window.xrefInputDiv && window.xrefInputDiv.id != id)
	{
		//alert("hiding "+window.xrefInputDiv.id);
		window.xrefInputDiv.style.display = "none";
	}

	//create if not exists
	if (document.getElementById(id) == null) 
	{
		//alert("create"); 
		var myInput = new XrefInput(input, xref_entity, xref_field, xref_labels, xref_filters);
		window.xrefInputDiv = myInput.xrefDiv;		
	} 
	else
	{
		window.xrefInputDiv = document.getElementById(id);
			
		//hide if shown,
		if (window.xrefInputDiv.style.display == "block")
		{
			window.xrefInputDiv.style.display = "none";
			input.focus();
			input.blur();
		}
		// else show 
		else
		{
			window.xrefInputDiv.style.display = "block";
			input.focus();
			input.blur();			
			searchbox = xrefInputDiv.getElementsByTagName("input")[0];
			searchbox.focus();		
		}
	}
	
		//make sure that if somebody clicks outside it is hidden again
	if(window.xrefInputDiv.style.display == "block")
	{
		document.onclick = function check(e)
		{
			var target = (e && e.target) || (event && event.srcElement);
			
			//find out if the click was on the date time input
			while(target.parentNode)
			{
				//alert(target);
				if (target == window.xrefInputDiv || target == input)
				{
					return false;
				}
				target = target.parentNode;
			}
			//if we got here, it was outside the dateinput and we need to hide it
			window.xrefInputDiv.style.display ='none';
		}
	}
	
	return myInput;
}

//constructor
function XrefInput(input, xref_entity, xref_field, xref_labels, xref_filters) {
	//alert("constructor"); 
	return this.init(input, xref_entity, xref_field, xref_labels, xref_filters);
}

//define XrefInput class prototype
//XrefInput decorates the select input with mechanisms to automatically populate itself
XrefInput.prototype = {		
//constructor		
init : function(input, xref_entity, xref_field, xref_labels, xref_filters) {
	
	//alert("init");
	this.input = input;
	
	this.xref_entity = xref_entity;
	this.xref_field = xref_field;
	this.xref_labels = xref_labels;
	this.xref_filters = xref_filters;

	//create the xref <div class="xrefinput" style="block" id="input.id+'_'+input.form.name">
	this.xrefDiv = document.createElement("div");
	this.xrefDiv.className = "xrefinput";
	this.xrefDiv.style.display = "block";
	this.xrefDiv.id = input.id + "_" + input.form.name;
	
	//add the div as child of the input
	this.input.parentNode.insertBefore(this.xrefDiv, this.input.nextSibling);
	this.xrefDiv.setAttribute("XrefInputObject", this); //???

	//add the search box <label>search:</label><input /><br>
	this.searchLabel = document.createElement("label");
	this.searchLabel.appendChild(document.createTextNode("search:"));
	this.xrefDiv.appendChild(this.searchLabel);
	this.searchInput = document.createElement("input");
	this.xrefDiv.appendChild(this.searchInput);
	this.searchInput.focus();
	this.xrefDiv.appendChild(document.createElement("br"));

	//initialize with current value (i.e. the first option that is set) 
	this.xrefDiv.value = this.input.value;
	if(this.input.options.length > 0)
	{
		this.searchInput.value = this.input.options[0].text;
	} 

	//add a handler to the search box to update select when changed 
	var _this = this;
	this.addEvent(this.searchInput, "keyup", function(e) {
		_this.reload();
		
		//if only one option auto-select
		if(_this.selectInput.options.length == 1)
		{
			_this.selectInput.options[0].style.background = "blue";
			_this.selectInput.options[0].style.color = "white";
		}
		
		//on 'enter' and if only one option, choose that
		if( (e.which == 13 || e.keyCode == 13) && _this.selectInput.options.length == 1)
		{
			_this.selectInput.selectedIndex = 0;
			_this.select(_this);
			return;
		}
		//on 'esc' close dialog
		if(e.which == 27)
		{
			_this.xrefDiv.style.display = "none";
		}
	});

	//add the select box 
	this.selectInput = document.createElement("select");	
	//this.selectInput.multiple = "true";
	
	this.selectInput.style.width = "100%";
	this.xrefDiv.appendChild(this.selectInput);

	//add handler so the input is updated when clicking one select option
	var _this = this;	
	this.addEvent(this.selectInput, "click", function(e) {
		_this.select(_this);
	});	
	
	this.reload();
	this.searchInput.focus();
},
/*Copy the current selected item to 'input' and close dialog*/
select : function(_this)
{
	//alert('clicked');
	if(_this.selectInput.options.length > 0)
	{
		//alert("clicked option "+ _this.selectInput.options[_this.selectInput.selectedIndex].value);
		
		//remove existing options this.selectInput.options; 
		for (i = this.input.options.length - 1; i >=0; i--) 
		{
			this.input.removeChild(this.input.options[i]);
		}
		
		//create a new option with selected value
		var option = document.createElement("option");
		option.value = _this.selectInput.options[_this.selectInput.selectedIndex].value;
		option.text = _this.selectInput.options[_this.selectInput.selectedIndex].text;
		option.selected = true;
		this.input.appendChild(option);
		
		//hide the search box
		_this.xrefDiv.style.display = "none";
	}	
},
/* reload function*/
reload : function() {
	//alert("reload"); 

	//load the select box contents via AJAX 
	//xref_label instead of xref_labels for downward compatability
	var url = "xref/find?xref_entity="+this.xref_entity+"&xref_field="+this.xref_field+"&xref_label="+this.xref_labels+"&xref_label_search="+this.searchInput.value;
	if(this.xref_filters != null) url += "&xref_filters="+this.xref_filters;
	//alert(url);

	// branch for native XMLHttpRequest object
	var _this = this;

	req = false;
	if (window.XMLHttpRequest  && !(window.ActiveXObject)) //NOT IE
	{
		req = new XMLHttpRequest();
	}
	else if (window.ActiveXObject) {
		req = new ActiveXObject("Microsoft.XMLHTTP");
	}

	if (req) {
		req.onreadystatechange = function(e) {
			// only if req shows "complete" 
			if (req.readyState == 4) {
				// only if "OK" 
			if (req.status == 200) {
				// ...processing statements go here...
				var options = eval('(' + req.responseText + ')');
				//delegate handling to redrawOptions
				_this.redrawOptions(options);
			} else {
				alert("There was a problem retrieving the XML data:\n"
						+ req.statusText);
			}
		}
		};
		req.open("GET", url, true);
		req.send("");
	}
},
redrawOptions : function(options) {
	//remove existing options this.selectInput.options; 
	for (i = this.selectInput.options.length - 1; i >=0; i--) 
	{
		this.selectInput.removeChild(this.selectInput.options[i]);
	}

	//add empty option to set 'null' when search is empty
	if(this.searchInput.value == "")
	{
		this.selectInput.appendChild(document.createElement("option"));
	}	
	
	//add the current options
	for ( var i in options) {
		var option = document.createElement("option");

		//add the value
		option.value = i;
		
		option.text = options[i];	

		//add option to select box
		this.selectInput.appendChild(option);
	}
	
	//resize select to fit
	this.selectInput.size = options.length <= 10 ? options.length : 10;
},
///* TODO: this method is unused? */
//handleAjax: function(e) {
//	//alert("render");
//		// only if req shows "complete" 
//		if (req.readyState == 4) 
//		{
//			// only if "OK" 
//			if (req.status == 200) 
//			{
//				var options = eval('(' + req.responseText + ')');
//				this.redrawOptions(options);
//			} else 
//			{
//			alert("There was a problem retrieving the XML data:\n"
//					+ req.statusText);
//			}
//		}
//	},
/*helper function to add events for both IE and FF in one call
@obj = the oject to add the event ont
@eventname = name of the event minus the 'on', e.g. 'click' means 'onclick'
@func = the function to be called if this event happens
 */
addEvent : function(obj, eventname, func) {
	//alert(eventname);
	if (navigator.userAgent.match(/MSIE/)) {
		obj.attachEvent("on" + eventname, func);
	} else {
		obj.addEventListener(eventname, func, true);
	}
}
}

/*
 * Post data via the rest interface
 * param: uncapitalised entityClass e.g. mutationPhenotype
 * return: the object inserted into db including updated primary key
 */
function postData(entityClass) {
	var url         = "api/rest/json/" + entityClass; // api path should be a parameter inside Molgenis
	var dataHash    = new Object();
	var entity;
	
	for (var i = 0; i < document.forms[0].elements.length; i++) {
		if (document.forms[0].elements[i].name.toLowerCase().indexOf(entityClass.toLowerCase() + '_') != 0) {
			continue;
		}
		var elementName    = document.forms[0].elements[i].name.substr(0, 1).toLowerCase() + document.forms[0].elements[i].name.substr(1);
		var xref_attribute = elementName.replace(entityClass + '_', '');
		if (document.forms[0].elements[i].value != '') {
			dataHash[xref_attribute] = document.forms[0].elements[i].value;
		}
	}

	successFunction = function(data, textStatus) {
		entity = data[entityClass];
		return entity;
	};

	errorFunction = function (xhr, textStatus, errorThrown) {
		alert("Error: " + textStatus + ", " + errorThrown);
	};

	jQuery.ajax({
		url: url,
		data: dataHash,
		dataType: "json",
		async: false,
		success: successFunction,
		error: errorFunction,
		timeout: 30000,
		type: "POST"
	});

	return entity;
}


function setXrefOption(field, id_field, label_field, entity) {
	//alert($.param(entity) + 'label_field = '+label_field);
	var label = entity[label_field];
	var id    = entity[id_field];

	if (jQuery.isArray(label)) {
		label = label[0];
	}

	if (jQuery.isArray(id)) {
		id = id[0];
	}

	var select = document.getElementById(field);
	
	if(select.multiple) {
		$(select).append('<option value="'+ id +'" selected>'+ label +'</option>');
		
		//trigger adding the new option to the choices list, only in jquery
		if($(select).hasClass('chzn-done')) {
			$(select).trigger('liszt:updated');
			//click the newly added element
			$('#'+field+'_chzn').find('.active-result:last').click();
			//hide the container again
			$('#'+field+'_chzn').removeClass('chzn-container-active');
		}
	}
	else {
		//select.options[0]  = new Option(label, id, true);
		$(select).append('<option value="'+ id +'" selected>'+ label +'</option>');
		
		//trigger updating in case of jqeuery
		if($(select).hasClass('chzn-done')) {
			$(select).trigger('liszt:updated');
		}
	}
	
	//only for jquery

	

}