import DeepPureRenderMixin from "./mixin/DeepPureRenderMixin";
import EntityLoaderMixin from "./mixin/EntityLoaderMixin";
import EntityInstanceLoaderMixin from "./mixin/EntityInstanceLoaderMixin";
import I18nStringsMixin from "./mixin/I18nStringsMixin";
import React from "react";
import FormFactory from "./Form";
import Spinner from "./Spinner";
import Button from "./Button";
import _ from "underscore";
import moment from "moment";

var div = React.DOM.div;

var Questionnaire = React.createClass({
    mixins: [DeepPureRenderMixin, EntityLoaderMixin, EntityInstanceLoaderMixin, I18nStringsMixin],
    displayName: 'Questionnaire',
    propTypes: {
        entity: React.PropTypes.string.isRequired,
        entityInstance: React.PropTypes.string.isRequired,
        onContinueLaterClick: React.PropTypes.func,
        successUrl: React.PropTypes.string
    },
    getDefaultProps: function () {
        return {
            successUrl: null,
            onContinueLaterClick: function () {
            }
        };
    },
    getInitialState: function () {
        return {
            entity: null,			// transfered from props to state, loaded from server if required
            entityInstance: null,	// transfered from props to state, loaded from server if required
            i18nStrings: null //loaded from server
        };
    },
    _onSubmitClick: function (e) {
        this.refs.form.submit(e);
    },
    render: function () {
        if (this.state.entity === null || this.state.entityInstance === null || this.state.i18nStrings === null) {
            return Spinner();
        }

        // a edit form with save-on-blur doesn't have a submit button
        var QuestionnaireButtons = this.state.entityInstance.status !== 'SUBMITTED' ? (
            div({className: 'row', style: {textAlign: 'right'}},
                div({className: 'col-md-12'},
                    Button({
                        text: this.state.i18nStrings.questionnaire_save_and_continue,
                        onClick: this.props.onContinueLaterClick
                    }),
                    Button({
                        type: 'button',
                        style: 'primary',
                        css: {marginLeft: 5},
                        text: this.state.i18nStrings.questionnaire_submit,
                        onClick: this._onSubmitClick
                    })
                )
            )
        ) : null;

        var Form = FormFactory({
            entity: this.state.entity,
            entityInstance: this.state.entityInstance,
            mode: this.state.entityInstance.status === 'SUBMITTED' ? 'view' : 'edit',
            formLayout: 'vertical',
            modal: false,
            enableOptionalFilter: false,
            saveOnBlur: true,
            enableFormIndex: true,
            enableAlertMessageInFormIndex: true,
            categoricalMrefShowSelectAll: false,
            showAsteriskIfNotNillable: false,
            beforeSubmit: this._handleBeforeSubmit,
            onValueChange: this._handleValueChange,
            onSubmitSuccess: this._handleSubmitSuccess,
            ref: 'form'
        }, QuestionnaireButtons);

        return div(null,
            Form
        );
    },
    _handleValueChange: function (e) {
        // update value in entity instance
        var entityInstance = _.extend({}, this.state.entityInstance);
        entityInstance[e.attr] = e.value;
        this.setState({entityInstance: entityInstance});
    },
    _handleBeforeSubmit: function (arr, $form, options) {

        var values = {};
        _.each(this.state.entity.allAttributes, function (attr) {
            var value = this.state.entityInstance[attr.name];

            if (value !== null && value !== undefined) {
                switch (attr.fieldType) {
                    case 'CATEGORICAL':
                    case 'XREF':
                        values[attr.name] = value[attr.refEntity.idAttribute];
                        break;
                    case 'CATEGORICAL_MREF':
                    case 'MREF':
                    case 'ONE_TO_MANY':
                        values[attr.name] = _.map(value.items, function (item) {
                            return item[attr.refEntity.idAttribute];
                        }).join();
                        break;
                    case 'COMPOUND':
                        //nothing, no value
                        break;
                    case 'DATE':
                        values[attr.name] = moment(value, 'YYYY-MM-DD', true);
                        break;
                    case 'DATE_TIME':
                        values[attr.name] = moment(value, moment.ISO_8601, true);
                        break;
                    default:
                        values[attr.name] = value;
                        break;
                }
            }
        }, this);

        for (var i = 0; i < arr.length; ++i) {
            var attrName = arr[i].name;
            var attr = this.state.entity.allAttributes[attrName];

            //Set status to SUBMITTED
            if (attrName === 'status') {
                arr[i].value = 'SUBMITTED';
            }

            if (attrName === 'submitDate') {
              // Generate submit timestamp
              var currentTime = moment().toISOString();
              arr[i].value = currentTime
            }

            // Set value of attribute with visibleExpression that is not visible to null
            // Ignore the submitDate attribute. This should never be set to null
            if (attr.visibleExpression && evalScript(attr.visibleExpression, values) === false && attrName !== 'submitDate') {
                arr[i].value = null;
            }
        }
    },
    _handleSubmitSuccess: function () {
        if (this.props.successUrl !== null) {
            document.location = this.props.successUrl;
        }
    }
});

export default React.createFactory(Questionnaire);