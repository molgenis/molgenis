package org.molgenis.dataexplorer.settings;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.dataexplorer.controller.DataExplorerController;
import org.molgenis.settings.DefaultSettingsEntity;
import org.molgenis.settings.DefaultSettingsEntityType;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.molgenis.data.meta.AttributeType.*;

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
	private static class Meta extends DefaultSettingsEntityType
	{
		public static final String GENERAL = "general_";
		public static final String GENERAL_SEARCHBOX = "searchbox";
		public static final String GENERAL_ITEM_SELECT_PANEL = "item_select_panel";
		public static final String GENERAL_LAUNCH_WIZARD = "launch_wizard";
		public static final String GENERAL_HEADER_ABBREVIATE = "header_abbreviate";
		public static final String GENERAL_NAVIGATOR_LINK = "show_navigator_link";

		private static final boolean DEFAULT_GENERAL_SEARCHBOX = true;
		private static final boolean DEFAULT_GENERAL_ITEM_SELECT_PANEL = true;
		private static final boolean DEFAULT_GENERAL_LAUNCH_WIZARD = false;
		private static final int DEFAULT_GENERAL_HEADER_ABBREVIATE = 180;

		public static final String MOD = "mods";
		public static final String MOD_AGGREGATES = "mod_aggregates";
		public static final String MOD_ANNOTATORS = "mod_annotators";
		public static final String MOD_DATA = "mod_data";
		public static final String MOD_REPORTS = "mod_reports";
		public static final String MOD_STANDALONE_REPORTS = "mod_standalone_reports";

		private static final boolean DEFAULT_MOD_AGGREGATES = true;
		private static final boolean DEFAULT_MOD_ANNOTATORS = true;
		private static final boolean DEFAULT_MOD_DATA = true;
		private static final boolean DEFAULT_MOD_REPORT = true;
		private static final boolean DEFAULT_MOD_STANDALONE_REPORT = false;

		public static final String DATA = "data";
		public static final String DATA_GENOME_BROWSER = "data_genome_browser";

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

		private static final boolean DEFAULT_SHOW_NAVIGATOR_LINK = true;

		public Meta()
		{
			super(ID);
		}

		@Override
		public void init()
		{
			super.init();
			setLabel("Data explorer settings");
			setDescription("Settings for the data explorer plugin.");

			addGeneralSettings();
			addModulesSettings();
		}

		private void addGeneralSettings()
		{
			Attribute generalAttr = addAttribute(GENERAL).setDataType(COMPOUND).setLabel("General");
			addAttribute(GENERAL_SEARCHBOX).setParent(generalAttr)
										   .setDataType(BOOL)
										   .setNillable(false)
										   .setDefaultValue(String.valueOf(DEFAULT_GENERAL_SEARCHBOX))
										   .setLabel("Show search box");
			addAttribute(GENERAL_ITEM_SELECT_PANEL).setParent(generalAttr)
												   .setDataType(BOOL)
												   .setNillable(false)
												   .setDefaultValue(String.valueOf(DEFAULT_GENERAL_ITEM_SELECT_PANEL))
												   .setLabel("Show data item selection");
			addAttribute(GENERAL_LAUNCH_WIZARD).setParent(generalAttr)
											   .setDataType(BOOL)
											   .setNillable(false)
											   .setDefaultValue(String.valueOf(DEFAULT_GENERAL_LAUNCH_WIZARD))
											   .setLabel("Launch data item filter wizard");
			addAttribute(GENERAL_HEADER_ABBREVIATE).setParent(generalAttr)
												   .setDataType(INT)
												   .setNillable(false)
												   .setDefaultValue(String.valueOf(DEFAULT_GENERAL_HEADER_ABBREVIATE))
												   .setLabel("Entity description abbreviation length");
			addAttribute(GENERAL_NAVIGATOR_LINK).setParent(generalAttr)
												.setDataType(BOOL)
												.setNillable(true)
												.setDefaultValue(String.valueOf(DEFAULT_SHOW_NAVIGATOR_LINK))
												.setLabel(
														"Show a link to the navigator for the package of the selected entity");
		}

		private void addModulesSettings()
		{
			Attribute modAttr = addAttribute(MOD).setDataType(COMPOUND).setLabel("Modules");

			addAttribute(MOD_AGGREGATES).setParent(modAttr)
										.setDataType(BOOL)
										.setNillable(false)
										.setDefaultValue(String.valueOf(DEFAULT_MOD_AGGREGATES))
										.setLabel("Aggregates");
			createModAggregatesSettings(modAttr);
			addAttribute(MOD_ANNOTATORS).setParent(modAttr)
										.setDataType(BOOL)
										.setNillable(false)
										.setDefaultValue(String.valueOf(DEFAULT_MOD_ANNOTATORS))
										.setLabel("Annotators");
			addAttribute(MOD_DATA).setParent(modAttr)
								  .setDataType(BOOL)
								  .setNillable(false)
								  .setDefaultValue(String.valueOf(DEFAULT_MOD_DATA))
								  .setLabel("Data");
			createModDataSettings(modAttr);
			addAttribute(MOD_REPORTS).setParent(modAttr)
									 .setDataType(BOOL)
									 .setNillable(false)
									 .setDefaultValue(String.valueOf(DEFAULT_MOD_REPORT))
									 .setLabel("Reports");
			addAttribute(MOD_STANDALONE_REPORTS).setParent(modAttr)
												.setDataType(BOOL)
												.setNillable(true)
												.setDefaultValue(String.valueOf(DEFAULT_MOD_STANDALONE_REPORT))
												.setLabel("Standalone Reports");
			createModReportSettings(modAttr);
		}

		private void createModDataSettings(Attribute modAttr)
		{
			Attribute dataAttr = addAttribute(DATA).setParent(modAttr)
												   .setDataType(COMPOUND)
												   .setLabel("Data")
												   .setVisibleExpression("$('" + MOD_DATA + "').eq(true).value()");

			// genome browser
			Attribute genomeBrowserAttr = addAttribute(GENOMEBROWSER).setParent(dataAttr)
																	 .setDataType(COMPOUND)
																	 .setLabel("Genome Browser")
																	 .setVisibleExpression("$('" + DATA_GENOME_BROWSER
																			 + "').eq(true).value()");

			Attribute genomeBrowserInitAttr = addAttribute(GENOMEBROWSER_INIT).setParent(genomeBrowserAttr)
																			  .setDataType(COMPOUND)
																			  .setLabel("Initialization");

			addAttribute(GENOMEBROWSER_INIT_BROWSER_LINKS).setParent(genomeBrowserInitAttr)
														  .setNillable(false)
														  .setDataType(TEXT)
														  .setDefaultValue(DEFAULT_GENOMEBROWSER_INIT_BROWSER_LINKS)
														  .setLabel("Browser links");
			addAttribute(GENOMEBROWSER_INIT_COORD_SYSTEM).setParent(genomeBrowserInitAttr)
														 .setNillable(false)
														 .setDataType(TEXT)
														 .setDefaultValue(DEFAULT_GENOMEBROWSER_INIT_COORD_SYSTEM)
														 .setLabel("Coordinate system");
			addAttribute(GENOMEBROWSER_INIT_LOCATION).setParent(genomeBrowserInitAttr)
													 .setNillable(false)
													 .setDataType(TEXT)
													 .setDefaultValue(DEFAULT_GENOMEBROWSER_INIT_LOCATION)
													 .setLabel("Location");
			addAttribute(GENOMEBROWSER_INIT_SOURCES).setParent(genomeBrowserInitAttr)
													.setNillable(false)
													.setDataType(TEXT)
													.setDefaultValue(DEFAULT_GENOMEBROWSER_INIT_SOURCES)
													.setLabel("Sources");
			addAttribute(GENOMEBROWSER_INIT_HIGHLIGHT_REGION).setParent(genomeBrowserInitAttr)
															 .setNillable(false)
															 .setDataType(BOOL)
															 .setDefaultValue(String.valueOf(
																	 DEFAULT_GENOMEBROWSER_INIT_HIGHLIGHT_REGION))
															 .setLabel("Highlight region");

			addAttribute(DATA_GENOME_BROWSER).setParent(dataAttr)
											 .setDataType(BOOL)
											 .setNillable(false)
											 .setDefaultValue(String.valueOf(DEFAULT_DATA_GENOME_BROWSER))
											 .setLabel("Genome Browser");
		}

		private void createModAggregatesSettings(Attribute modAttr)
		{
			Attribute aggregatesAttr = addAttribute(AGGREGATES).setParent(modAttr)
															   .setDataType(COMPOUND)
															   .setLabel("Aggregates")
															   .setVisibleExpression(
																	   "$('" + MOD_AGGREGATES + "').eq(true).value()");
			addAttribute(AGGREGATES_DISTINCT_SELECT).setParent(aggregatesAttr)
													.setNillable(false)
													.setDataType(BOOL)
													.setDefaultValue(String.valueOf(DEFAULT_AGGREGATES_DISTINCT_SELECT))
													.setLabel("Distinct aggregates");
			addAttribute(AGGREGATES_DISTINCT_OVERRIDES).setParent(aggregatesAttr)
													   .setDataType(TEXT)
													   .setLabel("Distinct attribute overrides")
													   .setDescription(
															   "JSON object that maps entity names to attribute names")
													   .setVisibleExpression("$('" + AGGREGATES_DISTINCT_SELECT
															   + "').eq(true).value()");
		}

		private void createModReportSettings(Attribute modAttr)
		{
			Attribute reportsAttr = addAttribute(REPORTS).setParent(modAttr)
														 .setDataType(COMPOUND)
														 .setLabel("Reports")
														 .setVisibleExpression(
																 "$('" + MOD_REPORTS + "').eq(true).value()");
			addAttribute(REPORTS_ENTITIES).setParent(reportsAttr)
										  .setNillable(true)
										  .setDataType(TEXT)
										  .setLabel("Reports")
										  .setDescription(
												  "Comma-seperated report strings (e.g. MyDataSet:myreport,OtherDataSet:otherreport). The report name refers to an existing FreemarkerTemplate entity or file with name view-<report>-entitiesreport.ftl (e.g. view-myreport-entitiesreport.ftl)");
		}
	}

	public boolean getModAggregates()
	{
		Boolean value = getBoolean(Meta.MOD_AGGREGATES);
		return value != null ? value : false;
	}

	public void setModAggregates(boolean modAggregates)
	{
		set(Meta.MOD_AGGREGATES, modAggregates);
	}

	public boolean getModAnnotators()
	{
		Boolean value = getBoolean(Meta.MOD_ANNOTATORS);
		return value != null ? value : false;
	}

	public void setModAnnotators(boolean modAnnotators)
	{
		set(Meta.MOD_ANNOTATORS, modAnnotators);
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

	public boolean getModStandaloneReports()
	{
		Boolean value = getBoolean(Meta.MOD_STANDALONE_REPORTS);
		return value != null ? value : false;
	}

	public void setModReports(boolean modReports)
	{
		set(Meta.MOD_REPORTS, modReports);
	}

	public void setModStandaloneReports(boolean modStandaloneReports)
	{
		set(Meta.MOD_STANDALONE_REPORTS, modStandaloneReports);
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

	public boolean getGenomeBrowser()
	{
		Boolean value = getBoolean(Meta.DATA_GENOME_BROWSER);
		return value != null ? value : false;
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

	@Nullable
	public String getEntityReports()
	{
		return getString(Meta.REPORTS_ENTITIES);
	}

	@Nullable
	public String getEntityReport(String entityTypeId)
	{
		Map<String, String> entityReports = getEntityReportsAsMap();
		return entityReports != null ? entityReports.get(entityTypeId) : null;
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
			Map<String, String> entityReports = new LinkedHashMap<>();
			for (String entityReport : entityReportsStr.split(","))
			{
				String[] tokens = entityReport.split(":");
				String entityTypeId = tokens[0];
				String reportName = tokens[1];
				entityReports.put(entityTypeId, reportName);
			}
			return entityReports;
		}
		else
		{
			return null;
		}
	}

	@Nullable
	public boolean isShowNavigatorLink()
	{
		Boolean result = getBoolean(Meta.GENERAL_NAVIGATOR_LINK);
		return result == null ? false : result.booleanValue();
	}

	@Nullable
	public void setShowNavigatorLink(boolean showNavigatorLink)
	{
		set(Meta.GENERAL_NAVIGATOR_LINK, showNavigatorLink);
	}
}
