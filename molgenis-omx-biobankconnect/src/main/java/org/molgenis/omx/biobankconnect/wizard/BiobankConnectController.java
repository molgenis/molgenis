package org.molgenis.omx.biobankconnect.wizard;

import static org.molgenis.omx.biobankconnect.wizard.BiobankConnectController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Writable;
import org.molgenis.data.csv.CsvWriter;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.biobankconnect.algorithm.ApplyAlgorithms;
import org.molgenis.omx.biobankconnect.ontologyannotator.OntologyAnnotator;
import org.molgenis.omx.biobankconnect.ontologyannotator.UpdateIndexRequest;
import org.molgenis.omx.biobankconnect.ontologyindexer.AsyncOntologyIndexer;
import org.molgenis.omx.biobankconnect.ontologymatcher.AlgorithmGenerateResponse;
import org.molgenis.omx.biobankconnect.ontologymatcher.AsyncOntologyMatcher;
import org.molgenis.omx.biobankconnect.ontologymatcher.OntologyMatcher;
import org.molgenis.omx.biobankconnect.ontologymatcher.OntologyMatcherRequest;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchService;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.ui.wizard.AbstractWizardController;
import org.molgenis.ui.wizard.Wizard;
import org.molgenis.util.FileUploadUtils;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.common.collect.Iterables;

@Controller
@RequestMapping(URI)
public class BiobankConnectController extends AbstractWizardController
{
	public static final String ID = "biobankconnect";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final ChooseCataloguePage chooseCataloguePager;
	private final OntologyAnnotatorPage ontologyAnnotatorPager;
	private final OntologyMatcherPage ontologyMatcherPager;
	private final MappingManagerPage mappingManagerPager;
	private final AlgorithmReportPage algorithmReportPager;
	private BiobankConnectWizard wizard;

	@Autowired
	private DataService dataService;

	@Autowired
	private UserAccountService userAccountService;

	@Autowired
	private OntologyAnnotator ontologyAnnotator;

	@Autowired
	private OntologyMatcher ontologyMatcher;

	@Autowired
	private SearchService searchService;

	@Autowired
	private CurrentUserStatus currentUserStatus;

	@Autowired
	public BiobankConnectController(ChooseCataloguePage chooseCataloguePager,
			OntologyAnnotatorPage ontologyAnnotatorPager, OntologyMatcherPage ontologyMatcherPager,
			MappingManagerPage mappingManagerPager, AlgorithmReportPage algorithmReportPager)
	{
		super(URI, "biobankconnect");
		if (chooseCataloguePager == null) throw new IllegalArgumentException("ChooseCataloguePager is null");
		if (ontologyAnnotatorPager == null) throw new IllegalArgumentException("OntologyAnnotatorPager is null");
		if (ontologyMatcherPager == null) throw new IllegalArgumentException("OntologyMatcherPager is null");
		if (mappingManagerPager == null) throw new IllegalArgumentException("MappingManagerPager is null");
		if (algorithmReportPager == null) throw new IllegalArgumentException("AlgorithmGeneratorPage is null");
		this.chooseCataloguePager = chooseCataloguePager;
		this.ontologyAnnotatorPager = ontologyAnnotatorPager;
		this.ontologyMatcherPager = ontologyMatcherPager;
		this.mappingManagerPager = mappingManagerPager;
		this.algorithmReportPager = algorithmReportPager;
		this.wizard = new BiobankConnectWizard();
	}

	@Override
	public void onInit(HttpServletRequest request)
	{
		wizard.setDataSets(getBiobankDataSets());
		currentUserStatus.setUserLoggedIn(userAccountService.getCurrentUser().getUsername(),
				request.getRequestedSessionId());
	}

	@Override
	protected Wizard createWizard()
	{
		wizard = new BiobankConnectWizard();
		wizard.setDataSets(getBiobankDataSets());
		wizard.setUserName(userAccountService.getCurrentUser().getUsername());
		wizard.addPage(chooseCataloguePager);
		wizard.addPage(ontologyAnnotatorPager);
		wizard.addPage(ontologyMatcherPager);
		wizard.addPage(mappingManagerPager);
		wizard.addPage(algorithmReportPager);
		return wizard;
	}

	private List<DataSet> getBiobankDataSets()
	{
		List<DataSet> dataSets = new ArrayList<DataSet>();
		Iterable<DataSet> allDataSets = dataService.findAll(DataSet.ENTITY_NAME, DataSet.class);
		for (DataSet dataSet : allDataSets)
		{
			if (dataSet.getProtocolUsed().getIdentifier().equals(AsyncOntologyMatcher.PROTOCOL_IDENTIFIER)) continue;
			if (dataSet.getIdentifier().matches("^" + userAccountService.getCurrentUser().getUsername() + ".*derived$")) continue;
			dataSets.add(dataSet);
		}
		return dataSets;
	}

	@RequestMapping(value = "/uploadfeatures", method = RequestMethod.POST, headers = "Content-Type=multipart/form-data")
	public String importFeatures(@RequestParam
	String dataSetName, @RequestParam
	Part file, @ModelAttribute("wizard")
	BiobankConnectWizard biobankConnectWizard, HttpServletRequest request, Model model) throws IOException
	{
		File uploadFile = FileUploadUtils.saveToTempFolder(file);
		String message = ontologyAnnotator.uploadFeatures(uploadFile, dataSetName);

		List<DataSet> dataSets = new ArrayList<DataSet>();
		Iterable<DataSet> allDataSets = dataService.findAll(DataSet.ENTITY_NAME, DataSet.class);
		for (DataSet dataSet : allDataSets)
		{
			if (!dataSet.getProtocolUsed().getIdentifier().equals(AsyncOntologyMatcher.PROTOCOL_IDENTIFIER)) dataSets
					.add(dataSet);
		}
		biobankConnectWizard.setDataSets(dataSets);
		if (message.length() > 0) model.addAttribute("message", message);
		return init(request);
	}

	@RequestMapping(value = "/annotate", method = RequestMethod.POST)
	public String annotate(HttpServletRequest request)
	{
		ontologyAnnotator.removeAnnotations(wizard.getSelectedDataSet().getId());
		if (request.getParameter("selectedOntologies") != null)
		{
			List<String> documentTypes = new ArrayList<String>();
			for (String ontologyUri : request.getParameter("selectedOntologies").split(","))
			{
				documentTypes.add(AsyncOntologyIndexer.createOntologyTermDocumentType(ontologyUri));
			}
			ontologyAnnotator.annotate(wizard.getSelectedDataSet().getId(), documentTypes);
		}
		return init(request);
	}

	@RequestMapping(value = "/annotate/remove", method = RequestMethod.POST)
	public String removeAnnotations(HttpServletRequest request) throws Exception
	{
		ontologyAnnotator.removeAnnotations(wizard.getSelectedDataSet().getId());
		return init(request);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/annotate/update", consumes = APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateDocument(@RequestBody
	UpdateIndexRequest request)
	{
		ontologyAnnotator.updateIndex(request);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/rematch", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public AlgorithmGenerateResponse rematch(@RequestBody
	OntologyMatcherRequest request)
	{
		String userName = userAccountService.getCurrentUser().getUsername();
		ontologyMatcher.match(userName, request.getTargetDataSetId(), request.getSelectedDataSetIds(),
				request.getFeatureId());
		AlgorithmGenerateResponse response = new AlgorithmGenerateResponse(null, ontologyMatcher.isRunning(),
				ontologyMatcher.matchPercentage(userName), null, null, null, null);
		return response;
	}

	@RequestMapping(value = "/running", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Boolean> isRunning(HttpServletRequest request)
	{
		Map<String, Boolean> result = new HashMap<String, Boolean>();
		result.put(
				"isRunning",
				currentUserStatus.isUserLoggedIn(userAccountService.getCurrentUser().getUsername(),
						request.getRequestedSessionId()));
		return result;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/progress", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public AlgorithmGenerateResponse checkMatch()
	{
		String userName = userAccountService.getCurrentUser().getUsername();
		DataSet derivedDataSet = null;
		if (!currentUserStatus.isUserMatching(userName))
		{
			String deriveDataSetIdentifier = ApplyAlgorithms.createDerivedDataSetIdentifier(userName, wizard
					.getSelectedDataSet().getId().toString(), wizard.getSelectedBiobanks());
			derivedDataSet = dataService.findOne(DataSet.ENTITY_NAME,
					new QueryImpl().eq(DataSet.IDENTIFIER, deriveDataSetIdentifier), DataSet.class);

		}
		AlgorithmGenerateResponse response = new AlgorithmGenerateResponse(
				currentUserStatus.getUserCurrentStage(userName), currentUserStatus.isUserMatching(userName),
				ontologyMatcher.matchPercentage(userName), currentUserStatus.getTotalNumberOfUsers(), wizard
						.getSelectedDataSet().getId(), wizard.getSelectedBiobanks(),
				derivedDataSet != null ? derivedDataSet.getId() : null);
		return response;
	}

	@RequestMapping(value = "/download", method = RequestMethod.POST)
	public void download(@RequestParam("request")
	String requestString, HttpServletResponse response) throws IOException
	{
		requestString = URLDecoder.decode(requestString, "UTF-8");
		UpdateIndexRequest request = new GsonHttpMessageConverter().getGson().fromJson(requestString,
				UpdateIndexRequest.class);
		response.setContentType("text/csv");
		response.addHeader("Content-Disposition", "attachment; filename=" + getCsvFileName(request.getDocumentType()));

		Writable writer = null;
		try
		{
			Integer selectedDataSetId = request.getDataSetId();
			DataSet selectedDataSet = dataService.findOne(DataSet.ENTITY_NAME, selectedDataSetId, DataSet.class);
			List<String> mappingIdentifiers = new ArrayList<String>();
			for (Integer mappedDataSetId : request.getMatchedDataSetIds())
			{
				mappingIdentifiers.add(AsyncOntologyMatcher.createMappingDataSetIdentifier(userAccountService
						.getCurrentUser().getUsername(), selectedDataSetId, mappedDataSetId));
			}
			Iterable<DataSet> dataSetsStoringMappings = dataService.findAll(DataSet.ENTITY_NAME,
					new QueryImpl().in(DataSet.IDENTIFIER, mappingIdentifiers), DataSet.class);
			List<String> dataSetNames = new ArrayList<String>();
			dataSetNames.add(selectedDataSet.getName());

			Map<Integer, Map<String, String>> dataSetMappings = new HashMap<Integer, Map<String, String>>();

			if (Iterables.size(dataSetsStoringMappings) > 0)
			{
				for (DataSet dataSet : dataSetsStoringMappings)
				{
					Integer mappedDataSetId = Integer.parseInt(dataSet.getIdentifier().split("-")[2]);
					if (!mappedDataSetId.equals(selectedDataSetId))
					{
						DataSet mappedDataSet = dataService
								.findOne(DataSet.ENTITY_NAME, mappedDataSetId, DataSet.class);
						dataSetNames.add(mappedDataSet.getName());

						SearchRequest searchRequest = new SearchRequest(dataSet.getIdentifier(),
								new QueryImpl().pageSize(Integer.MAX_VALUE), null);

						Map<String, String> storeMappings = new HashMap<String, String>();

						for (Hit hit : searchService.search(searchRequest).getSearchHits())
						{
							Map<String, Object> columnValueMap = hit.getColumnValueMap();
							String featureId = columnValueMap.get(AsyncOntologyMatcher.STORE_MAPPING_FEATURE)
									.toString();
							String mappedFeatureIdsString = columnValueMap.get(
									AsyncOntologyMatcher.STORE_MAPPING_MAPPED_FEATURE).toString();
							StringBuilder mappedFeatureNames = new StringBuilder();
							if (mappedFeatureIdsString.length() > 2)
							{
								List<String> mappedFeatureIds = Arrays.asList(mappedFeatureIdsString.substring(1,
										mappedFeatureIdsString.length() - 1).split("\\s*,\\s*"));
								Iterable<ObservableFeature> mappedFeatures = dataService.findAll(
										ObservableFeature.ENTITY_NAME,
										new QueryImpl().in(ObservableFeature.ID, mappedFeatureIds),
										ObservableFeature.class);
								for (ObservableFeature feature : mappedFeatures)
								{
									if (mappedFeatureNames.length() > 0) mappedFeatureNames.append(";");
									mappedFeatureNames.append(feature.getName()).append(':')
											.append(feature.getDescription());
								}
							}
							storeMappings.put(featureId, mappedFeatureNames.toString());
						}
						dataSetMappings.put(mappedDataSetId, storeMappings);
					}
				}

				writer = new CsvWriter(response.getWriter(), dataSetNames);

				SearchRequest searchRequest = new SearchRequest("protocolTree-"
						+ selectedDataSet.getProtocolUsed().getId(), new QueryImpl().eq("type",
						ObservableFeature.class.getSimpleName().toLowerCase()).pageSize(Integer.MAX_VALUE), null);
				for (Hit hit : searchService.search(searchRequest).getSearchHits())
				{
					Entity entity = new MapEntity();
					Map<String, Object> columnValueMap = hit.getColumnValueMap();
					String featureName = columnValueMap.get(ObservableFeature.NAME.toLowerCase()).toString();
					String featureId = columnValueMap.get(ObservableFeature.ID.toLowerCase()).toString();
					entity.set(dataSetNames.get(0), featureName);
					int i = 1;
					for (DataSet dataSet : dataSetsStoringMappings)
					{
						Integer mappedDataSetId = Integer.parseInt(dataSet.getIdentifier().split("-")[2]);
						if (!mappedDataSetId.equals(selectedDataSetId))
						{
							StringBuilder value = new StringBuilder();
							Map<String, String> storeMappings = dataSetMappings.get(mappedDataSetId);
							if (storeMappings != null && storeMappings.containsKey(featureId))
							{
								value.append(storeMappings.get(featureId));
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
}