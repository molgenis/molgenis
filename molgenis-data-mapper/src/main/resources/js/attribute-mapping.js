(function($, molgenis) {	
	"use strict";
	$(function() {
		$('textarea.ace.readonly').ace({
			mode: 'javascript',
			readOnly: true,
			showGutter: false,
			highlightActiveLine: false,
			theme: 'eclipse'
		});
	});
		
}($, window.top.molgenis = window.top.molgenis || {}));