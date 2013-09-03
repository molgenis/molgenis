package org.molgenis.omx.harmonization.controllers;

import static org.molgenis.omx.harmonization.controllers.OntologyMatcherController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.io.TupleWriter;
import org.molgenis.io.csv.CsvWriter;
import org.molgenis.omx.harmonization.ontologymatcher.OntologyMatcherDeleteRequest;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.molgenis.util.GsonHttpMessageConverter;
import org.molgenis.util.tuple.ValueTuple;
import org.springframework.beans.factory.annotation.Autowired;
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
public class OntologyMatcherController extends MolgenisPlugin
{
	public static final String URI = MolgenisPlugin.PLUGIN_URI_PREFIX + "ontologymatcher";
	private static final Logger logger = Logger.getLogger(OntologyMatcherController.class);
	private static final String FEATURE_ID = "id";
	private static final String FEATURE_NAME = "name";
	private static final String PROTOCOL_IDENTIFIER = "store_mapping";
	private static final String STORE_MAPPING_FEATURE = "store_mapping_feature";
	private static final String STORE_MAPPING_MAPPED_FEATURE = "store_mapping_mapped_feature";
	private static final String STORE_MAPPING_CONFIRM_MAPPING = "store_mapping_confirm_mapping";

	@Autowired
	private SearchService searchService;
	@Autowired
	private Database database;

	public OntologyMatcherController()
	{
		super(URI);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model) throws DatabaseException
	{
		List<DataSet> dataSets = new ArrayList<DataSet>();
		for (DataSet dataSet : database.find(DataSet.class))
		{
			if (!dataSet.getProtocolUsed_Identifier().equals(PROTOCOL_IDENTIFIER)) dataSets.add(dataSet);
		}
		model.addAttribute("dataSets", dataSets);

		return "OntologyMatcherPlugin";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/delete", consumes = APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteDocuments(@RequestBody
	OntologyMatcherDeleteRequest request)
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
	OntologyMatcherDeleteRequest request)
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

	@RequestMapping(value = "/download", method = RequestMethod.POST)
	public void download(@RequestParam("request")
	String requestString, HttpServletResponse response) throws IOException, DatabaseException, TableException
	{
		requestString = URLDecoder.decode(requestString, "UTF-8");
		logger.info("Download request: [" + requestString + "]");

		OntologyMatcherDeleteRequest request = new GsonHttpMessageConverter().getGson().fromJson(requestString,
				OntologyMatcherDeleteRequest.class);
		response.setContentType("text/csv");
		response.addHeader("Content-Disposition", "attachment; filename=" + getCsvFileName(request.getDocumentType()));

		TupleWriter tupleWriter = null;
		try
		{
			Set<Integer> featureIds = new HashSet<Integer>();
			tupleWriter = new CsvWriter(response.getWriter());
			Integer dataSetId = request.getDataSetId();
			DataSet mappingDataSet = database.findById(DataSet.class, dataSetId);
			List<DataSet> storeMappingDataSet = database.find(DataSet.class, new QueryRule(DataSet.IDENTIFIER,
					Operator.LIKE, "" + dataSetId));
			List<String> dataSetNames = new ArrayList<String>();
			dataSetNames.add(mappingDataSet.getName());
			Map<Integer, Map<Integer, MappingClass>> dataSetMappings = new HashMap<Integer, Map<Integer, MappingClass>>();
			for (DataSet dataSet : storeMappingDataSet)
			{
				Integer mappedDataSetId = Integer.parseInt(dataSet.getIdentifier().split("-")[1]);
				if (!mappedDataSetId.equals(dataSetId))
				{
					DataSet mappedDataSet = database.findById(DataSet.class, mappedDataSetId);
					dataSetNames.add(mappedDataSet.getName());
					List<QueryRule> rules = new ArrayList<QueryRule>();
					rules.add(new QueryRule(QueryRule.Operator.LIMIT, 1000000));
					SearchRequest searchRequest = new SearchRequest(dataSet.getIdentifier(), rules, null);
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
			for (ObservableFeature feature : database.find(ObservableFeature.class, new QueryRule(ObservableFeature.ID,
					Operator.IN, new ArrayList<Integer>(featureIds))))
			{
				featureMap.put(feature.getId(), feature);
			}

			tupleWriter.write(new ValueTuple(dataSetNames));

			List<QueryRule> rules = new ArrayList<QueryRule>();
			rules.add(new QueryRule(QueryRule.Operator.LIMIT, 1000000));
			SearchRequest searchFeatures = new SearchRequest("protocolTree-" + dataSetId, rules, null);
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
					Integer mappedDataSetId = Integer.parseInt(dataSet.getIdentifier().split("-")[1]);
					if (!mappedDataSetId.equals(dataSetId))
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

	class MappingClass
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