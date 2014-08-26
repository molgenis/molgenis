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
	function createDataTable(editable, rowClickable) {
		var attributes = getAttributes();
		$('#data-table-container').table({
			'entityMetaData' : getEntity(),
			'attributes' : attributes,
			'query' : getQuery(),
			'editable' : editable,
			'rowClickable': rowClickable
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
			colNames : $('input[name=ColNames]:checked').val()
		};

		dataRequest.query.sort = $('#data-table-container').table('getSort');
		
		var colAttributes = molgenis.getAtomicAttributes(getAttributes(), restApi);
		
		$.each(colAttributes, function() {
			var feature = this;
			dataRequest.attributeNames.push(feature.name);
			if (feature.fieldType === 'XREF' || feature.fieldType === 'MREF')
				dataRequest.attributeNames.push("key-" + feature.name);
		});

		return dataRequest;
	}

	//--BEGIN genome browser--
	/**
	 * @memberOf molgenis.dataexplorer.data
	 */
	function doShowGenomeBrowser() {
		return genomebrowserStartAttribute !== undefined &&
            genomebrowserChromosomeAttribute !== undefined;
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
                    stylesheet_uri : '/css/not_selected_dataset-track.xml'
                };
                settings.sources.push(dallianceTrack);
            }
        });
            
        settings.registry = 'https://www.dasregistry.org/das/sources';
        genomeBrowser = new Browser(settings);
        genomeBrowser.realInit();
        // highlight region specified with viewStart and viewEnd
        genomeBrowser.highlightRegion(genomeBrowser.chr, (genomeBrowser.viewStart + 9990), (genomeBrowser.viewEnd - 9990));
        var featureInfoMap = {};
        genomeBrowser.addFeatureInfoPlugin(function(f, info) {
            //check if there is cached information for this clicked item
            if(featureInfoMap.hasOwnProperty(f.id+f.label)){
                $.each(featureInfoMap[f.id+f.label].sections, function(section) {
                    info.sections.push(featureInfoMap[f.id+f.label].sections[section]);
                });
            }
            else{
                var selectedTrack = false;
                var molgenisIndex = f.notes.indexOf("source:MOLGENIS");
                if(molgenisIndex!==-1){
                    //get the value of the "track" field to see if this is the selected Entity in the dataexplorer
                    $.each(f.notes, function(note) {
                        var trackIndex = f.notes[note].indexOf("track:");
                        if(trackIndex!==-1){
                            var trackName = f.notes[note].substr(trackIndex+6);
                            if(entity.name == trackName){
                                selectedTrack = true;
                            }
                            info.feature.notes.splice(trackIndex,1);
                            return false;
                        }
                    });
                    //get the patient note to create a filter on patient link
                    $.each(f.notes, function(note) {
                        var patientIndex = f.notes[note].indexOf("patient:");
                        if(patientIndex!==-1){
                            var patientID = f.notes[note].substr(patientIndex+8);
                            info.feature.notes.splice(note,1);
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
                            return false;
                        }
                    });
                    //get the mutation note to create a mutations filter link
                    info.feature.notes.splice(molgenisIndex,1);
                    if(selectedTrack) {
                        var a = $('<a href="javascript:void(0)">' + f.id + '</a>');
                        a.click(function () {
                            $.each(getAttributes(), function (key, attribute) {
                                if (attribute === genomebrowserIdentifierAttribute) {
                                    createFilter(attribute, undefined, undefined, f.id);
                                }
                            });
                        });
                        if (f.id !== "-") {
                            info.add('Filter on mutation:', a[0]);
                            //cache the information
                            featureInfoMap[f.id + f.label] = info;
                        }
                        return false;
                    }
                }
            }
        });
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
			if($('#data-table-container'))
				$('#data-table-container').table('setAttributes', data.attributes);
		});

		$(document).on('updateAttributeFilters.data', function(e, data) {
			// TODO implement elegant solution for genome browser specific code
			$.each(data.filters, function() {
				if(this.attribute === genomebrowserStartAttribute){
                    genomeBrowser.setLocation(genomeBrowser.chr, this.getComplexFilterElements()[0].simpleFilter.fromValue, this.getComplexFilterElements()[0].simpleFilter.toValue)
                };
				if(this.attribute === genomebrowserChromosomeAttribute){
                    genomeBrowser.setLocation(this.getComplexFilterElements()[0].simpleFilter.getValues()[0], genomeBrowser.viewStart, genomeBrowser.viewEnd)
                };
			});
		});

		$(document).on('changeQuery.data', function(e, query) {
			$('#data-table-container').table('setQuery', query);
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
})($, window.top.molgenis = window.top.molgenis || {});