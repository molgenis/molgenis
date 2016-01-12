/**
 * @module MolgenisInternationalization
 */

import $ from 'jquery';
import React from 'react';
import LanguageSelectBox from './react-components/LanguageSelectBox';

var i18nStrings = {};

export function get(str, lang) {
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
}

export function getAll(str, lang) {
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
}

export function I18nStrings(callback) {	
	if (!i18nStrings) {
        $.ajax({
            type: 'GET',
            url: '/api/v2/i18n',
            contentType: 'application/json',
            async: true,
            success: function(data) {
                i18nStrings = data;
                callback(data);
            }
        });
    } else {
        callback(i18nStrings);
    }
}

export function renderLanguageSelectBox(element) {
    React.render(LanguageSelectBox({}), element);
}

