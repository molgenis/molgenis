package org.molgenis.dataexplorer.controller;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.BOOL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.CATEGORICAL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.XREF;
import static org.molgenis.dataexplorer.controller.DataExplorerController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import org.molgenis.entityexplorer.controller.EntityExplorerController;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.search.SearchService;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
	private static final String MODEL_KEY_MOD_AGGREGATES = "mod_aggregates";
	private static final String MODEL_KEY_MOD_CHARTS = "mod_charts";
	private static final String MODEL_KEY_MOD_DATA = "mod_data";
	private static final boolean DEFAULT_VAL_MOD_AGGREGATES = true;
	private static final boolean DEFAULT_VAL_MOD_CHARTS = true;
	private static final boolean DEFAULT_VAL_MOD_DATA = true;

	public static final String INITLOCATION = "initLocation";
	public static final String COORDSYSTEM = "coordSystem";
	public static final String CHAINS = "chains";
	public static final String SOURCES = "sources";
	public static final String BROWSERLINKS = "browserLinks";
	public static final String SEARCHENDPOINT = "searchEndpoint";
	public static final String KARYOTYPEENDPOINT = "karyotypeEndpoint";
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

	@Autowired
	private SearchService searchService;

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
	public String init(DataExplorerRequest request, Model model) throws Exception
	{
		// set entityExplorer URL for link to EntityExplorer for x/mrefs, but only if the user has permission to see the
		// plugin
		if (molgenisPermissionService.hasPermissionOnPlugin(EntityExplorerController.ID, Permission.READ)
				|| molgenisPermissionService.hasPermissionOnPlugin(EntityExplorerController.ID, Permission.WRITE))
		{
			model.addAttribute("entityExplorerUrl", EntityExplorerController.ID);
		}

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

		if (request.getDataset() == null)
		{
			request.setDataset(entitiesMeta.iterator().next().getName());
		}

		model.addAttribute("selectedEntityName", request.getDataset());
		model.addAttribute("wizard", request.isWizard());
		model.addAttribute("tab", request.getTab());

		// Init genome browser
		model.addAttribute("genomeBrowserSets", getGenomeBrowserSetsToModel());

		// define which modules to display
		Boolean modCharts = molgenisSettings.getBooleanProperty(KEY_MOD_CHARTS, DEFAULT_VAL_MOD_CHARTS);
		model.addAttribute(MODEL_KEY_MOD_CHARTS, modCharts);
		Boolean modData = molgenisSettings.getBooleanProperty(KEY_MOD_DATA, DEFAULT_VAL_MOD_DATA);
		model.addAttribute(MODEL_KEY_MOD_DATA, modData);
		Boolean modAggregates = molgenisSettings.getBooleanProperty(KEY_MOD_AGGREGATES, DEFAULT_VAL_MOD_AGGREGATES);
		model.addAttribute(MODEL_KEY_MOD_AGGREGATES, modAggregates);

		model.addAttribute(INITLOCATION, molgenisSettings.getProperty(INITLOCATION));
		model.addAttribute(COORDSYSTEM, molgenisSettings.getProperty(COORDSYSTEM));
		model.addAttribute(CHAINS, molgenisSettings.getProperty(CHAINS));
		model.addAttribute(SOURCES, molgenisSettings.getProperty(SOURCES));
		model.addAttribute(BROWSERLINKS, molgenisSettings.getProperty(BROWSERLINKS));
		model.addAttribute(SEARCHENDPOINT, molgenisSettings.getProperty(SEARCHENDPOINT));
		model.addAttribute(KARYOTYPEENDPOINT, molgenisSettings.getProperty(KARYOTYPEENDPOINT));
		model.addAttribute(GENOMEBROWSERTABLE, molgenisSettings.getProperty(GENOMEBROWSERTABLE));

		return "view-dataexplorer";
	}

	private Map<String, String> getGenomeBrowserSetsToModel()
	{
		Map<String, String> genomeBrowserSets = new HashMap<String, String>();
		for (String entityName : dataService.getEntityNames())
		{
			EntityMetaData entityMetaData = dataService.getEntityMetaData(entityName);
			AttributeMetaData attributeStartPosition = entityMetaData.getAttribute(MUTATION_START_POSITION);
			AttributeMetaData attributeId = entityMetaData.getAttribute(MUTATION_ID);
			AttributeMetaData attributeChromosome = entityMetaData.getAttribute(MUTATION_CHROMOSOME);
			if (attributeStartPosition != null && attributeId != null && attributeChromosome != null)
			{
				genomeBrowserSets.put(entityName, entityMetaData.getLabel());
			}
		}
		return genomeBrowserSets;
	}

	@RequestMapping(value = "/download", method = POST)
	public void download(@RequestParam("dataRequest")
	String dataRequestStr, HttpServletResponse response) throws IOException
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
			csvWriter.add(dataService.findAll(entityName, dataRequest.getQuery()));
		}
		finally
		{
			csvWriter.close();
		}
	}

	@RequestMapping(value = "/aggregate", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public AggregateResponse aggregate(@Valid
	@RequestBody
	AggregateRequest request)
	{
		String entityName = request.getEntityName();
		String xAttributeName = request.getXAxisAttributeName();
		String yAttributeName = request.getYAxisAttributeName();
		QueryImpl q = request.getQ() != null ? new QueryImpl(request.getQ()) : new QueryImpl();

		if (StringUtils.isBlank(xAttributeName) && StringUtils.isBlank(yAttributeName))
		{
			throw new InputValidationException("Missing aggregate attribute");
		}

		EntityMetaData entityMeta = dataService.getEntityMetaData(entityName);

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

		Iterable<?> xValues;
		Iterable<?> yValues;
		List<List<Long>> matrix = new ArrayList<List<Long>>();
		Set<String> xLabels = Sets.newLinkedHashSet();
		Set<String> yLabels = Sets.newLinkedHashSet();

		if (xDataType == null)
		{
			xValues = Lists.newArrayList();
		}
		else if (xDataType == BOOL)
		{
			xValues = Arrays.asList(true, false);
			xLabels.add(xAttributeName + ": true");
			xLabels.add(xAttributeName + ": false");
		}
		else
		{
			EntityMetaData xRefEntityMeta = xAttributeMeta.getRefEntity();
			String xRefEntityName = xRefEntityMeta.getName();
			String xRefEntityLblAttr = xRefEntityMeta.getLabelAttribute().getName();

			xValues = dataService.findAll(xRefEntityName);
			for (Object xRefEntity : xValues)
			{
				xLabels.add(((Entity) xRefEntity).getString(xRefEntityLblAttr));
			}
		}

		if (yDataType == null)
		{
			yValues = Lists.newArrayList();
		}
		else if (yDataType == BOOL)
		{
			yValues = Arrays.asList(true, false);
			yLabels.add(yAttributeName + ": true");
			yLabels.add(yAttributeName + ": false");
		}
		else
		{
			EntityMetaData yRefEntityMeta = yAttributeMeta.getRefEntity();
			String yRefEntityName = yRefEntityMeta.getName();
			String yRefEntityLblAttr = yRefEntityMeta.getLabelAttribute().getName();

			yValues = dataService.findAll(yRefEntityName);
			for (Object yRefEntity : yValues)
			{
				yLabels.add(((Entity) yRefEntity).getString(yRefEntityLblAttr));
			}
		}

		boolean hasXValues = !Iterables.isEmpty(xValues);
		boolean hasYValues = !Iterables.isEmpty(yValues);

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

	@ExceptionHandler(InputValidationException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Map<String, String> handleInputValidationException(InputValidationException e)
	{
		logger.info(null, e);
		return Collections.singletonMap("errorMessage", e.getMessage());
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
