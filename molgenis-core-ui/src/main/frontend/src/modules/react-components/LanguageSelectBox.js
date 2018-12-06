import RestClient from "rest-client/RestClientV2";
import DeepPureRenderMixin from "./mixin/DeepPureRenderMixin";
import Spinner from "./Spinner";
import Select2 from "./wrapper/Select2";
import React from "react";
import {packageSeparator} from "rest-client";

var api = new RestClient();

/**
 * Shows a Select2 box for switching the user language
 *
 * @memberOf component
 */
var LanguageSelectBox = React.createClass({
    mixins: [DeepPureRenderMixin],
    displayName: 'LanguageSelectBox',
    propTypes: {
        onValueChange: React.PropTypes.func
    },
    getInitialState: function () {
        return {
            select2Data: null,
            selectedLanguage: null
        };
    },
    componentDidMount: function () {
        this._loadLanguages();
    },
    render: function () {
        if (this.state.select2Data === null) {
            return Spinner();
        }

        if (this.state.select2Data.length > 1) {
            return Select2({
                options: {
                    data: this.state.select2Data,
                },
                value: this.state.selectedLanguage,
                name: 'Language',
                onChange: this._handleChange
            });
        }

        return React.DOM.div();
    },
    _loadLanguages: function () {
        var self = this;
        var query = {
            'q': [{
                'field': "active",
                'operator': "EQUALS",
                'value': true
            }]
        };
        api.get('/api/v2/sys' + packageSeparator + 'Language', query).done(function (languages) {
            var selectedLanguage = null;
            var select2Data = languages.items.map(function (item) {
                if (item.code === languages.meta.languageCode) {
                    selectedLanguage = {id: item.code, text: item.name};
                }
                return {id: item.code, text: item.name};
            });

            self.setState({
                select2Data: select2Data,
                selectedLanguage: selectedLanguage
            });
        });
    },
    _handleChange: function (language) {
        $.ajax({
            type: 'POST',
            url: '/plugin/useraccount/language/update',
            data: 'languageCode=' + language.id,
            success: function () {
                location.reload(true);
            }
        });
    }
});

export default React.createFactory(LanguageSelectBox);