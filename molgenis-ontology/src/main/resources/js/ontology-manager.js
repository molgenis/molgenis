(function ($, molgenis) {
    "use strict";

    var restApi = new molgenis.RestClient();

    molgenis.searchAvailableIndices = function () {
        $.ajax({
            type: 'GET',
            url: molgenis.getContextUrl() + '/ontology',
            contentType: 'application/json',
            success: function (data) {
                var ontologies = data.results;
                if (ontologies && ontologies.length > 0) {
                    var table = $('#ontology-table');
                    $.each(ontologies, function (inex, ontology) {
                        var ontologyUri = ontology.ontologyIRI;
                        var ontologyName = ontology.ontologyName;
                        var status = "Indexed";
                        var eachRow = $('<tr />');
                        $('<td />').append(ontologyName).appendTo(eachRow);
                        $('<td />').append('<a href="' + ontologyUri + '" target="_blank">' + ontologyUri + '</a>').appendTo(eachRow);
                        $('<td />').append(status).appendTo(eachRow);
                        var removeIcon = $('<span class="glyphicon glyphicon-remove"></span>').click(function () {
                            $('input[name="ontologyUri"]').val(ontologyUri);
                            $('#ontologymanager-form').attr({
                                'action': molgenis.getContextUrl() + '/remove',
                                'method': 'POST'
                            }).submit();
                        });
                        $('<td />').append(removeIcon).appendTo(eachRow);
                        eachRow.appendTo(table);
                    });
                }
            }
        });
    };

    molgenis.searchAvailableIndices();

}($, window.top.molgenis = window.top.molgenis || {}));