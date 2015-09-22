(function($, molgenis) {
	"use strict";
	
	$(function() {
		React.render(molgenis.ui.Button({text: 'Refresh', onClick: alert('refresh')}, 'Refresh'), $('#button-holder')[0]);
	});
	
}($, window.top.molgenis = window.top.molgenis || {}));