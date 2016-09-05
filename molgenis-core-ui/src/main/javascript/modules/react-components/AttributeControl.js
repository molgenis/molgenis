import RestClient from "rest-client/RestClientV1";
import React from "react";
import DeepPureRenderMixin from "./mixin/DeepPureRenderMixin";
import AttributeLoaderMixin from "./mixin/AttributeLoaderMixin";
import I18nStringsMixin from "./mixin/I18nStringsMixin";
import _ from "underscore";
import Spinner from "./Spinner";
import BoolControl from "./BoolControl";
import CheckboxGroup from "./CheckboxGroup";
import RadioGroup from "./RadioGroup";
import CodeEditor from "./CodeEditor";
import TextArea from "./TextArea";
import Input from "./Input";
import DateControl from "./DateControl";
import EntitySelectBox from "./EntitySelectBox";


var api = new RestClient();

/**
 * REST entity attribute control
 *
 * @memberOf component
 */
var AttributeControl = React.createClass({
    mixins: [DeepPureRenderMixin, AttributeLoaderMixin, I18nStringsMixin],
    displayName: 'AttributeControl',
    propTypes: {
        attr: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.object]),
        entity: React.PropTypes.object.isRequired,
        required: React.PropTypes.bool, // optional overwrite for attr.required
        readOnly: React.PropTypes.bool, // optional overwrite for attr.readOnly
        visible: React.PropTypes.bool,  // optional overwrite for attr.visible
        disabled: React.PropTypes.bool, // optional overwrite for attr.disabled
        multiple: React.PropTypes.bool,
        focus: React.PropTypes.bool,
        onValueChange: React.PropTypes.func.isRequired,
        onBlur: React.PropTypes.func,
        categoricalMrefShowSelectAll: React.PropTypes.bool
    },
    getDefaultProps: function () {
        return {
            focus: false,
            onBlur: function () {
            }
        };
    },
    getInitialState: function () {
        return {
            attr: null,
            i18nStrings: null
        };
    },
    render: function () {
        if ((this.state.attr === null) || (this.state.i18nStrings === null)) {
            // attribute not available yet
            return Spinner();
        }

        var props = this.props;
        var attr = this.state.attr;

        if (attr.expression === undefined) {
            // default props for all controls
            var controlProps = {
                id: props.id,
                name: attr.name,
                required: this._isRequired(),
                disabled: this._isDisabled(),
                readOnly: this._isReadOnly(),
                focus: this.props.focus,
                value: props.value,
                onValueChange: this._handleValueChange,
                onBlur: this.props.onBlur
            };

            switch (attr.fieldType) {
                case 'BOOL':
                    return BoolControl(_.extend({}, controlProps, {
                        label: props.label,
                        layout: props.layout || 'horizontal',
                    }));
                case 'CATEGORICAL':
                    if (this.state.options === undefined) {
                        // options not yet available
                        return Spinner();
                    }

                    // convert entity to component value
                    var value = props.value !== undefined ? props.value[attr.refEntity.idAttribute] : undefined;

                    var CategoricalControl = props.multiple === true ? CheckboxGroup : RadioGroup;
                    var options = this.state.options;
                    if (CategoricalControl === RadioGroup && controlProps.required === false) {
                        options = options.concat({value: '', label: this.state.i18nStrings.form_bool_missing});
                    }

                    return CategoricalControl(_.extend({}, controlProps, {
                        options: options,
                        layout: props.layout || 'vertical',
                        value: value,
                        onValueChange: function (event) {
                            // convert component value back to entity
                            event.value = this._idValueToEntity(event.value);
                            this._handleValueChange(event);
                        }.bind(this)
                    }));
                case 'CATEGORICAL_MREF':
                    if (this.state.options === undefined) {
                        // options not yet available
                        return Spinner();
                    }

                    // convert entities to component values
                    var values = props.value ? _.map(this._pagedValueToValue(props.value), function (item) {
                        return item[attr.refEntity.idAttribute];
                    }) : [];
                    return CheckboxGroup(_.extend({}, controlProps, {
                        options: this.state.options,
                        selectAll: this.props.categoricalMrefShowSelectAll,
                        layout: 'vertical', // FIXME make configurable
                        value: values,
                        onValueChange: function (event) {
                            // convert component values back to entities
                            event.value = this._valueToPagedValue(event.value);
                            event.value.items = _.map(event.value.items, function (id) {
                                return this._idValueToEntity(id);
                            }.bind(this));
                            this._handleValueChange(event);
                        }.bind(this)
                    }));
                case 'DATE':
                    return this._createDateControl(controlProps, false, props.placeholder || this.state.i18nStrings.form_date_control_placeholder);
                case 'DATE_TIME':
                    return this._createDateControl(controlProps, true, props.placeholder || this.state.i18nStrings.form_date_control_placeholder);
                case 'DECIMAL':
                    return this._createStringControl(controlProps, 'text', props.placeholder || '');
                case 'EMAIL':
                    return this._createStringControl(controlProps, 'email', props.placeholder || this.state.i18nStrings.form_email_control_placeholder);
                case 'ENUM':
                    if (this.state.options === undefined) {
                        // options not yet available
                        return Spinner();
                    }

                    var EnumControl = props.multiple === true ? CheckboxGroup : RadioGroup;
                    var options = this.state.options;
                    if (EnumControl === RadioGroup && controlProps.required === false) {
                        options = options.concat({value: '', label: this.state.i18nStrings.form_bool_missing});
                    }

                    return EnumControl(_.extend({}, controlProps, {
                        options: options,
                        layout: props.layout
                    }));
                case 'FILE':
                    return this._createFileControl(controlProps);
                case 'HTML':
                    return CodeEditor(_.extend({}, controlProps, {
                        placeholder: this.props.placeholder,
                        language: 'html',
                        maxLength: attr.maxLength
                    }));
                case 'HYPERLINK':
                    return this._createStringControl(controlProps, 'url', props.placeholder || this.state.i18nStrings.form_url_control_placeholder);
                case 'INT':
                case 'LONG':
                    return this._createNumberControl(controlProps, '1');
                case 'XREF':
                    return this._createEntitySelectBox(controlProps, props.multiple || false, props.placeholder || this.state.i18nStrings.form_xref_control_placeholder, props.value);
                case 'MREF':
                    return this._createEntitySelectBox(controlProps, props.multiple || true, props.placeholder || this.state.i18nStrings.form_mref_control_placeholder, props.value);
                case 'ONE_TO_MANY':
                    var query = {field: attr.mappedBy, operator: 'EQUALS', value: null};
                    return this._createEntitySelectBox(controlProps, props.multiple || true, props.placeholder || this.state.i18nStrings.form_mref_control_placeholder, props.value, query);
                case 'SCRIPT':
                    return CodeEditor(_.extend({}, controlProps, {
                        placeholder: this.props.placeholder
                    }));
                case 'STRING':
                    return this._createStringControl(controlProps, 'text', props.placeholder || '');
                case 'TEXT':
                    return TextArea(_.extend({}, controlProps, {
                        placeholder: this.props.placeholder,
                        maxLength: attr.maxLength
                    }));
                case 'COMPOUND' :
                    throw 'Unsupported data type: ' + attr.fieldType;
                default:
                    throw 'Unknown data type: ' + attr.fieldType;
            }
        } else {
            return Input({
                type: 'text',
                disabled: true,
                placeholder: this.state.i18nStrings.form_computed_control_placeholder,
                onValueChange: function () {
                }
            });
        }
    },
    _handleValueChange: function (event) {
        this.props.onValueChange(_.extend({}, event, {attr: this.state.attr.name}));
    },
    _isRequired: function () {
        return this.props.required !== undefined ? this.props.required : !this.state.attr.nillable;
    },
    _isVisible: function () {
        return this.props.visible !== undefined ? this.props.visible : this.state.attr.visible;
    },
    _isDisabled: function () {
        return this.props.disabled !== undefined ? this.props.disabled : this.state.attr.disabled;
    },
    _isReadOnly: function () {
        return this.props.readOnly !== undefined ? this.props.readOnly : this.state.attr.readOnly;
    },
    _createNumberControl: function (controlProps, step) {
        var range = this.state.attr.range;
        var min = range ? range.min : undefined;
        var max = range ? range.max : undefined;
        var placeholder = this.props.placeholder || this.state.i18nStrings.form_number_control_placeholder;

        return Input(_.extend({}, controlProps, {
            type: 'number',
            placeholder: placeholder,
            step: step,
            min: min,
            max: max
        }));
    },
    _createFileControl: function (controlProps) {
        return Input(_.extend({}, controlProps, {
            type: 'file',
            value: this.props.value ? this.props.value.filename : null,
            onValueChange: function (event) {
                event.value = _.extend({}, this.state.attr.refEntity, {filename: event.value});
                this._handleValueChange(event);
            }.bind(this)
        }));
    },
    _createStringControl: function (controlProps, type, placeholder) {
        return Input(_.extend({}, controlProps, {
            type: type,
            placeholder: placeholder,
            maxLength: this.state.attr.maxLength
        }));
    },
    _createDateControl: function (controlProps, time, placeholder) {
        return DateControl(_.extend({}, controlProps, {
            placeholder: placeholder,
            time: time
        }));
    },
    _createEntitySelectBox: function (controlProps, multiple, placeholder, value, query) {
        return EntitySelectBox(_.extend({}, controlProps, {
            mode: 'create',
            placeholder: placeholder,
            multiple: multiple,
            entity: this.state.attr.refEntity,
            query: query,
            value: multiple ? this._pagedValueToValue(value) : value, // TODO same for CATEGORICAL_MREF
            onValueChange: multiple ? function (event) {
                event.value = this._valueToPagedValue(event.value);
                this._handleValueChange(event);
            }.bind(this) : this._handleValueChange
        }));
    },
    _pagedValueToValue: function (value) {
        return value ? value.items : value;
    },
    _valueToPagedValue: function (value) {
        if (value) {
            return {
                start: 0,
                num: value.length,
                total: value.length,
                items: value
            };
        } else {
            return {
                start: 0,
                num: 0,
                total: 0,
                items: []
            };
        }
    },
    _idValueToEntity: function (id) {
        if (id === null || id === undefined) {
            return undefined;// N/A selected
        }

        var refEntity = this.state.attr.refEntity;
        if (refEntity.attributes[refEntity.idAttribute].fieldType === 'INT' || refEntity.attributes[refEntity.idAttribute].fieldType === 'LONG') {
            id = parseInt(id);
        } else if (refEntity.attributes[refEntity.idAttribute].fieldType === 'DOUBLE') {
            id = parseInt(id);
        }

        var entity = {};
        entity[refEntity.idAttribute] = id;
        entity[refEntity.labelAttribute] = _.find(this.state.options, function (option) {
            return option.value === id;
        }).label;
        return entity;
    },
    _onAttrInit: function () {
        var attr = this.state.attr;
        if (attr.fieldType === 'CATEGORICAL' || attr.fieldType === 'CATEGORICAL_MREF') {
            // retrieve all categories
            api.getAsync(attr.refEntity.hrefCollection).done(function (data) { // FIXME problems in case of large number of categories
                var idAttr = data.meta.idAttribute;
                var lblAttr = data.meta.labelAttribute;

                if (this.isMounted()) {
                    var options = _.map(data.items, function (entity) {
                        return {value: entity[idAttr], label: entity[lblAttr]};
                    });
                    this.setState({options: options});
                }
            }.bind(this));
        }
        else if (attr.fieldType === 'ENUM') {
            var options = _.map(attr.enumOptions, function (option) {
                return {value: option, label: option};
            });
            this.setState({options: options});
        }
    }
});

export default React.createFactory(AttributeControl);