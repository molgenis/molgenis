package org.molgenis.omx.biobankconnect.wizard;

import static org.molgenis.omx.biobankconnect.wizard.BiobankConnectController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.biobankconnect.ontologyannotator.OntologyAnnotator;
import org.molgenis.omx.biobankconnect.ontologyannotator.UpdateIndexRequest;
import org.molgenis.omx.biobankconnect.ontologymatcher.OntologyMatcher;
import org.molgenis.omx.biobankconnect.ontologymatcher.OntologyMatcherRequest;
import org.molgenis.omx.biobankconnect.ontologymatcher.OntologyMatcherResponse;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.search.SearchService;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.ui.wizard.AbstractWizardController;
import org.molgenis.ui.wizard.Wizard;
import org.molgenis.util.FileUploadUtils;
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

@Controller
@RequestMapping(URI)
public class BiobankConnectController extends AbstractWizardController
{
	private static final Logger logger = Logger.getLogger(BiobankConnectController.class);

	public static final String ID = "biobankconnect";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final ChooseCataloguePage chooseCataloguePager;
	private final OntologyAnnotatorPage ontologyAnnotatorPager;
	private final OntologyMatcherPage ontologyMatcherPager;
	private final MappingManagerPage mappingManagerPager;
	private final ProgressingBarPage progressingBarPager;

	private BiobankConnectWizard wizard;
	private static final String PROTOCOL_IDENTIFIER = "store_mapping";
	private static final String VIEW_NAME = "view-wizard";

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
			MappingManagerPage mappingManagerPager, ProgressingBarPage progressingBarPager)
	{
		super(URI, "biobankconnect");
		if (chooseCataloguePager == null) throw new IllegalArgumentException("ChooseCataloguePager is null");
		if (ontologyAnnotatorPager == null) throw new IllegalArgumentException("OntologyAnnotatorPager is null");
		if (ontologyMatcherPager == null) throw new IllegalArgumentException("OntologyMatcherPager is null");
		if (mappingManagerPager == null) throw new IllegalArgumentException("MappingManagerPager is null");
		if (progressingBarPager == null) throw new IllegalArgumentException("ProgressingBarPager is null");
		this.chooseCataloguePager = chooseCataloguePager;
		this.ontologyAnnotatorPager = ontologyAnnotatorPager;
		this.ontologyMatcherPager = ontologyMatcherPager;
		this.mappingManagerPager = mappingManagerPager;
		this.progressingBarPager = progressingBarPager;
		this.wizard = new BiobankConnectWizard();
	}

	@Override
	@RequestMapping(value = "/**", method = GET)
	public String init(HttpServletRequest request)
	{
		List<DataSet> dataSets = new ArrayList<DataSet>();

		Iterable<DataSet> allDataSets = dataService.findAll(DataSet.ENTITY_NAME);
		for (DataSet dataSet : allDataSets)
		{
			if (!dataSet.getProtocolUsed().getIdentifier().equals(PROTOCOL_IDENTIFIER)) dataSets.add(dataSet);
		}
		wizard.setDataSets(dataSets);
		currentUserStatus.setUserLoggedIn(userAccountService.getCurrentUser().getUsername(),
				request.getRequestedSessionId());

		return VIEW_NAME;
	}

	@Override
	protected Wizard createWizard()
	{
		wizard = new BiobankConnectWizard();
		List<DataSet> dataSets = new ArrayList<DataSet>();
		Iterable<DataSet> allDataSets = dataService.findAll(DataSet.ENTITY_NAME);
		for (DataSet dataSet : allDataSets)
		{
			if (!dataSet.getProtocolUsed().getIdentifier().equals(PROTOCOL_IDENTIFIER)) dataSets.add(dataSet);
		}
		wizard.setDataSets(dataSets);
		wizard.setUserName(userAccountService.getCurrentUser().getUsername());
		wizard.addPage(chooseCataloguePager);
		wizard.addPage(ontologyAnnotatorPager);
		wizard.addPage(ontologyMatcherPager);
		wizard.addPage(progressingBarPager);
		wizard.addPage(mappingManagerPager);
		return wizard;
	}

	@RequestMapping(value = "/uploadfeatures", method = RequestMethod.POST, headers = "Content-Type=multipart/form-data")
	public String importFeatures(@RequestParam
	String dataSetName, @RequestParam
	Part file, HttpServletRequest request, Model model) throws IOException
	{
		File uploadFile = FileUploadUtils.saveToTempFolder(file);
		String message = ontologyAnnotator.uploadFeatures(uploadFile, dataSetName);

		BiobankConnectWizard biobankConnectWizard = (BiobankConnectWizard) request.getSession().getAttribute(
				"biobankconnect");
		List<DataSet> dataSets = new ArrayList<DataSet>();
		Iterable<DataSet> allDataSets = dataService.findAll(DataSet.ENTITY_NAME, new QueryImpl());
		for (DataSet dataSet : allDataSets)
		{
			if (!dataSet.getProtocolUsed().getIdentifier().equals(PROTOCOL_IDENTIFIER)) dataSets.add(dataSet);
		}
		biobankConnectWizard.setDataSets(dataSets);
		logger.error(message);
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
				documentTypes.add("ontologyTerm-" + ontologyUri);
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
	public OntologyMatcherResponse rematch(@RequestBody
	OntologyMatcherRequest request)
	{
		String userName = userAccountService.getCurrentUser().getUsername();
		ontologyMatcher.match(userName, request.getSourceDataSetId(), request.getSelectedDataSetIds(),
				request.getFeatureId());
		OntologyMatcherResponse response = new OntologyMatcherResponse(null, ontologyMatcher.isRunning(),
				ontologyMatcher.matchPercentage(userName), null);
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

	@RequestMapping(method = RequestMethod.GET, value = "/match/status", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public OntologyMatcherResponse checkMatch()
	{
		String userName = userAccountService.getCurrentUser().getUsername();
		OntologyMatcherResponse response = new OntologyMatcherResponse(currentUserStatus.getUserCurrentStage(userName),
				currentUserStatus.isUserMatching(userName), ontologyMatcher.matchPercentage(userName),
				currentUserStatus.getTotalNumberOfUsers());
		return response;
	}

	@Override
	@ModelAttribute("javascripts")
	public List<String> getJavascripts()
	{
		return Arrays.asList("bootstrap-fileupload.min.js", "jquery-ui-1.9.2.custom.min.js", "common-component.js",
				"catalogue-chooser.js", "ontology-annotator.js", "ontology-matcher.js", "mapping-manager.js",
				"simple_statistics.js", "biobank-connect.js");
	}

	@Override
	@ModelAttribute("stylesheets")
	public List<String> getStylesheets()
	{
		return Arrays.asList("bootstrap-fileupload.min.css", "jquery-ui-1.9.2.custom.min.css", "biobank-connect.css",
				"catalogue-chooser.css", "ontology-matcher.css", "mapping-manager.css", "ontology-annotator.css");
	}
}