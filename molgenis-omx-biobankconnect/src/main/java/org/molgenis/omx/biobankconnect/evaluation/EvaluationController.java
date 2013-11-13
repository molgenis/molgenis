package org.molgenis.omx.biobankconnect.evaluation;

import static org.molgenis.omx.biobankconnect.evaluation.EvaluationController.URI;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
		ExcelWriter excelWriterRanks = null;

		try
		{
			if (selectedDataSetId != null)
			{
				File uploadFile = fileStore.store(file.getInputStream(), file.getName());
				response.setContentType("application/vnd.ms-excel");
				response.addHeader("Content-Disposition", "attachment; filename="
						+ getCsvFileName(file.getName() + "-ranks"));
				excelWriterRanks = new ExcelWriter(response.getOutputStream());
				excelWriterRanks.addCellProcessor(new LowerCaseProcessor(true, false));

				TupleWriter sheetWriterRank = excelWriterRanks.createTupleWriter("result");
				TupleWriter sheetWriterRankStatistics = excelWriterRanks.createTupleWriter("rank statistics");
				TupleWriter sheetWriteBiobankRanks = excelWriterRanks.createTupleWriter("biobank average ranks");
				TupleWriter sheetWriteSpssInput = excelWriterRanks.createTupleWriter("spss ranks");

				reader = new ExcelReader(uploadFile);
				ExcelSheetReader inputSheet = reader.getSheet(0);
				Iterator<String> columnIterator = inputSheet.colNamesIterator();

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
					Iterator<Tuple> iterator = inputSheet.iterator();
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
					sheetWriterRank.writeColNames(lowerCaseBiobankNames);

					Map<String, Map<String, List<Integer>>> rankCollection = new HashMap<String, Map<String, List<Integer>>>();
					List<Object> allRanks = new ArrayList<Object>();

					for (Entry<String, Map<String, List<String>>> entry : maunalMappings.entrySet())
					{
						String variableName = entry.getKey();
						List<String> ranks = new ArrayList<String>();
						ranks.add(variableName);
						Map<String, List<String>> mappingDetail = entry.getValue();
						List<ObservableFeature> features = database.find(ObservableFeature.class, new QueryRule(
								ObservableFeature.NAME, Operator.EQUALS, variableName));
						String description = features.get(0).getDescription();
						if (!rankCollection.containsKey(description)) rankCollection.put(description,
								new HashMap<String, List<Integer>>());

						if (!features.isEmpty())
						{
							KeyValueTuple row = new KeyValueTuple();
							row.set(firstColumn.toLowerCase(), description);

							for (DataSet dataSet : dataSets)
							{
								List<Integer> ranksBiobank = new ArrayList<Integer>();
								if (mappingDetail.containsKey(dataSet.getName().toLowerCase()))
								{
									Map<String, Hit> mappedFeatureIds = findFeaturesFromIndex("name",
											mappingDetail.get(dataSet.getName().toLowerCase()), dataSet.getId());

									String mappingDataSetIdentifier = selectedDataSetId + "-" + dataSet.getId();
									List<QueryRule> queryRules = new ArrayList<QueryRule>();
									queryRules.add(new QueryRule("store_mapping_feature", Operator.EQUALS, features
											.get(0).getId()));
									queryRules.add(new QueryRule(Operator.LIMIT, 50));
									queryRules.add(new QueryRule(Operator.SORTDESC, "store_mapping_score"));
									SearchRequest searchRequest = new SearchRequest(mappingDataSetIdentifier,
											queryRules, null);
									SearchResult result = searchService.search(searchRequest);

									if (mappedFeatureIds.size() == 0)
									{
										row.set(dataSet.getName().toLowerCase(), "N/A2");
										continue;
									}

									List<String> ids = new ArrayList<String>();
									for (Hit hit : result.getSearchHits())
									{
										Map<String, Object> columnValueMap = hit.getColumnValueMap();
										ids.add(columnValueMap.get("store_mapping_mapped_feature").toString());
									}
									Map<String, Hit> featureInfos = findFeaturesFromIndex("id", ids, dataSet.getId());

									String previousDescription = null;
									int rank = 0;
									for (Hit hit : result.getSearchHits())
									{
										Map<String, Object> columnValueMap = hit.getColumnValueMap();
										String mappedFeatureId = columnValueMap.get("store_mapping_mapped_feature")
												.toString();
										String mappedFeatureDescription = featureInfos.get(mappedFeatureId)
												.getColumnValueMap().get("description").toString()
												.replaceAll("[^0-9a-zA-Z ]", " ");

										rank++;
										if (previousDescription != null
												&& previousDescription.equalsIgnoreCase(mappedFeatureDescription)) rank--;

										if (mappedFeatureIds.containsKey(mappedFeatureId))
										{
											ranksBiobank.add(rank);
											allRanks.add(rank);
											mappedFeatureIds.remove(mappedFeatureId);
										}
										previousDescription = mappedFeatureDescription;
									}
									if (mappedFeatureIds.size() == 0)
									{
										String output = StringUtils.join(ranksBiobank, ',');
										if (ranksBiobank.size() > 1)
										{
											output += " (" + averageRank(ranksBiobank) + ")";
										}
										row.set(dataSet.getName().toLowerCase(), output);
									}
									else
									{
										for (int i = 0; i < mappedFeatureIds.size(); i++)
											allRanks.add("Not mapped");
										row.set(dataSet.getName().toLowerCase(), "Not mapped");
										ranksBiobank.clear();
									}
								}
								else row.set(dataSet.getName().toLowerCase(), "N/A1");

								rankCollection.get(description).put(dataSet.getName().toLowerCase(), ranksBiobank);
							}
							sheetWriterRank.write(row);
						}
					}

					Map<String, List<Integer>> rankCollectionPerBiobank = new HashMap<String, List<Integer>>();
					{
						sheetWriterRankStatistics.writeColNames(Arrays.asList(firstColumn.toLowerCase(),
								"average rank", "round-up rank", "median rank", "minium", "maximum"));
						for (Entry<String, Map<String, List<Integer>>> entry : rankCollection.entrySet())
						{
							String variableName = entry.getKey();
							KeyValueTuple row = new KeyValueTuple();
							row.set(firstColumn.toLowerCase(), variableName);
							List<Integer> rankAllBiobanks = new ArrayList<Integer>();
							for (Entry<String, List<Integer>> rankBiobanks : entry.getValue().entrySet())
							{
								if (!rankCollectionPerBiobank.containsKey(rankBiobanks.getKey())) rankCollectionPerBiobank
										.put(rankBiobanks.getKey(), new ArrayList<Integer>());
								rankCollectionPerBiobank.get(rankBiobanks.getKey()).addAll(rankBiobanks.getValue());
								rankAllBiobanks.addAll(rankBiobanks.getValue());
							}

							row.set("average rank", averageRank(rankAllBiobanks));
							row.set("round-up rank", Math.ceil(averageRank(rankAllBiobanks)));
							Collections.sort(rankAllBiobanks);
							row.set("minium", rankAllBiobanks.get(0));
							row.set("maximum", rankAllBiobanks.get(rankAllBiobanks.size() - 1));
							double medianRank = 0;
							if (rankAllBiobanks.size() % 2 == 0)
							{
								medianRank = (double) (rankAllBiobanks.get(rankAllBiobanks.size() / 2 - 1) + rankAllBiobanks
										.get(rankAllBiobanks.size() / 2)) / 2;
							}
							else
							{
								medianRank = (double) rankAllBiobanks.get(rankAllBiobanks.size() / 2);
							}
							row.set("median rank", medianRank);
							sheetWriterRankStatistics.write(row);
						}
					}

					{
						lowerCaseBiobankNames.remove(0);
						sheetWriteBiobankRanks.writeColNames(lowerCaseBiobankNames);
						KeyValueTuple tuple = new KeyValueTuple();
						for (Entry<String, List<Integer>> entry : rankCollectionPerBiobank.entrySet())
						{
							tuple.set(entry.getKey(), averageRank(entry.getValue()));
						}
						sheetWriteBiobankRanks.write(tuple);
					}

					{
						sheetWriteSpssInput.writeColNames(Arrays.asList("rank"));
						for (Object rank : allRanks)
						{
							KeyValueTuple tuple = new KeyValueTuple();
							tuple.set("rank", rank);
							sheetWriteSpssInput.write(tuple);
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
			if (excelWriterRanks != null) IOUtils.closeQuietly(excelWriterRanks);
		}
	}

	private double averageRank(List<Integer> ranks)
	{
		double result = 0;
		for (Integer rank : ranks)
		{
			result = result + rank;
		}
		DecimalFormat df = new DecimalFormat("#0.0");
		return ranks.size() == 0 ? -1 : Double.parseDouble(df.format(result / ranks.size()));
	}

	private Map<String, Hit> findFeaturesFromIndex(String field, List<String> featureNames, Integer dataSetId)
	{
		List<QueryRule> queryRules = new ArrayList<QueryRule>();
		for (String featureName : featureNames)
		{
			if (queryRules.size() > 0) queryRules.add(new QueryRule(Operator.OR));
			queryRules.add(new QueryRule(field, Operator.EQUALS, featureName));
		}
		queryRules.add(new QueryRule(Operator.LIMIT, 10000));
		SearchResult result = searchService.search(new SearchRequest("protocolTree-" + dataSetId, queryRules, null));

		Map<String, Hit> featureIds = new HashMap<String, Hit>();
		for (Hit hit : result.getSearchHits())
		{
			featureIds.put(hit.getColumnValueMap().get("id").toString(), hit);
		}
		return featureIds;
	}

	private String getCsvFileName(String dataSetName)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dataSetName + "_" + dateFormat.format(new Date()) + ".xls";
	}
}