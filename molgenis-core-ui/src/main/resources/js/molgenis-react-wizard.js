$(function($, molgenis) {
	"use strict";
	
	var div = React.DOM.div, ul = React.DOM.ul, li = React.DOM.li, span = React.DOM.span, a = React.DOM.a;

	var Wizard = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'Wizard',
		propTypes: {
			steps: React.PropTypes.arrayOf(React.PropTypes.object).isRequired
		},
		render: function() {console.log('render Wizard', this.state, this.props);
			var WizardListItems = [];
			for(var i = 0; i < this.props.steps.length; ++i) {
				var step = this.props.steps[i];
				var WizardListItem = WizardListItemFactory({name: step.name, step: i, active: i === 0, key: '' + i});
				WizardListItems.push(WizardListItem);
			}
			
			var WizardTabPanes = [];
			for(var i = 0; i < this.props.steps.length; ++i) {
				var step = this.props.steps[i];
				var WizardTabPane = WizardTabPaneFactory({content: step.content, active: i === 0, key: '' + i});
				WizardTabPanes.push(WizardTabPane);
			}
			
			return (
				div({}, 
					ul({className: 'bwizard-steps', role: 'tablist'},
						WizardListItems
					),
					div({className: 'tab-content'},
						WizardTabPanes
					),
					WizardPagerFactory()
				)
			);
		}
	});
	
	var WizardListItem = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'Wizard',
		propTypes: {
			name: React.PropTypes.string.isRequired,
			step: React.PropTypes.number.isRequired,
			active: React.PropTypes.bool.isRequired
		},
		render: function() {console.log('render Wizard', this.state, this.props);
			var liClasses = this.props.active ? 'active' : ''; 	
			var spanClasses = this.props.active ? 'badge inverse' : 'badge';
			return (
				li({className: liClasses, role: 'tab'},
					a({href: '#tab' + this.props.step, 'data-toggle': 'tab'},
						span({className: spanClasses}, this.props.step), ' ' + this.props.name
					)
				)
			);
		}
	});
	var WizardListItemFactory = React.createFactory(WizardListItem);
	
	var WizardTabPane = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'WizardTabPane',
		propTypes: {
			content: React.PropTypes.object.isRequired,
			step: React.PropTypes.number.isRequired,
			active: React.PropTypes.bool.isRequired
		},
		render: function() {console.log('render WizardTabPane', this.state, this.props);
			var divClasses = this.props.active ? 'tab-pane active well' : 'tab-pane well';
			return (
				div({className: divClasses, id: 'tab' + this.props.step},
					this.props.content
				)
			);
		}
	});
	var WizardTabPaneFactory = React.createFactory(WizardTabPane);
	
	var WizardPager = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'WizardPager',
		propTypes: {
			content: React.PropTypes.object.isRequired,
			step: React.PropTypes.number.isRequired,
			active: React.PropTypes.bool.isRequired
		},
		render: function() {console.log('render WizardPager', this.state, this.props);
			return (
				ul({className: 'pager wizard'},
					li({className: 'previous next hidden'},
						a({href: '#'}, 'First')
					),
					li({className: 'previous'},
						a({href: '#'}, 'Previous')
					),
					li({className: 'next last hidden'},
						a({href: '#'}, 'Last')
					),
					li({className: 'next'},
						a({href: '#'}, 'Next')
					)
				)
			);
		}
	});
	var WizardPagerFactory = React.createFactory(WizardPager);
	
	// export module
	molgenis.control = molgenis.control || {};
	
	$.extend(molgenis.control, {
		Wizard: React.createFactory(Wizard)
	});
}($, window.top.molgenis = window.top.molgenis || {}));