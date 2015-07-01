/**
 * Data module
 * 
 * Dependencies: dataexplorer.js
 *  
 * @param $
 * @param molgenis
 */
(function($, molgenis) {
	"use strict";
	
	molgenis.dataexplorer = molgenis.dataexplorer || {};
	var self = molgenis.dataexplorer.data = molgenis.dataexplorer.data || {};
	self.createDataTable = createDataTable;
	self.createGenomeBrowser = createGenomeBrowser;
    self.doShowGenomeBrowser = doShowGenomeBrowser;
    self.setGenomeBrowserAttributes = setGenomeBrowserAttributes;
    self.setGenomeBrowserSettings = setGenomeBrowserSettings;
    self.setGenomeBrowserEntities = setGenomeBrowserEntities;
    
	var restApi = new molgenis.RestClient();
	var genomeBrowser;
	var genomeEntities;

    var genomebrowserStartAttribute;
    var genomebrowserChromosomeAttribute;
    var genomebrowserIdentifierAttribute;
    var genomebrowserPatientAttribute;
    var genomeBrowserSettings = {};
    var featureInfoMap = {};

    var Table, tableSort;
    
    $(document).on('dataChange.diseasematcher', function(e) {
    	if (e.namespace !== 'data' && Table){
    		Table.setProps({query: getQuery()});
    	}
	});
    
    /**
	 * @memberOf molgenis.dataexplorer.data
	 */
    function setGenomeBrowserSettings(settings) {
    	genomeBrowserSettings = settings;
    }
    
    /**
	 * @memberOf molgenis.dataexplorer.data
	 */
    function setGenomeBrowserEntities(genomeEntitiesKeyValues) {
    	genomeEntities = genomeEntitiesKeyValues;
    }
    
	/**
	 * @memberOf molgenis.dataexplorer.data
	 */
	function createDataTable() {
		Table = React.render(molgenis.ui.Table({
			entity: getEntity().name,
			attrs: getAttributesTree(),
			query: getQuery(),
			maxRows: 18,
			onRowAdd: onDataChange,
			onRowDelete: onDataChange,
			onRowEdit: onDataChange,
			onRowInspect: onRowInspect,
			onSort: function(e) {
				tableSort = {
					'orders' : [ {
						'attr' : e.attr.name,
						'direction' : e.order === 'desc' ? 'DESC' : 'ASC'
					} ]
				};
			}
		}), $('#data-table-container')[0]);
	}
	
	function onDataChange() {
		$(document).trigger('dataChange.data');
	}
	
	function onRowInspect(e) {
		var entityId = e.id;
		var entityName = e.name;
		
		$('#entityReport').load("dataexplorer/details",{entityName: entityName, entityId: entityId}, function() {
			  $('#entityReportModal').modal("show");
			  
			  // Button event handler when a button is placed inside an entity report ftl
			  $(".modal-body button", "#entityReport").on('click', function() {
					$.download($(this).data('href'), {entityName: entityName, entityId: entityId}, "GET");
			  });
		});
	}
	
	/**
	 * @memberOf molgenis.dataexplorer.data
	 */
	function download() {
		$.download(molgenis.getContextUrl() + '/download', {
			// Workaround, see http://stackoverflow.com/a/9970672
			'dataRequest' : JSON.stringify(createDownloadDataRequest())
		});
		
		$('#downloadModal').modal('hide');
	}
	
	/**
	 * @memberOf molgenis.dataexplorer.data
	 */
	function createDownloadDataRequest() {
		var entityQuery = getQuery();
		
		var dataRequest = {
			entityName : getEntity().name,
			attributeNames: [],
			query : {
				rules : [entityQuery.q]
			},
			colNames : $('input[name=colNames]:checked').val(),
			entityValues : $('input[name=entityValues]:checked').val(),
			downloadType : $('input[name=downloadTypes]:checked').val()
		};

		dataRequest.query.sort = tableSort;
		
		var colAttributes = molgenis.getAtomicAttributes(getAttributes(), restApi);
		
		$.each(colAttributes, function() {
			var feature = this;
			dataRequest.attributeNames.push(feature.name);
		});

		return dataRequest;
	}

	//--BEGIN genome browser--
	/**
	 * @memberOf molgenis.dataexplorer.data
	 */
	function doShowGenomeBrowser() {
		// dalliance is not compatible with IE9
		return molgenis.ie9 !== true && genomebrowserStartAttribute !== undefined && genomebrowserChromosomeAttribute !== undefined && molgenis.dataexplorer.settings["genomebrowser"] !== 'false';
	}

    function getAttributeFromList(attributesString){
        var result;
        var attrs = getEntity().attributes;
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
	 * @memberOf molgenis.dataexplorer.data
	 */
	function createGenomeBrowser(specificSettings) {
        var showHighlight = false;
        if(specificSettings !== null) {
            showHighlight = specificSettings.highlightRegion;
        }
		var settings = $.extend(true, {}, genomeBrowserSettings, specificSettings || {});
		
        $('#genomebrowser').css('display', 'block');
        $('#genomebrowser').css('visibility', 'visible');

        var entity = getEntity();
        // add track for current genomic entity
        var dallianceTrack = {
            name : entity.label || entity.name,
            uri : '/das/molgenis/dasdataset_' + entity.name + '/',
            desc : entity.description,
            stylesheet_uri : '/css/selected_dataset-track.xml'
        };
        
        settings.sources.push(dallianceTrack);
        // add reference tracks for all other genomic entities
        $.each(genomeEntities, function(i, refEntity) {
            if(refEntity.name !== entity.name) {
                var dallianceTrack = {
                    name : refEntity.label || refEntity.name,
                    uri : '/das/molgenis/dasdataset_' + refEntity.name + '/',
                    desc : refEntity.description,
                    stylesheet_uri : '/css/selected_dataset-track.xml'
                };
                settings.sources.push(dallianceTrack);
            }
        });
            
        settings.registry = 'https://www.dasregistry.org/das/sources';
        settings.prefix = 'https://www.biodalliance.org/release-0.13/';
        genomeBrowser = new Browser(settings);
        // highlight region specified with viewStart and viewEnd
        if(showHighlight === true) {
            genomeBrowser.highlightRegion(genomeBrowser.chr, (genomeBrowser.viewStart + 9990), (genomeBrowser.viewEnd - 9990));
        }
        genomeBrowser.addFeatureInfoPlugin(function(f, info){createGenomeBrowserInfoPopup(f, info, entity);});

        return genomeBrowser;
	}

    function createGenomeBrowserInfoPopup(f, info, entity) {
        //check if there is cached information for this clicked item
        //move down
        if(featureInfoMap.hasOwnProperty(info.tier.dasSource.name + f.id + f.label)){
            $.each(featureInfoMap[info.tier.dasSource.name + f.id + f.label].sections, function(section) {
                info.sections.push(featureInfoMap[info.tier.dasSource.name + f.id + f.label].sections[section]);
            });
        }
        else{
            var molgenisIndex = f.notes.indexOf("source:MOLGENIS");
            if(info.feature.score==="0.0") {
                info.feature.score = undefined;
            }
            if(info.feature.method==="not_recorded") {
                info.feature.method = undefined;
            }

            var selectedTrack = false;
            if(molgenisIndex!==-1){
                $.each(f.notes, function(note) {
                    var patientIndex = f.notes[note].indexOf("patient:");
                    var noteParts = f.notes[note].split("~");

                    var trackName;
                    var trackIndex = f.notes[note].indexOf("track:");
                    if (trackIndex !== -1) {
                        trackName = f.notes[note].substr(trackIndex + 6);
                        if (entity.name === trackName) {
                            selectedTrack = true;
                        }
                    }

                    if(patientIndex!==-1){
                        var patientID = f.notes[note].substr(patientIndex+8);
                        if(selectedTrack){
                            var a = $('<a href="javascript:void(0)">' + patientID + '</a>');
                            a.click(function() {
                                $.each(getAttributes(), function(key, attribute) {
                                    if(attribute === genomebrowserPatientAttribute) {
                                        createFilter(attribute, undefined, undefined, patientID);
                                    }
                                });
                            });
                            info.add('Filter on patient:', a[0]);
                        }
                    }
                    else if(noteParts.length === 2) {
                        info.add(noteParts[0], noteParts[1]);
                    }
                });
                //get the mutation note to create a mutations filter link
                if(selectedTrack) {
                    var a = $('<a href="javascript:void(0)">' + f.id + '</a>');
                    var attr;
                    $.each(getAttributes(), function (key, attribute) {
                        if (attribute === genomebrowserIdentifierAttribute) {
                            attr = attribute;
                        }
                    });
                    a.click(function () {
                        createFilter(attr, undefined, undefined, f.id);
                    });

                    if (f.id !== "-" && attr !== undefined) {
                        info.setTitle(f.id);
                        info.add('Filter on mutation:', a[0]);
                        //cache the information
                    }
                    else{
                        info.setTitle("Chromosome:"+f.segment+" Position:"+ f.min);
                    }
                }
            }
            featureInfoMap[info.tier.dasSource.name + f.id + f.label] = info;
        }
        //all notes are parsed and added to the info window if necessary
        info.feature.notes = [];
    }


	/**
	 * @memberOf molgenis.dataexplorer.data
	 */
	function setDallianceFilter() {
		$.each(getAttributes(), function(key, attribute) {
			if(attribute === genomebrowserStartAttribute) {
                createFilter(attribute, Math.floor(genomeBrowser.viewStart).toString(), Math.floor(genomeBrowser.viewEnd).toString());
			} else if(attribute === genomebrowserChromosomeAttribute) {
                createFilter(attribute, undefined, undefined, genomeBrowser.chr);
			}
		});
	}

	/**
	 * @memberOf molgenis.dataexplorer.data
	 */
    function setGenomeBrowserAttributes(start, chromosome, id, patient){
        genomebrowserStartAttribute = getAttributeFromList(start);
        genomebrowserChromosomeAttribute = getAttributeFromList(chromosome);
        genomebrowserIdentifierAttribute = getAttributeFromList(id);
        genomebrowserPatientAttribute = getAttributeFromList(patient);
    }
	//--END genome browser--

	/**
	 * @memberOf molgenis.dataexplorer.data
	 */
	function getEntity() {
		return molgenis.dataexplorer.getSelectedEntityMeta();
	}

	/**
	 * @memberOf molgenis.dataexplorer.data
	 */
	function getAttributes() {
		return molgenis.dataexplorer.getSelectedAttributes();
	}

	/**
	 * @memberOf molgenis.dataexplorer.data
	 */
	function getAttributesTree() {
		return molgenis.dataexplorer.getSelectedAttributesTree();
	}
	
	/**
	 * @memberOf molgenis.dataexplorer.data
	 */
	function getQuery() {
		return molgenis.dataexplorer.getEntityQuery();
	}

    function createFilter(attribute, fromValue, toValue, values){
        var attributeFilter = new molgenis.dataexplorer.filter.SimpleFilter(attribute, fromValue, toValue, values);
        var complexFilter = new molgenis.dataexplorer.filter.ComplexFilter(attribute);
        var complexFilterElement = new molgenis.dataexplorer.filter.ComplexFilterElement(attribute);
        complexFilterElement.simpleFilter = attributeFilter;
        complexFilterElement.operator = undefined;
        complexFilter.addComplexFilterElement(complexFilterElement);
        $(document).trigger('updateAttributeFilters', {'filters': [complexFilter]});
    }

	/**
	 * @memberOf molgenis.dataexplorer.data
	 */
	$(function() {
		$(document).on('changeAttributeSelection.data', function(e, data) {
			if(Table) {
				Table.setProps({attrs: data.attributesTree});
			}
		});

		$(document).on('updateAttributeFilters.data', function(e, data) {
			/**
			 * Validation before using the setLocation of the browser
			 */
			function setLocation(chr, viewStart, viewEnd){
				var maxViewWidth = 999999999;
				if(chr){
					viewStart = viewStart && viewStart > 0 ? viewStart : 1;
					viewEnd = viewEnd && viewEnd > 0 ? viewEnd : viewStart + maxViewWidth;
					genomeBrowser.setLocation(chr, viewStart, viewEnd);
				}
			}

			if(molgenis.dataexplorer.settings["genomebrowser"] !== 'false'){
				// TODO implement elegant solution for genome browser specific code
				$.each(data.filters, function() {
					if(this.getComplexFilterElements && this.getComplexFilterElements()[0]){
						if(this.attribute === genomebrowserStartAttribute){
							setLocation(genomeBrowser.chr,
									parseInt(this.getComplexFilterElements()[0].simpleFilter.fromValue),
									parseInt(this.getComplexFilterElements()[0].simpleFilter.toValue));
						}
						else if(this.attribute === genomebrowserChromosomeAttribute){
							setLocation(this.getComplexFilterElements()[0].simpleFilter.getValues()[0],
									genomeBrowser.viewStart,
									genomeBrowser.viewEnd);
						}
					}
				});
			}
		});

		$(document).on('changeQuery.data', function(e, query) {
			if(Table) {
				Table.setProps({
					query : query
				});
			}
			// TODO what to do for genome browser
		});

		$('#download-button').click(function() {
			download();
		});

		$('form[name=galaxy-export-form]').validate({
			rules : {
				galaxyUrl : {
					required : true,
					url : true
				},
				galaxyApiKey : {
					required : true,
					minlength: 32,
					maxlength: 32
				}
			}
		});
		
		$('form[name=galaxy-export-form]').submit(function(e) {
			e.preventDefault();
			if($(this).valid()) {
				$.ajax({
					type : $(this).attr('method'),
					url : $(this).attr('action'),
					data : JSON.stringify($.extend({}, $(this).serializeObject(), {dataRequest : createDownloadDataRequest()})),
					contentType: 'application/json'
				}).done(function() {
					molgenis.createAlert([{'message' : 'Exported data set to Galaxy'}], 'success');
				}).always(function() {
					$('#galaxy-export-modal').modal('hide');
				});
			}
		});
		
		$('#genomebrowser-filter-button').click(function() {
			setDallianceFilter();
		});
	});
}($, window.top.molgenis = window.top.molgenis || {}));