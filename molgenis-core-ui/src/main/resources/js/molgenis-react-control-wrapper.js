/**
 * MOLGENIS control wrappers for JQuery plugins
 * 
 * Dependencies: TODO
 * 
 * @param $
 * @param molgenis
 */
(function($, molgenis) {
	"use strict";
	
	var div = React.DOM.div, input = React.DOM.input, span = React.DOM.span;
	
	/**
	 * React component for Select2
	 * 
	 * @memberOf controls
	 */
	var Select2 = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'Select2',
		propTypes: {
			options: React.PropTypes.object,
			disabled: React.PropTypes.bool,
			value: React.PropTypes.oneOfType([React.PropTypes.object, React.PropTypes.array]),
			onChange: React.PropTypes.func
		},
		getInitialState: function() {
			return {value: this.props.value};
		},
		componentWillReceiveProps : function(nextProps) {
			this.setState({value: nextProps.value});
		},
		componentDidMount: function() {console.log('componentDidMount Select2');
			var $container = $(this.refs.select2.getDOMNode());
			
			// create select2
			$container.select2($.extend({
					containerCssClass: 'form-control',
					placeholder : ' ', // cannot be an empty string
					minimumResultsForSearch: -1, // permanently hide the search field
					width: '100%'
				}, this.props.options));

			// create event select2 handlers
			var self = this;
			$container.on('change', function() {
				self._handleChange($container.select2('data'));
			});

			// initialize select2
			this._updateSelect2();
		},
		componentWillUnmount: function() {console.log('componentWillUnmount Select2');
			var $container = $(this.refs.select2.getDOMNode());
			$container.off();
			$container.select2('destroy');
		},
		render: function() {console.log('render Select2', this.state, this.props);
			if (this.isMounted()) {
				this._updateSelect2();
			}
			return input({type: 'hidden', ref: 'select2', onChange: function(){}}); // empty onChange callback to suppress React warning 
		},
		_handleChange: function(value) {console.log('_handleChange Select2', value);
			this.setState({value: value});
			this.props.onChange(value);
		},
		_updateSelect2: function() {
			var $container = $(this.refs.select2.getDOMNode());
			console.log('_updateSelect2', this.state);
			$container.select2('data', this.state.value);
			$container.select2('enable', !this.props.disabled);
		}
	});
	
	/**
	 * React component for jQRangeSlider
	 * 
	 * @memberOf controls
	 */
	var JQRangeSlider = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'JQRangeSlider',
		propTypes: {
			options: React.PropTypes.object,
			disabled: React.PropTypes.array,
			onChange: React.PropTypes.func
		},
		componentDidMount: function() {console.log('componentDidMount JQRangeSlider');
			var $container = $(this.refs.rangeslider.getDOMNode());
			$container.editRangeSlider(this.props.options);

			if(this.props.disabled) {
				$container.editRangeSlider('disable');
			}

			var props = this.props;
			$container.on('userValuesChanged', function(e, data) {
				props.onChange({value: [data.values.min, data.values.max]});
			});
		},
		componentWillUnmount: function() {console.log('componentWillUnmount JQRangeSlider');
			var $container = $(this.refs.rangeslider.getDOMNode());
			$container.off();
			$container.editRangeSlider('destroy');
		},
		render: function() {console.log('render JQRangeSlider', this.state, this.props);
			if(this.isMounted()) {
				var $container = $(this.refs.rangeslider.getDOMNode());
				$container.editRangeSlider(this.props.disabled ? 'disable' : 'enable');
				$container.editRangeSlider('values', this.props.value[0], this.props.value[1]);
			}
			return div({ref: 'rangeslider'});
		}
	});
	
	/**
	 * React component for datetimepicker
	 * 
	 * @memberOf controls
	 */
	var DateTimePicker = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'DateTimePicker',
		propTypes: {
			time: React.PropTypes.bool.isRequired,
			placeholder: React.PropTypes.string,
			required: React.PropTypes.bool,
			disabled: React.PropTypes.bool,
			value: React.PropTypes.string,
			onChange: React.PropTypes.func
		},
		componentDidMount: function() {console.log('componentDidMount DateTimePicker');
			var props = this.props;

			var format = props.time ? 'YYYY-MM-DDTHH:mm:ssZZ' : 'YYYY-MM-DD';

			var $container = $(this.refs.datepicker.getDOMNode());
			$container.datetimepicker({
				format: format
			});

			$container.on('dp.change', function(event) {console.log('event.date', event.date.format(props.time));
				props.onChange({value: event.date.format(format)});
			});

			var $clearBtn = $(this.refs.clearbtn.getDOMNode());
			$clearBtn.on('click', function() {
				props.onChange({value: undefined});
			});
		},
		componentWillUnmount: function() {console.log('componentWillUnmount DateTimePicker');
			var $container = $(this.refs.datepicker.getDOMNode());
			$container.datetimepicker('destroy');
		},
		render: function() {console.log('render DateTimePicker', this.state, this.props);
			var placeholder = this.props.placeholder;
			var required = this.props.required;
			var disabled = this.props.disabled;

			return (
				div({className: 'input-group date group-append', ref: 'datepicker'},
					input({type: 'text', className: 'form-control', value: this.props.value, placeholder: placeholder, required: required, disabled: disabled, onChange: this.props.onChange}),
					span({className: 'input-group-addon'},
						span({className: 'glyphicon glyphicon-remove empty-date-input', ref: 'clearbtn'})
					),
					span({className: 'input-group-addon datepickerbutton'},
							span({className: 'glyphicon glyphicon-calendar'})
					)
				)
			);
		},
	});
	
	// export module
	molgenis.control = molgenis.control || {};
	molgenis.control.wrapper = molgenis.control.wrapper || {};
	
	$.extend(molgenis.control.wrapper, {
		Select2: React.createFactory(Select2),
		JQRangeSlider: React.createFactory(JQRangeSlider),
		DateTimePicker: React.createFactory(DateTimePicker)
	});
}($, window.top.molgenis = window.top.molgenis || {}));