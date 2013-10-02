//package org.molgenis.omx.harmonization.controllers;
//
//import static org.molgenis.omx.harmonization.controllers.back_up_BiobankConnectController.URI;
//import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpSession;
//
//import org.molgenis.framework.db.Database;
//import org.molgenis.framework.db.DatabaseException;
//import org.molgenis.framework.ui.MolgenisPluginController;
//import org.molgenis.omx.harmonization.ontologyannotator.OntologyAnnotator;
//import org.molgenis.omx.harmonization.ontologyannotator.UpdateIndexRequest;
//import org.molgenis.omx.harmonization.ontologymatcher.OntologyMatcher;
//import org.molgenis.omx.harmonization.ontologymatcher.OntologyMatcherResponse;
//import org.molgenis.omx.observ.DataSet;
//import org.molgenis.search.SearchService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.bind.annotation.ResponseStatus;
//
//@Controller
//@RequestMapping(URI)
//public class back_up_BiobankConnectController extends MolgenisPluginController
//{
//	public static final String URI = "/plugin/biobankconnect";
//	private static final List<String> viewNames = Arrays.asList("CatalogueChooserPlugin.ftl",
//			"OntologyAnnotatorPlugin.ftl", "OntologyMatcherPlugin.ftl", "ProgressingBarPlugin.ftl",
//			"MappingManagerPlugin.ftl");
//	private static final String PROTOCOL_IDENTIFIER = "store_mapping";
//	private static final String BIOBANK_CONNECT_VIEW = "BiobankConnectPlugin";
//	private static final AtomicInteger stepNumber = new AtomicInteger();
//
//	@Autowired
//	private OntologyAnnotator ontologyAnnotator;
//
//	@Autowired
//	private OntologyMatcher ontologyMatcher;
//
//	@Autowired
//	private SearchService searchService;
//
//	@Autowired
//	private Database database;
//
//	public back_up_BiobankConnectController()
//	{
//		super(URI);
//	}
//
//	@RequestMapping(method = RequestMethod.GET)
//	public String init(HttpSession session, Model model) throws Exception
//	{
//		List<DataSet> dataSets = new ArrayList<DataSet>();
//		for (DataSet dataSet : database.find(DataSet.class))
//		{
//			if (!dataSet.getProtocolUsed_Identifier().equals(PROTOCOL_IDENTIFIER)) dataSets.add(dataSet);
//		}
//		session.setAttribute("dataSets", dataSets);
//		model.addAttribute("dataSets", session.getAttribute("dataSets"));
//		model.addAttribute("viewName", viewNames.get(stepNumber.get()));
//		return BIOBANK_CONNECT_VIEW;
//	}
//
//	@RequestMapping(value = "/reset", method = RequestMethod.GET)
//	public String reset(HttpSession session, Model model) throws Exception
//	{
//		stepNumber.set(0);
//		List<DataSet> dataSets = new ArrayList<DataSet>();
//		for (DataSet dataSet : database.find(DataSet.class))
//		{
//			if (!dataSet.getProtocolUsed_Identifier().equals(PROTOCOL_IDENTIFIER)) dataSets.add(dataSet);
//		}
//		session.setAttribute("dataSets", dataSets);
//		model.addAttribute("dataSets", session.getAttribute("dataSets"));
//		model.addAttribute("viewName", viewNames.get(stepNumber.get()));
//		return BIOBANK_CONNECT_VIEW;
//	}
//
//	@RequestMapping(value = "/next", method = RequestMethod.GET)
//	public String next(HttpServletRequest request, HttpSession session, Model model) throws Exception
//	{
//		String viewName = null;
//		if (stepNumber.incrementAndGet() < viewNames.size()) viewName = viewNames.get(stepNumber.get());
//		else viewName = viewNames.get(stepNumber.decrementAndGet());
//		if (request.getParameter("selectedDataSet") != null)
//		{
//			Integer selectedDataSetId = Integer.parseInt(request.getParameter("selectedDataSet").toString());
//			session.setAttribute("selectedDataSet", database.findById(DataSet.class, selectedDataSetId));
//		}
//
//		// TODO : temporary solution
//		if (stepNumber.get() == 3)
//		{
//			DataSet dataSet = (DataSet) session.getAttribute("selectedDataSet");
//			List<Integer> selectedTargetDataSetIds = new ArrayList<Integer>();
//			for (String id : request.getParameter("selectedTargetDataSets").split(","))
//			{
//				selectedTargetDataSetIds.add(Integer.parseInt(id));
//			}
//			ontologyMatcher.match(dataSet.getId(), selectedTargetDataSetIds);
//		}
//
//		model.addAttribute("viewName", viewName);
//		model.addAttribute("dataSets", session.getAttribute("dataSets"));
//		model.addAttribute("selectedDataSet", session.getAttribute("selectedDataSet"));
//		return BIOBANK_CONNECT_VIEW;
//	}
//
//	@RequestMapping(value = "/prev", method = RequestMethod.GET)
//	public String prev(HttpSession session, Model model) throws Exception
//	{
//		String viewName = null;
//		if (stepNumber.decrementAndGet() >= 0) viewName = viewNames.get(stepNumber.get());
//		else viewName = viewNames.get(stepNumber.incrementAndGet());
//		model.addAttribute("viewName", viewName);
//		model.addAttribute("dataSets", session.getAttribute("dataSets"));
//		model.addAttribute("selectedDataSet", session.getAttribute("selectedDataSet"));
//		return BIOBANK_CONNECT_VIEW;
//	}
//
//	@RequestMapping(value = "/annotate", method = RequestMethod.GET)
//	public String annotate(HttpSession session, Model model) throws Exception
//	{
//		DataSet dataSet = (DataSet) session.getAttribute("selectedDataSet");
//		ontologyAnnotator.annotate(dataSet.getId());
//
//		model.addAttribute("viewName", viewNames.get(stepNumber.get()));
//		model.addAttribute("dataSets", session.getAttribute("dataSets"));
//		model.addAttribute("selectedDataSet", session.getAttribute("selectedDataSet"));
//		return BIOBANK_CONNECT_VIEW;
//	}
//
//	@RequestMapping(method = RequestMethod.POST, value = "/annotate/update", consumes = APPLICATION_JSON_VALUE)
//	@ResponseStatus(HttpStatus.NO_CONTENT)
//	public void updateDocument(@RequestBody
//	UpdateIndexRequest request)
//	{
//		ontologyAnnotator.updateIndex(request);
//	}
//
//	@RequestMapping(method = RequestMethod.GET, value = "/match/status", produces = APPLICATION_JSON_VALUE)
//	@ResponseBody
//	public OntologyMatcherResponse checkMatch() throws DatabaseException
//	{
//		OntologyMatcherResponse response = new OntologyMatcherResponse(ontologyMatcher.isRunning(),
//				ontologyMatcher.matchPercentage());
//		return response;
//	}
// }