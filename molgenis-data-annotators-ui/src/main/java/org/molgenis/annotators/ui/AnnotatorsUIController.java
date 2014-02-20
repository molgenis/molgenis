package org.molgenis.annotators.ui;

import static org.molgenis.annotators.ui.AnnotatorsUIController.URI;

import java.io.File;
import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.Part;

import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.data.RepositoryAnnotator;
import org.molgenis.data.omx.annotation.OmxDataSetAnnotator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.search.DataSetsIndexer;
import org.molgenis.search.SearchService;
import org.molgenis.util.FileStore;
import org.molgenis.util.FileUploadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.common.collect.Lists;

/**
 * Controller wrapper for the Annotation UI.
 * 
 * @author mdehaan
 * 
 */
@Controller
@RequestMapping(URI)
public class AnnotatorsUIController extends MolgenisPluginController
{

	private static final Logger logger = Logger.getLogger(AnnotatorsUIController.class);

	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + "annotateUI";

	private final AnnotatorsUIService pluginAnnotatorsUIService;

	@Autowired
	DataService dataService;

	@Autowired
	FileStore fileStore;

	@Autowired
	SearchService searchService;

	@Autowired
	DataSetsIndexer indexer;

	@Resource(name = "ebiService")
	RepositoryAnnotator ebiServiceAnnotator;

	@Resource(name = "caddService")
	RepositoryAnnotator caddServiceAnnotator;

	@Resource(name = "dbnsfpVariantService")
	RepositoryAnnotator dbnsfpVariantServiceAnnotator;

	@Resource(name = "dbnsfpGeneService")
	RepositoryAnnotator dbnsfpGeneServiceAnnotator;

	@Resource(name = "omimHpoService")
	RepositoryAnnotator omimHpoAnnotator;

	@Autowired
	public AnnotatorsUIController(AnnotatorsUIService pluginAnnotatorsUIService)
	{
		super(URI);

		if (pluginAnnotatorsUIService == null) throw new IllegalArgumentException("pluginAnnotatorsUIService is null");
		this.pluginAnnotatorsUIService = pluginAnnotatorsUIService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(String selectedDataSetIdentifier, Model model)
	{

		List<DataSet> dataSets = Lists.newArrayList(dataService.findAll(DataSet.ENTITY_NAME,
				new QueryImpl().sort(new Sort(Direction.DESC, DataSet.STARTTIME)), DataSet.class));

		model.addAttribute("dataSets", dataSets);

		// TODO add the real list of annotators from the annotation registry
		model.addAttribute("annotators", new ArrayList<String>());

		if (dataSets != null && !dataSets.isEmpty())
		{
			// determine selected data set and add to model
			DataSet selectedDataSet = null;
			if (selectedDataSetIdentifier != null)
			{
				for (DataSet dataSet : dataSets)
				{
					if (dataSet.getIdentifier().equals(selectedDataSetIdentifier))
					{
						selectedDataSet = dataSet;
						break;
					}
				}

				if (selectedDataSet == null) throw new IllegalArgumentException(selectedDataSetIdentifier
						+ " is not a valid data set identifier");
			}
			else
			{
				// select first data set by default
				selectedDataSet = dataSets.iterator().next();
			}
			
			model.addAttribute("selectedDataSet", selectedDataSet);
		}

		return "view-annotation-ui";
	}
	
	@RequestMapping(value = "/change-current-dataset", method = RequestMethod.GET)
	public String changeSelectedDataSet(@RequestParam("dataset-select") String userSelectedDataSet){
		System.out.println("CHANGED TO " + userSelectedDataSet);
		return "view-annotation-ui";
	}

	@RequestMapping(value = "/create-new-dataset-from-tsv", headers = "content-type=multipart/*", method = RequestMethod.POST)
	public String handleVcfInput(@RequestParam("file-input-field")
	Part part, @RequestParam("dataset-name")
	String submittedDataSetName, Model model) throws IOException
	{
		if (!part.equals(null) && part.getSize() > 5000000)
		{ // 5mb limit
			throw new RuntimeException("File too large");
		}
		else if (part.equals(null))
		{
			throw new RuntimeException("No file submitted");
		}

		String file = "cartagenia-export-file";
		fileStore.store(part.getInputStream(), file);

		pluginAnnotatorsUIService.tsvToOmxRepository(file, model, submittedDataSetName);

		return "view-annotation-ui";
	}

	@RequestMapping(value = "/execute-variant-app", method = RequestMethod.POST)
	public String filterMyVariants(@RequestParam("annotation-form") List<String> selectedAnnotators)
	{
		OmxDataSetAnnotator omxDataSetAnnotator = new OmxDataSetAnnotator(dataService, searchService, indexer);
		for(String annotator : selectedAnnotators){
			//TODO annotate
		}
		
		
		// omxDataSetAnnotator.annotate(ebiServiceAnnotator, dataService.getRepositoryByEntityName("uniprotTest"),
		// true);
		// omxDataSetAnnotator.annotate(caddServiceAnnotator, dataService.getRepositoryByEntityName("variantSet"),
		// false);
		//omxDataSetAnnotator.annotate(dbnsfpVariantServiceAnnotator,
		//		dataService.getRepositoryByEntityName("variantSet"), true);
		//omxDataSetAnnotator.annotate(dbnsfpGeneServiceAnnotator, dataService.getRepositoryByEntityName("variantSet"),
		//		true);
		// omxDataSetAnnotator.annotate(omimHpoAnnotator, dataService.getRepositoryByEntityName("5gpm set"), true);

		return "view-result-page";
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Map<String, String> handleRuntimeException(RuntimeException e)
	{
		logger.error(null, e);
		return Collections.singletonMap("errorMessage",
				"An error occured. Please contact the administrator.<br />Message:" + e.getMessage());
	}
}
