package org.molgenis.omx;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.molgenis.data.DataService;
import org.molgenis.data.annotation.impl.CaddServiceAnnotator;
import org.molgenis.data.annotation.impl.ClinVarServiceAnnotator;
import org.molgenis.data.annotation.impl.ClinicalGenomicsDatabaseServiceAnnotator;
import org.molgenis.data.annotation.impl.DbnsfpGeneServiceAnnotator;
import org.molgenis.data.annotation.impl.DbnsfpVariantServiceAnnotator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.dataexplorer.controller.DataExplorerController;
import org.molgenis.framework.db.WebAppDatabasePopulatorService;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.controller.HomeController;
import org.molgenis.omx.core.RuntimeProperty;
import org.molgenis.security.MolgenisSecurityWebAppDatabasePopulator;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WebAppDatabasePopulatorServiceImpl implements WebAppDatabasePopulatorService
{
	public static final String INITLOCATION = "initLocation";
	public static final String COORDSYSTEM = "coordSystem";
	public static final String CHAINS = "chains";
	public static final String SOURCES = "sources";
	public static final String BROWSERLINKS = "browserLinks";
	public static final String SEARCHENDPOINT = "searchEndpoint";
	public static final String KARYOTYPEENDPOINT = "karyotypeEndpoint";
	public static final String GENOMEBROWSERTABLE = "genomeBrowserTable";

	private final DataService dataService;
	private final MolgenisSecurityWebAppDatabasePopulator molgenisSecurityWebAppDatabasePopulator;

	@Autowired
	public WebAppDatabasePopulatorServiceImpl(DataService dataService,
			MolgenisSecurityWebAppDatabasePopulator molgenisSecurityWebAppDatabasePopulator)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		this.dataService = dataService;

		if (molgenisSecurityWebAppDatabasePopulator == null) throw new IllegalArgumentException(
				"MolgenisSecurityWebAppDatabasePopulator is null");
		this.molgenisSecurityWebAppDatabasePopulator = molgenisSecurityWebAppDatabasePopulator;

	}

	@Override
	@Transactional
	@RunAsSystem
	public void populateDatabase()
	{
		molgenisSecurityWebAppDatabasePopulator.populateDatabase(HomeController.ID);

		// Genomebrowser stuff
		Map<String, String> runtimePropertyMap = new HashMap<String, String>();

		runtimePropertyMap.put(INITLOCATION,
				"chr:'1',viewStart:10000000,viewEnd:10100000,cookieKey:'human',nopersist:true");
		runtimePropertyMap.put(COORDSYSTEM,
				"{speciesName: 'Human',taxon: 9606,auth: 'GRCh',version: '37',ucscName: 'hg19'}");
		runtimePropertyMap
				.put(CHAINS,
						"{hg18ToHg19: new Chainset('http://www.derkholm.net:8080/das/hg18ToHg19/', 'NCBI36', 'GRCh37',{speciesName: 'Human',taxon: 9606,auth: 'NCBI',version: 36,ucscName: 'hg18'})}");
		// for use of the demo dataset add to
		// SOURCES:",{name:'molgenis mutations',uri:'http://localhost:8080/das/molgenis/',desc:'Default from WebAppDatabasePopulatorService'}"
		runtimePropertyMap
				.put(SOURCES,
						"[{name:'Genome',twoBitURI:'http://www.biodalliance.org/datasets/hg19.2bit',tier_type: 'sequence'},{name: 'Genes',desc: 'Gene structures from GENCODE 19',bwgURI: 'http://www.biodalliance.org/datasets/gencode.bb',stylesheet_uri: 'http://www.biodalliance.org/stylesheets/gencode.xml',collapseSuperGroups: true,trixURI:'http://www.biodalliance.org/datasets/geneIndex.ix'},{name: 'Repeats',desc: 'Repeat annotation from Ensembl 59',bwgURI: 'http://www.biodalliance.org/datasets/repeats.bb',stylesheet_uri: 'http://www.biodalliance.org/stylesheets/bb-repeats.xml'},{name: 'Conservation',desc: 'Conservation',bwgURI: 'http://www.biodalliance.org/datasets/phastCons46way.bw',noDownsample: true}]");
		runtimePropertyMap
				.put(BROWSERLINKS,
						"{Ensembl: 'http://www.ensembl.org/Homo_sapiens/Location/View?r=${chr}:${start}-${end}',UCSC: 'http://genome.ucsc.edu/cgi-bin/hgTracks?db=hg19&position=chr${chr}:${start}-${end}',Sequence: 'http://www.derkholm.net:8080/das/hg19comp/sequence?segment=${chr}:${start},${end}'}");

		// Charts include/exclude charts
		runtimePropertyMap.put(DataExplorerController.KEY_MOD_AGGREGATES, String.valueOf(true));
		runtimePropertyMap.put(DataExplorerController.KEY_MOD_CHARTS, String.valueOf(true));
		runtimePropertyMap.put(DataExplorerController.KEY_MOD_DATA, String.valueOf(true));

		// Annotators include files/tools
		String molgenisHomeDir = System.getProperty("molgenis.home");

		if (molgenisHomeDir == null)
		{
			throw new IllegalArgumentException("missing required java system property 'molgenis.home'");
		}

		if (!molgenisHomeDir.endsWith("/")) molgenisHomeDir = molgenisHomeDir + '/';
		String molgenisHomeDirAnnotationResources = molgenisHomeDir + "data/annotation_resources";

		runtimePropertyMap.put(CaddServiceAnnotator.CADD_FILE_LOCATION_PROPERTY, molgenisHomeDirAnnotationResources
				+ "/CADD/1000G.vcf.gz");
		runtimePropertyMap.put(ClinicalGenomicsDatabaseServiceAnnotator.CGD_FILE_LOCATION_PROPERTY,
				molgenisHomeDirAnnotationResources + "/CGD/CGD.txt");
		runtimePropertyMap.put(DbnsfpGeneServiceAnnotator.GENE_FILE_LOCATION_PROPERTY,
				molgenisHomeDirAnnotationResources + "/dbnsfp/dbNSFP2.3_gene");
		runtimePropertyMap.put(DbnsfpVariantServiceAnnotator.CHROMOSOME_FILE_LOCATION_PROPERTY,
				molgenisHomeDirAnnotationResources + "/dbnsfp/dbNSFP2.3_variant.chr");
		runtimePropertyMap.put(ClinVarServiceAnnotator.CLINVAR_FILE_LOCATION_PROPERTY,
				molgenisHomeDirAnnotationResources + "/Clinvar/variant_summary.txt");

		for (Entry<String, String> entry : runtimePropertyMap.entrySet())
		{
			RuntimeProperty runtimeProperty = new RuntimeProperty();
			String propertyKey = entry.getKey();
			runtimeProperty.setIdentifier(RuntimeProperty.class.getSimpleName() + '_' + propertyKey);
			runtimeProperty.setName(propertyKey);
			runtimeProperty.setValue(entry.getValue());
			dataService.add(RuntimeProperty.ENTITY_NAME, runtimeProperty);
		}
	}

	@Override
	@Transactional
	@RunAsSystem
	public boolean isDatabasePopulated()
	{
		return dataService.count(MolgenisUser.ENTITY_NAME, new QueryImpl()) > 0;
	}
}