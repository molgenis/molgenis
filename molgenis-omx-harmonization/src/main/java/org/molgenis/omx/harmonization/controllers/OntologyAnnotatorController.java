package org.molgenis.omx.harmonization.controllers;

import static org.molgenis.omx.harmonization.controllers.OntologyAnnotatorController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.omx.harmonization.ontologyannotator.OntologyAnnotator;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
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
public class OntologyAnnotatorController extends MolgenisPlugin
{
	public static final String URI = MolgenisPlugin.PLUGIN_URI_PREFIX + "ontologyannotator";
	private static final String ONTOLOGY_TERM_IRI = "ontologyTermIRI";
	private static final String SYNONYMS = "ontologyTermSynonym";
	private static final Logger logger = Logger.getLogger(OntologyAnnotatorController.class);
	@Autowired
	private OntologyAnnotator ontologyAnnotator;

	@Autowired
	private SearchService searchService;

	@Autowired
	private Database database;

	private static final String PROTOCOL_IDENTIFIER = "store_mapping";

	public OntologyAnnotatorController()
	{
		super(URI);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(@RequestParam(value = "selectedDataSet", required = false)
	String selectedDataSetId, Model model) throws Exception
	{
		List<DataSet> dataSets = new ArrayList<DataSet>();
		for (DataSet dataSet : database.find(DataSet.class))
		{
			if (!dataSet.getProtocolUsed_Identifier().equals(PROTOCOL_IDENTIFIER)) dataSets.add(dataSet);
		}
		if (selectedDataSetId != null) model.addAttribute("selectedDataSet", selectedDataSetId);
		model.addAttribute("isComplete", ontologyAnnotator.isComplete());
		model.addAttribute("dataSets", dataSets);
		model.addAttribute("isRunning", ontologyAnnotator.isRunning());
		ontologyAnnotator.initComplete();

		return "OntologyAnnotatorPlugin";
	}

	@RequestMapping(value = "/annotate", method = RequestMethod.POST, headers = "Content-Type=multipart/form-data")
	public String annotate(@RequestParam(value = "selectedDataSet", required = true)
	String selectedDataSetId, Model model) throws DatabaseException
	{
		ontologyAnnotator.annotate(Integer.parseInt(selectedDataSetId));
		List<DataSet> dataSets = new ArrayList<DataSet>();
		for (DataSet dataSet : database.find(DataSet.class))
		{
			if (!dataSet.getProtocolUsed_Identifier().equals(PROTOCOL_IDENTIFIER)) dataSets.add(dataSet);
		}
		model.addAttribute("selectedDataSet", selectedDataSetId);
		model.addAttribute("dataSets", dataSets);
		model.addAttribute("isRunning", ontologyAnnotator.isRunning());

		return "OntologyAnnotatorPlugin";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/update", consumes = APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateDocument(@RequestBody
	UpdateIndexRequest request)
	{
		List<QueryRule> rules = new ArrayList<QueryRule>();
		rules.add(new QueryRule(ONTOLOGY_TERM_IRI, Operator.EQUALS, request.getOntologyTermIRI()));
		rules.add(new QueryRule(Operator.LIMIT, 1000));
		SearchRequest searchRequest = new SearchRequest(null, rules, null);
		SearchResult searchResult = searchService.search(searchRequest);

		for (Hit hit : searchResult.getSearchHits())
		{
			try
			{
				searchService.updateDocumentById(hit.getDocumentType(), hit.getId(), request.getUpdateScript());
			}
			catch (Exception e)
			{
				logger.error("Exception calling searchservice for request [" + request + "]", e);
			}
		}
	}

	class UpdateIndexRequest
	{
		final String ontologyTermIRI;
		final String updateScript;

		public UpdateIndexRequest(String ontologyTermIRI, String boost)
		{
			this.ontologyTermIRI = ontologyTermIRI;
			this.updateScript = boost;
		}

		public String getUpdateScript()
		{
			return updateScript;
		}

		public String getOntologyTermIRI()
		{
			return ontologyTermIRI;
		}
	}
}