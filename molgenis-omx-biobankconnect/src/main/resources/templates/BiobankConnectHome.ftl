<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["home.css"]>
<@header css/>
	<!-- move it to home-controller and do css inline at the moment -->
	<div class="offset4 span4">
		<img id="biobankconnect-logo" src="/img/biobankconnect/logo.png"/>
	</div>
	<div class="row-fluid">
		<div class="span12 well">
			<div class="row-fluid">
				<div class="offset3 span6">
					<img src="/img/biobankconnect/landing_page.jpg"/>
				</div>
			</div>
		</div>
	</div>
	
	<div class="row-fluid">
		<div class="span6">
			<legend><strong>Motivation</strong></legend>
			<p class="text-justify">
				To gain enough statistical power to uncover the subtle associations 
				between phenotypes and diseases, researchers need increasingly large 
				data sets. Unfortunately, existing data sets, as typically collected 
				in biobanks, are not large enough, due to financial limitations and 
				discordant policies for data collection between different countries. 
				Therefore pooled analysis, where multiple data sets are analyzed together, is desirable. 
			</p>
		</div>
	</div>		
	<div class="row-fluid">
		<div class="span6">
			<legend><strong>Harmonization</strong></legend>
			<dl>
				<dt>
					1. Translating research questions
				</dt>
				<dd>
					Desired data items are created/defined depending on the research questions, 
					e.g. in order to statistically model the risk of diabetes mellitus, data items 
					for well-known risk factors such as body mass index, gender, age, smoking status, 
					blood pressure, and cholesterol are selected.
				</dd>
				<dt>
					2. Determining harmonization potential
				</dt>
				<dd>
					Desired items are compared with 'data dictionaries' of each Biobank. 
					Each data dictionary lists all the data items available in the biobank and their definitions. 
					The challenges in this are finding matching items and decide whether or not they can be used for harmonization. 
					Once the harmonization potential is determined, the mappings between desired items and biobank items could be established. 
				</dd>
				<dt>
					3. Pooling data
				</dt>
				<dd>
					the algorithms, which are developed based on the mappings created in previous step, could derive the values of 
					desired data items for each of the biobanks, which involves value conversions and unit conversions. E.g. a) 
					the biobank items height and weight are used to calculate desired data item 'body mass index'. b) the biobank data item weight(g) 
					is converted to desired data item weight(kg) by unit conversion.
				</dd>	
			</dl>
		</div>
	</div>
	<div class="row-fluid">
		<div class="span6">
			<legend><strong>Strategy</strong></legend>
		</div>
	</div>	
	
	<div class="row-fluid">			
		<div class="offset2 span1">
			<img class="classBorder" src="/img/biobankconnect/catalog.png" id="catalog"/>
		</div>
		<div class="span1">
			<img class="pico" src="/img/biobankconnect/pico.png"/>
		</div>
		<div class="span1">
			<img class="classBorder" src="/img/biobankconnect/anchor.jpg" id="anchor"/>
		</div>
		<div class="span1">
			<img class="pico" src="/img/biobankconnect/pico.png"/>
		</div>
		<div class="span1">
			<img class="classBorder" src="/img/biobankconnect/harmonize.jpg" id="search"/>
		</div>
		<div class="span1">
			<img class="pico" src="/img/biobankconnect/pico.png"/>
		</div>
		<div class="span1">
			<img class="classBorder" style='border:1px solid #000000' src="/img/biobankconnect/curate.png" id="curate"/>
		</div>
	</div>
	<div class="row-fluid">
		<div id="step-description" class="offset2 span8 well">
			<div id="catalog-description">
				<h4>Select a catalogue</h4>
				<p class="text-justify">Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown
				printer took a galley of type and scrambled it to make a type specimen book. It has survived not
				only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. 
				It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, 
				and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem
				Ipsum.
				</p>
			</div>
			<div id="anchor-description">
				<h4>Anchor ontology terms</h4>
				<p class="text-justify">Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown
				printer took a galley of type and scrambled it to make a type specimen book. It has survived not
				only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. 
				It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, 
				and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem
				Ipsum.
				</p>
			</div>
			<div id="search-description">
				<h4>Matching catalogues</h4>
				<p class="text-justify">Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown
				printer took a galley of type and scrambled it to make a type specimen book. It has survived not
				only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. 
				It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, 
				and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem
				Ipsum.
				</p>
			</div>
			<div id="curate-description">
				<h4>Curate mappings</h4>
				<p class="text-justify">Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown
				printer took a galley of type and scrambled it to make a type specimen book. It has survived not
				only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. 
				It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, 
				and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem
				Ipsum.
				</p>
			</div>
		</div>
	</div>
	<!--
	<div class="span4">
		<img src="/img/biobankconnect/motivation-slide.jpg"/>
	</div>-->
	<script>
		$(document).ready(function(){
			$('#molgenis-header').remove();
			$('#footer').remove();
			
			$('#step-description').children('div').hide();
			$('#step-description').children('div:eq(0)').show();
		
			$('#catalog').click(function (){	
				$('#step-description').children('div').hide();
				$('#catalog-description').show();
			});
			$('#anchor').click(function (){
				$('#step-description').children('div').hide();
				$('#anchor-description').show();
			
			});
			$('#search').click(function (){
				$('#step-description').children('div').hide();
				$('#search-description').show();
			
			});
			$('#curate').click(function (){
				$('#step-description').children('div').hide();
				$('#curate-description').show();
			});
			
			$('.classBorder').each(function(){
				$(this).hover(
					function(){
						$(this).removeClass('classBorder').addClass('hoverClassBorder');		
				 	},
				 	function(){
				 		$(this).removeClass('hoverClassBorder').addClass('classBorder');
				 	}
				 );
			});
		});
	</script>
<@footer/>

