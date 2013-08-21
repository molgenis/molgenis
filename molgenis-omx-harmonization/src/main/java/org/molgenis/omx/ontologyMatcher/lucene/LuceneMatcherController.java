package org.molgenis.omx.ontologyMatcher.lucene;

import static org.molgenis.omx.ontologyMatcher.lucene.LuceneMatcherController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.io.TupleWriter;
import org.molgenis.io.csv.CsvWriter;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.molgenis.util.GsonHttpMessageConverter;
import org.molgenis.util.tuple.ValueTuple;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(URI)
public class LuceneMatcherController implements InitializingBean
{

	public static final String URI = "/matcher";
	private static final Logger logger = Logger.getLogger(LuceneMatcherController.class);
	private static final String FEATURE_NAME = "name";
	private static final String STORE_MAPPING_FEATURE = "store_mapping_feature";
	private static final String STORE_MAPPING_MAPPED_FEATURE = "store_mapping_mapped_feature";
	private static final String STORE_MAPPING_CONFIRM_MAPPING = "store_mapping_confirm_mapping";

	@Autowired
	private SearchService searchService;
	@Autowired
	private Database database;

	@Override
	public void afterPropertiesSet() throws Exception
	{
		if (searchService == null) throw new IllegalArgumentException("Missing bean of type SearchService");
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
			tupleWriter = new CsvWriter(response.getWriter());
			Integer dataSetId = request.getDataSetId();
			DataSet mappingDataSet = database.findById(DataSet.class, dataSetId);
			List<DataSet> storeMappingDataSet = database.find(DataSet.class, new QueryRule(DataSet.IDENTIFIER,
					Operator.LIKE, "" + dataSetId));
			List<String> dataSetNames = new ArrayList<String>();
			dataSetNames.add(mappingDataSet.getName());
			Map<Integer, Map<String, MappingClass>> dataSetMappings = new HashMap<Integer, Map<String, MappingClass>>();
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
					Map<String, MappingClass> storeMappings = new HashMap<String, MappingClass>();
					for (Hit hit : result.getSearchHits())
					{
						Map<String, Object> map = hit.getColumnValueMap();
						String storeMappingFeature = map.get(STORE_MAPPING_FEATURE).toString();
						String storeMappingMappedFeature = map.get(STORE_MAPPING_MAPPED_FEATURE).toString();
						boolean confirmation = (Boolean) map.get(STORE_MAPPING_CONFIRM_MAPPING);
						if (!storeMappings.containsKey(storeMappingFeature)) storeMappings.put(storeMappingFeature,
								new MappingClass());
						storeMappings.get(storeMappingFeature).addMapping(storeMappingFeature,
								storeMappingMappedFeature, confirmation);
					}
					dataSetMappings.put(mappedDataSetId, storeMappings);
				}
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
				values.add(featureName);
				for (DataSet dataSet : storeMappingDataSet)
				{
					Integer mappedDataSetId = Integer.parseInt(dataSet.getIdentifier().split("-")[1]);
					if (!mappedDataSetId.equals(dataSetId))
					{
						String value = null;
						Map<String, MappingClass> storeMappings = dataSetMappings.get(mappedDataSetId);
						if (storeMappings.containsKey(featureName))
						{
							MappingClass mappingClass = storeMappings.get(featureName);
							if (mappingClass.isConfirmation()) value = mappingClass.getFinalizedMapping();
							else value = StringUtils.join(mappingClass.getCandidateMappings(), " , ");
						}
						values.add(value);
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
		private String featureName = null;
		private boolean confirmation = false;
		private List<String> finalizedMapping = null;
		private List<String> candidateMappings = null;

		public void addMapping(String featureName, String singleMappedFeature, boolean confirmation)
		{
			if (this.featureName == null) this.featureName = featureName;
			if (this.finalizedMapping == null) this.finalizedMapping = new ArrayList<String>();
			if (this.candidateMappings == null) this.candidateMappings = new ArrayList<String>();
			this.confirmation = confirmation;
			this.candidateMappings.add(singleMappedFeature);
			if (confirmation) finalizedMapping.add(singleMappedFeature);
		}

		public String getFeatureName()
		{
			return featureName;
		}

		public boolean isConfirmation()
		{
			return confirmation;
		}

		public String getFinalizedMapping()
		{
			return StringUtils.join(finalizedMapping, " , ");
		}

		public List<String> getCandidateMappings()
		{
			return candidateMappings;
		}
	}
}