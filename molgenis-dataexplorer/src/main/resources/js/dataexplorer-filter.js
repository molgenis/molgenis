/**
 * Attribute filter modal
 * 
 * Dependencies: dataexplorer.js
 *  
 * @param $
 * @param molgenis
 */
(function($, molgenis) {
	"use strict";
	
	molgenis.dataexplorer = molgenis.dataexplorer || {};
	var self = molgenis.dataexplorer.filter = molgenis.dataexplorer.filter || {};
	
	/**
	 * Create the filter
	 * 
	 */
	self.createFilterQueryUserReadableList = function (attributeFilters) {
		var attributes = molgenis.dataexplorer.getSelectedEntityMeta().attributes;
		
		var items = [];
		$.each(attributeFilters, function(attrName, query) {
			var attribute = attributes[attrName];
			items.push('<p><a class="feature-filter-edit" data-id="' + attrName + '" href="#">'
					+ attribute.label + ': ' + htmlEscape(self.toString(query))
					+ '</a><a class="feature-filter-remove" data-id="' + attrName + '" href="#" title="Remove '
					+ attribute.label + ' filter" ><span class="glyphicon glyphicon-remove"></span></a></p>');
		});
		items.push('</div>');
		$('#feature-filters').html(items.join(''));
	};
	
	/**
	 * Create the user simple representation of the query
	 */
	self.toString = function (query) {
			var str = '';
			switch(query.operator) {
				case 'EQUALS':
					str += query.value;
					break;
				case 'RANGE':
					str += query.value[0] + ' \u2264 x \u2264 ' + query.value[1];
					break;
				case 'LESS_EQUAL':
					str += '\u2264 ' + query.value; // unicode for less-than or equal to char 
					break;
				case 'GREATER_EQUAL':
					str += '\u2265 ' + query.value; // unicode for greater-than or equal to char 
					break;
				case 'NESTED':
					str += '(';
					var nestedRules = query.nestedRules;
					for(var i = 0; i < nestedRules.length; ++i) {
						str += self.toString(nestedRules[i]);
					}
					str += ')';
					break;
				case 'AND':
					str += ' and ';
					break;
				case 'OR':
					str += ' or ';
					break;
				case 'NOT':
					str += ' not ';
					break;
				default:
					throw 'unsupported operator [' + query.operator + ']';
			}
			return str;
	};
	
	/**
	 * Create filter label
	 */
	self.createFilterLabel = function(attribute, isFirstElement, wizard)
	{
		var label = attribute.label || attribute.name;
		if(isFirstElement && wizard) 
		{
            var labelHtml = $('<label class="col-md-3 control-label" data-placement="right" data-title="' + attribute.description + '">' + label + '</label>');
            if(attribute.description !== undefined){
                labelHtml.tooltip()
            }
			return labelHtml;
		}
		else if (!isFirstElement && wizard) 
		{
			return $('<label class="col-md-3 control-label"></label>');
		} 
		else
		{
			return null;
		}
	}
	
	/**
	 * Create simple filter
	 */
	self.createSimpleFilter = function(attribute, filter, wizard, wrap) {
		var $container = $('<div class="simple-filter-container form-group"></div>');
		var $label = self.createFilterLabel(attribute, true, wizard);
		$container.append($label);
		$container.append(self.createSimpleFilterControls(attribute, filter, wizard));
		$container.data('attribute', attribute);
		if( wrap ) {
			var $wrapper = $('<div>').addClass((wizard ? 'col-md-9' : 'col-md-10'));
			$container.children('.col-md-9').wrap($wrapper);
		}
		return $('<div class="form-group">').append($container);
	}
	
	/**
	 * Create simple filter controls
	 */
	self.createSimpleFilterControls = function(attribute, simpleFilter, wizard) {
		var $controls = $('<div>');
		$controls.addClass((wizard ? 'col-md-9' : 'col-md-10'));
		var name = 'input-' + attribute.name + '-' + new Date().getTime();
		var values = simpleFilter ? simpleFilter.getValues() : null;
		var fromValue = simpleFilter ? simpleFilter.fromValue : null;
		var toValue = simpleFilter ? simpleFilter.toValue : null;
		switch(attribute.fieldType) {
			case 'BOOL':
				var attrs = {'name': name};
				var attrsTrue = values && values[0] === 'true' ? $.extend({}, attrs, {'checked': 'checked'}) : attrs;
				var attrsFalse = values && values[0] === 'false' ? $.extend({}, attrs, {'checked': 'checked'}) : attrs;
				var inputTrue = createInput(attribute, attrsTrue, true);
				var inputFalse = createInput(attribute, attrsFalse, false);
				$controls.append($('<div class="filter-radio-inline-container">').append(inputTrue.addClass('radio-inline')).append(inputFalse.addClass('radio-inline')));
				break;
			case 'CATEGORICAL':
				var restApi = new molgenis.RestClient();
				var entityMeta = restApi.get(attribute.refEntity.href);
				var entitiesUri = entityMeta.href.replace(new RegExp('/meta[^/]*$'), ''); // TODO do not manipulate uri
				var entities = restApi.get(entitiesUri, {
					q : {
						sort : {
							orders : [ {
								direction : 'ASC',
								property : entityMeta.labelAttribute
							} ]
						}
					}
				});
				$.each(entities.items, function() {
					var attrs = {'name': name, 'id': name};
					if(values && $.inArray(this[entityMeta.idAttribute], values) > -1)
						attrs.checked = 'checked';
					$controls.append(createInput(attribute, attrs, this[entityMeta.idAttribute], this[entityMeta.labelAttribute]));
				});
				break;
			case 'DATE':
			case 'DATE_TIME':
				var nameFrom = name + '-from', nameTo = name + '-to';
				var valFrom = fromValue ? fromValue : undefined;
				var valTo = toValue ? toValue : undefined;
				var inputFrom = createInput(attribute, {'name': nameFrom, 'placeholder': 'Start date'}, valFrom);
				var inputTo = createInput(attribute, {'name': nameTo, 'placeholder': 'End date'}, valTo);
				$controls.append($('<div class="form-group">').append(inputFrom)).append($('<div class="form-group">').append(inputTo));
				break;
			case 'DECIMAL':
			case 'INT':
			case 'LONG':
				if (attribute.range) {
					var slider = $('<div id="slider" class="form-group"></div>');
					var min = fromValue ? fromValue : attribute.range.min;
					var max = toValue ? toValue : attribute.range.max;
					slider.editRangeSlider({
						symmetricPositionning: true,
						bounds: {min: attribute.range.min, max: attribute.range.max},
						defaultValues: {min: min, max: max},
						type: 'number'
					});
					$controls.addClass('range-container');
					if (fromValue || toValue){
						// Values differ from range min and max
						$controls.data('dirty', true);
					}
					slider.bind("userValuesChanged", function(e, data){
						// User changed slider values
						$controls.data('dirty', true);
					});
					$controls.append(slider);
				} else {
					var nameFrom = name + '-from', nameTo = name + '-to';
					var labelFrom = $('<label class="horizontal-inline" for="' + nameFrom + '">From</label>');
					var labelTo = $('<label class="horizontal-inline inbetween" for="' + nameTo + '">To</label>');
					var inputFrom = createInput(attribute, {'name': nameFrom, 'id': nameFrom, 'style' : 'width: 189px'}, values ? fromValue : undefined).addClass('input-small');
					var inputTo = createInput(attribute, {'name': nameTo, 'id': nameTo, 'style' : 'width: 189px'}, values ? toValue : undefined).addClass('input-small');
					$controls.addClass('form-inline').append(labelFrom).append(inputFrom).append(labelTo).append(inputTo);
				}
				break;
			case 'EMAIL':
			case 'HTML':
			case 'HYPERLINK':
			case 'STRING':
			case 'TEXT':
			case 'ENUM':
			case 'SCRIPT':
				$controls.append(createInput(attribute, {'name': name, 'id': name}, values ? values[0] : undefined));
				break;
			case 'XREF':
			case 'MREF':
				var operator = simpleFilter ? simpleFilter.operator : 'OR';
				var container = $('<div class="xrefmrefsearch">');
				$controls.append(container);
				container.xrefmrefsearch({
					width: '100%',
					attribute : attribute,
					values : values,
					labels : simpleFilter ? simpleFilter.getLabels() : null,
					operator : operator,
					autofocus : 'autofocus',
					isfilter : true
				});
				break;
			case 'COMPOUND' :
			case 'FILE':
			case 'IMAGE':
				throw 'Unsupported data type: ' + attribute.fieldType;
			default:
				throw 'Unknown data type: ' + attribute.fieldType;			
		}
		return $controls;
	}
	
	/**
	 * JavaScript filter representaion as an interface for filters
	 */
	self.Filter = function (){
		this.operators = {'OR' : 'OR', 'AND' : 'AND'};
		this.types = {'simple' : 'simple', 'complex': 'complex'};
		this.type = undefined;
		this.operator = this.operators['OR'];
		this.attribute = undefined;
		
		/**
		 * this.update = function ($domElement) 
		 * {
		 * 		Implement this method in subclass filters
		 * }
		 */
		
		/**
		 * this.isEmpty = function () 
		 * {
		 * 		Implement this method in subclass filters
		 * }
		 */
		
		/**
		 * this.createQueryRule = function (){ 
		 * {
		 * 		Implement this method in subclass filters
		 * }
		 */
		
		this.isType = function(type)
		{
			return type && this.types[type] && this.type === type;
		};
		
		this.formatOperator = function(operator)
		{
			return this.operators[operator];
		}
		
		return this;
	}
	
	/**
	 * JavaScript representation of a simple filter
	 */
	self.SimpleFilter = function(attribute, fromValue, toValue, value)
	{
		this.fromValue = fromValue;
		this.toValue = toValue;
		var values = [];
		var labels = [];
		this.type = 'simple';
		this.attribute = attribute;

        if(value !== undefined) values.push(value);

		this.isEmpty = function() 
		{
			return !(values.length || this.fromValue || this.toValue);
		};
		
		this.getValues = function ()
		{
			return values;
		};
		
		this.getLabels = function()
		{
			return labels;
		};
		
		this.update = function ($domElement) {
			var fromValue = this.fromValue;
			var toValue = this.toValue;
			var operator = this.operator;
			
			// Add operator
			var operator = $(':input[data-filter=xrefmref-operator]',$domElement).val();
			
			$(':input',$domElement).not('[type=radio]:not(:checked)')
					.not('[type=checkbox]:not(:checked)')
					.not('[data-filter=complex-operator]')
					.not('[data-filter=xrefmref-operator]')
					.not('button.btn.btn-default.dropdown-toggle')
					.not('.select2-input')
					.not('.exclude').each(function(){
				var value = $(this).val();
				var name =  $(this).attr('name');
				
				if(value) {
					// Add values
					if(attribute.fieldType === 'MREF' || attribute.fieldType === 'XREF'){
						var mrefValues = value.split(',');
						$(mrefValues).each(function(i){
							values.push(mrefValues[i]);
						});
						
						labels = $(this).data('labels');
					} 
					else if(attribute.fieldType == 'CATEGORICAL') {
						labels.push($(this).parent().text());
						values[values.length] = value;
					}
					else if(attribute.fieldType === 'INT'
						|| attribute.fieldType === 'LONG'
							|| attribute.fieldType === 'DECIMAL'
								|| attribute.fieldType === 'DATE'
									|| attribute.fieldType === 'DATE_TIME'
							){
					
						if($domElement.closest('.range-container').data('dirty') || !attribute.range)
						{
							// Add toValue
							if(name && (name.match(/-to$/g) || name === 'sliderright')){
								toValue = value;
							}
							
							// Add fromValue
							if(name && (name.match(/-from$/g) || name === 'sliderleft')){
								fromValue = value;
							}
						}
						
						// Validate that to > from
						if(attribute.fieldType === 'DECIMAL' || attribute.fieldType === 'INT' || attribute.fieldType === 'LONG') {
							if(parseFloat(toValue) <= parseFloat(fromValue)) {
								toValue = undefined;
							} 
						}
					}
					else
					{
						values[values.length] = value;
						labels[values.length] = value;
					}
				}
			});
			this.fromValue = fromValue;
			this.toValue = toValue;
			this.operator = operator;
			return this;
		};
		
		this.createQueryRule = function (){
			var attribute = this.attribute;
			var fromValue = this.fromValue;
			var toValue = this.toValue;
			var operator = this.operator;
			var rule;
			var rangeQuery = attribute.fieldType === 'DATE' || attribute.fieldType === 'DATE_TIME' || attribute.fieldType === 'DECIMAL' || attribute.fieldType === 'INT' || attribute.fieldType === 'LONG';

			if (rangeQuery) {
				if(attribute.fieldType === 'DATE_TIME'){
					if(fromValue){
						fromValue = fromValue.replace("'T'", "T");
					}
					if(toValue){
						toValue = toValue.replace("'T'", "T");
					}
				}
				
				// add range fromValue / toValue
				if (fromValue && toValue) {
					rule = {
						operator: 'NESTED',
						nestedRules:[
						{
							field : attribute.name,
							operator : 'GREATER_EQUAL',
							value : fromValue
						},
						{
							operator : 'AND'
						},
						{
							field : attribute.name,
							operator : 'LESS_EQUAL',
							value : toValue
						}]
					};
				} else if (fromValue) {
					rule = {
						field : attribute.name,
						operator : 'GREATER_EQUAL',
						value : fromValue
					};
				} else if (toValue) {
					rule = {
						field : attribute.name,
						operator : 'LESS_EQUAL',
						value : toValue
					};
				}
			} else {
				if(values){
					// determine query operator for attribute type
					var attrOperator;
					switch(attribute.fieldType) {
						case 'BOOL':
						case 'CATEGORICAL':
						case 'DATE':
						case 'DATE_TIME':
						case 'DECIMAL':
						case 'ENUM':
						case 'INT':
						case 'LONG':
						case 'MREF':
						case 'XREF':
							attrOperator = 'EQUALS';
							break;
						case 'EMAIL':
						case 'HTML':
						case 'HYPERLINK':
						case 'SCRIPT':
						case 'STRING':
						case 'TEXT':
							attrOperator = 'SEARCH';
							break;
						case 'COMPOUND':
						case 'FILE':
						case 'IMAGE':
							throw 'Unsupported data type: ' + attribute.fieldType;
						default:
							throw 'Unknown data type: ' + attribute.fieldType;
					}
					
					if (values.length > 1) {
						var nestedRule = {
							operator: 'NESTED',
							nestedRules:[]
						};
					
						$.each(values, function(index, value) {
							if (index > 0) {
								nestedRule.nestedRules.push({
									operator : operator || 'OR'
								});
							}
		
							nestedRule.nestedRules.push({
								field : attribute.name,
								operator : attrOperator,
								value : value
							});
						});
						rule = nestedRule;
					} else {
						rule = {
							field : attribute.name,
							operator : attrOperator,
							value : values[0]
						};
					}
				}
			}
			
			return rule;
		};

		return this;
	};
	self.SimpleFilter.prototype = new self.Filter();
	
	/**
	 * JavaScript representation of een elementin a complex filter
	 */
	self.ComplexFilterElement = function(attribute)
	{
		this.simpleFilter = null;
		this.type = 'complex-element';
		this.attribute = attribute;
		
		this.update = function ($domElement)
		{
			this.operator = this.formatOperator($(':input[data-filter=complex-operator]', $domElement).val());
			this.simpleFilter = (new self.SimpleFilter(attribute)).update($('[data-filter=complex-simplefilter]', $domElement));
			return this;
		};
	};
	self.ComplexFilterElement.prototype = new self.Filter();
	
	/**
	 * JavaScript representation of a complex filter
	 */
	self.ComplexFilter = function(attribute)
	{
		var complexFilterElements = [];
		this.type = 'complex';
		this.attribute = attribute;
		
		this.update = function ($domElement) {			
			$('[data-filter=complex-element]', $domElement).each(function(){
				var complexFilterElement = (new self.ComplexFilterElement(attribute)).update($(this));
				if(!complexFilterElement.simpleFilter.isEmpty())
				{
					complexFilterElements.push(complexFilterElement);
				}
			});
			return this;
		};

        this.addComplexFilterElement = function (complexFilterElement) {
            if(!complexFilterElement !== null)
            {
            	complexFilterElements.push(complexFilterElement);
            }
			return this;
		};
		
		this.isEmpty = function () {
			for(var i = 0; i < complexFilterElements.length; i++)
			{
				if(!complexFilterElements[i].simpleFilter.isEmpty()){
					return false;
				}
			}
			return true;
		};
		
		this.getComplexFilterElements = function () {
			return complexFilterElements;
		};
		
		/**
		 * Implements the SQL Logic Operator Precedence: And and Or
		 * 
		 * And has precedence over Or
		 * 
		 * Example: (A and B and C) or (D) or (E and F)
		 */
		this.createQueryRule = function() {
			var nestedRules = [];
			var lastOperator, rule, lastNestedRule = null;
			
			$.each(complexFilterElements, function(index, complexFilterElement) {
				if(index > 0){
					lastOperator = complexFilterElement.operator;
					
					if(lastOperator === 'AND'){
						lastNestedRule.nestedRules.push({
							operator : lastOperator
						});
					}
					else if (lastOperator === 'OR'){
						nestedRules.push(lastNestedRule);
						lastNestedRule = null;
						nestedRules.push({
							operator : lastOperator
						});
					}
				}
				
				if(lastNestedRule === null){
					lastNestedRule = {
						operator: 'NESTED',
						nestedRules:[]
					};
				}

				lastNestedRule.nestedRules.push(complexFilterElement.simpleFilter.createQueryRule());
				
			});
			
			if(lastNestedRule !== null){
				nestedRules.push(lastNestedRule);
			}
			
			rule = {
				operator: 'NESTED',
				nestedRules: nestedRules
			};
			
			return rule;
		};
		
		return this;
	};
	self.ComplexFilter.prototype = new self.Filter();
}($, window.top.molgenis = window.top.molgenis || {}));