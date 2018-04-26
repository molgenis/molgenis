import $ from "jquery";

let i18nStrings = undefined;

export function I18nStrings(callback) {
    if (!i18nStrings) {
        $.ajax({
            type: 'GET',
            url: '/api/v2/i18n',
            contentType: 'application/json',
            async: true,
            success: function (data) {
                i18nStrings = data;
                callback(data);
            }
        });
    } else {
        callback(i18nStrings);
    }
}