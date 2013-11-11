package org.molgenis.omx.biobankconnect.evaluation;

import static org.molgenis.omx.biobankconnect.evaluation.EvaluationController.URI;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.io.TupleWriter;
import org.molgenis.io.excel.ExcelReader;
import org.molgenis.io.excel.ExcelSheetReader;
import org.molgenis.io.excel.ExcelWriter;
import org.molgenis.io.processor.LowerCaseProcessor;
import org.molgenis.omx.biobankconnect.ontologymatcher.OntologyMatcher;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.molgenis.util.FileStore;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(URI)
public class EvaluationController extends MolgenisPluginController
{
	public static final String ID = "evaluation";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private static final String PROTOCOL_IDENTIFIER = "store_mapping";
	private final SearchService searchService;
	private final Database database;

	@Autowired
	private FileStore fileStore;

	@Autowired
	public EvaluationController(OntologyMatcher ontologyMatcher, SearchService searchService, Database database)
	{
		super(URI);
		if (ontologyMatcher == null) throw new IllegalArgumentException("OntologyMatcher is null");
		if (searchService == null) throw new IllegalArgumentException("SearchService is null");
		if (database == null) throw new IllegalArgumentException("Database is null");
		this.searchService = searchService;
		this.database = database;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(@RequestParam(value = "selectedDataSet", required = false)
	String selectedDataSetId, Model model) throws DatabaseException
	{
		List<DataSet> dataSets = new ArrayList<DataSet>();
		for (DataSet dataSet : database.find(DataSet.class))
		{
			if (selectedDataSetId == null) selectedDataSetId = dataSet.getId().toString();
			if (!dataSet.getProtocolUsed_Identifier().equals(PROTOCOL_IDENTIFIER)) dataSets.add(dataSet);
		}
		model.addAttribute("dataSets", dataSets);

		List<String> mappedDataSets = new ArrayList<String>();
		if (selectedDataSetId != null)
		{
			model.addAttribute("selectedDataSet", selectedDataSetId);
			for (DataSet dataSet : database.find(DataSet.class, new QueryRule(DataSet.IDENTIFIER, Operator.LIKE,
					selectedDataSetId)))
			{
				if (dataSet.getIdentifier().startsWith(selectedDataSetId))
				{
					String[] dataSetIds = dataSet.getIdentifier().toString().split("-");
					if (dataSetIds.length > 1) mappedDataSets.add(dataSetIds[1]);
				}
			}
		}
		model.addAttribute("mappedDataSets", mappedDataSets);
		return "EvaluationPlugin";
	}

	@RequestMapping(value = "/verify", method = RequestMethod.POST, headers = "Content-Type=multipart/form-data")
	public void verify(@RequestParam(value = "selectedDataSet", required = false)
	String selectedDataSetId, @RequestParam
	Part file, HttpServletResponse response, Model model) throws IOException, DatabaseException
	{
		ExcelReader reader = null;
		ExcelWriter excelWriter = null;

		try
		{
			if (selectedDataSetId != null)
			{
				File uploadFile = fileStore.store(file.getInputStream(), file.getName());
				response.setContentType("application/vnd.ms-excel");
				response.addHeader("Content-Disposition", "attachment; filename="
						+ getCsvFileName(file.getName() + "-ranks.xls"));
				excelWriter = new ExcelWriter(response.getOutputStream());
				excelWriter.addCellProcessor(new LowerCaseProcessor(true, false));
				TupleWriter sheetWriter = excelWriter.createTupleWriter("result");

				reader = new ExcelReader(uploadFile);
				ExcelSheetReader sheet = reader.getSheet(0);
				Iterator<String> columnIterator = sheet.colNamesIterator();

				List<String> biobankNames = new ArrayList<String>();
				while (columnIterator.hasNext())
				{
					biobankNames.add(columnIterator.next());
				}
				String firstColumn = biobankNames.get(0);
				biobankNames.remove(0);

				// First column has to correspond to the selected dataset
				if (database.findById(DataSet.class, Integer.parseInt(selectedDataSetId)).getName()
						.equalsIgnoreCase(firstColumn))
				{
					Map<String, Map<String, List<String>>> maunalMappings = new HashMap<String, Map<String, List<String>>>();
					Iterator<Tuple> iterator = sheet.iterator();
					while (iterator.hasNext())
					{
						Tuple row = iterator.next();
						String variableName = row.getString(firstColumn);
						if (!maunalMappings.containsKey(variableName)) maunalMappings.put(variableName,
								new HashMap<String, List<String>>());
						for (String biobank : biobankNames)
						{
							if (row.get(biobank) != null)
							{
								String mappingString = row.get(biobank).toString();
								if (!maunalMappings.containsKey(variableName))
								{
									maunalMappings.put(variableName, new HashMap<String, List<String>>());
								}
								if (!maunalMappings.get(variableName).containsKey(biobank.toLowerCase()))
								{
									maunalMappings.get(variableName)
											.put(biobank.toLowerCase(), new ArrayList<String>());
								}
								maunalMappings.get(variableName).get(biobank.toLowerCase())
										.addAll(Arrays.asList(mappingString.split(",")));
							}
						}
					}

					List<String> lowerCaseBiobankNames = new ArrayList<String>();
					for (String element : biobankNames)
					{
						lowerCaseBiobankNames.add(element.toLowerCase());
					}

					List<DataSet> dataSets = database.find(DataSet.class, new QueryRule(DataSet.NAME, Operator.IN,
							lowerCaseBiobankNames));
					lowerCaseBiobankNames.add(0, firstColumn.toLowerCase());
					sheetWriter.writeColNames(lowerCaseBiobankNames);

					for (Entry<String, Map<String, List<String>>> entry : maunalMappings.entrySet())
					{
						String variableName = entry.getKey();
						List<String> ranks = new ArrayList<String>();
						ranks.add(variableName);
						Map<String, List<String>> mappingDetail = entry.getValue();
						List<ObservableFeature> features = database.find(ObservableFeature.class, new QueryRule(
								ObservableFeature.NAME, Operator.EQUALS, variableName));
						if (!features.isEmpty())
						{
							KeyValueTuple row = new KeyValueTuple();
							row.set(firstColumn.toLowerCase(), variableName);

							for (DataSet dataSet : dataSets)
							{
								if (mappingDetail.containsKey(dataSet.getName().toLowerCase()))
								{
									StringBuilder outputRank = new StringBuilder();
									List<String> mappedFeatureIds = findFeaturesFromIndex(
											mappingDetail.get(dataSet.getName().toLowerCase()), dataSet.getId());

									String mappingDataSetIdentifier = selectedDataSetId + "-" + dataSet.getId();
									List<QueryRule> queryRules = new ArrayList<QueryRule>();
									queryRules.add(new QueryRule("store_mapping_feature", Operator.EQUALS, features
											.get(0).getId()));
									queryRules.add(new QueryRule(Operator.LIMIT, 30));
									queryRules.add(new QueryRule(Operator.SORTDESC, "store_mapping_score"));
									SearchRequest searchRequest = new SearchRequest(mappingDataSetIdentifier,
											queryRules, null);
									SearchResult result = searchService.search(searchRequest);

									if (mappedFeatureIds.size() == 0)
									{
										row.set(dataSet.getName().toLowerCase(), "Missing");
										continue;
									}

									double previousScore = -1;
									int rank = 0;
									for (Hit hit : result.getSearchHits())
									{
										Map<String, Object> columnValueMap = hit.getColumnValueMap();
										String mappedFeatureId = columnValueMap.get("store_mapping_mapped_feature")
												.toString();
										String score = columnValueMap.get("store_mapping_score").toString();

										if (previousScore != Double.parseDouble(score))
										{
											previousScore = Double.parseDouble(score);
											rank++;
										}
										if (mappedFeatureIds.contains(mappedFeatureId))
										{
											// foundMappings = true;
											if (outputRank.length() != 0) outputRank.append(',');
											outputRank.append(rank);
											mappedFeatureIds.remove(mappedFeatureId);
										}
									}
									if (mappedFeatureIds.size() == 0) row.set(dataSet.getName().toLowerCase(),
											outputRank.toString());
									else row.set(dataSet.getName().toLowerCase(), "Not mapped");
								}
								else row.set(dataSet.getName().toLowerCase(), "");
							}
							sheetWriter.write(row);
						}
					}
				}
			}
		}
		catch (DatabaseException e)
		{
			new RuntimeException(e);
		}
		finally
		{
			if (reader != null) reader.close();
			if (excelWriter != null) IOUtils.closeQuietly(excelWriter);
		}
	}

	private List<String> findFeaturesFromIndex(List<String> featureNames, Integer dataSetId)
	{
		List<QueryRule> queryRules = new ArrayList<QueryRule>();
		for (String featureName : featureNames)
		{
			if (queryRules.size() > 0) queryRules.add(new QueryRule(Operator.OR));
			queryRules.add(new QueryRule("name", Operator.EQUALS, featureName));
		}
		queryRules.add(new QueryRule(Operator.LIMIT, 10000));
		SearchResult result = searchService.search(new SearchRequest("protocolTree-" + dataSetId, queryRules, null));

		Set<String> featureIds = new HashSet<String>();
		for (Hit hit : result.getSearchHits())
		{
			featureIds.add(hit.getColumnValueMap().get("id").toString());
		}
		return new ArrayList<String>(featureIds);
	}

	private String getCsvFileName(String dataSetName)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dataSetName + "_" + dateFormat.format(new Date()) + ".csv";
	}
}