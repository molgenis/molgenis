package org.molgenis.omx.biobankconnect.wizard;

import static org.molgenis.omx.biobankconnect.wizard.BiobankConnectController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.biobankconnect.ontologyannotator.OntologyAnnotator;
import org.molgenis.omx.biobankconnect.ontologyannotator.UpdateIndexRequest;
import org.molgenis.omx.biobankconnect.ontologymatcher.OntologyMatcher;
import org.molgenis.omx.biobankconnect.ontologymatcher.OntologyMatcherResponse;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.ui.wizard.AbstractWizardController;
import org.molgenis.ui.wizard.Wizard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(URI)
public class BiobankConnectController extends AbstractWizardController
{
	private static final Logger logger = Logger.getLogger(BiobankConnectController.class);
	public static final String URI = "/plugin/biobankconnect";
	private final ChooseCataloguePage chooseCataloguePager;
	private final OntologyAnnotatorPage ontologyAnnotatorPager;
	private final OntologyMatcherPage ontologyMatcherPager;
	private final MappingManagerPage mappingManagerPager;
	private final ProgressingBarPage progressingBarPager;

	private BiobankConnectWizard wizard;

	private static final String PROTOCOL_IDENTIFIER = "store_mapping";

	@Autowired
	private Database database;

	@Autowired
	private OntologyAnnotator ontologyAnnotator;

	@Autowired
	private OntologyMatcher ontologyMatcher;

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
	protected Wizard createWizard()
	{
		List<DataSet> dataSets = new ArrayList<DataSet>();
		try
		{
			for (DataSet dataSet : database.find(DataSet.class))
			{
				if (!dataSet.getProtocolUsed_Identifier().equals(PROTOCOL_IDENTIFIER)) dataSets.add(dataSet);
			}
		}
		catch (Exception e)
		{
			logger.error("Exception validating import file", e);
		}
		wizard = new BiobankConnectWizard();
		wizard.setDataSets(dataSets);
		wizard.addPage(chooseCataloguePager);
		wizard.addPage(ontologyAnnotatorPager);
		wizard.addPage(ontologyMatcherPager);
		wizard.addPage(progressingBarPager);
		wizard.addPage(mappingManagerPager);
		return wizard;
	}

	// TODO : requestParam
	@RequestMapping(value = "/annotate", method = RequestMethod.POST)
	public String annotate(HttpServletRequest request) throws Exception
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
		return init();
	}

	// TODO : requestParam
	@RequestMapping(value = "/annotate/remove", method = RequestMethod.POST)
	public String removeAnnotations(HttpServletRequest request) throws Exception
	{
		ontologyAnnotator.removeAnnotations(wizard.getSelectedDataSet().getId());
		return init();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/annotate/update", consumes = APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateDocument(@RequestBody
	UpdateIndexRequest request)
	{
		ontologyAnnotator.updateIndex(request);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/match/status", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public OntologyMatcherResponse checkMatch() throws DatabaseException
	{
		OntologyMatcherResponse response = new OntologyMatcherResponse(ontologyMatcher.isRunning(),
				ontologyMatcher.matchPercentage());
		return response;
	}

	// TODO : to template
	@ModelAttribute("javascripts")
	public List<String> getJavascripts()
	{
		return Arrays.asList("jquery-ui-1.9.2.custom.min.js", "common-component.js", "catalogue-chooser.js",
				"ontology-annotator.js", "ontology-matcher.js", "mapping-manager.js", "simple_statistics.js",
				"biobank-connect.js");
	}

	// TODO : to template
	@ModelAttribute("stylesheets")
	public List<String> getStylesheets()
	{
		return Arrays.asList("jquery-ui-1.9.2.custom.min.css", "biobank-connect.css", "catalogue-chooser.css",
				"ontology-matcher.css", "mapping-manager.css", "ontology-annotator.css");
	}
}