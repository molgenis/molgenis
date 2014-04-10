package org.molgenis.dataexplorer.controller;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.BOOL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.CATEGORICAL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.XREF;
import static org.molgenis.dataexplorer.controller.DataExplorerController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.csv.CsvWriter;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Controller class for the data explorer.
 * 
 * The implementation javascript file for the resultstable is defined in a MolgenisSettings property named
 * 'dataexplorer.resultstable.js' possible values are '/js/SingleObservationSetTable.js' or
 * '/js/MultiObservationSetTable.js' with '/js/MultiObservationSetTable.js' as the default
 * 
 * @author erwin
 * 
 */
@Controller
@RequestMapping(URI)
public class DataExplorerController extends MolgenisPluginController
{
	private static final Logger logger = Logger.getLogger(DataExplorerController.class);

	public static final String ID = "dataexplorer";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	public static final String KEY_MOD_AGGREGATES = "plugin.dataexplorer.mod.aggregates";
	public static final String KEY_MOD_CHARTS = "plugin.dataexplorer.mod.charts";
	public static final String KEY_MOD_DATA = "plugin.dataexplorer.mod.data";
	private static final boolean DEFAULT_VAL_MOD_AGGREGATES = true;
	private static final boolean DEFAULT_VAL_MOD_CHARTS = true;
	private static final boolean DEFAULT_VAL_MOD_DATA = true;

	public static final String INITLOCATION = "initLocation";
	public static final String COORDSYSTEM = "coordSystem";
	public static final String CHAINS = "chains";
	public static final String SOURCES = "sources";
	public static final String BROWSERLINKS = "browserLinks";
	public static final String GENOMEBROWSERTABLE = "genomeBrowserTable";

	public static final String MUTATION_START_POSITION = "start_nucleotide";
	public static final String MUTATION_ID = "mutation_id";
	public static final String MUTATION_CHROMOSOME = "chromosome";

	@Autowired
	private DataService dataService;

	@Autowired
	private MolgenisPermissionService molgenisPermissionService;

	@Autowired
	private MolgenisSettings molgenisSettings;

	public DataExplorerController()
	{
		super(URI);
	}

	/**
	 * Show the explorer page
	 * 
	 * @param model
	 * @return the view name
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String init(@RequestParam(value = "dataset", required = false) String selectedEntityName,
			@RequestParam(value = "wizard", required = false) Boolean wizard, Model model) throws Exception
	{
		boolean entityExists = false;
		Iterable<EntityMetaData> entitiesMeta = Iterables.transform(dataService.getEntityNames(),
				new Function<String, EntityMetaData>()
				{
					@Override
					public EntityMetaData apply(String entityName)
					{
						return dataService.getEntityMetaData(entityName);
					}
				});
		model.addAttribute("entitiesMeta", entitiesMeta);
		if (selectedEntityName != null)
		{
			entityExists = dataService.hasRepository(selectedEntityName);
		}

		if (entityExists)
		{
			model.addAttribute("hideDatasetSelect", true);
		}
		else
		{
			Iterator<EntityMetaData> entitiesIterator = entitiesMeta.iterator();
			if (entitiesIterator.hasNext())
			{
				selectedEntityName = entitiesIterator.next().getName();
			}
		}
		model.addAttribute("selectedEntityName", selectedEntityName);
		model.addAttribute("wizard", (wizard != null) && wizard.booleanValue());

		return "view-dataexplorer";
	}

	@RequestMapping(value = "/module/{moduleId}", method = GET)
	public String getModule(@PathVariable("moduleId") String moduleId, Model model)
	{
		if (moduleId.equals("data"))
		{
			// Init genome browser
			model.addAttribute("genomeEntities", getGenomeBrowserEntities());

			model.addAttribute(INITLOCATION, molgenisSettings.getProperty(INITLOCATION));
			model.addAttribute(COORDSYSTEM, molgenisSettings.getProperty(COORDSYSTEM));
			model.addAttribute(CHAINS, molgenisSettings.getProperty(CHAINS));
			model.addAttribute(SOURCES, molgenisSettings.getProperty(SOURCES));
			model.addAttribute(BROWSERLINKS, molgenisSettings.getProperty(BROWSERLINKS));
			model.addAttribute(GENOMEBROWSERTABLE, molgenisSettings.getProperty(GENOMEBROWSERTABLE));
		}
		return "view-dataexplorer-mod-" + moduleId; // TODO bad request in case of invalid module id
	}

	/**
	 * Returns modules configuration for this entity based on current user permissions.
	 * 
	 * @param entityName
	 * @return
	 */
	@RequestMapping(value = "/modules", method = GET)
	@ResponseBody
	public ModulesConfigResponse getModules(@RequestParam("entity") String entityName)
	{
		// get data explorer settings
		boolean modCharts = molgenisSettings.getBooleanProperty(KEY_MOD_CHARTS, DEFAULT_VAL_MOD_CHARTS);
		boolean modData = molgenisSettings.getBooleanProperty(KEY_MOD_DATA, DEFAULT_VAL_MOD_DATA);
		boolean modAggregates = molgenisSettings.getBooleanProperty(KEY_MOD_AGGREGATES, DEFAULT_VAL_MOD_AGGREGATES);

		// set data explorer permission
		Permission pluginPermission = null;
		if (molgenisPermissionService.hasPermissionOnEntity(entityName, Permission.WRITE)) pluginPermission = Permission.WRITE;
		else if (molgenisPermissionService.hasPermissionOnEntity(entityName, Permission.READ)) pluginPermission = Permission.READ;
		else if (molgenisPermissionService.hasPermissionOnEntity(entityName, Permission.COUNT)) pluginPermission = Permission.COUNT;

		ModulesConfigResponse modulesConfig = new ModulesConfigResponse();
		if (pluginPermission != null)
		{
			switch (pluginPermission)
			{
				case COUNT:
					if (modAggregates)
					{
						modulesConfig.add(new ModuleConfig("aggregates", "Aggregates", "grid-icon.png"));
					}
					break;
				case READ:
				case WRITE:
					if (modData)
					{
						modulesConfig.add(new ModuleConfig("data", "Data", "grid-icon.png"));
					}
					if (modAggregates)
					{
						modulesConfig.add(new ModuleConfig("aggregates", "Aggregates", "aggregate-icon.png"));
					}
					if (modCharts)
					{
						modulesConfig.add(new ModuleConfig("charts", "Charts", "chart-icon.png"));
					}
					break;
				default:
					throw new RuntimeException("unknown plugin permission: " + pluginPermission);
			}
		}
		return modulesConfig;
	}

	/**
	 * Get readable genome entities
	 * 
	 * @return
	 */
	private Map<String, String> getGenomeBrowserEntities()
	{
		Map<String, String> genomeEntities = new HashMap<String, String>();
		for (String entityName : dataService.getEntityNames())
		{
			EntityMetaData entityMetaData = dataService.getEntityMetaData(entityName);
			if (isGenomeBrowserEntity(entityMetaData))
			{
				boolean canRead = molgenisPermissionService.hasPermissionOnEntity(entityName, Permission.READ);
				boolean canWrite = molgenisPermissionService.hasPermissionOnEntity(entityName, Permission.WRITE);
				if (canRead || canWrite)
				{
					genomeEntities.put(entityMetaData.getName(), entityMetaData.getLabel());
				}
			}
		}
		return genomeEntities;
	}

	private boolean isGenomeBrowserEntity(EntityMetaData entityMetaData)
	{
		AttributeMetaData attributeStartPosition = entityMetaData.getAttribute(MUTATION_START_POSITION);
		AttributeMetaData attributeId = entityMetaData.getAttribute(MUTATION_ID);
		AttributeMetaData attributeChromosome = entityMetaData.getAttribute(MUTATION_CHROMOSOME);
		return attributeStartPosition != null && attributeId != null && attributeChromosome != null;
	}

	@RequestMapping(value = "/download", method = POST)
	public void download(@RequestParam("dataRequest") String dataRequestStr, HttpServletResponse response)
			throws IOException
	{
		// Workaround because binding with @RequestBody is not possible:
		// http://stackoverflow.com/a/9970672
		dataRequestStr = URLDecoder.decode(dataRequestStr, "UTF-8");
		logger.info("Download request: [" + dataRequestStr + "]");
		DataRequest dataRequest = new GsonHttpMessageConverter().getGson().fromJson(dataRequestStr, DataRequest.class);

		String entityName = dataRequest.getEntityName();
		EntityMetaData entityMetaData = dataService.getEntityMetaData(entityName);
		final Set<String> attributes = new HashSet<String>(dataRequest.getAttributeNames());
		String fileName = entityName + '_' + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".csv";

		QueryImpl query = dataRequest.getQuery();
		response.setContentType("text/csv");
		response.addHeader("Content-Disposition", "attachment; filename=" + fileName);

		CsvWriter csvWriter = new CsvWriter(response.getOutputStream());
		try
		{
			csvWriter.writeAttributeNames(Iterables.transform(
					Iterables.filter(entityMetaData.getAtomicAttributes(), new Predicate<AttributeMetaData>()
					{
						@Override
						public boolean apply(AttributeMetaData attributeMetaData)
						{
							return attributes.contains(attributeMetaData.getName());
						}
					}), new Function<AttributeMetaData, String>()
					{
						@Override
						public String apply(AttributeMetaData attributeMetaData)
						{
							return attributeMetaData.getName();
						}
					}));
			csvWriter.add(dataService.findAll(entityName, query));
		}
		finally
		{
			csvWriter.close();
		}
	}

	@RequestMapping(value = "/aggregate", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public AggregateResponse aggregate(@Valid @RequestBody AggregateRequest request)
	{
		String entityName = request.getEntityName();
		String xAttributeName = request.getXAxisAttributeName();
		String yAttributeName = request.getYAxisAttributeName();

		if (StringUtils.isBlank(xAttributeName) && StringUtils.isBlank(yAttributeName))
		{
			throw new InputValidationException("Missing aggregate attribute");
		}
		EntityMetaData entityMeta = dataService.getEntityMetaData(entityName);

		QueryImpl q = request.getQ() == null ? new QueryImpl() : new QueryImpl(request.getQ());
		if (q.getRules().size() > 0)
		{
			q.and();
		}

		AttributeMetaData xAttributeMeta = null;
		FieldTypeEnum xDataType = null;
		if (StringUtils.isNotBlank(xAttributeName))
		{
			xAttributeMeta = entityMeta.getAttribute(xAttributeName);
			if (xAttributeMeta == null)
			{
				throw new InputValidationException("Unknow attribute '" + xAttributeName + "'");
			}

			xDataType = xAttributeMeta.getDataType().getEnumType();
			if ((xDataType != BOOL) && (xDataType != CATEGORICAL) && (xDataType != XREF))
			{
				throw new InputValidationException("Unsupported data type " + xDataType);
			}
		}

		AttributeMetaData yAttributeMeta = null;
		FieldTypeEnum yDataType = null;
		if (StringUtils.isNotBlank(yAttributeName))
		{

			yAttributeMeta = entityMeta.getAttribute(yAttributeName);
			if (yAttributeMeta == null)
			{
				throw new InputValidationException("Unknow attribute '" + yAttributeName + "'");
			}

			yDataType = yAttributeMeta.getDataType().getEnumType();
			if ((yDataType != BOOL) && (yDataType != CATEGORICAL) && (yDataType != XREF))
			{
				throw new InputValidationException("Unsupported data type " + yDataType);
			}

		}

		List<Object> xValues = Lists.newArrayList();
		List<Object> yValues = Lists.newArrayList();
		List<List<Long>> matrix = new ArrayList<List<Long>>();
		Set<String> xLabels = Sets.newLinkedHashSet();
		Set<String> yLabels = Sets.newLinkedHashSet();

		if (xDataType != null)
		{
			if (xDataType == BOOL)
			{
				xValues.add(Boolean.TRUE);
				xValues.add(Boolean.FALSE);
				xLabels.add(xAttributeName + ": true");
				xLabels.add(xAttributeName + ": false");
			}
			else
			{
				EntityMetaData xRefEntityMeta = xAttributeMeta.getRefEntity();
				String xRefEntityName = xRefEntityMeta.getName();
				String xRefEntityLblAttr = xRefEntityMeta.getLabelAttribute().getName();

				Iterable<Entity> xEntities = dataService.findAll(xRefEntityName);
				for (Entity xRefEntity : xEntities)
				{
					xLabels.add(xRefEntity.getString(xRefEntityLblAttr));
					xValues.add(xRefEntity.get(xRefEntityLblAttr));
				}
			}
		}

		if (yDataType != null)
		{
			if (yDataType == BOOL)
			{
				yValues.add(Boolean.TRUE);
				yValues.add(Boolean.FALSE);
				yLabels.add(yAttributeName + ": true");
				yLabels.add(yAttributeName + ": false");
			}
			else
			{
				EntityMetaData yRefEntityMeta = yAttributeMeta.getRefEntity();
				String yRefEntityName = yRefEntityMeta.getName();
				String yRefEntityLblAttr = yRefEntityMeta.getLabelAttribute().getName();

				Iterable<Entity> yEntities = dataService.findAll(yRefEntityName);
				for (Entity yRefEntity : yEntities)
				{
					yLabels.add(yRefEntity.getString(yRefEntityLblAttr));
					yValues.add(yRefEntity.get(yRefEntityLblAttr));
				}
			}
		}

		boolean hasXValues = !xValues.isEmpty();
		boolean hasYValues = !yValues.isEmpty();

		if (hasXValues)
		{
			List<Long> totals = Lists.newArrayList();

			for (Object xValue : xValues)
			{
				List<Long> row = Lists.newArrayList();

				if (hasYValues)
				{
					int i = 0;
					for (Object yValue : yValues)
					{
						// Both x and y choosen
						Query query = q.getRules().isEmpty() ? new QueryImpl() : new QueryImpl(q).and();
						query.eq(xAttributeName, xValue).and().eq(yAttributeName, yValue);
						long count = dataService.count(entityName, query);
						row.add(count);
						if (totals.size() == i)
						{
							totals.add(count);
						}
						else
						{
							totals.set(i, totals.get(i) + count);
						}
						i++;
					}
				}
				else
				{
					// No y attribute chosen
					Query query = q.getRules().isEmpty() ? new QueryImpl() : new QueryImpl(q).and();
					query.eq(xAttributeName, xValue);
					long count = dataService.count(entityName, query);
					row.add(count);
					if (totals.isEmpty())
					{
						totals.add(count);
					}
					else
					{
						totals.set(0, totals.get(0) + count);
					}

				}

				matrix.add(row);
			}

			yLabels.add(hasYValues ? "Total" : "Count");
			xLabels.add("Total");

			matrix.add(totals);
		}
		else
		{
			// No xattribute chosen
			List<Long> row = Lists.newArrayList();
			for (Object yValue : yValues)
			{
				Query query = q.getRules().isEmpty() ? new QueryImpl() : new QueryImpl(q).and();
				query.eq(yAttributeName, yValue);
				long count = dataService.count(entityName, query);
				row.add(count);
			}
			matrix.add(row);

			xLabels.add("Count");
			yLabels.add("Total");
		}

		// Count row totals
		if (hasYValues)
		{
			for (List<Long> row : matrix)
			{
				long total = 0;
				for (Long count : row)
				{
					total += count;
				}
				row.add(total);
			}
		}

		return new AggregateResponse(matrix, xLabels, yLabels);
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Map<String, String> handleRuntimeException(RuntimeException e)
	{
		logger.error(null, e);
		return Collections.singletonMap("errorMessage",
				"An error occurred. Please contact the administrator.<br />Message:" + e.getMessage());
	}
}
