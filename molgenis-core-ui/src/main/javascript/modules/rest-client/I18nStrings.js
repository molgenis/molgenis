(function($, molgenis) {
    "use strict";

    molgenis.I18nStrings = function(callback) {
        if (!molgenis.i18nStrings) {
            $.ajax({
                type : 'GET',
                url : '/api/v2/i18n',
                contentType : 'application/json',
                async : true,
                success : function(data) {
                    molgenis.i18nStrings = data;
                    callback(data);
                }
            });
        } else {
            callback(molgenis.i18nStrings);
        }
    }

}($, window.top.molgenis = window.top.molgenis || {}));