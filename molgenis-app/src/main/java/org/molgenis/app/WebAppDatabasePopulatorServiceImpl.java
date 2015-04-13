package org.molgenis.app;

import static org.molgenis.ui.MolgenisPluginInterceptor.DEFAULT_VAL_FOOTER;
import static org.molgenis.ui.MolgenisPluginInterceptor.KEY_FOOTER;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.molgenis.app.controller.HomeController;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.UserAuthority;
import org.molgenis.data.DataService;
import org.molgenis.data.IndexedCrudRepositorySecurityDecorator;
import org.molgenis.data.annotation.impl.CaddServiceAnnotator;
import org.molgenis.data.annotation.impl.ClinVarServiceAnnotator;
import org.molgenis.data.annotation.impl.DbnsfpGeneServiceAnnotator;
import org.molgenis.data.annotation.impl.DbnsfpVariantServiceAnnotator;
import org.molgenis.data.annotation.provider.CgdDataProvider;
import org.molgenis.data.annotation.provider.HgncLocationsProvider;
import org.molgenis.data.annotation.provider.HpoMappingProvider;
import org.molgenis.data.support.GenomeConfig;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.dataexplorer.controller.DataExplorerController;
import org.molgenis.framework.db.WebAppDatabasePopulatorService;
import org.molgenis.security.MolgenisSecurityWebAppDatabasePopulatorService;
import org.molgenis.security.account.AccountService;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.system.core.RuntimeProperty;
import org.molgenis.ui.MolgenisInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WebAppDatabasePopulatorServiceImpl implements WebAppDatabasePopulatorService
{
	private final DataService dataService;
	private final MolgenisSecurityWebAppDatabasePopulatorService molgenisSecurityWebAppDatabasePopulatorService;

	@Autowired
	public WebAppDatabasePopulatorServiceImpl(DataService dataService,
			MolgenisSecurityWebAppDatabasePopulatorService molgenisSecurityWebAppDatabasePopulatorService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		this.dataService = dataService;

		if (molgenisSecurityWebAppDatabasePopulatorService == null) throw new IllegalArgumentException(
				"MolgenisSecurityWebAppDatabasePopulator is null");
		this.molgenisSecurityWebAppDatabasePopulatorService = molgenisSecurityWebAppDatabasePopulatorService;

	}

	@Override
	@Transactional
	@RunAsSystem
	public void populateDatabase()
	{
		molgenisSecurityWebAppDatabasePopulatorService.populateDatabase(this.dataService, HomeController.ID);

		// Genomebrowser stuff
		Map<String, String> runtimePropertyMap = new HashMap<String, String>();

		runtimePropertyMap.put("plugin.dataexplorer.genomebrowser", "true");
		runtimePropertyMap.put(DataExplorerController.INITLOCATION,
				"chr:'1',viewStart:10000000,viewEnd:10100000,cookieKey:'human',nopersist:true");
		runtimePropertyMap.put(DataExplorerController.COORDSYSTEM,
				"{speciesName: 'Human',taxon: 9606,auth: 'GRCh',version: '37',ucscName: 'hg19'}");
		// for use of the demo dataset add to
		// SOURCES:",{name:'molgenis mutations',uri:'http://localhost:8080/das/molgenis/',desc:'Default from WebAppDatabasePopulatorService'}"
		runtimePropertyMap
				.put(DataExplorerController.SOURCES,
						"[{name:'Genome',twoBitURI:'//www.biodalliance.org/datasets/hg19.2bit',tier_type: 'sequence'},{name: 'Genes',desc: 'Gene structures from GENCODE 19',bwgURI: '//www.biodalliance.org/datasets/gencode.bb',stylesheet_uri: '//www.biodalliance.org/stylesheets/gencode.xml',collapseSuperGroups: true,trixURI:'//www.biodalliance.org/datasets/geneIndex.ix'},{name: 'Repeats',desc: 'Repeat annotation from Ensembl 59',bwgURI: '//www.biodalliance.org/datasets/repeats.bb',stylesheet_uri: '//www.biodalliance.org/stylesheets/bb-repeats.xml'},{name: 'Conservation',desc: 'Conservation',bwgURI: '//www.biodalliance.org/datasets/phastCons46way.bw',noDownsample: true}]");
		runtimePropertyMap
				.put(DataExplorerController.BROWSERLINKS,
						"{Ensembl: 'http://www.ensembl.org/Homo_sapiens/Location/View?r=${chr}:${start}-${end}',UCSC: 'http://genome.ucsc.edu/cgi-bin/hgTracks?db=hg19&position=chr${chr}:${start}-${end}',Sequence: 'http://www.derkholm.net:8080/das/hg19comp/sequence?segment=${chr}:${start},${end}'}");

		// include/exclude dataexplorer mods
		runtimePropertyMap.put(DataExplorerController.KEY_MOD_AGGREGATES, String.valueOf(true));
		runtimePropertyMap.put(DataExplorerController.KEY_MOD_CHARTS, String.valueOf(true));
		runtimePropertyMap.put(DataExplorerController.KEY_MOD_DATA, String.valueOf(true));
		runtimePropertyMap.put(DataExplorerController.KEY_MOD_DISEASEMATCHER, String.valueOf(false));
		runtimePropertyMap.put(DataExplorerController.KEY_MOD_ANNOTATORS, String.valueOf(false));

		// DataExplorer table editable yes/no
		runtimePropertyMap.put(DataExplorerController.KEY_DATAEXPLORER_EDITABLE, String.valueOf(false));
		runtimePropertyMap.put(DataExplorerController.KEY_GALAXY_ENABLED, String.valueOf(false));

		// DataExplorer rows clickable yes / no
		runtimePropertyMap.put(DataExplorerController.KEY_DATAEXPLORER_ROW_CLICKABLE, String.valueOf(false));

		// Aggregate anonymization threshold (default no threshold)
		runtimePropertyMap.put(IndexedCrudRepositorySecurityDecorator.SETTINGS_KEY_AGGREGATE_ANONYMIZATION_THRESHOLD,
				Integer.toString(0));

		// Annotators include files/tools
		String molgenisHomeDir = System.getProperty("molgenis.home");

		if (molgenisHomeDir == null)
		{
			throw new IllegalArgumentException("missing required java system property 'molgenis.home'");
		}

		// HPO and HGNC Download URLs
		runtimePropertyMap.put(HpoMappingProvider.KEY_HPO_MAPPING, HpoMappingProvider.DEFAULT_HPO_MAPPING_VALUE);
		runtimePropertyMap.put(HgncLocationsProvider.KEY_HGNC_LOCATIONS_VALUE,
				HgncLocationsProvider.DEFAULT_HGNC_LOCATIONS_VALUE);

		if (!molgenisHomeDir.endsWith("/")) molgenisHomeDir = molgenisHomeDir + '/';
		String molgenisHomeDirAnnotationResources = molgenisHomeDir + "data/annotation_resources";

		runtimePropertyMap.put(CaddServiceAnnotator.CADD_FILE_LOCATION_PROPERTY, molgenisHomeDirAnnotationResources
				+ "/CADD/1000G.vcf.gz");
		runtimePropertyMap.put(CgdDataProvider.CGD_FILE_LOCATION_PROPERTY, molgenisHomeDirAnnotationResources
				+ "/CGD/CGD.txt");
		runtimePropertyMap.put(DbnsfpGeneServiceAnnotator.GENE_FILE_LOCATION_PROPERTY,
				molgenisHomeDirAnnotationResources + "/dbnsfp/dbNSFP2.3_gene");
		runtimePropertyMap.put(DbnsfpVariantServiceAnnotator.CHROMOSOME_FILE_LOCATION_PROPERTY,
				molgenisHomeDirAnnotationResources + "/dbnsfp/dbNSFP2.3_variant.chr");
		runtimePropertyMap.put(ClinVarServiceAnnotator.CLINVAR_FILE_LOCATION_PROPERTY,
				molgenisHomeDirAnnotationResources + "/Clinvar/variant_summary.txt");

		runtimePropertyMap.put(KEY_FOOTER, DEFAULT_VAL_FOOTER);

		runtimePropertyMap.put(DataExplorerController.KEY_HIDE_SEARCH_BOX, String.valueOf(false));
		runtimePropertyMap.put(DataExplorerController.KEY_HIDE_ITEM_SELECTION, String.valueOf(false));
		runtimePropertyMap.put(DataExplorerController.KEY_HEADER_ABBREVIATE,
				DataExplorerController.DEFAULT_VAL_HEADER_ABBREVIATE);
		runtimePropertyMap.put(DataExplorerController.KEY_SHOW_WIZARD_ONINIT,
				String.valueOf(DataExplorerController.DEFAULT_VAL_SHOW_WIZARD_ONINIT));
		runtimePropertyMap.put(DataExplorerController.AGGREGATES_NORESULTS_MESSAGE,
				DataExplorerController.DEFAULT_AGGREGATES_NORESULTS_MESSAGE);
		runtimePropertyMap.put(DataExplorerController.KEY_MOD_AGGREGATES_DISTINCT_HIDE,
				String.valueOf(DataExplorerController.DEFAULT_VAL_AGGREGATES_DISTINCT_HIDE));

		runtimePropertyMap.put(MolgenisInterceptor.I18N_LOCALE, "en");
		runtimePropertyMap.put(MolgenisInterceptor.APP_HREF_LOGO, "/img/logo_molgenis_small.png");

		runtimePropertyMap.put(GenomeConfig.GENOMEBROWSER_CHROM, "CHROM,#CHROM,chromosome");
		runtimePropertyMap.put(GenomeConfig.GENOMEBROWSER_POS, "POS,start_nucleotide");
		runtimePropertyMap.put(GenomeConfig.GENOMEBROWSER_REF, "REF");
		runtimePropertyMap.put(GenomeConfig.GENOMEBROWSER_ALT, "ALT");
		runtimePropertyMap.put(GenomeConfig.GENOMEBROWSER_ID, "ID,Mutation_id");
		runtimePropertyMap.put(GenomeConfig.GENOMEBROWSER_DESCRIPTION, "INFO");
		runtimePropertyMap.put(GenomeConfig.GENOMEBROWSER_PATIENT_ID, "patient_id");
		runtimePropertyMap.put(GenomeConfig.GENOMEBROWSER_STOP, "stop_pos,stop_nucleotide,end_nucleotide");

		runtimePropertyMap.put(AccountService.KEY_PLUGIN_AUTH_ENABLE_SELFREGISTRATION, String.valueOf(true));

		for (Entry<String, String> entry : runtimePropertyMap.entrySet())
		{
			RuntimeProperty runtimeProperty = new RuntimeProperty();
			String propertyKey = entry.getKey();
			runtimeProperty.setName(propertyKey);
			runtimeProperty.setValue(entry.getValue());
			dataService.add(RuntimeProperty.ENTITY_NAME, runtimeProperty);
		}

		MolgenisUser anonymousUser = molgenisSecurityWebAppDatabasePopulatorService.getAnonymousUser();
		UserAuthority anonymousHomeAuthority = new UserAuthority();
		anonymousHomeAuthority.setMolgenisUser(anonymousUser);
		anonymousHomeAuthority.setRole(SecurityUtils.AUTHORITY_PLUGIN_WRITE_PREFIX + HomeController.ID.toUpperCase());
		dataService.add(UserAuthority.ENTITY_NAME, anonymousHomeAuthority);
	}

	@Override
	@Transactional
	@RunAsSystem
	public boolean isDatabasePopulated()
	{
		return dataService.count(MolgenisUser.ENTITY_NAME, new QueryImpl()) > 0;
	}
}