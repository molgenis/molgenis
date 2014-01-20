function patientMutationTable(data) {
	//determine the molgenis server url for the purpose of adding patient tracks to Dalliance
	var molgenisUrl = location.hostname;
	if (location.port != "") {
		molgenisUrl = molgenisUrl + ":" + location.port;
	}

	var length = data.length, element = null;
	for ( var i = 0; i < length; i++) {
		//add a plus sign for every datarow to give the user a visual que that the row is clickable
		//also add a empty value for the empty column between the data of the two alelles. 
		//TODO find a cleaner solution for the empty column
		element = data[i];
		element.addTrack = "<center><img src='/img/PlusSign.gif' alt='+'></center>";
		element.empty = "";
	}

	$('#patientMutationTableHolder')
			.html(
					'<table cellpadding="0" cellspacing="0" border="0" class="display" id="patientMutationDataTable">'
							+ '<thead>'
							+ '<tr><th rowspan="2">Add Track</th><th rowspan="2">Patient ID</th><th colspan=3>Allele 1</th><th rowspan="2"/><th colspan=3>Allele 2</th><th rowspan="2">Phenotype</th><th rowspan="2">PubMed ID</th></tr>'
							+ '<tr><th>cDNA change</th><th>Consequence</th><th>Exon/Intron</th><th>cDNA change</th><th>Consequence</th><th>Exon/Intron</th></tr>'
							+ '</thead></table>');

	$('#patientMutationDataTable').dataTable(
			{
				"aaData" : data,
				"aoColumns" : [{
					"mData" : "addTrack"
				}, {
					"mData" : "Name"
				}, {
					"mData" : "CdnaNotation1"
				}, {
					"mData" : "Consequence1"
				}, {
					"mData" : "Exon1"
				}, {
					"mData" : "empty"
				}, {
					"mData" : "CdnaNotation2"
				}, {
					"mData" : "Consequence2"
				}, {
					"mData" : "Exon2"
				}, {
					"mData" : "Pheno"
				}, {
					"mData" : "PubMedID"
				}],
				"fnRowCallback" : function(nRow, aData, iDisplayIndex) {
					$('td:eq(10)', nRow).html(
							'<a href="' + aData.Reference + '">'
									+ aData.PubMedID + '</a>');
					return nRow;
				}
			});
	//kill the event to prevent the event form being added multiple times in case of refreshes
	$('#patientMutationDataTable tbody td').die('click');
	$('#patientMutationDataTable tbody td').live(
			'click',
			function() {
				var clickedCellInfo = $('#patientMutationDataTable')
						.dataTable().fnGetPosition(this);
				var dataRow = $('#patientMutationDataTable').dataTable()
						.fnGetData(clickedCellInfo[0]);

				//clicked first column (the '+' to add a track)
				if (clickedCellInfo[1] == 0) {
					var patientId = dataRow['id'];
					var patientName = dataRow['Name'];

					var dallianceTrack = [{
						name : 'Patient ' + patientName,
						uri : '/das/molgenis/patient_' + patientId + '/',
						desc : 'Mutations observed in patient ' + patientName,
						stylesheet_uri : '/css/patient-track.xml'
					}];
					dalliance.addTier(dallianceTrack[0]);
				}
			});
}
