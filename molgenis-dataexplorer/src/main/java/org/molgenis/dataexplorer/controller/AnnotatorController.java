package org.molgenis.dataexplorer.controller;

import com.google.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.web.AnnotationService;
import org.molgenis.data.annotation.web.meta.AnnotationJobExecution;
import org.molgenis.data.annotation.web.meta.AnnotationJobExecutionFactory;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.util.ErrorMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static org.molgenis.MolgenisFieldTypes.AttributeType.COMPOUND;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.settings.SettingsPackage.PACKAGE_SETTINGS;
import static org.molgenis.dataexplorer.controller.AnnotatorController.URI;

@Controller
@RequestMapping(URI)
public class AnnotatorController
{
	private static final Logger LOG = LoggerFactory.getLogger(AnnotatorController.class);

	public static final String URI = "/annotators";
	private final DataService dataService;
	private final AnnotationService annotationService;
	private final MolgenisPermissionService molgenisPermissionService;
	private final UserAccountService userAccountService;
	private final AnnotationJobFactory annotationJobFactory;
	private final ExecutorService taskExecutor;
	private final AnnotationJobExecutionFactory annotationJobExecutionFactory;

	@Autowired
	public AnnotatorController(DataService dataService, AnnotationService annotationService,
			MolgenisPermissionService molgenisPermissionService, PermissionSystemService permissionSystemService,
			UserAccountService userAccountService, AnnotationJobFactory annotationJobFactory,
			ExecutorService taskExecutor, AnnotationJobExecutionFactory annotationJobExecutionFactory)
	{
		this.dataService = dataService;
		this.annotationService = annotationService;
		this.molgenisPermissionService = molgenisPermissionService;
		this.userAccountService = userAccountService;
		this.annotationJobFactory = annotationJobFactory;
		this.taskExecutor = taskExecutor;
		this.annotationJobExecutionFactory = annotationJobExecutionFactory;
	}

	/**
	 * Gets a map of all available annotators.
	 *
	 * @param dataSetName
	 * @return annotatorMap
	 */
	@RequestMapping(value = "/get-available-annotators", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Map<String, Object>> getMapOfAvailableAnnotators(@RequestBody String dataSetName)
	{
		Map<String, Map<String, Object>> annotatorMap = setMapOfAnnotators(dataSetName);
		return annotatorMap;
	}

	/**
	 * Annotates an entity based on selected entity and selected annotators. Creates a copy of the entity dataset if
	 * option is ticked by the user.
	 *
	 * @param annotatorNames
	 * @param entityName
	 * @return repositoryName
	 */
	@RequestMapping(value = "/annotate-data", method = RequestMethod.POST)
	@ResponseBody
	public String annotateData(HttpServletRequest request,
			@RequestParam(value = "annotatorNames", required = false) String[] annotatorNames,
			@RequestParam("dataset-identifier") String entityName)
	{
		Repository<Entity> repository = dataService.getRepository(entityName);
		if (annotatorNames != null && repository != null)
		{
			scheduleAnnotatorRun(repository.getEntityMetaData().getName(), annotatorNames);
		}
		return entityName;
	}

	public String scheduleAnnotatorRun(String entityName, String[] annotatorNames)
	{
		AnnotationJobExecution annotationJobExecution = annotationJobExecutionFactory.create();
		annotationJobExecution.setUser(userAccountService.getCurrentUser());
		annotationJobExecution.setTargetName(entityName);
		annotationJobExecution.setAnnotators(String.join(",", (CharSequence[]) annotatorNames));
		annotationJobExecution.setResultUrl("/menu/main/dataexplorer?entity=" + entityName);
		AnnotationJob job = annotationJobFactory.createJob(annotationJobExecution);
		taskExecutor.submit(job);
		return annotationJobExecution.getIdentifier();
	}

	/**
	 * Sets a map of annotators, whether they can be used by the selected data set.
	 *
	 * @param dataSetName
	 * @return mapOfAnnotators
	 */
	private Map<String, Map<String, Object>> setMapOfAnnotators(String dataSetName)
	{
		Map<String, Map<String, Object>> mapOfAnnotators = new HashMap<String, Map<String, Object>>();

		if (dataSetName != null)
		{
			EntityMetaData entityMetaData = dataService.getEntityMetaData(dataSetName);
			for (RepositoryAnnotator annotator : annotationService.getAllAnnotators())
			{
				List<AttributeMetaData> outputAttrs = annotator.getOutputAttributes();
				outputAttrs = getAtomicAttributesFromList(outputAttrs);
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("description", annotator.getDescription());
				map.put("canAnnotate", annotator.canAnnotate(entityMetaData));
				map.put("inputAttributes", createAttrsResponse(annotator.getRequiredAttributes()));
				map.put("inputAttributeTypes", toMap(annotator.getRequiredAttributes()));
				map.put("outputAttributes", createAttrsResponse(outputAttrs));
				map.put("outputAttributeTypes", toMap(annotator.getOutputAttributes()));

				String settingsEntityName = PACKAGE_SETTINGS + PACKAGE_SEPARATOR + annotator.getInfo().getCode();
				map.put("showSettingsButton",
						molgenisPermissionService.hasPermissionOnEntity(settingsEntityName, Permission.WRITE));
				mapOfAnnotators.put(annotator.getSimpleName(), map);
			}
		}
		return mapOfAnnotators;
	}

	private List<Map<String, Object>> createAttrsResponse(List<AttributeMetaData> inputMetaData)
	{
		return inputMetaData.stream().map(attr ->
		{
			Map<String, Object> attrMap = new HashMap<String, Object>();
			attrMap.put("name", attr.getName());
			attrMap.put("description", attr.getDescription());
			return attrMap;
		}).collect(Collectors.toList());
	}

	private List<AttributeMetaData> getAtomicAttributesFromList(List<AttributeMetaData> outputAttrs)
	{
		if (outputAttrs.size() == 1 && outputAttrs.get(0).getDataType() == COMPOUND)
		{
			return getAtomicAttributesFromList(Lists.newArrayList(outputAttrs.get(0).getAttributeParts()));
		}
		else
		{
			return outputAttrs;
		}
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
