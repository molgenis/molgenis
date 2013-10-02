package org.molgenis.omx.harmonization.controllers;

import static org.molgenis.omx.harmonization.controllers.BiobankConnectController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.harmonization.ontologyannotator.OntologyAnnotator;
import org.molgenis.omx.harmonization.ontologyannotator.UpdateIndexRequest;
import org.molgenis.omx.harmonization.ontologymatcher.OntologyMatcher;
import org.molgenis.omx.harmonization.ontologymatcher.OntologyMatcherResponse;
import org.molgenis.omx.harmonization.pagers.BiobankConnectWizard;
import org.molgenis.omx.harmonization.pagers.ChooseCataloguePager;
import org.molgenis.omx.harmonization.pagers.MappingManagerPager;
import org.molgenis.omx.harmonization.pagers.OntologyAnnotatorPager;
import org.molgenis.omx.harmonization.pagers.OntologyMatcherPager;
import org.molgenis.omx.harmonization.pagers.ProgressingBarPager;
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
	private final ChooseCataloguePager chooseCataloguePager;
	private final OntologyAnnotatorPager ontologyAnnotatorPager;
	private final OntologyMatcherPager ontologyMatcherPager;
	private final MappingManagerPager mappingManagerPager;
	private final ProgressingBarPager progressingBarPager;

	private BiobankConnectWizard wizard;

	private static final String PROTOCOL_IDENTIFIER = "store_mapping";

	@Autowired
	private Database database;

	@Autowired
	private OntologyAnnotator ontologyAnnotator;

	@Autowired
	private OntologyMatcher ontologyMatcher;

	@Autowired
	public BiobankConnectController(ChooseCataloguePager chooseCataloguePager,
			OntologyAnnotatorPager ontologyAnnotatorPager, OntologyMatcherPager ontologyMatcherPager,
			MappingManagerPager mappingManagerPager, ProgressingBarPager progressingBarPager)
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

	@RequestMapping(value = "/annotate", method = RequestMethod.GET)
	public String annotate() throws Exception
	{
		ontologyAnnotator.annotate(wizard.getSelectedDataSet().getId());
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

	@ModelAttribute("javascripts")
	public List<String> getJavascripts()
	{
		return Arrays.asList("common-component.js", "catalogue-chooser.js", "ontology-annotator.js",
				"ontology-matcher.js", "mapping-manager.js", "simple_statistics.js", "biobank-connect.js");
	}

	@ModelAttribute("stylesheets")
	public List<String> getStylesheets()
	{
		return Arrays.asList("biobank-connect.css", "catalogue-chooser.css", "ontology-matcher.css",
				"mapping-manager.css", "ontology-annotator.css");
	}
}