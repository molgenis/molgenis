<#assign tableId = tableId?replace(" ", "_")?lower_case />
<script type="text/javascript">
$.fn.extend({
    molgenisGrid: function(options) { 
        return this.each(function(){
           	
           	var container = $(this);
			var columnPager = $('.columnpager', container).detach();
           	var grid; //The jqGrid
           	var currentPage = 1;
           	var maxColPage;
           	
       		reloadGrid = function(operation, keepCurrentPageNr) {
       			$.ajax(options.url + '&Operation=' + operation).done(function(config) {
       				
       				//Unload the grid if it already exists
       				if (grid) {
       					grid.jqGrid('GridUnload');
       				}
       				
       				//For the column type requests remember the current page otherwise it wil be reset to 1
       				//But remember it only once cause other type of requests can follow
       				config.loadComplete = function(data) {
       					currentPage = data.page;
       				};
       				
       				config.beforeRequest = function(){
       		
       					if (keepCurrentPageNr) {
       						getPostData().page = currentPage;
       						keepCurrentPageNr = false;
     					}
     					
       					return true;
       				};
    				
       				//Create new jqGrid
       				var deleteRecordConfig = {
       					onclickSubmit : function(param) {
							config.postData.Operation = "DELETE_RECORD";
							
							var selRowId = grid.jqGrid ('getGridParam', 'selrow');
							var rowData = grid.jqGrid('getRowData', selRowId);
							
							for (col in rowData) {
								config.postData[col] = rowData[col];
							}
							
							return config.postData;
						},
						afterComplete : function (response, postdata, formid) {
							delete config.postdata.Operation;
						} 
       				}
       				
       				var editRecordConfig = {
       					onclickSubmit : function(param) {
							config.postData.Operation = "EDIT_RECORD";
							return config.postData;
						},
						afterComplete : function (response, postdata, formid) {
							delete config.postData.Operation;
						} 
       				}
       				
       				var addRecordConfig = {
       					onclickSubmit : function(param) {
							config.postData.Operation = "ADD_RECORD";
							return config.postData;
						},
						afterComplete : function (response, postdata, formid) {
							delete config.postData.Operation;
						} 
       				}
       				
       				//Set search to true if the reset button on the search dialog is clicked, so server can distinquish between
       				// a request to clear the filters and another (for example hide column) where we want to keep the filters
       				var searchConfig = config.searchOptions;
       				searchConfig.onReset = function(){
       					$('#' + options.tableId, container).jqGrid('setGridParam', { search: true });
       					
       					//closeAfterReset setting doesn't work, is a bug in jqGrid, we do it ourselfs
       					var gridid = grid[0].id;
        				$.jgrid.hideModal("#searchmodfbox_" + gridid,{gb: "#gbox_" + gridid, jqm: true, onClose: null});
       				};
       				
       				grid = $('#' + options.tableId, container).jqGrid(config).jqGrid('navGrid', config.pager, config.settings, editRecordConfig, addRecordConfig, deleteRecordConfig, searchConfig);
       				 
       				addColumnRemoveButtons(config);
					
       				//Put the columnpager in the grid toolbar
       				var toolbar = $('#t_' + options.tableId);
       				toolbar.css({"height" : "30px", "width" : "100%"});//Style of the toolbar where all paging widges are
        			columnPager.appendTo(toolbar);
					
					if (config.hiddenColumns.length == 0) {
						$('#hiddenColumnsEditor').hide();
					
					} else {
						$('#hiddenColumnsEditor').show();
						
						//Add hidden columns to the dropdown
						$('#hiddenColumnsDropdown').find('option').remove();//Remove all options
					
						//Add hidden columns as options
						$.each(config.hiddenColumns, function(index, col) {
							$('#hiddenColumnsDropdown').append(new Option(col.label, col.name));
						});
					
						//Button to add a hidden column (next to dropdown)
						$('#addColumn').click(function (e) {
							var selectedColumn = $('#hiddenColumnsDropdown').val();
							reloadGrid('SHOW_COLUMN&column=' + selectedColumn, true);
							return false;
						});
					}
					
        			//Add the columnpaging info
        			var start = config.colOffset + 1;
        			var end = start + config.colLimit - 1;
        			if (end > config.totalColumnCount) 
        			{
        				end = config.totalColumnCount;
        			}
        			
					$('.ui-columnpaging-info', container).html('Column ' + start + '-' + end + ' of ' + config.totalColumnCount);
					
					maxColPage = Math.floor(config.totalColumnCount / config.colLimit);
					if ((config.totalColumnCount % config.colLimit) > 0) {
						maxColPage++;
					}
					$('.total-column-pages', container).html(maxColPage);
					
					//Calculate the current columnpage
					var colPage = Math.floor(end / config.colLimit);
					if ((end % config.colLimit) > 0) {
						colPage++;
					}
					$('.colpager-input', container).val(colPage);
					
					//Update the buttons
					$('.first_columnpager', container).removeClass('ui-state-disabled'); 
					$('.prev_columnpager', container).removeClass('ui-state-disabled'); 
					$('.next_columnpager', container).removeClass('ui-state-disabled'); 
					$('.last_columnpager', container).removeClass('ui-state-disabled'); 
					
					if (start <= 1) {
						$('.prev_columnpager', container).addClass('ui-state-disabled');
						$('.first_columnpager', container).addClass('ui-state-disabled');
					}
					if (end >= config.totalColumnCount) {
						$('.next_columnpager', container).addClass('ui-state-disabled');
						$('.last_columnpager', container).addClass('ui-state-disabled');
					}	
					
					//Remove titlebar
					$('.ui-jqgrid-titlebar').remove();
					
					//Paging controls, bit of a hack but bootstrap and jqGrid bit each other a bit
    				$('input[type=text].ui-pg-input').css({"width" : "20px"});
    				$('.ui-pg-selbox').css({"height" : "23px", "width" : "200px"});
    				$('.ui-jqgrid-pager').css({"height" : "27px"});
       			});	
       		} 
       		
       		addColumnRemoveButtons = function(config) {
       			grid.closest("div.ui-jqgrid-view")
       				.find("div.ui-jqgrid-hdiv table.ui-jqgrid-htable tr.ui-jqgrid-labels > th.ui-th-column > div.ui-jqgrid-sortable")
    				.each(function (index) {
    				
    					if (!config.firstColumnFixed || index > 0) {
        					var btn = $('<button class="btn">').css({"width" : "16px", "height": "16px", "position" : "absolute", "top" : "50%", "margin-top" : "-8px","left" : "100%", "margin-left" : "-28px" }).appendTo(this).click(function (e) {
        			 			var idPrefix = "jqgh_" + grid[0].id + "_";
                				var thId = $(e.target).closest('div.ui-jqgrid-sortable')[0].id;
                		
            					// thId will be like "jqgh_tablename_column"
            					if (thId.substr(0, idPrefix.length) === idPrefix) {
            						var column = thId.substr(idPrefix.length);
                					reloadGrid('HIDE_COLUMN&column=' + column, true);
                			
                					return false;
            					}
								
       						});
       						$('<i class="icon-remove">').css({"width" : "20px", "height": "16px", "position" : "absolute", "top" : "50%", "margin-top" : "-7px","left" : "100%", "margin-left" : "-18px" }).appendTo(btn);
       						
       					}
    			});
       				
       		}
       		
       		getPostData = function() {
       			return $('#' + options.tableId, container).getGridParam('postData');
       		}
       		
       		//Handle first column range click
       		$('.first_columnpager', container).live('click',function() {
       			if (!$('.first_columnpager', container).hasClass('ui-state-disabled')) {
					reloadGrid('SET_COLUMN_PAGE&colPage=1', true);
				}
			});
			
       		//Handle next column range click
       		$('.next_columnpager', container).live('click',function() {
       			if (!$('.next_columnpager', container).hasClass('ui-state-disabled')) {
					reloadGrid('NEXT_COLUMNS', true);
				}
			});
				
			//Handle column inputbox
       		$('.colpager-input', container).live('change',function(event) {
       			reloadGrid('SET_COLUMN_PAGE&colPage=' + $(this).val(), true);
			});
			
			//Handle prev column range click
       		$('.prev_columnpager', container).live('click',function() {
       			if (!$('.prev_columnpager', container).hasClass('ui-state-disabled')) {
					reloadGrid('PREVIOUS_COLUMNS', true);
				}
			});
			
			//Handle last column range click
       		$('.last_columnpager', container).live('click',function() {
       			if (!$('.last_columnpager', container).hasClass('ui-state-disabled')) {
					reloadGrid('SET_COLUMN_PAGE&colPage=' + maxColPage, true);
				}
			});
			
			
			//Add or remove hover class from all elements that have the hovarable class
			$('.hoverable', container).live({
        		mouseenter: function() {
        			if (!$(this).hasClass('ui-state-disabled')) {//Only for enabled elements
        				$(this).addClass('ui-state-hover');
        			}
           		},
        		mouseleave: function() {
					$(this).removeClass('ui-state-hover');
           		}
       		});	
			
       		reloadGrid('LOAD_CONFIG', false);
        });
        
        return this;
    }
});


// On first load do:
$(document).ready(function() {
	$('#${tableId}_gridBox').molgenisGrid({url:'${url}', tableId:'${tableId}'});
});

</script>

<style type="text/css">
    #${tableId} input:hover{
        background-color:#65A5D1;
    }
</style>

<div id="${tableId}_gridBox">
    
    <div class="columnpager" style="width:100%;">
    	<table class="ui-pg-table" cellspacing="0" cellpadding="0" border="0" role="row" style="width:100%;table-layout:fixed;height:100%;">
			<tbody>
				<tr>
    				<td align="left">
    					<div id="hiddenColumnsEditor">
    						<select id="hiddenColumnsDropdown" role="listbox" class="ui-pg-selbox ui-widget-content ui-corner-all" style="float:left"></select>
							<button id="addColumn" class="btn" style="float:left;height:23px;margin-left:4px"><i class="icon-plus"></i></button>
						</div>
    				</td>
    				<td align="center" style="width:200px">
        				<table class="ui-pg-table" cellspacing="0" cellpadding="0" border="0" style="table-layout:auto; width:100%">
            				<tbody>
                				<tr>
                					<td class="first_columnpager hoverable ui-pg-button ui-corner-all" style="cursor: default;">
										<span class="ui-icon ui-icon-seek-first"></span>
									</td>
                    				<td class="prev_columnpager hoverable ui-pg-button ui-corner-all" style="cursor: default;">
                        				<span class="ui-icon ui-icon-seek-prev"></span>
                    				</td>
                    				<td dir="ltr" style="font-size:10px" align="center">
										Columns <input class="colpager-input ui-pg-input" type="text" role="textbox" value="1" maxlength="7" size="1"/> of <span class="total-column-pages"></span>
									</td>
                    				<td class="next_columnpager hoverable ui-pg-button ui-corner-all" style="cursor: default;">
                        				<span class="ui-icon ui-icon-seek-next"></span>
                    				</td>
                    				<td class="last_columnpager hoverable ui-pg-button ui-corner-all" style="cursor: default;">
										<span class="ui-icon ui-icon-seek-end"></span>
									</td>
                				</tr>
            				</tbody>
        				</table>
        			</td>
        			<td align="right">
        				<div class="ui-columnpaging-info" style="text-align:right" dir="ltr"></div>
        			</td>
        		</tr>
       		</tbody>
 		</table>       	       
    </div>
   
  
    <table id="${tableId}"></table>
   
    <div id="${tableId}_pager"></div>
</div>