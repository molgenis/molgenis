		
	
		
	
		
	
		// Add endsWith function to the string class
		if (typeof String.prototype.endsWith !== 'function') {
			String.prototype.endsWith = function(suffix) {
				return this.indexOf(suffix, this.length - suffix.length) !== -1;
			};
		}
	
		function getCurrentTimezoneOffset() {
			function padNumber(number, length) {
				var str = "" + number;
				while (str.length < length) {
					str = '0' + str;
				}
	
				return str;
			}
	
			var offset = new Date().getTimezoneOffset();
			offset = ((offset < 0 ? '+' : '-') + padNumber(parseInt(Math.abs(offset / 60)), 2) + padNumber(Math.abs(offset % 60), 2));
	
			return offset;
		}
	
		var entityMap = {
			"&" : "&amp;",
			"<" : "&lt;",
			"\u2264" : "&lte;",
			">" : "&gt;",
			"\u2265" : "&gte;",
			'"' : '&quot;',
			"'" : '&#39;',
			"/" : '&#x2F;'
		};
	
		window.htmlEscape = function(string) {
			return String(string).replace(/[&<>"'\/]/g, function(s) {
				return entityMap[s];
			});
		};
	
		
	
		/**
		 * Is s is longer then maxLength cut it and add ...
		 * 
		 * @param s
		 * @param maxLength
		 */
		function abbreviate(s, maxLength) {
			if (s.length <= maxLength) {
				return s;
			}
	
			return s.substr(0, maxLength - 3) + '...';
		}
	
		
	
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
	
		var languageSelectBox = document.getElementById('language-select-box');
		if (languageSelectBox) {
			React.render(molgenis.ui.LanguageSelectBox({}), languageSelectBox);
		}
	
		// disable all ajax request caching
		$.ajaxSetup({
			cache : false
		});
	

	
		
	
		// async load bootstrap modal and display
		$(document).on('click', 'a.modal-href', function(e) {
			e.preventDefault();
			e.stopPropagation();
			if (!$(this).hasClass('disabled')) {
				var container = $('#' + $(this).data('target'));
				if (container.is(':empty')) {
					container.load($(this).attr('href'), function() {
						$('.modal:first', container).modal('show');
					});
				} else {
					$('.modal:first', container).modal('show');
				}
			}
		});
	
		// support overlapping bootstrap modals:
		// http://stackoverflow.com/questions/19305821/bootstrap-3-0-multiple-modals-overlay
		$(document).on('show.bs.modal', '.modal', function(event) {
			var zIndex = 1040 + (10 * $('.modal:visible').length);
			$(this).css('z-index', zIndex);
			setTimeout(function() {
				$('.modal-backdrop').not('.modal-stack').css('z-index', zIndex - 1).addClass('modal-stack');
			}, 0);
		});
	
		// if modal closes, check if other modal remains open, if so, reapply the
		// modal-open class to the body
		$(document).on('hidden.bs.modal', '.modal', function(event) {
			if ($('.modal:visible').length) {
				$('body').addClass('modal-open');
			}
		});
	
		// focus first input on modal display
		$(document).on('shown.bs.modal', '.modal', function() {
			$(this).find('input:visible:first').focus();
		});
	
		// focus first input on modal display
		$(document).on('click', '.plugin-settings-btn', function() {
			React.unmountComponentAtNode($('#plugin-settings-container')[0]); // fix
			// https://github.com/molgenis/molgenis/issues/3587
			React.render(molgenis.ui.Form({
				entity : molgenis.getPluginSettingsId(),
				entityInstance : molgenis.getPluginId(),
				mode : 'edit',
				modal : true,
				enableOptionalFilter : false,
				enableFormIndex : false,
				onSubmitSuccess : function() {
					location.reload();
				}
			}), $('#plugin-settings-container')[0]);
		});
	
		
	
		// Call handleBarHelperBlock function to set helper blocks for entire
		// application
		handlebarHelper.handleBarHelperBlocks();
	
		// clear datetimepicker on pressing cancel button
		$(document).on('click', '.clear-date-time-btn', function(e) {
			$(this).closest('div.date').find('input').val('');
			$(this).trigger('changeDate');
		});
	
		if (molgenis.getCookieWall()) {
			// show cookie wall
			var cookieValue = $.cookie("permissionforcookies");
	
			if (undefined === cookieValue) {
				$('.navbar.navbar-default.navbar-fixed-top')
						.prepend(
								$('<div id="accept-cookies-container" class="container-fluid">'
										+ '<div class="jumbotron">'
										+ '<p class="text-center">'
										+ window.location.hostname
										+ ' uses third-party analytical cookies to analyze the use of the site and improve usability. By clicking on the accept button, or by continuing to use this website, you consent to the placing of cookies.</p>'
										+ '<p class="text-center"><a id="accept-cookies" class="btn btn-primary btn-lg" href="#" role="button">Accept cookies</a></p>'
										+ '</div>' + '</div>'));
	
				$('body').css({
					'margin-top' : $('#accept-cookies-container').height()
				});
	
				$('#accept-cookies').on('click', function() {
					$.cookie("permissionforcookies", "true", {
						expires : 365,
						path : '/',
						secure : false
					});
					$('#accept-cookies-container').fadeOut(1000);
	
					// Reset body margin-top default value
					setTimeout(function() {
						$('body').css({
							'margin-top' : 0
						});
					}, 1000);
				});
			}
		}
	});