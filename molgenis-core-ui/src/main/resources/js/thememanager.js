(function($, molgenis) {
	"use strict";

	$(function() {
		var styleName;

		function updatePageTheme() {
			styleName = $('#bootstrap-theme-select').find(":selected").text();
			var cssLocation = $('#bootstrap-theme-select').val();
			var link = $('<link />').attr('id', 'bootstrap-theme').attr('rel', 'stylesheet').attr('type', 'text/css');

            $(link).attr('href', "/css/bootstrap-3/" + cssLocation);

			$('#bootstrap-theme').remove(); // Remove existing preview theme
			$('head').append(link); // Set new preview theme
		}
		
		$('#bootstrap-theme-select').on('change', updatePageTheme);

        function toggleAddBtn() {
            var disableBtn = $('#bootstrap3-file').val() == '';
            $('#add-themes-btn').prop('disabled', disableBtn);
        }

        $('#bootstrap3-file').on('change', function () {
            toggleAddBtn();
        });

		$('#save-selected-bootstrap-theme').on('click', function(event) {
			event.preventDefault();
			updatePageTheme();
			$.ajax({
				contentType : 'application/json',
				type : 'POST',
				url : molgenis.getContextUrl() + '/set-bootstrap-theme',
				data : '"' + styleName + '"',
                success: function () {
					molgenis.createAlert([ {
						'message' : 'Succesfully updated the application theme'
					} ], 'success');
				}
			});
		});

        $('#show-add-theme-btn').on('click', function () {
            $('#bootstrap-theme-select').prop('disabled', true);
            $('#save-selected-bootstrap-theme').prop('disabled', true);
            $('#show-add-theme-btn').prop('disabled', true);
            $('#add-theme-container').show();
            toggleAddBtn();
        });

        $('#cancel-add-themes-btn').on('click', function () {
            $('#bootstrap-theme-select').prop('disabled', false);
            $('#save-selected-bootstrap-theme').prop('disabled', false);
            $('#show-add-theme-btn').prop('disabled', false);
            $('#add-theme-container').hide();
        });

        $('#add-themes-btn').on('click', function () {
            var bs3ThemeFile = $('#bootstrap3-file')[0].files[0];
            var bs4ThemeFile = $('#bootstrap4-file')[0].files[0];

            var data = new FormData();
            data.append('bootstrap3-style', bs3ThemeFile);
            if (bs4ThemeFile) {
                data.append('bootstrap4-style', bs4ThemeFile);
            }

            $.ajax({
                url: molgenis.getContextUrl() + '/add-bootstrap-theme',
                data: data,
                cache: false,
                contentType: false,
                processData: false,
                type: 'POST',
                success: function (theme) {

                    molgenis.createAlert([{
                        'message': 'Successfully added the application theme'
                    }], 'success');

                    $('#save-selected-bootstrap-theme').prop('disabled', false);
                    $('#show-add-theme-btn').prop('disabled', false);
                    $('#add-theme-container').hide();

                    var newThemeOption = $("<option/>").val(theme.location).text(theme.name);

                    $('#bootstrap-theme-select')
                        .append(newThemeOption)
                        .val(theme.location)
                        .prop('disabled', false);

                    updatePageTheme();
                },
                error: function (response) {

                    var detailMessage = response.responseJSON ? " :" + response.responseJSON.errors[0].message : ".";

                    molgenis.createAlert([{
                        'message': 'Error adding theme' + detailMessage
                    }], 'error');
                }
            });


        });
	});
}($, window.top.molgenis = window.top.molgenis || {}));