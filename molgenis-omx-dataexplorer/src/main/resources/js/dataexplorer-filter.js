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
	
	self.openFilterModal = function(attribute, attributeFilter) {
		var modal = createFilterModal();
		
		var title = attribute.label || attribute.name;
		var description = attribute.description ? attribute.description : 'No description available';
		var controls = molgenis.dataexplorer.createFilterControls(attribute, attributeFilter);
		
		$('.filter-title', modal).html(title);
		$('.filter-description', modal).html(description);
		$('.filter-controls', modal).html(controls);
		
		modal.modal('show');
	};
	
	function createFilterModal() {		
		var modal = $('#filter-modal');
		if(!modal.length) {
			var items = [];
			items.push('<div class="modal hide medium" id="filter-modal" tabindex="-1">');
			items.push('<div class="modal-header">');
			items.push('<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>');
			items.push('<h3 class="filter-title"></h3>');
			items.push('</div>');
			items.push('<div class="modal-body">');
			items.push('<legend>Description</legend>');
			items.push('<p class="filter-description"></p>');
			items.push('<legend>Filter</legend>');
			items.push('<form class="form-horizontal filter-controls"></form>');
			items.push('</div>');
			items.push('<div class="modal-footer">');
			items.push('<a href="#" class="btn" data-dismiss="modal">Cancel</a>');
			items.push('<a href="#" class="btn btn-primary filter-apply-btn" data-dismiss="modal">Apply</a>');
			items.push('</div>');
			items.push('</div>');
			
			modal = $(items.join(''));
			
			modal.modal({'show': false});
			
			createFilterModalControls(modal);
		}
		return modal;
	}
	
	function createFilterModalControls(modal) {
		$('.filter-apply-btn', modal).click(function() {
			var filters = molgenis.dataexplorer.createFilters($('form', modal));
			if (filters.length > 0) {
				$(document).trigger('updateAttributeFilters', {
					'filters' : filters
				});
			}
		});
		
		modal.on('shown', function () {
			$('form input:visible:first', modal).focus();
		});
		
		modal.keypress(function(e) {
		    if(e.which == 13) {
		    	e.preventDefault();
		    	$('.filter-apply-btn', modal).click();
		    }
		});
	}
	
	function Filter(){
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
		}
		
		return this;
	}
	
	self.SimpleFilter = function(attribute){
		this.fromValue = undefined;
		this.toValue = undefined;
		var values = [];
		this.type = 'simple';
		this.attribute = attribute;
		
		this.isEmpty = function() 
		{
			return !(values.length || this.fromValue || this.toValue);
		};
		
		this.getValues = function ()
		{
			return values;
		};
		
		this.update = function ($domElement) {
			var fromValue = this.fromValue;
			var toValue = this.toValue;
			var operator = this.operator;
			
			$(":input",$domElement).not('[type=radio]:not(:checked)').not('[type=checkbox]:not(:checked)').each(function(){
				var value = $(this).val();
				var name =  $(this).attr("name");
				
				if(value) {
					// Add operator
					if ($(this).hasClass('operator')) {
						operator = value;
					} 
					
					// Add values
					else 
					{
		                if(attribute.fieldType === 'MREF'){
		                    var mrefValues = value.split(',');
		                    $(mrefValues).each(function(i){
		                    	values.push(mrefValues[i]);
		                    });
		                } 
		                else if(attribute.fieldType === 'INT'
							|| attribute.fieldType === 'LONG'
								|| attribute.fieldType === 'DECIMAL'
									|| attribute.fieldType === 'DATE'
										|| attribute.fieldType === 'DATE_TIME'
								){
							
							// Add toValue
							if(name && (name.match(/-to$/g) || name === 'sliderright')){
								toValue = value;
							}
							
							// Add fromValue
							if(name && (name.match(/-from$/g) || name === 'sliderleft')){
								fromValue = value;
							}
						}
		                else
		                {
		                	values[values.length] = value;
		                }
					}
				}
			});
			this.fromValue = fromValue;
			this.toValue = toValue;
			this.operator = operator;
			return this;
		}
		
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
					if (values.length > 1) {
						var nestedRule = {
							operator: 'NESTED',
							nestedRules:[]
						};
					
						$.each(values, function(index, value) {
							if (index > 0) {
								nestedRule.nestedRules.push({
									operator : operator
								});
							}
		
							nestedRule.nestedRules.push({
								field : attribute.name,
								operator : 'EQUALS',
								value : value
							});
						});
						rule = nestedRule;
					} else {
						rule = {
							field : attribute.name,
							operator : 'EQUALS',
							value : values[0]
						};
					}
				}else{
					alert("values is empty: " + values);
				}
			}
			
			return rule;
		}

		return this;
	}
	self.SimpleFilter.prototype = new Filter();
	
	self.ComplexFilter = function(attribute){
		var filters = [];
		this.type = 'complex';
		this.attribute = attribute;
		
		this.update = function ($domElement) {
			var simpleFilter;
			this.operator = $(":input.complexFilter.operator", $domElement).val();
			$(".controls", $domElement).each(function(){
				simpleFilter = (new self.SimpleFilter(attribute)).update($(this));
				if(!simpleFilter.isEmpty())
				{
					filters.push(simpleFilter);
				}
			});
			return this;
		};
		
		this.isEmpty = function () {
			for(var i = 0; i < filters.length; i++)
			{
				if(!filters[i].isEmpty()){
					return false;
				}
			}
			return true;
		};
		
		this.getFilters = function () {
			return filters;
		};
		
		this.createQueryRule = function() {
			var nestedRules = [];
			var attribute = this.attribute;
			var operator = this.operator;
			var rule;
			
			$.each(filters, function(index, subFilter) {
				if(index > 0){
					nestedRules.push({
						operator : operator
					});
				}
				nestedRules.push(subFilter.createQueryRule());
			});
			
			rule = {
				operator: 'NESTED',
				nestedRules: nestedRules
			};
			
			return rule;
		}
		
		return this;
	}
	self.ComplexFilter.prototype = new Filter();
}($, window.top.molgenis = window.top.molgenis || {}));