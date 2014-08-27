(function($, molgenis){
	"use strict";
	
	molgenis.StandardModal = function StandardModal(){
		this.modal = $('<div />');
	};
	
	molgenis.StandardModal.prototype.getModal = function(){
		return this.modal;
	};
	
	molgenis.StandardModal.prototype.closeModal = function(){
		$('#annotation-modal').modal('hide');
	};
	
	molgenis.StandardModal.prototype.getHeader = function(){
		return this.modal.find('.modal-header :eq(0)');
	}
	
	molgenis.StandardModal.prototype.createModal = function(title, bodyComponents, style){
		this.modal = $('<div />');
		if(style !== undefined && style !== null){
			this.modal.css(style);
		}
		if($('#annotation-modal').length != 0){
			this.modal = $('#annotation-modal');
			this.modal.empty();
		}else{
			$('body').append(this.modal);
		}
		this.modal.addClass('modal hide');
		this.modal.attr('id', 'annotation-modal');
		
		var header = $('<div />').css('cursor','pointer');
		header.addClass('modal-header');
		header.append('<button type="button" name="annotation-btn-close" class="close" data-dismiss="#annotation-modal" data-backdrop="true" aria-hidden="true">&times;</button>');
		header.append('<h3>' + title + '</h3>');
		
		var body = $('<div />');
		body.addClass('modal-body').css('overflow', 'none');
		if(bodyComponents!== undefined && bodyComponents !== null){
			body.append(bodyComponents);
		}
		var footer = $('<div />');
		footer.addClass('modal-footer');
		footer.append('<button name="annotation-btn-close" class="btn btn-default" data-dismiss="#annotation-modal" aria-hidden="true">Close</button>');

		this.modal.append(header);
		this.modal.append(body);
		this.modal.append(footer);
		
		$('button[name="annotation-btn-close"]').click(function(){
			$('#annotation-modal').modal('hide');
		});
		
		this.modal.modal({
			'show' : true
		}).draggable();
		
		return this.modal;
	};
	
	molgenis.StandardModal.prototype.createModalCallback = function(title, callback){
		this.modal = $('<div />');
		if($('#annotation-modal').length != 0){
			this.modal = $('#annotation-modal');
			this.modal.empty();
		}
		this.modal.appendTo('body');
		this.modal.addClass('modal hide');
		this.modal.attr('id', 'annotation-modal');
		
		var header = $('<div />').css('cursor','pointer');
		header.addClass('modal-header');
		header.append('<button type="button" name="annotation-btn-close" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>');
		header.append('<h3>' + title + '</h3>');
		
		var body = $('<div />');
		body.addClass('modal-body').css('overflow', 'none');
	
		var footer = $('<div />');
		footer.addClass('modal-footer');
		footer.append('<button name="annotation-btn-close" class="btn btn-default" data-dismiss="modal" aria-hidden="true">Close</button>');

		this.modal.append(header);
		this.modal.append(body);
		this.modal.append(footer);
		this.modal.modal({
			'backdrop' : true,
			'show' : false
		});
		callback(this.modal);
	};
}($, window.top.molgenis = window.top.molgenis || {}));
