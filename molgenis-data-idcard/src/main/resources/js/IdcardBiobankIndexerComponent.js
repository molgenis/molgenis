/* global _: false, React: false, molgenis: true */
(function (_, React, molgenis) {
    "use strict";

    var div = React.DOM.div, strong = React.DOM.strong, p = React.DOM.p, h4 = React.DOM.h4, legend = React.DOM.legend, table = React.DOM.table, thead = React.DOM.thead, tbody = React.DOM.tbody, tr = React.DOM.tr, th = React.DOM.th, td = React.DOM.td, a = React.DOM.a, span = React.DOM.span, em = React.DOM.em, br = React.DOM.br, label = React.DOM.label;
    var Table, Button;
    /**
     * @memberOf component
     */
    var IdcardBiobankIndexerComponent = React.createClass({
        displayName: 'IdcardBiobankIndexerComponent',
        propTypes: {
            biobankEntity: React.PropTypes.string.isRequired,
            indexEntity: React.PropTypes.string.isRequired,
            buttonEnabled: React.PropTypes.bool,
            onButtonClick: React.PropTypes.func
        },
        getInitialState: function () {
            return {
                biobankEntity: "",
                indexEntity: "",
                buttonDisabled: false,
                onButtonClick: function () {
                }
            };
        },
        getDefaultProps: function () {
            return {
                biobankEntity: "",
                indexEntity: "",
                buttonDisabled: false,
                onButtonClick: function () {
                }
            };
        },
        render: function () {
            Table = molgenis.ui.Table({
                entity: this.props.indexEntity,
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
            });
            Button = molgenis.ui.Button({
                text: 'Reindex',
                size: 'small',
                onClick: this.props.onButtonClick,
                disabled: this.props.buttonDisabled
            });
            return (

                div({className: "row"},
                    div({className: "col-md-10 col-md-offset-1 well"},
                        legend(null, "RD-Connect ID-Card indexer"),
                        div({className: "row"},
                            div({className: "col-md-6"},
                                h4(null, "Introduction"),
                                p(null, a({href: "http://catalogue.rd-connect.eu/", target: "_blank"}, "ID-Card"),
                                    " is an online catalogue listing biobanks and patient registries connected to,",
                                    a({href: "http://rd-connect.eu/", target: "_blank"},
                                        "RD-Connect")),
                                p({id: "index-event-table-container"}, "MOLGENIS indexes biobanks and patient registries from ID-Card such that they can be queried, ",
                                    a({
                                        href: "/menu/main/dataexplorer?entity=" + this.props.biobankEntity,
                                        target: "_blank"
                                    }, "displayed "),
                                    "and referred to from other entities. Querying biobank and patient registries is performed on the index build by MOLGENIS, the resulting entities are retrieved "
                                    , strong(null, "live"), " from ID-Card for display."),
                                h4(null, "How to use"),
                                p(null, "Indexing biobanks and patient registries is disabled by default and can be enabled/disabled by opening the settings panel (by selecting the settings cog on the top right). Additionally the index frequency can be changed in the settings panel."),
                                p(null, "Select the Reindex button in case you want to perform a manual index rebuild. Note that reindexing might take a couple of minutes and does not cancel any scheduled index rebuilds."),
                                span(null, Button)
                            ),
                            div({className: "col-md-6"},
                                h4(null, "Indexing history"),
                                div(null, Table)
                            )
                        )
                    )
                )
            );
        }
    });

    // export component
    molgenis.ui = molgenis.ui || {};
    _.extend(molgenis.ui, {
        IdcardBiobankIndexerComponent: React.createFactory(IdcardBiobankIndexerComponent)
    });
}(_, React, molgenis));