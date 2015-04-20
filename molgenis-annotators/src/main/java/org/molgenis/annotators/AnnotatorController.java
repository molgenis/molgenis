package org.molgenis.annotators;

import static org.molgenis.annotators.AnnotatorController.URI;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.CrudRepositoryAnnotator;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.validation.EntityValidator;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.util.ErrorMessageResponse;
import org.molgenis.util.FileStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
	private static final Logger LOG = LoggerFactory.getLogger(AnnotatorController.class);

	public static final String URI = "/annotators";

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
	PermissionSystemService permissionSystemService;

	@Autowired
	UserAccountService userAccountService;

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
	 * @param entityName
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
		Repository repository = dataService.getRepository(entityName);
		if (annotatorNames != null && repository != null)
		{
			CrudRepositoryAnnotator crudRepositoryAnnotator = new CrudRepositoryAnnotator(dataService,
					getNewRepositoryName(annotatorNames, repository.getEntityMetaData().getSimpleName()),
					permissionSystemService, userAccountService);

			for (String annotatorName : annotatorNames)
			{
				RepositoryAnnotator annotator = annotationService.getAnnotatorByName(annotatorName);
				if (annotator != null)
				{
					// running annotator
					try
					{
						repository = crudRepositoryAnnotator.annotate(annotator, repository, createCopy);
						entityName = repository.getName();
						createCopy = false;
					}
					catch (IOException e)
					{
						throw new RuntimeException(e.getMessage());
					}
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
				map.put("description", annotator.getDescription());
				map.put("canAnnotate", annotator.canAnnotate(entityMetaData));
				map.put("inputAttributes", annotator.getInputMetaData().getAttributes());
				map.put("inputAttributeTypes", toMap(annotator.getInputMetaData().getAttributes()));
				map.put("outputAttributes", annotator.getOutputMetaData().getAttributes());
				map.put("outputAttributeTypes", toMap(annotator.getOutputMetaData().getAttributes()));
				mapOfAnnotators.put(annotator.getSimpleName(), map);
			}

		}

		return mapOfAnnotators;
	}

	private Map<String, String> toMap(Iterable<AttributeMetaData> attrs)
	{
		Map<String, String> result = new HashMap<>();
		for (AttributeMetaData attr : attrs)
		{
			result.put(attr.getName(), attr.getDataType().toString());
		}
		return result;
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		LOG.error(e.getMessage(), e);
		return new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage(
				"An error occurred. Please contact the administrator.<br />Message:" + e.getMessage()));
	}
}
