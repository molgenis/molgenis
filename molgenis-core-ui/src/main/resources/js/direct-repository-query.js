(function($, molgenis) {
    "use strict";
    function setSearchboxClickHandler(searchbox, button, dataset, dataexplorer) {
        $(function () {
            $(button).on('click', function () {
                var queryValue = $(searchbox).val();
                window.location = dataexplorer+"?" + $.param({entity: dataset, searchTerm: queryValue});
            });
        });
    }

}($, window.top.molgenis = window.top.molgenis || {}));