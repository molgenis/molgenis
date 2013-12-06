package org.molgenis.omx.biobankconnect.mappingmanager;

import static org.molgenis.omx.biobankconnect.mappingmanager.MappingManagerController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
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
import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.io.TupleWriter;
import org.molgenis.io.csv.CsvWriter;
import org.molgenis.io.excel.ExcelReader;
import org.molgenis.io.excel.ExcelSheetReader;
import org.molgenis.omx.biobankconnect.ontologyannotator.UpdateIndexRequest;
import org.molgenis.omx.biobankconnect.ontologymatcher.OntologyMatcher;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.molgenis.security.SecurityUtils;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.util.FileStore;
import org.molgenis.util.GsonHttpMessageConverter;
import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.ValueTuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(URI)
public class MappingManagerController extends MolgenisPluginController
{
	private static final Logger logger = Logger.getLogger(MappingManagerController.class);

	public static final String ID = "mappingmanager";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private static final String FEATURE_ID = "id";
	private static final String FEATURE_NAME = "name";
	private static final String PROTOCOL_IDENTIFIER = "store_mapping";
	private static final String STORE_MAPPING_FEATURE = "store_mapping_feature";
	private static final String STORE_MAPPING_MAPPED_FEATURE = "store_mapping_mapped_feature";
	private static final String STORE_MAPPING_CONFIRM_MAPPING = "store_mapping_confirm_mapping";
	private final OntologyMatcher ontologyMatcher;
	private final SearchService searchService;
	private final DataService dataService;
	private final UserAccountService userAccountService;

	@Autowired
	private FileStore fileStore;

	@Autowired
	public MappingManagerController(OntologyMatcher ontologyMatcher, SearchService searchService,
			UserAccountService userAccountService, DataService dataService)
	{
		super(URI);
		if (ontologyMatcher == null) throw new IllegalArgumentException("OntologyMatcher is null");
		if (searchService == null) throw new IllegalArgumentException("SearchService is null");
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		if (userAccountService == null) throw new IllegalArgumentException("userAccountService is null");
		this.userAccountService = userAccountService;
		this.ontologyMatcher = ontologyMatcher;
		this.searchService = searchService;
		this.dataService = dataService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(@RequestParam(value = "selectedDataSet", required = false)
	String selectedDataSetId, Model model)
	{
		List<DataSet> dataSets = new ArrayList<DataSet>();

		Iterable<DataSet> allDataSets = dataService.findAll(DataSet.ENTITY_NAME, new QueryImpl());
		for (DataSet dataSet : allDataSets)
		{
			if (!dataSet.getProtocolUsed().getIdentifier().equals(PROTOCOL_IDENTIFIER)) dataSets.add(dataSet);
		}

		model.addAttribute("dataSets", dataSets);
		model.addAttribute("userName", SecurityUtils.getCurrentUsername());
		if (selectedDataSetId != null) model.addAttribute("selectedDataSet", selectedDataSetId);
		model.addAttribute("isRunning", ontologyMatcher.isRunning());

		return "MappingManagerPlugin";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/delete", consumes = APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteDocuments(@RequestBody
	UpdateIndexRequest request)
	{
		try
		{
			searchService.deleteDocumentByIds(request.getDocumentType(), request.getDocumentIds());
		}
		catch (Exception e)
		{
			logger.error("Exception calling searchservice for request [" + request + "]", e);
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/update", consumes = APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateDocument(@RequestBody
	UpdateIndexRequest request)
	{
		try
		{
			searchService.updateDocumentById(request.getDocumentType(), request.getDocumentIds().get(0),
					request.getUpdateScript());
		}
		catch (Exception e)
		{
			logger.error("Exception calling searchservice for request [" + request + "]", e);
		}
	}

	@RequestMapping(value = "/verify", method = RequestMethod.POST, headers = "Content-Type=multipart/form-data")
	public String verify(@RequestParam
	Integer selectedDataSet, @RequestParam
	Part file, HttpServletResponse response, Model model) throws IOException
	{
		ExcelReader reader = null;
		TupleWriter tupleWriter = null;

		try
		{
			File uploadFile = fileStore.store(file.getInputStream(), file.getName());

			response.setContentType("text/csv");
			response.addHeader("Content-Disposition", "attachment; filename="
					+ getCsvFileName(file.getName() + "-ranks.csv"));

			reader = new ExcelReader(uploadFile);
			tupleWriter = new CsvWriter(response.getWriter());
			ExcelSheetReader sheet = reader.getSheet(0);
			Iterator<String> columnIterator = sheet.colNamesIterator();

			List<String> biobankNames = new ArrayList<String>();
			while (columnIterator.hasNext())
			{
				biobankNames.add(columnIterator.next());
			}
			String firstColumn = biobankNames.get(0);
			tupleWriter.write(new ValueTuple(biobankNames));
			biobankNames.remove(0);

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
							maunalMappings.get(variableName).put(biobank.toLowerCase(), new ArrayList<String>());
						}
						maunalMappings.get(variableName).get(biobank.toLowerCase())
								.addAll(Arrays.asList(mappingString.split(",")));
					}
				}
			}

			Iterable<DataSet> dataSets = dataService.findAll(DataSet.ENTITY_NAME,
					new QueryImpl().in(DataSet.NAME, biobankNames));

			for (Entry<String, Map<String, List<String>>> entry : maunalMappings.entrySet())
			{
				String variableName = entry.getKey();
				List<String> ranks = new ArrayList<String>();
				ranks.add(variableName);
				Map<String, List<String>> mappingDetail = entry.getValue();
				List<ObservableFeature> features = dataService.findAllAsList(ObservableFeature.ENTITY_NAME,
						new QueryImpl().eq(ObservableFeature.NAME, variableName));

				if (!features.isEmpty())
				{
					for (DataSet dataSet : dataSets)
					{
						StringBuilder outputRank = new StringBuilder();
						if (mappingDetail.containsKey(dataSet.getName().toLowerCase()))
						{
							List<Integer> mappedFeatureIds = findFeaturesFromIndex(
									mappingDetail.get(dataSet.getName().toLowerCase()), dataSet.getId());

							String mappingDataSetIdentifier = selectedDataSet + "-" + dataSet.getId();

							Query q = new QueryImpl().eq("store_mapping_feature", features.get(0).getId()).sort(
									new Sort(Direction.DESC, "store_mapping_score"));

							SearchRequest searchRequest = new SearchRequest(mappingDataSetIdentifier, q, null);
							SearchResult result = searchService.search(searchRequest);

							double previousScore = -1;
							int rank = 0;
							for (Hit hit : result.getSearchHits())
							{
								Map<String, Object> columnValueMap = hit.getColumnValueMap();
								String mappedFeatureId = columnValueMap.get("store_mapping_mapped_feature").toString();
								String score = columnValueMap.get("store_mapping_score").toString();

								if (previousScore != Double.parseDouble(score))
								{
									previousScore = Double.parseDouble(score);
									rank++;
								}
								if (mappedFeatureIds.contains(Integer.parseInt(mappedFeatureId)))
								{
									if (outputRank.length() != 0) outputRank.append(',');
									outputRank.append(rank);
								}
							}
						}
						ranks.add(outputRank.toString());
					}
				}
				tupleWriter.write(new ValueTuple(ranks));
			}
		}
		finally
		{
			if (reader != null) reader.close();
			if (tupleWriter != null) IOUtils.closeQuietly(tupleWriter);
		}
		return init(null, model);
	}

	private List<Integer> findFeaturesFromIndex(List<String> featureNames, Integer dataSetId)
	{
		QueryImpl q = new QueryImpl();
		q.pageSize(10000);

		for (String featureName : featureNames)
		{
			if (q.getRules().size() > 0) q.addRule(new QueryRule(Operator.OR));
			q.addRule(new QueryRule("name", Operator.EQUALS, featureName));
		}

		SearchResult result = searchService.search(new SearchRequest("protocolTree-" + dataSetId, q, null));

		List<Integer> featureIds = new ArrayList<Integer>();
		for (Hit hit : result.getSearchHits())
		{
			String featureId = hit.getColumnValueMap().get("id").toString();
			featureIds.add(Integer.parseInt(featureId));
		}
		return featureIds;
	}

	@RequestMapping(value = "/download", method = RequestMethod.POST)
	public void download(@RequestParam("request")
	String requestString, HttpServletResponse response) throws IOException, TableException
	{
		requestString = URLDecoder.decode(requestString, "UTF-8");
		logger.info("Download request: [" + requestString + "]");

		UpdateIndexRequest request = new GsonHttpMessageConverter().getGson().fromJson(requestString,
				UpdateIndexRequest.class);
		response.setContentType("text/csv");
		response.addHeader("Content-Disposition", "attachment; filename=" + getCsvFileName(request.getDocumentType()));

		TupleWriter tupleWriter = null;
		try
		{
			Set<Integer> featureIds = new HashSet<Integer>();
			tupleWriter = new CsvWriter(response.getWriter());
			Integer selectedDataSetId = request.getDataSetId();
			DataSet mappingDataSet = dataService.findOne(DataSet.ENTITY_NAME, selectedDataSetId);
			List<DataSet> storeMappingDataSet = dataService.findAllAsList(DataSet.ENTITY_NAME,
					new QueryImpl().like(DataSet.IDENTIFIER, selectedDataSetId.toString()));

			List<String> dataSetNames = new ArrayList<String>();
			dataSetNames.add(mappingDataSet.getName());
			Map<Integer, Map<Integer, MappingClass>> dataSetMappings = new HashMap<Integer, Map<Integer, MappingClass>>();

			for (DataSet dataSet : storeMappingDataSet)
			{
				Integer mappedDataSetId = Integer.parseInt(dataSet.getIdentifier().split("-")[2]);
				if (dataSet.getIdentifier().startsWith(SecurityUtils.getCurrentUsername() + "-" + selectedDataSetId)
						&& !mappedDataSetId.equals(selectedDataSetId))
				{
					DataSet mappedDataSet = dataService.findOne(DataSet.ENTITY_NAME, mappedDataSetId);
					dataSetNames.add(mappedDataSet.getName());

					SearchRequest searchRequest = new SearchRequest(dataSet.getIdentifier(),
							new QueryImpl().pageSize(1000000), null);

					SearchResult result = searchService.search(searchRequest);

					Map<Integer, MappingClass> storeMappings = new HashMap<Integer, MappingClass>();
					for (Hit hit : result.getSearchHits())
					{
						Map<String, Object> map = hit.getColumnValueMap();
						Integer storeMappingFeatureId = Integer.parseInt(map.get(STORE_MAPPING_FEATURE).toString());
						Integer storeMappingMappedFeatureId = Integer.parseInt(map.get(STORE_MAPPING_MAPPED_FEATURE)
								.toString());
						boolean confirmation = (Boolean) map.get(STORE_MAPPING_CONFIRM_MAPPING);
						if (!storeMappings.containsKey(storeMappingFeatureId)) storeMappings.put(storeMappingFeatureId,
								new MappingClass());
						storeMappings.get(storeMappingFeatureId).addMapping(storeMappingFeatureId,
								storeMappingMappedFeatureId, confirmation);
						featureIds.add(storeMappingFeatureId);
						featureIds.add(storeMappingMappedFeatureId);
					}
					dataSetMappings.put(mappedDataSetId, storeMappings);
				}
			}

			Map<Integer, ObservableFeature> featureMap = new HashMap<Integer, ObservableFeature>();
			Iterable<ObservableFeature> features = dataService.findAll(ObservableFeature.ENTITY_NAME,
					new QueryImpl().in(ObservableFeature.ID, new ArrayList<Integer>(featureIds)));
			for (ObservableFeature feature : features)
			{
				featureMap.put(feature.getId(), feature);
			}

			tupleWriter.write(new ValueTuple(dataSetNames));

			SearchRequest searchFeatures = new SearchRequest("protocolTree-" + selectedDataSetId,
					new QueryImpl().pageSize(1000000), null);

			SearchResult featureSearchResult = searchService.search(searchFeatures);
			for (Hit hit : featureSearchResult.getSearchHits())
			{
				List<String> values = new ArrayList<String>();
				Map<String, Object> map = hit.getColumnValueMap();
				String featureName = map.get(FEATURE_NAME).toString();
				Integer featureId = Integer.parseInt(map.get(FEATURE_ID).toString());
				values.add(featureName);
				for (DataSet dataSet : storeMappingDataSet)
				{
					Integer mappedDataSetId = Integer.parseInt(dataSet.getIdentifier().split("-")[2]);
					if (!mappedDataSetId.equals(selectedDataSetId))
					{
						StringBuilder value = new StringBuilder();
						Map<Integer, MappingClass> storeMappings = dataSetMappings.get(mappedDataSetId);
						if (storeMappings.containsKey(featureId))
						{
							List<Integer> candidateIds = null;
							MappingClass mappingClass = storeMappings.get(featureId);
							if (mappingClass.isConfirmation()) candidateIds = mappingClass.getFinalizedMapping();
							else candidateIds = mappingClass.getCandidateMappings();
							for (Integer id : candidateIds)
							{
								if (featureMap.containsKey(id))
								{
									value.append(featureMap.get(id).getName());
									value.append(',');
								}
							}
						}
						values.add(value.toString());
					}
				}
				tupleWriter.write(new ValueTuple(values));
			}
		}
		finally
		{
			IOUtils.closeQuietly(tupleWriter);
		}
	}

	private String getCsvFileName(String dataSetName)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dataSetName + "_" + dateFormat.format(new Date()) + ".csv";
	}

	static class MappingClass
	{
		private Integer featureId = null;
		private boolean confirmation = false;
		private List<Integer> finalizedMapping = null;
		private List<Integer> candidateMappings = null;

		public void addMapping(Integer featureId, Integer singleMappedFeatureId, boolean confirmation)
		{
			if (this.featureId == null) this.featureId = featureId;
			if (this.finalizedMapping == null) this.finalizedMapping = new ArrayList<Integer>();
			if (this.candidateMappings == null) this.candidateMappings = new ArrayList<Integer>();
			this.confirmation = confirmation;
			this.candidateMappings.add(singleMappedFeatureId);
			if (confirmation) finalizedMapping.add(singleMappedFeatureId);
		}

		public Integer getFeatureId()
		{
			return featureId;
		}

		public boolean isConfirmation()
		{
			return confirmation;
		}

		public List<Integer> getFinalizedMapping()
		{
			return finalizedMapping;
		}

		public List<Integer> getCandidateMappings()
		{
			return candidateMappings;
		}
	}
}