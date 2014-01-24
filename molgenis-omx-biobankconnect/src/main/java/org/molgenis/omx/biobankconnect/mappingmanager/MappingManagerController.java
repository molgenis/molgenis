package org.molgenis.omx.biobankconnect.mappingmanager;

import static org.molgenis.omx.biobankconnect.mappingmanager.MappingManagerController.URI;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Writable;
import org.molgenis.data.csv.CsvWriter;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.biobankconnect.ontologyannotator.UpdateIndexRequest;
import org.molgenis.omx.biobankconnect.ontologymatcher.OntologyMatcher;
import org.molgenis.omx.biobankconnect.wizard.CurrentUserStatus;
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
	private CurrentUserStatus currentUserStatus;

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
	String selectedDataSetId, HttpServletRequest request, Model model)
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
		currentUserStatus.setUserLoggedIn(userAccountService.getCurrentUser().getUsername(),
				request.getRequestedSessionId());

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

	@RequestMapping(value = "/download", method = RequestMethod.POST)
	public void download(@RequestParam("request")
	String requestString, HttpServletResponse response) throws IOException
	{
		requestString = URLDecoder.decode(requestString, "UTF-8");
		logger.info("Download request: [" + requestString + "]");

		UpdateIndexRequest request = new GsonHttpMessageConverter().getGson().fromJson(requestString,
				UpdateIndexRequest.class);
		response.setContentType("text/csv");
		response.addHeader("Content-Disposition", "attachment; filename=" + getCsvFileName(request.getDocumentType()));

		Writable writer = null;
		try
		{
			Set<Integer> featureIds = new HashSet<Integer>();
			Integer selectedDataSetId = request.getDataSetId();
			DataSet mappingDataSet = dataService.findOne(DataSet.ENTITY_NAME, selectedDataSetId);
			@SuppressWarnings("deprecation")
			List<DataSet> storeMappingDataSet = dataService.findAllAsList(DataSet.ENTITY_NAME,
					new QueryImpl().like(DataSet.IDENTIFIER, selectedDataSetId.toString()));
			List<String> dataSetNames = new ArrayList<String>();
			dataSetNames.add(mappingDataSet.getName());
			Map<Integer, Map<Integer, MappingClass>> dataSetMappings = new HashMap<Integer, Map<Integer, MappingClass>>();
			if (storeMappingDataSet.size() > 0)
			{
				for (DataSet dataSet : storeMappingDataSet)
				{
					Integer mappedDataSetId = Integer.parseInt(dataSet.getIdentifier().split("-")[2]);
					if (dataSet.getIdentifier().startsWith(
							userAccountService.getCurrentUser().getUsername() + "-" + selectedDataSetId)
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
							Integer storeMappingMappedFeatureId = Integer.parseInt(map
									.get(STORE_MAPPING_MAPPED_FEATURE).toString());
							boolean confirmation = (Boolean) map.get(STORE_MAPPING_CONFIRM_MAPPING);
							if (!storeMappings.containsKey(storeMappingFeatureId)) storeMappings.put(
									storeMappingFeatureId, new MappingClass());
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

				writer = new CsvWriter<Entity>(response.getWriter(), dataSetNames);

				SearchRequest searchFeatures = new SearchRequest("protocolTree-" + selectedDataSetId,
						new QueryImpl().pageSize(1000000), null);
				SearchResult featureSearchResult = searchService.search(searchFeatures);
				for (Hit hit : featureSearchResult.getSearchHits())
				{
					Entity entity = new MapEntity();
					Map<String, Object> map = hit.getColumnValueMap();
					String featureName = map.get(FEATURE_NAME).toString();
					Integer featureId = Integer.parseInt(map.get(FEATURE_ID).toString());
					entity.set(dataSetNames.get(0), featureName);
					int i = 1;
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
										if (value.length() > 0) value.append(',').append('\r');
										value.append(featureMap.get(id).getName()).append(':')
												.append(featureMap.get(id).getDescription());
									}
								}
							}
							entity.set(dataSetNames.get(i++), value.toString());
						}
					}
					writer.add(entity);
				}
			}
		}
		finally
		{
			IOUtils.closeQuietly(writer);
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
		private List<Integer> finalizedMapping = null;
		private List<Integer> candidateMappings = null;

		public void addMapping(Integer featureId, Integer singleMappedFeatureId, boolean confirmation)
		{
			if (this.featureId == null) this.featureId = featureId;
			if (this.finalizedMapping == null) this.finalizedMapping = new ArrayList<Integer>();
			if (this.candidateMappings == null) this.candidateMappings = new ArrayList<Integer>();
			this.candidateMappings.add(singleMappedFeatureId);
			if (confirmation) finalizedMapping.add(singleMappedFeatureId);
		}

		public Integer getFeatureId()
		{
			return featureId;
		}

		public boolean isConfirmation()
		{
			return finalizedMapping.size() > 0;
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