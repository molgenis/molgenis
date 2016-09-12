import React from "react";
import DeepPureRenderMixin from "./mixin/DeepPureRenderMixin";
import $ from "jquery";

var div = React.DOM.div, button = React.DOM.button, span = React.DOM.span, h4 = React.DOM.h4;

/**
 * @memberOf component
 */
var Modal = React.createClass({
    mixins: [DeepPureRenderMixin],
    displayName: 'Modal',
    propTypes: {
        title: React.PropTypes.string.isRequired,
        size: React.PropTypes.oneOf(['small', 'medium', 'large']),
        show: React.PropTypes.bool,
        onHide: React.PropTypes.func,
        footer: React.PropTypes.bool
    },
    getDefaultProps: function () {
        return {
            size: 'medium',
            show: false,
            onHide: function () {
            }
        };
    },
    componentDidMount: function () {
        var $modal = $(this.refs.modal.getDOMNode());
        $modal.on('hide.bs.modal', function () {
            this.props.onHide();
        }.bind(this));
        this._initModal();
    },
    componentWillUnmount: function () {
        var $modal = $(this.refs.modal.getDOMNode());
        $modal.modal('hide'); // remove modal backdrop
        $modal.off();
        $modal.data('bs.modal', null); // see http://stackoverflow.com/a/18169689
    },
    render: function () {
        var modalDialogClasses = React.addons.classSet({
            'modal-dialog': true,
            'modal-sm': this.props.size == 'small',
            'modal-lg': this.props.size == 'large'
        });
        var id = 'modal-title-' + new Date().getTime();
        return (
            div({className: 'modal', tabIndex: -1, role: 'dialog', 'aria-labelledby': id, ref: 'modal'},
                div({className: modalDialogClasses},
                    div({className: 'modal-content'},
                        div({className: 'modal-header'},
                            button({type: 'button', className: 'close', 'data-dismiss': 'modal', 'aria-label': 'Close'},
                                span({'aria-hidden': true}, String.fromCharCode(215)) // &times;
                            ),
                            h4({className: 'modal-title', id: id},
                                this.props.title
                            )
                        ),
                        div({className: 'modal-body'},
                            this.props.show ? this.props.children : null
                        ),
                        this.props.footer && <div className="modal-footer">
                            <button type="button" className="btn btn-primary"
                                    data-dismiss="modal">Close
                            </button>
                        </div>
                    )
                )
            )
        );
    },
    componentDidUpdate: function () {
        if (this.isMounted()) {
            this._initModal();
        }
    },
    _initModal: function () {
        var $modal = $(this.refs.modal.getDOMNode());
        if (this.props.show) {
            $modal.modal('show');
        } else {
            $modal.modal('hide');
        }
    }
});

export {Modal};
export default React.createFactory(Modal);