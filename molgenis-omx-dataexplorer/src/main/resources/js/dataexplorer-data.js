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
	
	var genomeBrowser;
	var genomeEntities;
	
	/**
	 * @memberOf molgenis.dataexplorer.data
	 */
	function createDataTable() {
		var attributes = getAttributes();
		if (attributes.length > 0) {
			$('#data-table-container').table({
				'entityMetaData' : getEntity(),
				'attributes' : attributes,
				'query' : molgenis.dataexplorer.getEntityQuery()
			});
		} else {
			$('#data-table-container').html('No items selected');
		}
	};
	
	/**
	 * @memberOf molgenis.dataexplorer.data
	 */
	function download() {
		parent.showSpinner();
		$.download(molgenis.getContextUrl() + '/download', {
			// Workaround, see http://stackoverflow.com/a/9970672
			'dataRequest' : JSON.stringify(createDownloadDataRequest())
		});
		parent.hideSpinner();
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
			
			genomeBrowser = new Browser(settings);
			// workaround: init browser after DOMContentLoaded event was fired
			genomeBrowser.realInit();
			updateGenomeBrowser();
		} else {
			$('#genomebrowser').css('display', 'none');
		}
	}
	
	/**
	 * @memberOf molgenis.dataexplorer.data
	 */
	function updateGenomeBrowser() {
		if (isGenomeBrowserEntity()) {
			var entity = getEntity();
			genomeBrowser.reset();
			// add track for current genomic entity
			var dallianceTrack = {
				name : entity.label || entity.name,
				uri : '/das/molgenis/dataset_' + entity.name + '/',
				desc : "Selected dataset",
				stylesheet_uri : '/css/selected_dataset-track.xml'
			};
			console.log(dallianceTrack);
			genomeBrowser.addTier(dallianceTrack);
			
			// add reference tracks for all other genomic entities
			$.each(genomeEntities, function(i, refEntity) {
				if(refEntity.name !== entity.name) {
					var dallianceTrack = {
						name : refEntity.label || refEntity.name,
						uri : '/das/molgenis/dataset_' + refEntity.name + '/',
						desc : "unselected dataset",
						stylesheet_uri : '/css/not_selected_dataset-track.xml'
					};
					genomeBrowser.addTier(dallianceTrack);
				}
			});
		}
	}

	/**
	 * @memberOf molgenis.dataexplorer.data
	 */
	function setDallianceFilter() {
		$.each(getAttributes(), function(key, attribute) {
			if(key === 'start_nucleotide') {
				var attributeFilter = {
					attribute : attribute,
					range : true,
					values : [ Math.floor(genomeBrowser.viewStart).toString(), Math.floor(genomeBrowser.viewEnd).toString() ]
				};
				$(document).trigger('updateAttributeFilters', {'filters': [attributeFilter]});
			} else if(key === 'chromosome') {
				var attributeFilter = {
					attribute : attribute,
					values : [ genomeBrowser.chr ]
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
	
	/**
	 * @memberOf molgenis.dataexplorer.data
	 */
	$(function() {	
		$(document).on('show', '#genomebrowser .collapse', function() {
            $(this).parent().find(".icon-chevron-right").removeClass("icon-chevron-right").addClass("icon-chevron-down");
        }).on('hide', '#genomebrowser .collapse', function() {
            $(this).parent().find(".icon-chevron-down").removeClass("icon-chevron-down").addClass("icon-chevron-right");
        });
		
		$(document).on('changeAttributeSelection', function(e, data) {
			if($('#data-table-container'))
				$('#data-table-container').table('setAttributes', data.attributes);
			updateGenomeBrowser();
		});
		
		$(document).on('updateAttributeFilters', function(e, data) {
			molgenis.dataexplorer.data.createDataTable();
			
			// TODO implement elegant solution for genome browser specific code
			$.each(data.filters, function() {
				if(this.attribute.name === 'start_nucleotide') dalliance.setLocation(dalliance.chr, this.values[0], this.values[1]);
				if(this.attribute.name === 'chromosome') dalliance.setLocation(this.values[0], dalliance.viewStart, dalliance.viewEnd);
			});
		});
		
		$(document).on('changeEntitySearchQuery', function(e, entitySearchQuery) {
			molgenis.dataexplorer.data.createDataTable();
			// TODO what to do for genomebrowser?
		});
		
		$(document).on('removeAttributeFilter', function(e, data) {
			molgenis.dataexplorer.data.createDataTable();
			// TODO what to do for genomebrowser?
		});
		
		$('#download-button').click(function() {
			download();
		});
		
		$('#genomebrowser-filter-button').click(function() {
			setDallianceFilter();
		});
	});
})($, window.top.molgenis = window.top.molgenis || {});