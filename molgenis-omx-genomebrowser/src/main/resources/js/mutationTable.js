function initMutationTable() {
    var url = '/plugin/genomebrowser/mutationdata';
    $.ajax({
        url : url,
        type : "GET",
        dataType : "json",
        success : function(data) {
            if (typeof data == 'object') {
                mutationTable(data);
            }
        },
        error : function(jqXHR, textStatus, errorThrown) {
            console.log("jqXHR : " + jqXHR + " text status : " + textStatus
                    + " error : " + errorThrown);
        }
    });
}

function mutationTable(data) {

	$('#mutationTableHolder')
			.html(
					'<table cellpadding="0" cellspacing="0" border="0" class="display" id="mutationDataTable">'
							+ '<thead>'
							+ '<tr><th>Name</th><th>Description</th><th>Chromosome</th><th>Start position</th><th>End position</th><th>Lenght</th><th>Gene</th><th>Track</th></tr>'
							+ '</thead></table>');

	$('#mutationDataTable').dataTable({
		"aaData" : data,
		"aoColumns" : [{
			"mData" : "Name"},{
            "mData" : "description"},{
            "mData" : "Chromosome"},{
            "mData" : "bpStart"},{
            "mData" : "bpEnd"},{
            "mData" : "VariantLength"},{
            "mData" : "Gene"},{
            "mData" : "Track"
		}]
	});
}
