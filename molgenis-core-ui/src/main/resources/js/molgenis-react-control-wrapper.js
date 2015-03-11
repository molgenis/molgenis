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
	
	var div = React.DOM.div, input = React.DOM.input, span = React.DOM.span, textarea = React.DOM.textarea;
	
	/**
	 * React component for select box replacement Select2 (http://select2.github.io/)
	 * 
	 * @memberOf control.wrapper
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
		componentDidMount: function() {//console.log('componentDidMount Select2');
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
		componentWillUnmount: function() {//console.log('componentWillUnmount Select2');
			var $container = $(this.refs.select2.getDOMNode());
			$container.off();
			$container.select2('destroy');
		},
		render: function() {//console.log('render Select2', this.state, this.props);
			if (this.isMounted()) {
				this._updateSelect2();
			}
			return input({type: 'hidden', ref: 'select2', onChange: function(){}}); // empty onChange callback to suppress React warning FIXME use InputControl 
		},
		_handleChange: function(value) {//console.log('_handleChange Select2', value);
			this.setState({value: value});
			this.props.onChange(value);
		},
		_updateSelect2: function() {
			var $container = $(this.refs.select2.getDOMNode());
			//console.log('_updateSelect2', this.state);
			$container.select2('data', this.state.value);
			$container.select2('enable', !this.props.disabled);
			$container.select2('readonly', this.props.readOnly);
		}
	});
	
	/**
	 * React component for range slider jQRangeSlider (http://ghusse.github.io/jQRangeSlider/)
	 * 
	 * @memberOf control.wrapper
	 */
	var JQRangeSlider = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'JQRangeSlider',
		propTypes: {
			id: React.PropTypes.string,
			options: React.PropTypes.object,
			disabled: React.PropTypes.array,
			onChange: React.PropTypes.func
		},
		componentDidMount: function() {//console.log('componentDidMount JQRangeSlider');
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
		componentWillUnmount: function() {//console.log('componentWillUnmount JQRangeSlider');
			var $container = $(this.refs.rangeslider.getDOMNode());
			$container.off();
			$container.editRangeSlider('destroy');
		},
		render: function() {//console.log('render JQRangeSlider', this.state, this.props);
			if(this.isMounted()) {
				var $container = $(this.refs.rangeslider.getDOMNode());
				$container.editRangeSlider(this.props.disabled ? 'disable' : 'enable');
				$container.editRangeSlider('values', this.props.value[0], this.props.value[1]);
			}
			return div({id: this.props.id, ref: 'rangeslider'});
		}
	});
	
	/**
	 * React component for Datepicker (http://eonasdan.github.io/bootstrap-datetimepicker/)
	 * 
	 * @memberOf control.wrapper
	 */
	var DateTimePicker = React.createClass({ // FIXME should use controlled input
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'DateTimePicker',
		propTypes: {
			id: React.PropTypes.string,
			name: React.PropTypes.string,
			time: React.PropTypes.bool,
			placeholder: React.PropTypes.string,
			required: React.PropTypes.bool,
			disabled: React.PropTypes.bool,
			readonly: React.PropTypes.bool,
			value: React.PropTypes.string,
			onChange: React.PropTypes.func
		},
		componentDidMount: function() {//console.log('componentDidMount DateTimePicker');
			var props = this.props;

			var format = props.time === true ? 'YYYY-MM-DDTHH:mm:ssZZ' : 'YYYY-MM-DD';

			var $container = $(this.refs.datepicker.getDOMNode());
			$container.datetimepicker({
				format: format
			});

			$container.on('dp.change', function(event) {//console.log('event.date', event.date.format(format));
				props.onChange({value: event.date.format(format)});
			});

			if(!this.props.required) {
				var $clearBtn = $(this.refs.clearbtn.getDOMNode());
				$clearBtn.on('click', function() {
					props.onChange({value: undefined});
				});
			}
		},
		componentWillUnmount: function() {//console.log('componentWillUnmount DateTimePicker');
			var $container = $(this.refs.datepicker.getDOMNode());
			$container.datetimepicker('destroy');
		},
		render: function() {//console.log('render DateTimePicker', this.state, this.props);
			var placeholder = this.props.placeholder;
			var required = this.props.required;
			var disabled = this.props.disabled;
			var readOnly = this.props.readOnly;

			return (
				div({className: 'input-group date group-append', ref: 'datepicker'},
 					input({
						type : 'text',
						className : 'form-control',
						id: this.props.id,
						name: this.props.name,
						value : this.props.value,
						placeholder : placeholder,
						required : required,
						disabled : disabled,
						readOnly : readOnly,
						onChange : this.props.onChange
					}), // FIXME use InputControl
					!required ? span({className: 'input-group-addon'},
						span({className: 'glyphicon glyphicon-remove empty-date-input', ref: 'clearbtn'})
					) : null,
					span({className: 'input-group-addon datepickerbutton'},
							span({className: 'glyphicon glyphicon-calendar'})
					)
				)
			);
		},
	});
	
	/**
	 * React component for code editor Ace (http://ace.c9.io/)
	 * 
	 * @memberOf control.wrapper
	 */
	var Ace = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'Ace',
		propTypes: {
			name: React.PropTypes.string,
			required: React.PropTypes.bool,
			readOnly: React.PropTypes.bool,
			height: React.PropTypes.number,
			theme: React.PropTypes.string,
			mode: React.PropTypes.string,
			value: React.PropTypes.string,
			onChange: React.PropTypes.func.isRequired,
		},
		getDefaultProps: function() {
			return {
				height: 250,
				theme: 'eclipse',
				mode: 'r'
			};
		},
		getInitialState: function() {
			return {value: this.props.value};
		},
		componentWillReceiveProps : function(nextProps) {
			this.setState({value: nextProps.value});
		},
		componentDidMount: function() {//console.log('componentDidMount Ace');
			var container = this.refs.editor.getDOMNode();
			var editor = ace.edit(container);
			editor.setTheme('ace/theme/' + this.props.theme);
			editor.setReadOnly(this.props.readOnly === true || this.props.disabled === true);
			
			var session = editor.getSession();
			session.setMode('ace/mode/' + this.props.mode);
			session.setValue(this.props.value);
			
			var self = this;
			session.on('change', function() {
				var value = session.getValue();
				self.setState({value: value});
				self.props.onChange(value);
			});
		},
		componentWillUnmount: function() {//console.log('componentWillUnmount Ace');
			var container = this.refs.editor.getDOMNode();
			var editor = ace.edit(container);
			editor.destroy();
		},
		render: function() {//console.log('render Ace', this.state, this.props);
			// editor won't show up unless height is defined
			return div({},
				div({ref: 'editor', style: {height: this.props.height}}),
				textarea({
					className : 'form-control hidden',
					name : this.props.name,
					required : this.props.required,
					disabled: this.props.disabled,
					readOnly: this.props.readOnly,
					value : this.state.value, // FIXME see test-form html-required validation error
					onChange: this._handleChange,
				})
			);
		},
		_handleChange: function(value) {//console.log('_handleChange Ace', value);
			this.setState({value: value});
			this.props.onChange(value);
		},
	});
	
	// export module
	molgenis.control = molgenis.control || {};
	molgenis.control.wrapper = molgenis.control.wrapper || {};
	
	$.extend(molgenis.control.wrapper, {
		Select2: React.createFactory(Select2),
		JQRangeSlider: React.createFactory(JQRangeSlider),
		DateTimePicker: React.createFactory(DateTimePicker),
		Ace: React.createFactory(Ace),
	});
}($, window.top.molgenis = window.top.molgenis || {}));