$.when( $,
		window.top.molgenis = window.top.molgenis || {}, 
		$.get('dataexplorer/settings') 
).then(
function($, molgenis, settingsXhr) {	
	"use strict";
	var self = molgenis.dataexplorer = molgenis.dataexplorer || {};
	
	// module api
	self.getSelectedEntityMeta = getSelectedEntityMeta;
	self.getSelectedAttributes = getSelectedAttributes; 
	self.getEntityQuery = getEntityQuery;
    self.createHeader = createHeader;
    self.setGenomeAttributes = setGenomeAttributes;
    self.getPosAttribute = getPosAttribute;
    self.getChromosomeAttribute = getChromosomeAttribute;
    self.getIdentifierAttribute = getIdentifierAttribute;
    self.getPatientAttribute = getPatientAttribute;

    var restApi = new molgenis.RestClient();
	var selectedEntityMetaData = null;
	var attributeFilters = {};
	var selectedAttributes = [];
	var searchQuery = null;
	var modules = [];

    var posAttribute;
    var chromosomeAttribute;
    var identifierAttribute;
    var patientAttribute;

	if(settingsXhr[1] !== 'success') {
		molgenis.createAlert([{message: 'An error occurred initializing the data explorer.'}], 'error');
	}
	var settings = settingsXhr[0];
	self.settings = settings;

	var stateDefault = {
		entity: null,
		query: null,
		attrs: null,
		mod: null
	};
	
	var state;
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function getSelectedEntityMeta() {
		return selectedEntityMetaData;
	}

	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function getSelectedAttributes() {
		return selectedAttributes;
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function getEntityQuery() {
		return state.query || { q: [] };
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createModuleNav(modules, entity, container) {
		var items = [];
		items.push('<ul class="nav nav-tabs pull-left" role="tablist">');
		$.each(modules, function() {
			var href = molgenis.getContextUrl() + '/module/' + this.id+'?entity=' + entity;
			items.push('<li data-id="' + this.id + '"><a href="' + href + '" data-target="#tab-' + this.id + '" data-id="' + this.id + '" role="tab" data-toggle="tab"><img src="/img/' + this.icon + '"> ' + this.label + '</a></li>');
		});
		items.push('</ul>');
		items.push('<div class="tab-content">');
		$.each(modules, function() {
			items.push('<div class="tab-pane" id="tab-' + this.id + '" data-id="' + this.id + '">Loading...</div>');
		});
		items.push('</div>');
		
		// add menu to container 
		container.html(items.join(''));
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createEntityMetaTree(entityMetaData, attributes) {
		var container = $('#feature-selection');
		container.tree({
			'entityMetaData' : entityMetaData,
			'selectedAttributes' : attributes,
			'onAttributesSelect' : function(selects) {
				selectedAttributes = container.tree('getSelectedAttributes');
				$(document).trigger('changeAttributeSelection', {'attributes': selectedAttributes, 'totalNrAttributes': Object.keys(entityMetaData.attributes).length});
			},
			'onAttributeClick' : function(attribute) {
				$(document).trigger('clickAttribute', {'attribute': attribute});
			}
		});
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createHeader(entityMetaData) {
		$('#entity-class-name').html(entityMetaData.label);
		
		if (entityMetaData.description) {
			var description = $('<span data-placement="bottom"></span>');
			description.html(abbreviate(entityMetaData.description, 
					settings['header.abbreviate']||180));
			description.attr('data-title', entityMetaData.description);
			$('#entity-class-description').html(description.tooltip());
		} else {
			$('#entity-class-description').html('');
		}
	}

    function getAttributeFromList(attributesString){
        var result;
        var attrs = getSelectedEntityMeta().attributes;
        var list = attributesString.split(",");
        for(var item in list){
            result = attrs[list[item]];
            if(result !== undefined){
                break;
            }
        }
        return result;
    }

    /**
     * @memberOf molgenis.dataexplorer
     */
    function setGenomeAttributes(start, chromosome, id, patient){
        posAttribute = getAttributeFromList(start);
        chromosomeAttribute = getAttributeFromList(chromosome);
        identifierAttribute = getAttributeFromList(id);
        patientAttribute = getAttributeFromList(patient);
    }

    function getPosAttribute(){return posAttribute};
    function getChromosomeAttribute(){return chromosomeAttribute};
    function getIdentifierAttribute(){return identifierAttribute};
    function getPatientAttribute(){return patientAttribute};

	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createEntityQuery() {
        var entityCollectionRequest = {
			q : []
		};
		
		// add rules for the search term to the query
		if (searchQuery) {
			if (/\S/.test(searchQuery)) {
				var searchQueryRegex = /^\s*(?:chr)?([\d]{1,2}|X|Y|MT|XY):([\d]+)(?:-([\d]+)+)?\s*$/g;
				
				if(searchQueryRegex && searchQuery.match(searchQueryRegex)) {
					var match = searchQueryRegex.exec(searchQuery);
					
					// only chromosome and position
					if(match[3] === undefined){			
						var chromosome = match[1];
						var position = match[2];
						
				        entityCollectionRequest.q = 
				        	    [{
				        	        operator: "NESTED",
				        	        nestedRules: [{
				        	            field: chromosomeAttribute,
				        	            operator: "EQUALS",
				        	            value: chromosome
				        	        }]
				        	    }, {
				        	        operator: "AND"
				        	    }, {
				        	        operator: "NESTED",
				        	        nestedRules: [{
				        	            field: posAttribute,
				        	            operator: "EQUALS",
				        	            value: position
				        	        }]
				        	    }];
					// chromosome:startPos - endPos	
					}else if(match[3]) {
						
						var chromosome = match[1];
						var startPosition = match[2];
						var stopPosition = match[3];
						
						if(parseInt(startPosition, 10) > parseInt(stopPosition, 10)) {
							molgenis.createAlert([{message: 'The start position of the queried range is larger than the stop position. Please check the search query.'}], 'warning');
						}else{
							$('.alerts').empty();
						}
						
						entityCollectionRequest.q = 
							[{
								operator: "NESTED",
						        nestedRules: [{
							            operator: "NESTED",
							            nestedRules: [{
							                field: chromosomeAttribute,
							                operator: "EQUALS",
							                value: chromosome
						            }]
						        }]
						    }, {
						    	operator: "AND"
						    }, {
						    	operator: "NESTED",
						        nestedRules: [{
				                    field: posAttribute,
				                    operator: "GREATER_EQUAL",
				                    value: startPosition
				                }, {
				                	operator: "AND"
				                }, {
				                    field: posAttribute,
				                    operator: "LESS_EQUAL",
				                    value: stopPosition
				                }]
						    }];
					}
				} else {
					entityCollectionRequest.q.push({
						operator : 'SEARCH',
						value : searchQuery
					});
				}
			}
		}

		// add rules for attribute filters to the query
		$.each(attributeFilters, function(attributeUri, filter) {
			var rule = filter.createQueryRule();
			if(rule){
				if (entityCollectionRequest.q.length > 0) {
					entityCollectionRequest.q.push({
						operator : 'AND'
					});
				}
				entityCollectionRequest.q.push(rule);
			}
		});
		
		/**
		 * Debug info: 
		 * Activate this code to see the query
		 * 
		 * $("#debugFilterQuery").remove();
		 * $("#tab-data").append($('<div id="debugFilterQuery"><p>QUERY : </p><p>' + JSON.stringify(entityCollectionRequest) + '</p></div>'));
		 */
		
		return entityCollectionRequest;
	}
	
	function render() {
		// get entity meta data and update header and tree 
		var entityMetaDataRequest = restApi.getAsync('/api/v1/' + state.entity + '/meta', {'expand': ['attributes']}, function(entityMetaData) {
			selectedEntityMetaData = entityMetaData;
			
			self.createHeader(entityMetaData);
			
			selectedAttributes = $.map(entityMetaData.attributes, function(attribute) {
				if(state.attrs === undefined || state.attrs === null) return attribute.fieldType !== 'COMPOUND' ? attribute : null;
				else if(state.attrs === 'none') return null;
				else return state.attrs.indexOf(attribute.name) !== -1 && attribute.fieldType !== 'COMPOUND' ? attribute : null;
			});
			
			createEntityMetaTree(entityMetaData, selectedAttributes);
			
			//Show wizard on show of dataexplorer if url param 'wizard=true' is added
			if (settings['wizard.oninit'] && settings['wizard.oninit'] === 'true') {
				self.filter.wizard.openFilterWizardModal(entityMetaData, attributeFilters);
			}
		});
		
		// get entity modules and load visible module
		$.get(molgenis.getContextUrl() + '/modules?entity=' + state.entity).done(function(data) {
			var container = $('#module-nav');
			createModuleNav(data.modules, state.entity, container);
			
			// select first tab
			var moduleTab;
			if(state.mod) {
				moduleTab = $('a[data-toggle="tab"][data-target="#tab-' + state.mod + '"]', container);
			} else {
				moduleTab = $('a[data-toggle="tab"]', container).first();
			}
			
			// show tab once entity meta data is available
			$.when(entityMetaDataRequest).done(function(){
				moduleTab.tab('show');
			});
		});
		
		$('#observationset-search').focus();
	}
	
	function pushState() {
		// shorten URL by removing attributes with null or undefined values
		var cleanState = {};
		for (var key in state) {
		    if (state.hasOwnProperty(key)) {
		    	var val = state[key];
		    	if(val) {
		    		cleanState[key] = val;
		    	}
		    }
		}
		
		// FIXME remove if clause as part of http://www.molgenis.org/ticket/3110
		if(state.query) {
			delete cleanState.query;
	    	for (var i = 0; i < state.query.q.length; ++i) {
				var rule = state.query.q[i];
				if(rule.field === undefined && rule.operator === 'SEARCH') {
					cleanState.query = { q: [rule] };
					break;
				}
			}
		}
		
		// update browser state
		history.pushState(state, '', molgenis.getContextUrl() + '?' + $.param(cleanState));
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	$(function() {
		// lazy load tab contents
		$(document).on('show.bs.tab', 'a[data-toggle="tab"]', function(e) {
			var target = $($(e.target).attr('data-target'));
			if(target.data('status') !== 'loaded') {
				target.load($(e.target).attr('href'), function() {
					target.data('status', 'loaded');
				});
			}
		});
		
		$(document).on('changeQuery', function(e, query) {
			state.query = query;
			pushState();
		});
			
		$(document).on('changeEntity', function(e, entity) {
			// reset state
			state = {
				entity: entity,
				attributes: [],
				mod : null
			};
			pushState();
			
			// reset			
			selectedEntityMetaData = null;
			attributeFilters = {};
			selectedAttributes = [];
			searchQuery = null;
			
			$('#feature-filters p').remove();
			$("#observationset-search").val("");
			$('#data-table-pager').empty();
			
			// reset: unbind existing event handlers
			$.each(modules, function() {
				$(document).off('.' + this.id);	
			});
			
			render();
		});
		
		$(document).on('changeModule', function(e, mod) {
			state.mod = mod;
			pushState();
		});
		
		$(document).on('changeAttributeSelection', function(e, data) {
			if(data.attributes.length === 0) {
				state.attrs = 'none';
			}
			else if(data.attributes.length === data.totalNrAttributes) {
				state.attrs = null;
			}
			else { 
				state.attrs = $.map(data.attributes, function(attribute) {
					return attribute.name;
				});
			}
			pushState();
		});
		
		$(document).on('updateAttributeFilters', function(e, data) {
			$.each(data.filters, function() {
				if(this.isEmpty()){
					delete attributeFilters[this.attribute.href];
				}else{
					attributeFilters[this.attribute.href] = this;
				}
			});
			self.filter.createFilterQueryUserReadableList(attributeFilters);
			$(document).trigger('changeQuery', createEntityQuery());
		});
		
		$(document).on('removeAttributeFilter', function(e, data) {
			delete attributeFilters[data.attributeUri];
			self.filter.createFilterQueryUserReadableList(attributeFilters);
			$(document).trigger('changeQuery', createEntityQuery());
		});
		
		$(document).on('clickAttribute', function(e, data) {
			if(data.attribute.fieldType !== 'COMPOUND')
				self.filter.dialog.openFilterModal(data.attribute, attributeFilters[data.attribute.href]);
		});
		
		var container = $("#plugin-container");
		
		if ($('#dataset-select').length > 0) {
			$('#dataset-select').select2({ width: 'resolve' });
			$('#dataset-select').change(function() {
				$(document).trigger('changeEntity', $(this).val());
			});
		}

		$("#observationset-search").change(function(e) {
			searchQuery = $(this).val().trim();
			$(document).trigger('changeQuery', createEntityQuery());
		});
		
		$("#observationset-search").change(function(e) {
			searchQuery = $(this).val().trim();
			$(document).trigger('changeQuery', createEntityQuery());
		});
		
		$('#search-clear-button').click(function(){
			$("#observationset-search").val('');
			$("#observationset-search").change();
		});
		
		$('#filter-wizard-btn').click(function() {
			self.filter.wizard.openFilterWizardModal(selectedEntityMetaData, attributeFilters);
		});

		$('#module-nav').on('click', 'a', function(e) {
			$(document).trigger('changeModule', $(this).data('id'));
		});
	
		$(container).on('click', '.feature-filter-edit', function(e) {
			e.preventDefault();
			var filter = attributeFilters[$(this).data('href')];
			self.filter.dialog.openFilterModal(filter.attribute, filter);
		});
		
		$(container).on('click', '.feature-filter-remove', function(e) {
			e.preventDefault();
			$(document).trigger('removeAttributeFilter', {'attributeUri': $(this).data('href')});
		});
		
		$('#delete').on('click', function(){
			bootbox.confirm("Are you sure you want to delete all data and metadata for this entity?", function(confirmed){
				if(confirmed){
					$.ajax('/api/v1/'+selectedEntityMetaData.name+'/meta', {'type': 'DELETE'}).done(function(){
						document.location.href = "/menu/main/dataexplorer";
					});
				}
			})
		});

		function init() {
			// set entity in dropdown
			if(!state.entity) {
				state.entity = $('#dataset-select option:not(:empty)').first().val();
			}
			$('#dataset-select').select2('val', state.entity);
			
			if (state.query) {
				// set query in searchbox
				for (var i = 0; i < state.query.q.length; ++i) {
					var rule = state.query.q[i];
					if(rule.field === undefined && rule.operator === 'SEARCH') {
						$('#observationset-search').val(rule.value);
						break;
					}
				}
				
				// set filters in filter list
				// FIXME implement as part of http://www.molgenis.org/ticket/3110
			}
			
			render();
		}
		
		// set state from url
		if(window.location.search && window.location.search.length > 0) {
			var querystring = window.location.search.substring(1); // remove '?'
			if(querystring.length > 0) {
				state = $.deparam(querystring);
			}
		} else {
			state = stateDefault;
		}

		// handle browser back event
		window.onpopstate = function(event) {
			if (event.state !== null) {
				state = event.state;
				init();
			}
		};
		
		init();
	});
});
(function($, molgenis) {
	"use strict";
	
	// toggle <label class="toggle"><input type="checkbox"><span data-unchecked="Include" data-checked="Exclude"></span></label>
	var boolFilterSource = '{{#each states}}<label class="{{type}}-inline"><input type="{{type}}" name="bool-selector" value="{{value}}"{{#if selected}} checked{{/if}}> {{label}}</label>{{/each}}';
	var categoricalFilterSource = '{{#each categories}}<label class="checkbox"><input type="checkbox" name="categorical-selector" value="{{value}}"{{#if selected}} checked{{/if}}> {{label}}</label>{{/each}}';
	
	// lazy compile templates
	var boolFilterTemplate, categoricalFilterTemplate;
		
	function createFilter(settings) {
		var html;
		var attribute = settings.attribute;
		switch(attribute.fieldType) {
			case 'BOOL':
				html = createBoolFilter(attribute, settings);
				break;
			case 'CATEGORICAL':
				html = createCategoricalFilter(attribute, settings);
				break;
			case 'XREF':
			case 'DATE':
			case 'DATE_TIME':
			case 'DECIMAL':
			case 'LONG':
			case 'EMAIL':
			case 'HTML':
			case 'HYPERLINK':
			case 'STRING':
			case 'ENUM':
			case 'INT':
			case 'TEXT':
			case 'SCRIPT':
			case 'MREF':
			case 'COMPOUND' :
			case 'FILE':
			case 'IMAGE':
				throw 'Unsupported data type: ' + attribute.fieldType;
			default:
				throw 'Unknown data type: ' + attribute.fieldType;
		}
		settings.$container.html(html);
	}
	
	function createBoolFilter(attribute, settings) {
		// lazy compile template and register event handler
		if (!boolFilterTemplate) boolFilterTemplate = Handlebars.compile(boolFilterSource);
		registerBoolFilterEventHandler(settings);
		
		var type = attribute.nillable ? 'checkbox' : 'radio';
		var selectedValues = [];
		if(settings.query) {
			$.each(settings.query, function(i, rule) {
				selectedValues.push(rule.value);
			});
		}
		
		// use 'true', 'false' and 'null' instead of true false and null to distinguish between false and null on val()
		var states = []
		states.push({type: type ,value: 'true', label: 'True', selected: selectedValues.indexOf(true) !== -1});
		states.push({type: type ,value: 'false', label: 'False', selected: selectedValues.indexOf(false) !== -1});
		if(attribute.nillable) {
			states.push({type: type, value: 'null', label: 'N/A', selected: selectedValues.indexOf(null) !== -1});
		}

		return boolFilterTemplate({
			states : states
		});
	}

	function registerBoolFilterEventHandler(settings) {
		$(settings.$container).on('change', 'input[name="bool-selector"]', function(e) {
			var query = [];
			
			var checkedInputs = $('input[name="bool-selector"]:checked', settings.$container);
			for(var i = 0; i < checkedInputs.length; ++i) {
				// map back 'true', 'false' and 'null' to true, false or null
				var rawVal = $(checkedInputs[i]).val();
				var val = rawVal === 'true' ? true : (rawVal === 'false' ? false : null);
				if(i > 0)
					query.push({operator: 'OR'});
				query.push({field: settings.attribute.name, operator: 'EQUALS', value: val});
			}
						
			if(checkedInputs.length > 1) {
				query = [{operator: 'NESTED', nestedRules: query}];
			}
			
			settings.query = query;
			settings.onQueryChange(settings.query);
		});
	}
	
	function createCategoricalFilter(attribute, settings) {
		// lazy compile template and register event handler
		if (!categoricalFilterTemplate) categoricalFilterTemplate = Handlebars.compile(categoricalFilterSource);
		registerCategoricalFilterEventHandler(settings);
		
		// FIXME implement, get categories through rest API
		console.log(attribute.refEntity);
		var categories = [{value: 'a', label: 'a'},{value: 'b', label: 'b'},{value: 'c', label: 'c'}];
		if(attribute.nillable) {
			categories.push({value: 'null', label: 'N/A'});
		}
		return categoricalFilterTemplate({categories: categories});
	}
	
	function registerCategoricalFilterEventHandler(settings) {
		$(settings.$container).on('change', 'input[name="categorical-selector"]', function(e) {
			var query = [];
			
			var checkedInputs = $('input[name="categorical-selector"]:checked', settings.$container);
			for(var i = 0; i < checkedInputs.length; ++i) {
				var val = $(checkedInputs[i]).val();
				if(i > 0)
					query.push({operator: 'OR'});
				query.push({field: settings.attribute.name, operator: 'EQUALS', value: val});
			}
						
			if(checkedInputs.length > 1) {
				query = [{operator: 'NESTED', nestedRules: query}];
			}
			
			settings.query = query;
			settings.onQueryChange(settings.query);
		});
	}
	
	$.fn.attributeFilter = function(options) {
		var $container = this;
		
		// call plugin method
		if (typeof options === 'string') {
			var args = Array.prototype.slice.call(arguments, 1);
			if (args.length === 0)
				return container.data('attributeFilter')[options]();
			else
				throw 'invalid number of plugin method arguments';
		}
		
		var settings = $.extend({}, $.fn.attributeFilter.defaults, options, {'$container': $container});
		$container.off();
		$container.empty();
		$container.data('settings', settings);
		
		// plugin methods
		$container.data('attributeFilter', {
			getAttribute : function() {
				return settings.attribute;
			},
			getQuery : function() {
				return settings.query;
			}
		});
		
		// create filter
		createFilter(settings);
		
		return this;
	};
	
	// default tree settings
	$.fn.attributeFilter.defaults = {
		'onQueryChange' : function() {}
	};
}($, window.top.molgenis = window.top.molgenis || {}));