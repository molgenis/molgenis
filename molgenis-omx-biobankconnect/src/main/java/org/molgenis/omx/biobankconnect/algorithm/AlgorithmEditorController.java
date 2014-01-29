package org.molgenis.omx.biobankconnect.algorithm;

import static org.molgenis.omx.biobankconnect.algorithm.AlgorithmEditorController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.molgenis.data.DataService;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.biobankconnect.ontologymatcher.OntologyMatcher;
import org.molgenis.omx.biobankconnect.ontologymatcher.OntologyMatcherRequest;
import org.molgenis.omx.biobankconnect.wizard.BiobankConnectWizard;
import org.molgenis.omx.biobankconnect.wizard.ChooseCataloguePage;
import org.molgenis.omx.biobankconnect.wizard.CurrentUserStatus;
import org.molgenis.omx.biobankconnect.wizard.OntologyAnnotatorPage;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchResult;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.ui.wizard.AbstractWizardController;
import org.molgenis.ui.wizard.Wizard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(URI)
public class AlgorithmEditorController extends AbstractWizardController
{

	public static final String ID = "algorithm";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String PROTOCOL_IDENTIFIER = "store_mapping";

	@Autowired
	private DataService dataService;
	@Autowired
	private OntologyMatcher ontologyMatcher;
	@Autowired
	private UserAccountService userAccountService;
	@Autowired
	private CurrentUserStatus currentUserStatus;

	private BiobankConnectWizard wizard;
	private final AlgorithmEditorPage algorithmEditorPage;
	private final ChooseBiobankPage chooseBiobanksPage;
	private final ChooseCataloguePage chooseCataloguePage;
	private final OntologyAnnotatorPage ontologyAnnotatorPage;

	@Autowired
	public AlgorithmEditorController(AlgorithmEditorPage algorithmEditorPage, ChooseBiobankPage chooseBiobanksPage,
			ChooseCataloguePage chooseCataloguePage, OntologyAnnotatorPage ontologyAnnotatorPage)
	{
		super(URI, ID);
		if (algorithmEditorPage == null) throw new IllegalArgumentException("algorithmEditorPage is null!");
		if (chooseBiobanksPage == null) throw new IllegalArgumentException("chooseBiobanksPage is null!");
		if (chooseCataloguePage == null) throw new IllegalArgumentException("chooseCataloguePage is null!");
		if (ontologyAnnotatorPage == null) throw new IllegalArgumentException("ontologyAnnotatorPage is null!");
		this.algorithmEditorPage = algorithmEditorPage;
		this.chooseBiobanksPage = chooseBiobanksPage;
		this.chooseCataloguePage = chooseCataloguePage;
		this.ontologyAnnotatorPage = ontologyAnnotatorPage;
	}

	@Override
	public void onInit(HttpServletRequest request)
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
		wizard.addPage(chooseCataloguePage);
		wizard.addPage(ontologyAnnotatorPage);
		wizard.addPage(chooseBiobanksPage);
		wizard.addPage(algorithmEditorPage);
		return wizard;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/createmapping", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public SearchResult createMappings(@RequestBody
	OntologyMatcherRequest request)
	{
		String userName = userAccountService.getCurrentUser().getUsername();
		List<Integer> selectedDataSetIds = request.getSelectedDataSetIds();
		if (selectedDataSetIds.size() > 0)
		{
			return ontologyMatcher.generateMapping(userName, request.getSourceDataSetId(), selectedDataSetIds.get(0),
					request.getFeatureId());
		}
		return new SearchResult(0, Collections.<Hit> emptyList());
	}

	@Override
	@ModelAttribute("javascripts")
	public List<String> getJavascripts()
	{
		return Arrays.asList("bootstrap-fileupload.min.js", "jquery-ui-1.9.2.custom.min.js", "common-component.js",
				"catalogue-chooser.js", "ontology-annotator.js", "ontology-matcher.js", "mapping-manager.js",
				"simple_statistics.js", "algorithm-editor.js", "biobank-connect.js");
	}

	@Override
	@ModelAttribute("stylesheets")
	public List<String> getStylesheets()
	{
		return Arrays.asList("bootstrap-fileupload.min.css", "jquery-ui-1.9.2.custom.min.css", "biobank-connect.css",
				"catalogue-chooser.css", "ontology-matcher.css", "mapping-manager.css", "ontology-annotator.css",
				"algorithm-editor.css");
	}
}