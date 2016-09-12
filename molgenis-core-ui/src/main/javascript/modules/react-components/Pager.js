import React from "react";
import DeepPureRenderMixin from "./mixin/DeepPureRenderMixin";


var ul = React.DOM.ul, li = React.DOM.li, div = React.DOM.div, span = React.DOM.span, a = React.DOM.a;

/**
 * @memberOf component
 */
var Pager = React.createClass({
    mixins: [DeepPureRenderMixin],
    displayName: 'Pager',
    propTypes: {
        nrItems: React.PropTypes.number.isRequired,
        nrItemsPerPage: React.PropTypes.number.isRequired,
        start: React.PropTypes.number.isRequired,
        onPageChange: React.PropTypes.func
    },
    getDefaultProps: function () {
        return {
            onPageChange: function () {
            }
        };
    },
    render: function () {
        var nrPages = Math.ceil(this.props.nrItems / this.props.nrItemsPerPage);
        if (nrPages === 0 || nrPages === 1) {
            return div();
        }

        var ListElements = [];
        var page = Math.floor(this.props.start / this.props.nrItemsPerPage) + 1;


        // previous page
        if (page === 1) {
            ListElements.push(li({className: 'disabled', key: 'prev'}, span(null, 'Previous')));
        } else {
            ListElements.push(li({
                className: 'page-prev',
                key: 'prev',
                onClick: this._handlePageChange.bind(this, page - 1)
            }, a({href: '#'}, 'Previous')));
        }

        // pages
        for (var i = 1; i <= nrPages; ++i) {
            if (i === page) {
                ListElements.push(li({className: 'active', key: '' + i}, span(null, i)));
            }
            else if ((i === 1) || (i === nrPages) || ((i > page - 3) && (i < page + 3)) || ((i < 7) && (page < 5)) || ((i > nrPages - 6) && (page > nrPages - 4))) {
                ListElements.push(li({
                    className: 'page',
                    onClick: this._handlePageChange.bind(this, i),
                    key: '' + i
                }, a({href: '#'}, i)));
            }
            else if ((i === 2) || (i === nrPages - 1)) {
                ListElements.push(li({className: 'disabled', key: '' + i}, span(null, '...')));
            }
        }

        // next page
        if (page === nrPages) {
            ListElements.push(li({className: 'disabled', key: 'next'}, span(null, 'Next')));
        }
        else {
            ListElements.push(li({
                className: 'page-next',
                onClick: this._handlePageChange.bind(this, page + 1),
                key: 'next'
            }, a({href: '#'}, 'Next')));
        }

        return (
            ul({className: 'pagination', style: {margin: 0}},
                ListElements
            )
        );
    },
    _handlePageChange: function (page) {
        this.props.onPageChange({
            page: page,
            start: (page - 1) * this.props.nrItemsPerPage
        });
    }
});

export default React.createFactory(Pager);