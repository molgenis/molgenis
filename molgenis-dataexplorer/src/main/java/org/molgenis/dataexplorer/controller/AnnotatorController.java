package org.molgenis.dataexplorer.controller;

import com.google.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.web.AnnotationService;
import org.molgenis.data.annotation.web.meta.AnnotationJobExecution;
import org.molgenis.data.annotation.web.meta.AnnotationJobExecutionFactory;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.web.ErrorMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.dataexplorer.controller.AnnotatorController.URI;
import static org.molgenis.settings.SettingsPackage.PACKAGE_SETTINGS;

@Controller
@RequestMapping(URI)
public class AnnotatorController
{
	private static final Logger LOG = LoggerFactory.getLogger(AnnotatorController.class);

	public static final String URI = "/annotators";
	private final DataService dataService;
	private final AnnotationService annotationService;
	private final UserPermissionEvaluator permissionService;
	private final UserAccountService userAccountService;
	private final AnnotationJobFactory annotationJobFactory;
	private final ExecutorService taskExecutor;
	private final AnnotationJobExecutionFactory annotationJobExecutionFactory;

	public AnnotatorController(DataService dataService, AnnotationService annotationService,
			UserPermissionEvaluator permissionService, UserAccountService userAccountService,
			AnnotationJobFactory annotationJobFactory, ExecutorService taskExecutor,
			AnnotationJobExecutionFactory annotationJobExecutionFactory)
	{
		this.dataService = dataService;
		this.annotationService = annotationService;
		this.permissionService = permissionService;
		this.userAccountService = userAccountService;
		this.annotationJobFactory = annotationJobFactory;
		this.taskExecutor = taskExecutor;
		this.annotationJobExecutionFactory = annotationJobExecutionFactory;
	}

	/**
	 * Gets a map of all available annotators.
	 *
	 * @return annotatorMap
	 */
	@PostMapping("/get-available-annotators")
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
	 * @return repositoryName
	 */
	@PostMapping("/annotate-data")
	@ResponseBody
	public String annotateData(HttpServletRequest request,
			@RequestParam(value = "annotatorNames", required = false) String[] annotatorNames,
			@RequestParam("dataset-identifier") String entityTypeId)
	{
		Repository<Entity> repository = dataService.getRepository(entityTypeId);
		if (annotatorNames != null && repository != null)
		{
			scheduleAnnotatorRun(repository.getEntityType().getId(), annotatorNames);
		}
		return entityTypeId;
	}

	public String scheduleAnnotatorRun(String entityTypeId, String[] annotatorNames)
	{
		AnnotationJobExecution annotationJobExecution = annotationJobExecutionFactory.create();
		annotationJobExecution.setUser(userAccountService.getCurrentUser());
		annotationJobExecution.setTargetName(entityTypeId);
		annotationJobExecution.setAnnotators(String.join(",", (CharSequence[]) annotatorNames));
		annotationJobExecution.setResultUrl("/menu/main/dataexplorer?entity=" + entityTypeId);
		AnnotationJob job = annotationJobFactory.createJob(annotationJobExecution);
		taskExecutor.submit(job);
		return annotationJobExecution.getIdentifier();
	}

	/**
	 * Sets a map of annotators, whether they can be used by the selected data set.
	 *
	 * @return mapOfAnnotators
	 */
	private Map<String, Map<String, Object>> setMapOfAnnotators(String dataSetName)
	{
		Map<String, Map<String, Object>> mapOfAnnotators = new HashMap<>();

		if (dataSetName != null)
		{
			EntityType entityType = dataService.getEntityType(dataSetName);
			for (RepositoryAnnotator annotator : annotationService.getAllAnnotators())
			{
				List<Attribute> outputAttrs = annotator.getOutputAttributes();
				outputAttrs = getAtomicAttributesFromList(outputAttrs);
				Map<String, Object> map = new HashMap<>();
				map.put("description", annotator.getDescription());
				map.put("canAnnotate", annotator.canAnnotate(entityType));
				map.put("inputAttributes", createAttrsResponse(annotator.getRequiredAttributes()));
				map.put("inputAttributeTypes", toMap(annotator.getRequiredAttributes()));
				map.put("outputAttributes", createAttrsResponse(outputAttrs));
				map.put("outputAttributeTypes", toMap(annotator.getOutputAttributes()));

				String settingsEntityName = PACKAGE_SETTINGS + PACKAGE_SEPARATOR + annotator.getInfo().getCode();
				map.put("showSettingsButton",
						permissionService.hasPermission(new EntityTypeIdentity(settingsEntityName),
								EntityTypePermission.WRITE));
				mapOfAnnotators.put(annotator.getSimpleName(), map);
			}
		}
		return mapOfAnnotators;
	}

	private List<Map<String, Object>> createAttrsResponse(List<Attribute> inputMetaData)
	{
		return inputMetaData.stream().map(attr ->
		{
			Map<String, Object> attrMap = new HashMap<>();
			attrMap.put("name", attr.getName());
			attrMap.put("description", attr.getDescription());
			return attrMap;
		}).collect(Collectors.toList());
	}

	private List<Attribute> getAtomicAttributesFromList(List<Attribute> outputAttrs)
	{
		if (outputAttrs.size() == 1 && outputAttrs.get(0).getDataType() == COMPOUND)
		{
			return getAtomicAttributesFromList(Lists.newArrayList(outputAttrs.get(0).getChildren()));
		}
		else
		{
			return outputAttrs;
		}
	}

	private Map<String, String> toMap(Iterable<Attribute> attrs)
	{
		Map<String, String> result = new HashMap<>();
		for (Attribute attr : attrs)
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
