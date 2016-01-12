/**
 * @module MolgenisInternationalization
 */
'use strict';

import $ from 'jquery';
import React from 'react';
import LanguageSelectBox from './react-components/LanguageSelectBox';

var i18n = {
    get: function(str, lang) {
        lang = typeof lang !== 'undefined' ? lang : 'en';
        var i18nObj;

        if (str && (str.charAt(0) !== '{' || str.charAt(str.length - 1) !== '}')) {
            i18nObj = {
                'en': str
            };
        } else {
            i18nObj = JSON.parse(str ? str : '{}');
        }

        return i18nObj[lang];
    },

    getAll: function(str, lang) {
        lang = typeof lang !== 'undefined' ? lang : 'en';
        var i18nObj;
        if (str && (str.charAt(0) !== '{' || str.charAt(str.length - 1) !== '}')) {
            i18nObj = {
                'en': str
            };
        } else {
            i18nObj = JSON.parse(str ? str : '{}');
        }
        return i18nObj;
    },

    I18nStrings: function(callback) {
        if (!i18n.i18nStrings) {
            $.ajax({
                type: 'GET',
                url: '/api/v2/i18n',
                contentType: 'application/json',
                async: true,
                success: function(data) {
                    i18n.i18nStrings = data;
                    callback(data);
                }
            });
        } else {
            callback(i18n.i18nStrings);
        }
    },

    renderLanguageSelectBox: function(element) {
        React.render(LanguageSelectBox({}), element);
    }

};

export default i18n;