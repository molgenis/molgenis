<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<link rel="stylesheet" href="css/jquery-ui-1.9.2.custom.min.css" type="text/css">
		<link rel="stylesheet" href="css/chosen.css" type="text/css">
		<link rel="stylesheet" href="css/bootstrap.min.css" type="text/css">
		<link rel="stylesheet" href="css/dataexplorer.css" type="text/css">
		<script type="text/javascript" src="js/jquery-1.8.3.min.js"></script>
		<script type="text/javascript" src="js/jquery-ui-1.9.2.custom.min.js"></script>
		<script type="text/javascript" src="js/chosen.jquery.min.js"></script>
		<script type="text/javascript" src="js/bootstrap.min.js"></script>
		<script type="text/javascript" src="js/dataexplorer.js"></script>
	</head>
	<body>
		<div class="container-fluid">
			<div class="row-fluid">
				<div class="span2">
					<label>Choose a dataset:</label>
				</div>
				<div>
					<select data-placeholder="Choose a Dataset" id="dataset-chooser">
						<option value=""></option>
						<option value="1" selected>Dataset #1</option>
						<option value="2">Dataset #2</option>
						<option value="3">Dataset #3</option>
						<option value="4">Dataset #4</option>
						<option value="5">Dataset #5</option>
						<option value="6">Dataset #6</option>
						<option value="7">Dataset #7</option>
						<option value="8">Dataset #8</option>
						<option value="9">Dataset #9</option>
					</select>
				</div>
			</div>
			<div class="row-fluid">
				<div class="span4">
					<div class="row-fluid">
						<form id="observationset-search" class="navbar-search pull-left">
							<input type="text" class="search-query" placeholder="Search">
						</form>
					</div>
					<div class="row-fluid">
						<div id="feature-filters-accordion">
							<h3>Filters</h3>
							<div>
								<ul class="unstyled">
									<li><a class="edit-filter" href="#">Filter for Feature1</a><a class="remove-filter" href="#"><i class="icon-remove"></a></i><div id=feature-filter-1"></div></li>
									<li><a class="edit-filter" href="#">Filter for Feature2</a><a class="remove-filter" href="#"><i class="icon-remove"></a></i><div id=feature-filter-2"></div></li>
									<li><a class="edit-filter" href="#">Filter for Feature3</a><a class="remove-filter" href="#"><i class="icon-remove"></a></i><div id=feature-filter-3"></div></li>
								</ul>
							</div>
						</div>
					</div>
					<div class="row-fluid">
						<div id="feature-selection-accordion">
							<h3>Protocol 1</h3>
							<div class="protocol-feature-selection">
								<ul class="unstyled">
									<li><label class="checkbox"><input type="checkbox" class="select-all-features-checkbox"><b>Select all</b></label></li>
									<ul class="unstyled">
										<li><label class="checkbox"><input type="checkbox" class="select-feature-checkbox">Feature1</label><a class="edit-filter" href="#"><i class="icon-filter"></i></a></li>
										<li><label class="checkbox"><input type="checkbox" class="select-feature-checkbox">Feature2</label><a class="edit-filter" href="#"><i class="icon-filter"></i></a></li>
										<li><label class="checkbox"><input type="checkbox" class="select-feature-checkbox">Feature3</label><a class="edit-filter" href="#"><i class="icon-filter"></i></a></li>
									</ul>
								</ul>
							</div>
							<h3>Protocol 1 > Protocol 1.1</h3>
							<div>
								<ul class="unstyled">
									<li><label class="checkbox"><input type="checkbox" class="select-all-features-checkbox"><b>Select all</b></label></li>
									<ul class="unstyled">
										<li><label class="checkbox"><input type="checkbox" class="select-feature-checkbox">Feature1.1</label><a class="edit-filter" href="#"><i class="icon-filter"></i></a></li>
										<li><label class="checkbox"><input type="checkbox" class="select-feature-checkbox">Feature1.2</label><a class="edit-filter" href="#"><i class="icon-filter"></i></a></li>
										<li><label class="checkbox"><input type="checkbox" class="select-feature-checkbox">Feature1.3</label><a class="edit-filter" href="#"><i class="icon-filter"></i></a></li>
									</ul>
								</ul>
							</div>
						</div>
					</div>
				</div>
				<div class="span8">
					<div class="row-fluid">
						<p class="lead text-center">123 data items found</p>
					</div>
					<div class="row-fluid">
						<table class="table table-striped table-condensed">
							<thead>
								<tr>
									<th>Feature1</th>
									<th>Feature2</th>
									<th>Feature3</th>
									<th>Feature4</th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td>Value1.1</td>
									<td>Value1.2</td>
									<td>Value1.3</td>
									<td>Value1.4</td>
								</tr>
								<tr>
									<td>Value2.1</td>
									<td>Value2.2</td>
									<td>Value2.3</td>
									<td>Value2.4</td>
								</tr>
								<tr>
									<td>Value3.1</td>
									<td>Value3.2</td>
									<td>Value3.3</td>
									<td>Value3.4</td>
								</tr>
								<tr>
									<td>Value1.1</td>
									<td>Value1.2</td>
									<td>Value1.3</td>
									<td>Value1.4</td>
								</tr>
								<tr>
									<td>Value2.1</td>
									<td>Value2.2</td>
									<td>Value2.3</td>
									<td>Value2.4</td>
								</tr>
								<tr>
									<td>Value3.1</td>
									<td>Value3.2</td>
									<td>Value3.3</td>
									<td>Value3.4</td>
								</tr>
								<tr>
									<td>Value1.1</td>
									<td>Value1.2</td>
									<td>Value1.3</td>
									<td>Value1.4</td>
								</tr>
								<tr>
									<td>Value2.1</td>
									<td>Value2.2</td>
									<td>Value2.3</td>
									<td>Value2.4</td>
								</tr>
								<tr>
									<td>Value3.1</td>
									<td>Value3.2</td>
									<td>Value3.3</td>
									<td>Value3.4</td>
								</tr>
							</tbody>
						</table>
						<div class="pagination pagination-small pagination-centered">
							<ul>
								<li><a href="#">Prev</a></li>
								<li><a href="#">1</a></li>
								<li><a href="#">2</a></li>
								<li><a href="#">3</a></li>
								<li class="disabled"><a href="#">...</a></li>
								<li><a href="#">4</a></li>
								<li><a href="#">5</a></li>
								<li><a href="#">6</a></li>
								<li><a href="#">Next</a></li>
							</ul>
						</div>
						<button id="download-button" class="btn btn-small pull-right">Export to Excel</button>
					</div>
				</div>
			</div>
		</div>
	</body>
</html>