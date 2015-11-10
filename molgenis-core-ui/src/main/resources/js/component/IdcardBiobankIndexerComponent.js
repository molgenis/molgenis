/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
	"use strict";

	var div = React.DOM.div, strong = React.DOM.strong, p = React.DOM.p, h4 = React.DOM.h4, legend = React.DOM.legend, table = React.DOM.table, thead = React.DOM.thead, tbody = React.DOM.tbody, tr = React.DOM.tr, th = React.DOM.th, td = React.DOM.td, a = React.DOM.a, span = React.DOM.span, em = React.DOM.em, br = React.DOM.br, label = React.DOM.label;

	/**
	 * @memberOf component
	 */
	var IdcardBiobankIndexerComponent = React.createClass({
		displayName: 'IdcardBiobankIndexerComponent',
		render: function() {
			return (
				div({className: "row"},
                	div({className: "col-md-10 col-md-offset-1 well"},
                		legend(null,"RD-Connect ID-Card indexer"),
                        div({className: "row"},
                            div({className: "col-md-6"},
                                h4(null,"Introduction"),
                                p(null,a({href:"http://catalogue.rd-connect.eu/",target:"_blank"},"ID-Card"),
                                	" is an online catalogue listing biobanks and patient registries connected to,",
                                	a({href:"http://rd-connect.eu/",target:"_blank"},
                                	"RD-Connect")),
                                p({id:"index-event-table-container"},"MOLGENIS indexes biobanks and patient registries from ID-Card such that they can be queried, ",
                                	a({href:"/menu/main/dataexplorer?entity=${id_card_biobank_registry_entity_name?html}",target:"_blank"},"displayed "),//FIXME: entityname prop
                                	"and referred to from other entities. Querying biobank and patient registries is performed on the index build by MOLGENIS, the resulting entities are retrieved"
                                	,strong(null,"live"),"from ID-Card for display."),
                                h4(null,"How to use"),
                                p(null,"Indexing biobanks and patient registries is disabled by default and can be enabled/disabled by opening the settings panel (by selecting the settings cog on the top right). Additionally the index frequency can be changed in the settings panel."),
                                p(null,"Select the Reindex button in case you want to perform a manual index rebuild. Note that reindexing might take a couple of minutes and does not cancel any scheduled index rebuilds."),
                                span({id:"button-container"},renderReindexButton())
                            ),
                            div({className: "col-md-6"},
                                h4(null,"Indexing history"),
                                div({id:"table-container"},renderTable())
                			)
                		)
					)
				)
			);
		}
	});

		function renderTable() {
        		React.render(molgenis.ui.Table({
        			entity: 'IdCardIndexingEvent',
        			sort: {
        				attr: {
        					name: 'date'
        				},
        				order: 'desc',
        				path: []
        			},
        			enableAdd: false,
        			enableEdit: false,
        			enableDelete: false,
        			enableInspect: false
        		}), $('#table-container')[0]);
        	}

        	function refreshTable() {
        		// refresh table
        		React.unmountComponentAtNode($('#index-event-table-container')[0]);
        		renderTable();
        	}
        	function renderReindexButton() {
        	React.render(molgenis.ui.Button({
            			text : 'Reindex',
            			size: 'small',
            			onClick : function() {
            				// TODO disable button
            				$.post(molgenis.getContextUrl() + '/reindex').done(function(job) {
            					updateJobStatus(job);
            				});
            			}
            		}, 'Reindex'), $('#button-container')[0]);
            }

            function refreshTable() {
				// refresh table
				React.unmountComponentAtNode($('#index-event-table-container')[0]);
				renderTable();
			}

			function updateJobStatus(job) {
				if(job.triggerStatus === 'NONE' || job.triggerStatus === 'SUCCESS' || job.triggerStatus === 'ERROR') {
					// TODO enable button
					molgenis.createAlert([{'message': 'Reindexing ID-Card completed'}], 'success');
					refreshTable();
				}
				else {
					molgenis.createAlert([{'message': 'Reindexing ID-Card biobanks in progress ...'}], 'info');
					setTimeout(function() {
						$.get(molgenis.getContextUrl() + '/status/' + job.triggerGroup + '/' + job.triggerName).done(function(job) {
							updateJobStatus(job);
						});
					}, 1000);
				}
			}

    // export component
    molgenis.ui = molgenis.ui || {};
    _.extend(molgenis.ui, {
        IdcardBiobankIndexerComponent: React.createFactory(IdcardBiobankIndexerComponent)
    });
}(_, React, molgenis));