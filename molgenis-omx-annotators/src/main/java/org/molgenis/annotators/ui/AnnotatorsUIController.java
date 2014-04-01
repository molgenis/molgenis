package org.molgenis.annotators.ui;

import static org.molgenis.annotators.ui.AnnotatorsUIController.URI;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.Part;

import org.apache.log4j.Logger;
import org.molgenis.data.AttributeMetaData;
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

	@RequestMapping(value = "/change-selected-dataset")
	@ResponseBody
	public Map<String, Map<String,Object>> changeSelectedDataSet(@RequestBody
	String selectedDataSetIdentifier, Model model)
	{
		Map<String, Map<String,Object>> annotatorMap = setMapOfAnnotators(selectedDataSetIdentifier);
		return annotatorMap;
	}

	@RequestMapping(value = "/file-upload", headers = "content-type=multipart/*", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void handleAnnotatorFileUpload(@RequestParam("file-input-field")
	Part part, @RequestParam("dataset-name")
	String submittedDataSetName) throws IOException
	{
		if (part != null)
		{
			String file = "input-file";
			fileStore.store(part.getInputStream(), file);

			pluginAnnotatorsUIService.tsvToOmxRepository(file, submittedDataSetName);
		}
		else
		{
			throw new RuntimeException("No file submitted");
		}
	}

	@RequestMapping(value = "/execute-annotation-app", method = RequestMethod.POST)
    @ResponseBody
	public String filterMyVariants(@RequestParam(value = "annotatorNames", required = false)
                                     String[] annotatorNames, Model model, @RequestParam("dataset-identifier")
	String dataSetIdentifier)
	{
		OmxDataSetAnnotator omxDataSetAnnotator = new OmxDataSetAnnotator(dataService, searchService, indexer,
				entityValidator);
		Repository repository = dataService.getRepositoryByEntityName(dataSetIdentifier);
        List<RepositoryAnnotator> annotators = new ArrayList<RepositoryAnnotator>();
        String name = dataSetIdentifier;
        if (annotatorNames != null && repository != null)
		{
            boolean createCopy = true;
			for (String annotatorName : annotatorNames)
			{
				RepositoryAnnotator annotator = annotationService.getAnnotatorByName(annotatorName);
				if (annotator != null)
				{
					// FIXME do something about the this indexer problem
					while (indexer.isIndexingRunning())
					{
					}
                    Repository repo = dataService.getRepositoryByEntityName(name);

                    repository = omxDataSetAnnotator.annotate(annotator, repo, createCopy);
                    name = repository.getName();
                    createCopy = false;
				}
			}
		}
        return name;
	}

	private Map<String, Map<String,Object>> setMapOfAnnotators(String dataSetIdentifier)
	{
		Map<String, Map<String,Object>> mapOfAnnotators = new HashMap<String, Map<String,Object>>();

		if (dataSetIdentifier != null)
		{
			EntityMetaData entityMetaData = dataService.getEntityMetaData(dataSetIdentifier);

			for (RepositoryAnnotator annotator : annotationService.getAllAnnotators())
			{
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("canAnnotate", annotator.canAnnotate(entityMetaData));
                map.put("inputMetadata", metaDataToStringList(annotator.getInputMetaData()));
                map.put("outputMetadata", metaDataToStringList(annotator.getOutputMetaData()));
				mapOfAnnotators.put(annotator.getName(), map);
			}

		}

		return mapOfAnnotators;
	}

    private List<String> metaDataToStringList(EntityMetaData metaData){
        List<String> result = new ArrayList<String>();
        for(AttributeMetaData attribute : metaData.getAttributes()){
            result.add(attribute.getLabel()+"("+attribute.getDataType().toString()+")\n");
        }
        return result;
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
