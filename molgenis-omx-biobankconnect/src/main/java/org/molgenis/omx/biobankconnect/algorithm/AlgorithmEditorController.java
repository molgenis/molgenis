package org.molgenis.omx.biobankconnect.algorithm;

import static org.molgenis.omx.biobankconnect.algorithm.AlgorithmEditorController.URI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.molgenis.data.DataService;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.biobankconnect.ontologymatcher.OntologyMatcher;
import org.molgenis.omx.biobankconnect.wizard.BiobankConnectWizard;
import org.molgenis.omx.biobankconnect.wizard.ChooseCataloguePage;
import org.molgenis.omx.biobankconnect.wizard.CurrentUserStatus;
import org.molgenis.omx.biobankconnect.wizard.OntologyAnnotatorPage;
import org.molgenis.omx.biobankconnect.wizard.OntologyMatcherPage;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.ui.wizard.AbstractWizardController;
import org.molgenis.ui.wizard.Wizard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

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
	private final OntologyMatcherPage ontologyMatcherPage;
	private final ChooseCataloguePage chooseCataloguePage;
	private final OntologyAnnotatorPage ontologyAnnotatorPage;

	@Autowired
	public AlgorithmEditorController(AlgorithmEditorPage algorithmEditorPage, OntologyMatcherPage ontologyMatcherPage,
			ChooseCataloguePage chooseCataloguePage, OntologyAnnotatorPage ontologyAnnotatorPage)
	{
		super(URI, ID);
		if (algorithmEditorPage == null) throw new IllegalArgumentException("algorithmEditorPage is null!");
		if (ontologyMatcherPage == null) throw new IllegalArgumentException("algorithmSourcePage is null!");
		if (chooseCataloguePage == null) throw new IllegalArgumentException("chooseCataloguePage is null!");
		if (ontologyAnnotatorPage == null) throw new IllegalArgumentException("ontologyAnnotatorPage is null!");
		this.algorithmEditorPage = algorithmEditorPage;
		this.ontologyMatcherPage = ontologyMatcherPage;
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
		wizard.addPage(ontologyMatcherPage);
		wizard.addPage(algorithmEditorPage);
		return wizard;
	}

	@Override
	@ModelAttribute("javascripts")
	public List<String> getJavascripts()
	{
		return Arrays.asList("bootstrap-fileupload.min.js", "jquery-ui-1.9.2.custom.min.js", "common-component.js",
				"catalogue-chooser.js", "ontology-annotator.js", "ontology-matcher.js", "mapping-manager.js",
				"simple_statistics.js", "biobank-connect.js", "algorithm-editor.js");
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