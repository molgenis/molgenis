<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=["goad.css", "lasso.css", "scatterD3.css", "jquery-ui.css"]>
<#assign js=["goadmanager.js", "d3-legend.min.js", "htmlwidgets.js", "lasso.js", "scatterD3.js", "goadPublicationPart.js", "goadBarGraph.js", "jquery-ui.js", "sorttable.js"]>

<@header css js/>
<div class="container">
	<img id="goadImage" src="http://bioinf.nl:8080/GOAD2/img/Logo_GOAD.png" alt="" width="35%" height="35%" />
	<a href="http://www.umcg.nl/NL/Zorg/paginas/Default.aspx">
		<img id="umcgImage" src="https://www.umcg.nl/width/204/height/112/imageMaxWidth/204/imageMaxHeight/112/imageVAlign/mid/imageHAlign/mid/image/L19sYXlvdXRzLzE1L1VtY2dQb3J0YWwvaW1hZ2VzL2xvZ29fdW1jZy5wbmc=/Canvas.ashx" alt="" width="20%" height="20%" />
	</a>
	<div class="btn-group btn-group-justified">
		<div id="homeButton" class="btn-group returnButton"><button class="btn btn-primary" type="button"><span class="glyphicon glyphicon-home" aria-hidden="true"></span></button></div>
		<div id="contactButton" class="btn-group"><button class="btn btn-primary" type="button"><span class="glyphicon glyphicon-envelope" aria-hidden="true"></span></button></div>
		<div id="divEmptyButton" class="btn-group"><button class="btn btn-primary disabled" type="button">&nbsp;</button></div>
	</div>
	<div class="well">
		<h1>Welcome to the online Glia Open Access Database (GOAD)</h1>
		For more information about the features of GOAD, click<button id="tutorialButton" data-toggle="modal" data-target="#tutorialModal"><u>here</u></button>for the tutorial.
	<#include "goad-tutorialPage.ftl">
	</div>

	<div id="accordion" class="panel-group">
		<div class="panel panel-default">
			<div id="headingOne" class="panel-heading">
				<h4 class="panel-title">
					<a href="#collapseOne" data-toggle="collapse" data-parent="#accordion">Publications</a>
				</h4>
			</div>
			<div id="collapseOne" class="panel-collapse collapse in">
				<div id="StudyInfo" class="panel-body">
					<button id="refreshPublications" class="btn btn-default" type="button"><span class="glyphicon glyphicon-refresh"></span></button>
					<div class="input-group span3 col-md-3 col-md-push-9"><span class="input-group-addon">Search</span><input id="filter" class="form-control" type="text" placeholder="Type here..." /></div>
					<p>&nbsp;</p>
					<table id="publicationCard" class="table table-striped table-hover table-condensed table-responsive sortable">
						<thead>
							<tr><th id="title">Title</th><th id="author">Author</th><th id="organism">Organism</th><th id="year">Year</th></tr>
						</thead>
						<tbody id="tableContent" class="searchable"></tbody>
					</table>
				</div>
			</div>
		</div>
		<div class="panel panel-default">
			<div id="headingTwo" class="panel-heading">
				<h4 class="panel-title">
					<a class="collapsed" href="#collapseTwo" data-toggle="collapse" data-parent="#accordion">Genes</a>
				</h4>
			</div>
			<div id="collapseTwo" class="panel-collapse collapse">
				<div class="panel-body">
					<form action="">
						<input type="radio" name="organism" value="mice" checked="checked"> Mice <br/>
						<input type="radio" name="organism" value="human"> Human 
					</form>

					<br/>
					<div class="input-group pull-left">
						<span class="input-group-btn ">
					        <button id="geneSearch" class="btn btn-default" type="button"><span class="glyphicon glyphicon-search"></span></button>
					      </span>
					      <input id="geneText" type="text" class="form-control" placeholder="Search for...">
					</div>
				</div>
			</div>
		</div>
		<div class="panel panel-default">
			<div id="headingThree" class="panel-heading">
				<h4 class="panel-title">
					<a class="collapsed" href="#collapseThree" data-toggle="collapse" data-parent="#accordion">Conditions</a>
				</h4>
			</div>
			<div id="collapseThree" class="panel-collapse collapse">
				<div class="panel-body">
					<div class="input-group pull-left">
						<span class="input-group-btn ">
					        <button id="conditionSearch" class="btn btn-default" type="button"><span class="glyphicon glyphicon-search"></span></button>
					    </span>
					    <input id="conditionText" type="text" class="form-control" placeholder="Search for...">	
					</div>
				</div>
			</div>
		</div>
	</div>
	<#include "goad-publicationPage.ftl">
	<#include "goad-contactPage.ftl">
	<div id="genePart">
		<div class="well">
			<div id="geneInformation"></div> 
			<div id='dashboard'></div>
			<button type="button" id="returnTPM" class="btn btn-default btn-block returnButton"><span id="leftArrow" class="glyphicon glyphicon-chevron-left" aria-hidden="true"></span></button>
		</div>
	</div>
	
	<div class="panel panel-default">
		<div class="panel-footer">Copyright &copy; 2014</div>
	</div>
</div>
<@footer/>