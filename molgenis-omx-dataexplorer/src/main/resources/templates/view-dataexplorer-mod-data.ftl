<div id="dataexplorer-grid-data">
	<div class="accordion" id="genomebrowser">
	    <div class="accordion-group">
	        <div class="accordion-heading">
	            <a class="accordion-toggle" data-toggle="collapse" href="#dalliance"><i class="icon-chevron-down"></i> Genome Browser</a>
	        </div>
	        <div id="dalliance" class="accordion-body collapse in">
	            <div class="accordion-inner">
	            	<#-- dalliance default id to print browser -->
	                <div id="svgHolder"></div>
	                <div class="pull-right"><a id="genomebrowser-filter-button" class="btn btn-small"><img src="/img/filter-bw.png"> Apply filter</a></div>
	            </div>
	        </div>
	    </div>
	</div>
	<div class="row-fluid data-table-container" id="data-table-container"></div>
		<div class="row-fluid data-table-pager-container">
			<a id="download-button" class="btn" href="#">Download as csv</a>
		</div>
	</div>
</div>
<script>
	<#-- load css dependencies -->
	if (!$('link[href="/css/jquery.molgenis.table.css"]').length)
		$('head').append('<link rel="stylesheet" href="/css/jquery.molgenis.table.css" type="text/css" />');
	<#-- load js dependencies -->
	$.when(
		$.ajax("/js/jquery.bootstrap.pager.js", {'cache': true}),
		$.ajax("/js/jquery.molgenis.table.js", {'cache': true}),
		$.ajax("/js/dalliance-compiled.js", {'cache': true}),
		$.ajax("/js/dataexplorer-data.js", {'cache': true}))
		.done(function() {
			<#-- create genome browser -->
		    molgenis.dataexplorer.data.createGenomeBrowser({
                        chr:          '1',
                        viewStart:    10000000,
                        viewEnd:      10100000,
                        cookieKey:    'human',
                        nopersist:    true,
                coordSystem: {
                    speciesName: 'Human',
                    taxon: 9606,
                    auth: 'GRCh',
                    version: '37',
                    ucscName: 'hg19'
                },

                chains: {
                    hg18ToHg19: new Chainset('http://www.derkholm.net:8080/das/hg18ToHg19/', 'NCBI36', 'GRCh37',
                            {
                                speciesName: 'Human',
                                taxon: 9606,
                                auth: 'NCBI',
                                version: 36,
                                ucscName: 'hg18'
                            })
                },
                sources:     [{name:                 'Genome',
                    twoBitURI:            'http://www.biodalliance.org/datasets/hg19.2bit',
                    tier_type: 'sequence'},
                    {name: 'Genes',
                        desc: 'Gene structures from GENCODE 19',
                        bwgURI: 'http://www.biodalliance.org/datasets/gencode.bb',
                        stylesheet_uri: 'http://www.biodalliance.org/stylesheets/gencode.xml',
                        collapseSuperGroups: true,
                        trixURI: 'http://www.biodalliance.org/datasets/geneIndex.ix'},
                    {name: 'Repeats',
                        desc: 'Repeat annotation from Ensembl 59',
                        bwgURI: 'http://www.biodalliance.org/datasets/repeats.bb',
                        stylesheet_uri: 'http://www.biodalliance.org/stylesheets/bb-repeats.xml'}
                    ,{name: 'Conservation',
                        desc: 'Conservation',
                        bwgURI: 'http://www.biodalliance.org/datasets/phastCons46way.bw',
                        noDownsample: true}]
			}, [<#list genomeEntities?keys as entityName>{'name': '${entityName}', 'label': '${genomeEntities[entityName]}'}<#if entityName_has_next>,</#if></#list>]);
			<#-- create data table -->
		    molgenis.dataexplorer.data.createDataTable();    	
		})
		.fail(function() {
			molgenis.createAlert([{'message': 'An error occured. Please contact the administrator.'}], 'error');
		});
</script>