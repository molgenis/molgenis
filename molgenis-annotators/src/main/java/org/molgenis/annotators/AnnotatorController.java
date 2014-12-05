package org.molgenis.annotators;

import static org.molgenis.annotators.AnnotatorController.URI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.CrudRepositoryAnnotator;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.meta.WritableMetaDataService;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.validation.EntityValidator;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller wrapper for the dataexplorer annotator
 * 
 * @author mdehaan
 * 
 */
@Controller
@RequestMapping(URI)
public class AnnotatorController
{
	public static final String URI = "/annotators";
	private static final Logger logger = Logger.getLogger(AnnotatorController.class);

	@Autowired
	DataService dataService;

	@Autowired
	FileStore fileStore;

	@Autowired
	SearchService searchService;

	@Autowired
	AnnotationService annotationService;

	@Autowired
	EntityValidator entityValidator;

	@Autowired
	MysqlRepositoryCollection mysqlRepositoryCollection;

	/**
	 * Gets a map of all available annotators.
	 * 
	 * @param dataSetName
	 * @return annotatorMap
	 * 
	 * */
	@RequestMapping(value = "/get-available-annotators", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Map<String, Object>> getMapOfAvailableAnnotators(@RequestBody String dataSetName)
	{
		Map<String, Map<String, Object>> annotatorMap = setMapOfAnnotators(dataSetName);
		return annotatorMap;
	}

	/**
	 * Annotates a dataset based on selected dataset and selected annotators. Creates a copy of the original dataset if
	 * option is ticked by the user.
	 * 
	 * @param annotatorNames
	 * @param dataSetIdentifier
	 * @param createCopy
	 * @return repositoryName
	 * 
	 * */
	@RequestMapping(value = "/annotate-data", method = RequestMethod.POST)
	@ResponseBody
	@Transactional
	public String annotateData(@RequestParam(value = "annotatorNames", required = false) String[] annotatorNames,
			@RequestParam("dataset-identifier") String entityName,
			@RequestParam(value = "createCopy", required = false) boolean createCopy)
	{		
		Repository repository = dataService.getRepositoryByEntityName(entityName);
		if (annotatorNames != null && repository != null)
		{
			CrudRepositoryAnnotator crudRepositoryAnnotator = new CrudRepositoryAnnotator(mysqlRepositoryCollection,
					getNewRepositoryName(annotatorNames, repository.getEntityMetaData().getSimpleName()));
			
			for (String annotatorName : annotatorNames)
			{
				RepositoryAnnotator annotator = annotationService.getAnnotatorByName(annotatorName);
				if (annotator != null)
				{
					// running annotator
					Repository repo = dataService.getRepositoryByEntityName(entityName);
					repository = crudRepositoryAnnotator.annotate(annotator, repo, createCopy);
					entityName = repository.getName();
					createCopy = false;
				}
			}
		}
		return entityName;
	}

	private String getNewRepositoryName(String[] annotatorNames, String repositoryName)
	{
		String newRepositoryName = repositoryName;
		for (String annotatorName : annotatorNames)
		{
			newRepositoryName = newRepositoryName + "_" + annotatorName;
		}	
		return newRepositoryName;
	}

	/**
	 * Sets a map of annotators, whether they can be used by the selected data set.
	 * 
	 * @param dataSetName
	 * @return mapOfAnnotators
	 * 
	 * */
	private Map<String, Map<String, Object>> setMapOfAnnotators(String dataSetName)
	{
		Map<String, Map<String, Object>> mapOfAnnotators = new HashMap<String, Map<String, Object>>();

		if (dataSetName != null)
		{
			EntityMetaData entityMetaData = dataService.getEntityMetaData(dataSetName);

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

	/**
	 * Transforms metadata to a List of strings
	 * 
	 * @param metaData
	 * @return result
	 * */
	private List<String> metaDataToStringList(EntityMetaData metaData)
	{
		List<String> result = new ArrayList<String>();
		for (AttributeMetaData attribute : metaData.getAttributes())
		{
			result.add(attribute.getLabel() + "(" + attribute.getDataType().toString() + ")\n");
		}
		return result;
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Map<String, String> handleRuntimeException(RuntimeException e)
	{
		logger.error(e.getMessage(), e);
		return Collections.singletonMap("errorMessage",
				"An error occured. Please contact the administrator.<br />Message:" + e.getMessage());
	}
}
