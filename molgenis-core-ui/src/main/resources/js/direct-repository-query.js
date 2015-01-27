(function($, molgenis) {
    "use strict";

    $(function() {
        $("#direct-repository-query-button").on('click', function(){
            var queryValue = $("#direct-repository-query").val();
            window.location= "/menu/main/dataexplorer?" + $.param({dataset:'ASE', searchTerm: queryValue});
        });
    });

}($, window.top.molgenis = window.top.molgenis || {}));