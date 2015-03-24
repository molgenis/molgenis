/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
	"use strict";

	var div = React.DOM.div, button = React.DOM.button, span = React.DOM.span, h4 = React.DOM.h4;
	
	/**
     * @memberOf component
     */
    var Modal = React.createClass({
    	mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
        displayName: 'Modal',
        propTypes: {
        	title: React.PropTypes.string.isRequired,
        	size: React.PropTypes.oneOf(['small', 'medium', 'large']),
        	show: React.PropTypes.bool,
        	onHide: React.PropTypes.func
        },
        getDefaultProps: function() {
        	return {
        		size: 'medium',
        		show: false,
        		onHide: function(){}
        	};
        },
        componentDidMount: function() {
        	var $modal = $(this.refs.modal.getDOMNode());
        	$modal.on('hide.bs.modal', function (e) {
        		this.props.onHide();
        	}.bind(this));
        	this._initModal();
        },
        componentWillUnmount: function() {
        	var $modal = $(this.refs.modal.getDOMNode());
        	$modal.off();
        	$modal.data('bs.modal', null); // see http://stackoverflow.com/a/18169689
		},
    	render: function() {
    		var modalDialogClasses = React.addons.classSet({
    			'modal-dialog': true,
    			'modal-sm': this.props.size == 'small',
    			'modal-lg': this.props.size == 'large'
    		});
    		return (
				div({className: 'modal', tabIndex: -1, ref: 'modal'},
					div({className: modalDialogClasses},
						div({className: 'modal-content'},
							div({className: 'modal-header'},
								button({type: 'button', className: 'close', 'data-dismiss': 'modal'},
									span({}, '\u00D7') // &times;
								),
								h4({className: 'modal-title'},
									this.props.title
								)
							),
							div({className: 'modal-body'},
								this.props.show ? this.props.children : null
							)
						)
					)
				)
    		);
    	},
    	componentDidUpdate: function() {
    		if(this.isMounted()) {
    			this._initModal();
    		}
    	},
    	_initModal: function() {
    		var $modal = $(this.refs.modal.getDOMNode());
    		if(this.props.show) {
    			$modal.modal('show');
    		} else {
    			$modal.modal('hide');
    		}
    	}
    });
    
    // export component
    molgenis.ui = molgenis.ui || {};
    _.extend(molgenis.ui, {
        Modal: React.createFactory(Modal)
    });
}(_, React, molgenis));