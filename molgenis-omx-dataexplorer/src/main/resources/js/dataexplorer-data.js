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

	var genomeEntities;
    self.genomeBrowser;
	/**
	 * @memberOf molgenis.dataexplorer.data
	 */
	function createDataTable() {
		var attributes = getAttributes();
		$('#data-table-container').table({
			'entityMetaData' : getEntity(),
			'attributes' : attributes,
			'query' : molgenis.dataexplorer.getEntityQuery()
		});
	};
	
	/**
	 * @memberOf molgenis.dataexplorer.data
	 */
	function download() {
		$.download(molgenis.getContextUrl() + '/download', {
			// Workaround, see http://stackoverflow.com/a/9970672
			'dataRequest' : JSON.stringify(createDownloadDataRequest())
		});
	}
	
	/**
	 * @memberOf molgenis.dataexplorer.data
	 */
	function createDownloadDataRequest() {
		var entityQuery = molgenis.dataexplorer.getEntityQuery();
		
		var dataRequest = {
			entityName : getEntity().name,
			attributeNames: [],
			query : {
				rules : [entityQuery.q]
			}
		};

		var query = $('#data-table-container').table('getQuery');
		if (query && query.sort) {
			searchRequest.query.sort = query.sort;
		}
		
		$.each(getAttributes(), function() {
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
	function isGenomeBrowserEntity() {
		// FIXME check attributes on all levels
		var attrs = getEntity().attributes;
		return attrs.hasOwnProperty("start_nucleotide") && 
			attrs.hasOwnProperty("mutation_id") &&
			attrs.hasOwnProperty("chromosome");	
	}
	
	/**
	 * @memberOf molgenis.dataexplorer.data
	 */
	function createGenomeBrowser(settings, genomeEntityKeyVals) {
		genomeEntities = genomeEntityKeyVals;
        if (isGenomeBrowserEntity()) {
            $('#genomebrowser').css('display', 'block');
            $('#genomebrowser').css('visibility', 'visible');

            var entity = getEntity();
            // add track for current genomic entity
            var dallianceTrack = {
                name : entity.label || entity.name,
                uri : '/das/molgenis/dataset_' + entity.name + '/',
                desc : "",
                stylesheet_uri : '/css/selected_dataset-track.xml'
            };

            settings.sources.push(dallianceTrack);
            // add reference tracks for all other genomic entities
            $.each(genomeEntities, function(i, refEntity) {
                if(refEntity.name !== entity.name) {
                    var dallianceTrack = {
                        name : refEntity.label || refEntity.name,
                        uri : '/das/molgenis/dataset_' + refEntity.name + '/',
                        desc : "unselected dataset",
                        stylesheet_uri : '/css/not_selected_dataset-track.xml'
                    };
                    settings.sources.push(dallianceTrack);
                }
            });
            self.genomeBrowser = new Browser(settings);
            self.genomeBrowser.realInit();
            var featureInfoMap = new Object();
            self.genomeBrowser.addFeatureInfoPlugin(function(f, info) {
                if(f.id in featureInfoMap){
                    $.each(featureInfoMap[f.id+f.label].sections, function(section) {
                        info.sections.push(featureInfoMap[f.id+f.label].sections[section]);
                    });
                }
                else{
                    var selectedTrack = false;
                    $.each(f.notes, function(note) {
                        var trackIndex = f.notes[note].indexOf("track:")
                        if(trackIndex!=-1){
                            var trackName = f.notes[note].substr(trackIndex+6);
                            if(entity.name == trackName){
                                selectedTrack = true;
                            }
                            info.feature.notes.splice(trackIndex,1);
                            return false;
                        }
                    });
                    $.each(f.notes, function(note) {
                        var patientIndex = f.notes[note].indexOf("patient:")
                        if(patientIndex!=-1){
                            var patientID = f.notes[note].substr(patientIndex+8);
                            info.feature.notes.splice(note,1);
                            if(selectedTrack){
                                var a = document.createElement('a');
                                a.href =  'javascript:void(0)';
                                a.innerHTML = patientID
                                a.onclick = function () {
                                    $.each(getAttributes(), function(key, attribute) {
                                        if(attribute.name === 'patient_id') {
                                            var attributeFilter = {
                                                attribute : attribute,
                                                values : [patientID]
                                            };
                                            $(document).trigger('updateAttributeFilters', {'filters': [attributeFilter]});
                                        }
                                    });
                                };
                                info.add('Filter on patient:', a);
                            }
                            return false;
                        }
                    });
                    var molgenisIndex = f.notes.indexOf("source:MOLGENIS");
                    if(molgenisIndex!=-1){
                        info.feature.notes.splice(molgenisIndex,1);
                        if(selectedTrack){
                            var a = document.createElement('a');
                            a.href =  'javascript:void(0)';
                            a.innerHTML = f.id
                            a.onclick = function () {
                                $.each(getAttributes(), function(key, attribute) {
                                    if(attribute.name === 'mutation_id') {
                                        var attributeFilter = {
                                            attribute : attribute,
                                            values : [f.id]
                                        };
                                        $(document).trigger('updateAttributeFilters', {'filters': [attributeFilter]});
                                    }
                                });
                            };
                            info.add('Filter on mutation:', a);
                            featureInfoMap[f.id+f.label] = info;
                            return false;
                        }
                    }
                }
            });


            //self.genomeBrowser.addViewListener(function(chromosome,start,end,oldZoom,newZoom) {});
            //self.genomeBrowser.addFeatureListener(function(mouseEvent,feature,hit,tier) {});
            //self.genomeBrowser.highlightRegion(1,10017408,10018357);

		}else {
            $('#genomebrowser').css('display', 'none');
        }
	}


	/**
	 * @memberOf molgenis.dataexplorer.data
	 */
	function setDallianceFilter() {
		$.each(getAttributes(), function(key, attribute) {
			if(attribute.name === 'start_nucleotide') {
                var attributeFilter = {
					attribute : attribute,
					range : true,
					values : [ Math.floor(self.genomeBrowser.viewStart).toString(), Math.floor(self.genomeBrowser.viewEnd).toString() ]
				};
				$(document).trigger('updateAttributeFilters', {'filters': [attributeFilter]});
			} else if(attribute.name === 'chromosome') {
				var attributeFilter = {
					attribute : attribute,
					values : [ self.genomeBrowser.chr ]
				};
				$(document).trigger('updateAttributeFilters', {'filters': [attributeFilter]});
			}
		});
	}
	//--END genome browser--
	
	function getEntity() {
		return molgenis.dataexplorer.getSelectedEntityMeta();
	}
	
	function getAttributes() {
		return molgenis.dataexplorer.getSelectedAttributes();
	}
	
	function getQuery() {
		return molgenis.dataexplorer.getEntityQuery();
	}
	
	/**
	 * @memberOf molgenis.dataexplorer.data
	 */
	$(function() {		
		// bind event handlers with namespace 
		$(document).on('show.data', '#genomebrowser .collapse', function() {
            $(this).parent().find(".icon-chevron-right").removeClass("icon-chevron-right").addClass("icon-chevron-down");
        }).on('hide.data', '#genomebrowser .collapse', function() {
            $(this).parent().find(".icon-chevron-down").removeClass("icon-chevron-down").addClass("icon-chevron-right");
        });
		
		$(document).on('changeAttributeSelection.data', function(e, data) {
			if($('#data-table-container'))
				$('#data-table-container').table('setAttributes', data.attributes);
		});
		
		$(document).on('updateAttributeFilters.data', function(e, data) {
			// TODO implement elegant solution for genome browser specific code
			$.each(data.filters, function() {
				if(this.attribute.name === 'start_nucleotide') self.genomeBrowser.setLocation(self.genomeBrowser.chr, this.values[0], this.values[1]);
				if(this.attribute.name === 'chromosome') self.genomeBrowser.setLocation(this.values[0], self.genomeBrowser.viewStart, self.genomeBrowser.viewEnd);
			});
		});
		
		$(document).on('changeQuery.data', function(e, query) {
			$('#data-table-container').table('setQuery', query);
			// TODO what to do for genome browser
		});
		
		$('#download-button').click(function() {
			download();
		});
		
		$('#genomebrowser-filter-button').click(function() {
			setDallianceFilter();
		});
	});
})($, window.top.molgenis = window.top.molgenis || {});