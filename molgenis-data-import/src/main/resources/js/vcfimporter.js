(function($, molgenis) {
	"use strict";

	$(function() {
		$('form[name=vcf-importer-form]').validate({
			rules : {
				file : {
					required : true,
					extension : "vcf|vcf\.gz"
				}
			}
		});
		$('form[name=vcf-importer-form]').submit(function(e) {
			e.preventDefault();
			e.stopPropagation();
			if($(this).valid()) {
				$.ajax({
					type : $(this).attr('method'),
					url : $(this).attr('action'),
					data : new FormData($(this)[0]), // not supported in IE9 
					contentType: false,
		            processData: false,
                    success: function(name) {
                        molgenis.createAlert([
                            {'message': 'VCF ' + name + ' imported successfully'}
                        ], 'success');
                    }
				});
			}
		});
	});
}($, window.top.molgenis = window.top.molgenis || {}));