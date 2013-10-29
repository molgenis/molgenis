(function($, w) {
	"use strict";
	var molgenis = w.molgenis = w.molgenis || {};
	
	molgenis.Pagination = function Pagination(){
		this.offSet = 1;
		this.currentPage = 1;
		this.pager = 10;
		this.totalPage = 0;
	};
	
	molgenis.Pagination.prototype.reset = function(){
		this.offSet = 1;
		this.currentPage = 1;
	};
	
	molgenis.Pagination.prototype.setTotalPage = function(totalPage){
		this.totalPage = totalPage;
	};
	
	molgenis.Pagination.prototype.getPager = function(){
		return this.pager;
	};
	
	molgenis.Pagination.prototype.updateMatrixPagination = function(pageElement, callback) {
		pageElement.empty();
		if(this.totalPage > 0){
			pageElement.append('<li><a href="#">Prev</a></li>');
			var displayedPage = this.totalPage < this.pager ? this.totalPage : this.pager + this.offSet - 1; 
			if(this.offSet > 2){
				pageElement.append('<li><a href="#">' + 1 + ' </a></li>');
				pageElement.append('<li class="active"><a href="#">...</a></li>');
			}
			for(var i = this.offSet; i <= displayedPage ; i++){
				var element = $('<li />');
				if(i == this.currentPage)
					element.addClass('active');
				element.append('<a href="/">' + i + '</a>');
				pageElement.append(element);
			}
			var lastPage = this.totalPage > 10 ? this.totalPage : 10;
			if(this.totalPage - this.offSet > this.pager){
				pageElement.append('<li class="active"><a href="#">...</a></li>');
				pageElement.append('<li><a href="#">' + lastPage + ' </a></li>');
			}
			pageElement.append('<li><a href="#">Next</a></li>');
			var pagination = this;
			pageElement.find('li').each(function(){
				$(this).click($.proxy(function(){
					var pageNumber = this.clickElement.find('a').html();
					if(pageNumber === "Prev"){
						if(this.data.currentPage > 1){
							this.data.currentPage--;
							if(this.data.offSet > 1 && this.data.currentPage <= this.data.offSet)
								this.data.offSet--;
						}
					}else if(pageNumber === "Next"){
						if(this.data.currentPage < this.data.totalPage) {
							this.data.currentPage++;
							if(this.data.currentPage !== this.data.totalPage 
									&& this.data.currentPage >= this.data.offSet + this.data.pager - 1) this.data.offSet++;
						}
					}else if(pageNumber !== "..."){
						this.data.currentPage = parseInt(pageNumber);
						if(this.data.currentPage >= this.data.offSet + this.data.pager - 1){
							this.data.offSet = this.data.currentPage - this.data.pager + 2;
							if(this.data.currentPage === this.data.totalPage) this.data.offSet--;
						}else if(this.data.currentPage <= this.data.offSet + 1){
								this.data.offSet = this.data.currentPage - 1;
								if(this.data.currentPage === 1) this.data.offSet++;
						}
						
					}
					callback();
					return false;
				},{'clickElement' : $(this), 'data' : pagination}));
			});
		}
	};
	
	molgenis.Pagination.prototype.createSearchRequest = function (documentType, queryArrays) {
		var queryRules = [];
		//todo: how to unlimit the search result
		queryRules.push({
			operator : 'LIMIT',
			value : this.pager
		});
		queryRules.push({
			operator : 'OFFSET',
			value : (this.currentPage - 1) * this.pager
		});
		if(queryArrays !== undefined && queryArrays !== null){
			$.each(queryArrays, function(index, query){
				queryRules.push(query);
			});
		}
		var searchRequest = {
			documentType : documentType,
			queryRules : queryRules
		};
		return searchRequest;
	};
	
}($, window.top));

(function($, w){
	"use strict";
	var molgenis = w.molgenis = w.molgenis || {};
	
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
		footer.append('<button name="annotation-btn-close" class="btn" data-dismiss="#annotation-modal" aria-hidden="true">Close</button>');

		this.modal.append(header);
		this.modal.append(body);
		this.modal.append(footer);
		
		$('button[name="annotation-btn-close"]').click(function(){
			$('#annotation-modal').modal('hide');
		});
		
		this.modal.modal({
			'backdrop' : false,
			'show' : true
		}).draggable();
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
		this.modal.attr('data-backdrop', false);
		
		var header = $('<div />').css('cursor','pointer');
		header.addClass('modal-header');
		header.append('<button type="button" name="annotation-btn-close" class="close" data-dismiss="#annotation-modal" data-backdrop="true" aria-hidden="true">&times;</button>');
		header.append('<h3>' + title + '</h3>');
		
		var body = $('<div />');
		body.addClass('modal-body').css('overflow', 'none');
	
		var footer = $('<div />');
		footer.addClass('modal-footer');
		footer.append('<button name="annotation-btn-close" class="btn" data-dismiss="#annotation-modal" aria-hidden="true">Close</button>');

		this.modal.append(header);
		this.modal.append(body);
		this.modal.append(footer);
		this.modal.modal({
			'show' : false
		}).draggable();
		$('button[name="annotation-btn-close"]').click(function(){
			$('#annotation-modal').remove();
		});
		callback(this.modal);
	};
}($, window.top));