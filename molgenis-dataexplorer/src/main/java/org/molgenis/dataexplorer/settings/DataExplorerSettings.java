package org.molgenis.dataexplorer.settings;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.COMPOUND;
import static org.molgenis.MolgenisFieldTypes.HYPERLINK;
import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.MolgenisFieldTypes.TEXT;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.dataexplorer.controller.DataExplorerController;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Component
public class DataExplorerSettings extends DefaultSettingsEntity
{
	private static final long serialVersionUID = 1L;

	private static final String ID = DataExplorerController.ID;

	public DataExplorerSettings()
	{
		super(ID);
	}

	@Component
	private static class Meta extends DefaultSettingsEntityMetaData
	{
		public static final String GENERAL = "general_";
		public static final String GENERAL_SEARCHBOX = "searchbox";
		public static final String GENERAL_ITEM_SELECT_PANEL = "item_select_panel";
		public static final String GENERAL_LAUNCH_WIZARD = "launch_wizard";
		public static final String GENERAL_HEADER_ABBREVIATE = "header_abbreviate";

		private static final boolean DEFAULT_GENERAL_SEARCHBOX = true;
		private static final boolean DEFAULT_GENERAL_ITEM_SELECT_PANEL = true;
		private static final boolean DEFAULT_GENERAL_LAUNCH_WIZARD = false;
		private static final int DEFAULT_GENERAL_HEADER_ABBREVIATE = 180;

		public static final String MOD = "mods";
		public static final String MOD_AGGREGATES = "mod_aggregates";
		public static final String MOD_ANNOTATORS = "mod_annotators";
		public static final String MOD_CHARTS = "mod_charts";
		public static final String MOD_DATA = "mod_data";
		public static final String MOD_REPORTS = "mod_reports";

		private static final boolean DEFAULT_MOD_AGGREGATES = true;
		private static final boolean DEFAULT_MOD_ANNOTATORS = true;
		private static final boolean DEFAULT_MOD_CHARTS = true;
		private static final boolean DEFAULT_MOD_DATA = true;
		private static final boolean DEFAULT_MOD_REPORT = true;

		public static final String DATA = "data";
		public static final String DATA_GALAXY_EXPORT = "data_galaxy_export";
		public static final String DATA_GALAXY_URL = "data_galaxy_url";
		public static final String DATA_GALAXY_API_KEY = "data_galaxy_api_key";
		public static final String DATA_GENOME_BROWSER = "data_genome_browser";

		private static final boolean DEFAULT_DATA_GALAXY_EXPORT = true;
		private static final boolean DEFAULT_DATA_GENOME_BROWSER = true;

		public static final String GENOMEBROWSER = "genomebrowser";
		public static final String GENOMEBROWSER_INIT = "gb_init";
		public static final String GENOMEBROWSER_INIT_BROWSER_LINKS = "gb_init_browser_links";
		public static final String GENOMEBROWSER_INIT_COORD_SYSTEM = "gb_init_coord_system";
		public static final String GENOMEBROWSER_INIT_LOCATION = "gb_init_location";
		public static final String GENOMEBROWSER_INIT_SOURCES = "gb_init_sources";
		public static final String GENOMEBROWSER_INIT_HIGHLIGHT_REGION = "gb_init_highlight_region";

		private static final String DEFAULT_GENOMEBROWSER_INIT_BROWSER_LINKS = "{Ensembl: 'http://www.ensembl.org/Homo_sapiens/Location/View?r=${chr}:${start}-${end}',UCSC: 'http://genome.ucsc.edu/cgi-bin/hgTracks?db=hg19&position=chr${chr}:${start}-${end}',Sequence: 'http://www.derkholm.net:8080/das/hg19comp/sequence?segment=${chr}:${start},${end}'}";
		private static final String DEFAULT_GENOMEBROWSER_INIT_COORD_SYSTEM = "{speciesName: 'Human',taxon: 9606,auth: 'GRCh',version: '37',ucscName: 'hg19'}";
		private static final String DEFAULT_GENOMEBROWSER_INIT_LOCATION = "chr:'1',viewStart:10000000,viewEnd:10100000,cookieKey:'human',nopersist:true";
		private static final String DEFAULT_GENOMEBROWSER_INIT_SOURCES = "[{name:'Genome',twoBitURI:'//www.biodalliance.org/datasets/hg19.2bit',tier_type: 'sequence'},{name: 'Genes',desc: 'Gene structures from GENCODE 19',bwgURI: '//www.biodalliance.org/datasets/gencode.bb',stylesheet_uri: '//www.biodalliance.org/stylesheets/gencode.xml',collapseSuperGroups: true,trixURI:'//www.biodalliance.org/datasets/geneIndex.ix'},{name: 'Repeats',desc: 'Repeat annotation from Ensembl 59',bwgURI: '//www.biodalliance.org/datasets/repeats.bb',stylesheet_uri: '//www.biodalliance.org/stylesheets/bb-repeats.xml'},{name: 'Conservation',desc: 'Conservation',bwgURI: '//www.biodalliance.org/datasets/phastCons46way.bw',noDownsample: true}]";
		private static final boolean DEFAULT_GENOMEBROWSER_INIT_HIGHLIGHT_REGION = false;

		public static final String AGGREGATES = "aggregates";
		public static final String AGGREGATES_DISTINCT_SELECT = "agg_distinct";
		public static final String AGGREGATES_DISTINCT_OVERRIDES = "agg_distinct_overrides";

		public static final String REPORTS = "reports";
		public static final String REPORTS_ENTITIES = "reports_entities";

		private static final boolean DEFAULT_AGGREGATES_DISTINCT_SELECT = true;

		public Meta()
		{
			super(ID);
			setLabel("Data explorer settings");
			setDescription("Settings for the data explorer plugin.");

			addGeneralSettings();
			addModulesSettings();
		}

		private void addGeneralSettings()
		{
			DefaultAttributeMetaData generalAttr = addAttribute(GENERAL).setDataType(COMPOUND).setLabel("General");
			AttributeMetaData generalSearchboxAttr = new DefaultAttributeMetaData(GENERAL_SEARCHBOX).setDataType(BOOL)
					.setNillable(false).setDefaultValue(String.valueOf(DEFAULT_GENERAL_SEARCHBOX))
					.setLabel("Show search box");
			AttributeMetaData generalAttrSelectAttr = new DefaultAttributeMetaData(GENERAL_ITEM_SELECT_PANEL)
					.setDataType(BOOL).setNillable(false)
					.setDefaultValue(String.valueOf(DEFAULT_GENERAL_ITEM_SELECT_PANEL))
					.setLabel("Show data item selection");
			AttributeMetaData generalLaunchWizardAttr = new DefaultAttributeMetaData(GENERAL_LAUNCH_WIZARD)
					.setDataType(BOOL).setNillable(false).setDefaultValue(String.valueOf(DEFAULT_GENERAL_LAUNCH_WIZARD))
					.setLabel("Launch data item filter wizard");
			AttributeMetaData generalHeaderAbbreviateAttr = new DefaultAttributeMetaData(GENERAL_HEADER_ABBREVIATE)
					.setDataType(INT).setNillable(false)
					.setDefaultValue(String.valueOf(DEFAULT_GENERAL_HEADER_ABBREVIATE))
					.setLabel("Entity description abbreviation length");
			generalAttr.addAttributePart(generalSearchboxAttr);
			generalAttr.addAttributePart(generalAttrSelectAttr);
			generalAttr.addAttributePart(generalLaunchWizardAttr);
			generalAttr.addAttributePart(generalHeaderAbbreviateAttr);
		}

		private void addModulesSettings()
		{
			AttributeMetaData modAggregatesAttr = new DefaultAttributeMetaData(MOD_AGGREGATES).setDataType(BOOL)
					.setNillable(false).setDefaultValue(String.valueOf(DEFAULT_MOD_AGGREGATES)).setLabel("Aggregates");
			AttributeMetaData modAnnotatorsAttr = new DefaultAttributeMetaData(MOD_ANNOTATORS).setDataType(BOOL)
					.setNillable(false).setDefaultValue(String.valueOf(DEFAULT_MOD_ANNOTATORS)).setLabel("Annotators");
			AttributeMetaData modChartsAttr = new DefaultAttributeMetaData(MOD_CHARTS).setDataType(BOOL)
					.setNillable(false).setDefaultValue(String.valueOf(DEFAULT_MOD_CHARTS)).setLabel("Charts");
			AttributeMetaData modDataAttr = new DefaultAttributeMetaData(MOD_DATA).setDataType(BOOL).setNillable(false)
					.setDefaultValue(String.valueOf(DEFAULT_MOD_DATA)).setLabel("Data");
			AttributeMetaData modReportAttr = new DefaultAttributeMetaData(MOD_REPORTS).setDataType(BOOL)
					.setNillable(false).setDefaultValue(String.valueOf(DEFAULT_MOD_REPORT)).setLabel("Reports");

			DefaultAttributeMetaData modAttr = addAttribute(MOD).setDataType(COMPOUND).setLabel("Modules");
			modAttr.addAttributePart(modAggregatesAttr);
			modAttr.addAttributePart(createModAggregatesSettings());
			modAttr.addAttributePart(modAnnotatorsAttr);
			modAttr.addAttributePart(modChartsAttr);
			modAttr.addAttributePart(modDataAttr);
			modAttr.addAttributePart(createModDataSettings());
			modAttr.addAttributePart(modReportAttr);
			modAttr.addAttributePart(createModReportSettings());
		}

		private AttributeMetaData createModDataSettings()
		{
			DefaultAttributeMetaData dataAttr = new DefaultAttributeMetaData(DATA).setDataType(COMPOUND)
					.setLabel("Data").setVisibleExpression("$('" + MOD_DATA + "').eq(true).value()");

			AttributeMetaData dataGalaxyExportAttr = new DefaultAttributeMetaData(DATA_GALAXY_EXPORT).setDataType(BOOL)
					.setNillable(false).setDefaultValue(String.valueOf(DEFAULT_DATA_GALAXY_EXPORT))
					.setLabel("Galaxy export");
			AttributeMetaData dataGalaxyUrlAttr = new DefaultAttributeMetaData(DATA_GALAXY_URL).setDataType(HYPERLINK)
					.setNillable(true).setLabel("Galaxy URL")
					.setVisibleExpression("$('" + DATA_GALAXY_EXPORT + "').eq(true).value()");
			AttributeMetaData dataGalaxyApiKeyAttr = new DefaultAttributeMetaData(DATA_GALAXY_API_KEY).setNillable(true)
					.setLabel("Galaxy API key")
					.setVisibleExpression("$('" + DATA_GALAXY_EXPORT + "').eq(true).value()");
			dataAttr.addAttributePart(dataGalaxyExportAttr);
			dataAttr.addAttributePart(dataGalaxyUrlAttr);
			dataAttr.addAttributePart(dataGalaxyApiKeyAttr);

			// genome browser
			DefaultAttributeMetaData genomeBrowserInitAttr = new DefaultAttributeMetaData(GENOMEBROWSER_INIT)
					.setDataType(COMPOUND).setLabel("Initialization");
			AttributeMetaData genomeBrowserInitBrowserLinksAttr = new DefaultAttributeMetaData(
					GENOMEBROWSER_INIT_BROWSER_LINKS).setNillable(false).setDataType(TEXT)
							.setDefaultValue(DEFAULT_GENOMEBROWSER_INIT_BROWSER_LINKS).setLabel("Browser links");
			AttributeMetaData genomeBrowserInitCoordSystemAttr = new DefaultAttributeMetaData(
					GENOMEBROWSER_INIT_COORD_SYSTEM).setNillable(false).setDataType(TEXT)
							.setDefaultValue(DEFAULT_GENOMEBROWSER_INIT_COORD_SYSTEM).setLabel("Coordinate system");
			AttributeMetaData genomeBrowserInitLocationAttr = new DefaultAttributeMetaData(GENOMEBROWSER_INIT_LOCATION)
					.setNillable(false).setDataType(TEXT).setDefaultValue(DEFAULT_GENOMEBROWSER_INIT_LOCATION)
					.setLabel("Location");
			AttributeMetaData genomeBrowserInitSourcesAttr = new DefaultAttributeMetaData(GENOMEBROWSER_INIT_SOURCES)
					.setNillable(false).setDataType(TEXT).setDefaultValue(DEFAULT_GENOMEBROWSER_INIT_SOURCES)
					.setLabel("Sources");
			AttributeMetaData genomeBrowserInitHighlightRegionAttr = new DefaultAttributeMetaData(
					GENOMEBROWSER_INIT_HIGHLIGHT_REGION).setNillable(false).setDataType(BOOL)
							.setDefaultValue(String.valueOf(DEFAULT_GENOMEBROWSER_INIT_HIGHLIGHT_REGION))
							.setLabel("Highlight region");

			genomeBrowserInitAttr.addAttributePart(genomeBrowserInitBrowserLinksAttr);
			genomeBrowserInitAttr.addAttributePart(genomeBrowserInitCoordSystemAttr);
			genomeBrowserInitAttr.addAttributePart(genomeBrowserInitLocationAttr);
			genomeBrowserInitAttr.addAttributePart(genomeBrowserInitSourcesAttr);
			genomeBrowserInitAttr.addAttributePart(genomeBrowserInitHighlightRegionAttr);

			DefaultAttributeMetaData dataGenomeBrowserAttr = new DefaultAttributeMetaData(DATA_GENOME_BROWSER)
					.setDataType(BOOL).setNillable(false).setDefaultValue(String.valueOf(DEFAULT_DATA_GENOME_BROWSER))
					.setLabel("Genome Browser");

			DefaultAttributeMetaData genomeBrowserAttr = new DefaultAttributeMetaData(GENOMEBROWSER)
					.setDataType(COMPOUND).setLabel("Genome Browser")
					.setVisibleExpression("$('" + DATA_GENOME_BROWSER + "').eq(true).value()");
			genomeBrowserAttr.addAttributePart(genomeBrowserInitAttr);

			dataAttr.addAttributePart(dataGenomeBrowserAttr);
			dataAttr.addAttributePart(genomeBrowserAttr);

			return dataAttr;
		}

		private AttributeMetaData createModAggregatesSettings()
		{
			DefaultAttributeMetaData aggregatesAttr = new DefaultAttributeMetaData(AGGREGATES).setDataType(COMPOUND)
					.setLabel("Aggregates").setVisibleExpression("$('" + MOD_AGGREGATES + "').eq(true).value()");
			AttributeMetaData aggregatesDistinctSelectAttr = new DefaultAttributeMetaData(AGGREGATES_DISTINCT_SELECT)
					.setNillable(false).setDataType(BOOL)
					.setDefaultValue(String.valueOf(DEFAULT_AGGREGATES_DISTINCT_SELECT))
					.setLabel("Distinct aggregates");
			AttributeMetaData aggregatesDistinctOverrideAttr = new DefaultAttributeMetaData(
					AGGREGATES_DISTINCT_OVERRIDES).setDataType(TEXT).setLabel("Distinct attribute overrides")
							.setDescription("JSON object that maps entity names to attribute names")
							.setVisibleExpression("$('" + AGGREGATES_DISTINCT_SELECT + "').eq(true).value()");
			aggregatesAttr.addAttributePart(aggregatesDistinctSelectAttr);
			aggregatesAttr.addAttributePart(aggregatesDistinctOverrideAttr);
			return aggregatesAttr;
		}

		private AttributeMetaData createModReportSettings()
		{
			DefaultAttributeMetaData reportsAttr = new DefaultAttributeMetaData(REPORTS).setDataType(COMPOUND)
					.setLabel("Reports").setVisibleExpression("$('" + MOD_REPORTS + "').eq(true).value()");
			AttributeMetaData reportsEntitiesAttr = new DefaultAttributeMetaData(REPORTS_ENTITIES).setNillable(true)
					.setDataType(TEXT).setLabel("Reports").setDescription(
							"Comma-seperated report strings (e.g. MyDataSet:myreport,OtherDataSet:otherreport). The report name refers to an existing FreemarkerTemplate entity or file with name view-<report>-entitiesreport.ftl (e.g. view-myreport-entitiesreport.ftl)");
			reportsAttr.addAttributePart(reportsEntitiesAttr);
			return reportsAttr;
		}
	}

	public boolean getModAggregates()
	{
		Boolean value = getBoolean(Meta.MOD_AGGREGATES);
		return value != null ? value.booleanValue() : false;
	}

	public void setModAggregates(boolean modAggregates)
	{
		set(Meta.MOD_AGGREGATES, modAggregates);
	}

	public boolean getModAnnotators()
	{
		Boolean value = getBoolean(Meta.MOD_ANNOTATORS);
		return value != null ? value.booleanValue() : false;
	}

	public void setModAnnotators(boolean modAnnotators)
	{
		set(Meta.MOD_ANNOTATORS, modAnnotators);
	}

	public boolean getModCharts()
	{
		Boolean value = getBoolean(Meta.MOD_CHARTS);
		return value != null ? value.booleanValue() : false;
	}

	public void setModCharts(boolean modCharts)
	{
		set(Meta.MOD_CHARTS, modCharts);
	}

	public boolean getModData()
	{
		Boolean value = getBoolean(Meta.MOD_DATA);
		return value != null ? value : false;
	}

	public void setModData(boolean modData)
	{
		set(Meta.MOD_DATA, modData);
	}

	public boolean getModReports()
	{
		Boolean value = getBoolean(Meta.MOD_REPORTS);
		return value != null ? value : false;
	}

	public void setModReports(boolean modReports)
	{
		set(Meta.MOD_REPORTS, modReports);
	}

	public Boolean getGalaxyExport()
	{
		return getBoolean(Meta.DATA_GALAXY_EXPORT);
	}

	public void setGalaxyExport(boolean galaxyExport)
	{
		set(Meta.DATA_GALAXY_EXPORT, galaxyExport);
	}

	public Map<String, String> getAggregatesDistinctOverrides()
	{
		String distinctAttrOverridesStr = getString(Meta.AGGREGATES_DISTINCT_OVERRIDES);
		return new Gson().fromJson(distinctAttrOverridesStr, new TypeToken<Map<String, String>>()
		{
		}.getType());
	}

	public void setAggregatesDistinctOverrides(Map<String, String> aggregatesDistinctOverrides)
	{
		String value = aggregatesDistinctOverrides != null ? new Gson().toJson(aggregatesDistinctOverrides) : null;
		set(Meta.AGGREGATES_DISTINCT_OVERRIDES, value);
	}

	public boolean getSearchbox()
	{
		Boolean value = getBoolean(Meta.GENERAL_SEARCHBOX);
		return value != null ? value : false;
	}

	public void setSearchbox(boolean searchbox)
	{
		set(Meta.GENERAL_SEARCHBOX, searchbox);
	}

	public boolean getItemSelection()
	{
		Boolean value = getBoolean(Meta.GENERAL_ITEM_SELECT_PANEL);
		return value != null ? value : false;
	}

	public void setItemSelection(boolean itemSelection)
	{
		set(Meta.GENERAL_ITEM_SELECT_PANEL, itemSelection);
	}

	public boolean getLaunchWizard()
	{
		Boolean value = getBoolean(Meta.GENERAL_LAUNCH_WIZARD);
		return value != null ? value : false;
	}

	public void setLaunchWizard(boolean launchWizard)
	{
		set(Meta.GENERAL_LAUNCH_WIZARD, launchWizard);
	}

	public int getHeaderAbbreviate()
	{
		Integer value = getInt(Meta.GENERAL_HEADER_ABBREVIATE);
		return value != null ? value : -1;
	}

	public void setHeaderAbbreviate(int headerAbbreviate)
	{
		set(Meta.GENERAL_HEADER_ABBREVIATE, headerAbbreviate);
	}

	public URI getGalaxyUrl()
	{
		String galaxyUrl = getString(Meta.DATA_GALAXY_URL);
		if (galaxyUrl != null)
		{
			try
			{
				return new URI(galaxyUrl);
			}
			catch (URISyntaxException e)
			{
				throw new RuntimeException(e);
			}
		}
		else
		{
			return null;
		}
	}

	public void setGalaxyUrl(URI galaxyUrl)
	{
		set(Meta.DATA_GALAXY_URL, galaxyUrl.toString());
	}

	public boolean getGenomeBrowser()
	{
		Boolean value = getBoolean(Meta.DATA_GENOME_BROWSER);
		return value != null ? value.booleanValue() : false;
	}

	public void setGenomeBrowser(boolean genomeBrowser)
	{
		set(Meta.DATA_GENOME_BROWSER, genomeBrowser);
	}

	public String getGenomeBrowserLocation()
	{
		return getString(Meta.GENOMEBROWSER_INIT_LOCATION);
	}

	public void setGenomeBrowserLocation(String genomeBrowserLocation)
	{
		set(Meta.GENOMEBROWSER_INIT_LOCATION, genomeBrowserLocation);
	}

	public String getGenomeBrowserCoordSystem()
	{
		return getString(Meta.GENOMEBROWSER_INIT_COORD_SYSTEM);
	}

	public void setGenomeBrowserCoordSystem(String genomeBrowserCoordSystem)
	{
		set(Meta.GENOMEBROWSER_INIT_COORD_SYSTEM, genomeBrowserCoordSystem);
	}

	public String getGenomeBrowserSources()
	{
		return getString(Meta.GENOMEBROWSER_INIT_SOURCES);
	}

	public void setGenomeBrowserSources(String genomeBrowserSources)
	{
		set(Meta.GENOMEBROWSER_INIT_SOURCES, genomeBrowserSources);

	}

	public String getGenomeBrowserLinks()
	{
		return getString(Meta.GENOMEBROWSER_INIT_BROWSER_LINKS);
	}

	public void setGenomeBrowserLinks(String genomeBrowserLinks)
	{
		set(Meta.GENOMEBROWSER_INIT_BROWSER_LINKS, genomeBrowserLinks);
	}

	public boolean getGenomeBrowserHighlightRegion()
	{
		Boolean value = getBoolean(Meta.GENOMEBROWSER_INIT_HIGHLIGHT_REGION);
		return value != null ? value : false;
	}

	public void setGenomeBrowserHighlightRegion(boolean genomeBrowserHighlightRegion)
	{
		set(Meta.GENOMEBROWSER_INIT_HIGHLIGHT_REGION, genomeBrowserHighlightRegion);
	}

	public boolean getAggregatesDistinctSelect()
	{
		Boolean value = getBoolean(Meta.AGGREGATES_DISTINCT_SELECT);
		return value != null ? value : false;
	}

	public void setAggregatesDistinctSelect(boolean aggregatesDistinctSelect)
	{
		set(Meta.AGGREGATES_DISTINCT_SELECT, aggregatesDistinctSelect);
	}

	public String getEntityReports()
	{
		return getString(Meta.REPORTS_ENTITIES);
	}

	public String getEntityReport(String entityName)
	{
		Map<String, String> entityReports = getEntityReportsAsMap();
		return entityReports != null ? entityReports.get(entityName) : null;
	}

	public void setEntityReports(String entityReportsStr)
	{
		set(Meta.REPORTS_ENTITIES, entityReportsStr);
	}

	private Map<String, String> getEntityReportsAsMap()
	{
		String entityReportsStr = getEntityReports();
		if (entityReportsStr != null)
		{
			Map<String, String> entityReports = new LinkedHashMap<String, String>();
			for (String entityReport : entityReportsStr.split(","))
			{
				String[] tokens = entityReport.split(":");
				String entityName = tokens[0];
				String reportName = tokens[1];
				entityReports.put(entityName, reportName);
			}
			return entityReports;
		}
		else
		{
			return null;
		}
	}
}
