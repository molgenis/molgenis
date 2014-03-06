package org.molgenis.annotators.ui;

import static org.molgenis.annotators.ui.AnnotatorsUIController.URI;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Part;

import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.omx.annotation.OmxDataSetAnnotator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.validation.EntityValidator;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.search.DataSetsIndexer;
import org.molgenis.search.SearchService;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
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

	private static final String ID = "annotateUI";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final AnnotatorsUIService pluginAnnotatorsUIService;

	@Autowired
	DataService dataService;

	@Autowired
	FileStore fileStore;

	@Autowired
	SearchService searchService;

	@Autowired
	AnnotationService annotationService;

	@Autowired
	DataSetsIndexer indexer;

	@Autowired
	EntityValidator entityValidator;

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

		DataSet selectedDataSet = null;

		if (dataSets != null && !dataSets.isEmpty())
		{
			// determine selected data set and add to model
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

	@RequestMapping(value = "/changeSelectedDataSet")
	@ResponseBody
	public Map<String, Boolean> changeSelectedDataSet(@RequestBody
	String selectedDataSetIdentifier)
	{
		Map<String, Boolean> annotatorMap = setMapOfAnnotators(selectedDataSetIdentifier);
		return annotatorMap;
	}

	@RequestMapping(value = "/create-new-dataset-from-tsv", headers = "content-type=multipart/*", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void handleVcfInput(@RequestParam("file-input-field")
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
	}

	@RequestMapping(value = "/execute-annotation-app", method = RequestMethod.POST)
	public String filterMyVariants(@RequestParam(value = "annotatorNames", required = false)
	String[] annotatorNames, Model model, @RequestParam("dataset-identifier")
	String dataSetIdentifier)
	{
		OmxDataSetAnnotator omxDataSetAnnotator = new OmxDataSetAnnotator(dataService, searchService, indexer,
				entityValidator);
		Repository repository = dataService.getRepositoryByEntityName(dataSetIdentifier);

		if (annotatorNames != null && repository != null)
		{
			for (String annotatorName : annotatorNames)
			{
				RepositoryAnnotator annotator = annotationService.getAnnotatorByName(annotatorName);
				if (annotator != null)
				{
					// FIXME do something about the this indexer problem
					while (indexer.isIndexingRunning())
					{
					}

					omxDataSetAnnotator.annotate(annotator, repository, false);
				}
			}
		}

		return "view-result-page";
	}

	private Map<String, Boolean> setMapOfAnnotators(String dataSetIdentifier)
	{
		Map<String, Boolean> mapOfAnnotators = new HashMap<String, Boolean>();

		if (dataSetIdentifier != null)
		{

			EntityMetaData repo = (EntityMetaData) dataService.getRepositoryByEntityName(dataSetIdentifier);

			for (RepositoryAnnotator annotator : annotationService.getAllAnnotators())
			{
				mapOfAnnotators.put(annotator.getName(), annotator.canAnnotate(repo));
			}

		}

		return mapOfAnnotators;
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
