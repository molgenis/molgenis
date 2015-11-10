(function($, molgenis) {
	"use strict";

	$(function() {
		React.render(molgenis.ui.IdcardBiobankIndexerComponent(), $('#idCardBiobankIndexer-container')[0]);
	});

}($, window.top.molgenis = window.top.molgenis || {}));