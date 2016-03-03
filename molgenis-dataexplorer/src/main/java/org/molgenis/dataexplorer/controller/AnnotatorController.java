package org.molgenis.dataexplorer.controller;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.molgenis.dataexplorer.controller.AnnotatorController.URI;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.AnnotationJob;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.settings.SettingsEntityMeta;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.util.ErrorMessageResponse;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;

@Controller
@RequestMapping(URI)
public class AnnotatorController
{
	private static final Logger LOG = LoggerFactory.getLogger(AnnotatorController.class);

	public static final String URI = "/annotators";
	private static final String TRIGGER_GROUP = "annotators";
	private static final String JOB_GROUP = "annotators";
	private static final String INDEX_REBUILD_JOB_KEY = "annotate";
	private final String triggerNameSalt;
	private final DataService dataService;
	private final AnnotationService annotationService;
	private final MolgenisPermissionService molgenisPermissionService;
	private final Scheduler scheduler;
	private final UserAccountService userAccountService;

	@Autowired
	public AnnotatorController(DataService dataService, AnnotationService annotationService,
			MolgenisPermissionService molgenisPermissionService, Scheduler scheduler,
			UserAccountService userAccountService)
	{
		this.dataService = dataService;
		this.annotationService = annotationService;
		this.molgenisPermissionService = molgenisPermissionService;
		this.scheduler = requireNonNull(scheduler);
		this.triggerNameSalt = UUID.randomUUID().toString();
		this.userAccountService = userAccountService;
	}

	/**
	 * Gets a map of all available annotators.
	 * 
	 * @param dataSetName
	 * @return annotatorMap
	 * 
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
	 * 
	 */
	@RequestMapping(value = "/annotate-data", method = RequestMethod.POST)
	@ResponseBody
	public String annotateData(HttpServletRequest request,
			@RequestParam(value = "annotatorNames", required = false) String[] annotatorNames,
			@RequestParam("dataset-identifier") String entityName)
	{
		Repository repository = dataService.getRepository(entityName);

		if (annotatorNames != null && repository != null)
		{
			ArrayList<RepositoryAnnotator> annotators = new ArrayList<>();
			Arrays.asList(annotatorNames).stream()
					.forEach(a -> annotators.add(annotationService.getAnnotatorByName(a)));
			try
			{
				scheduleAnnotatorRun(repository.getEntityMetaData().getSimpleName(), annotators);
			}
			catch (SchedulerException e)
			{
				e.printStackTrace();
			}
		}
		return entityName;
	}

	public TriggerKey scheduleAnnotatorRun(String entityName, List<RepositoryAnnotator> annotators)
			throws SchedulerException
	{
		TriggerKey triggerKey = getIndexRebuildTriggerKeyCurrentUser();
		if (!scheduler.checkExists(triggerKey))
		{
			JobDataMap jobDataMap = new JobDataMap();
			jobDataMap.put(AnnotationJob.REPOSITORY_NAME, entityName);
			jobDataMap.put(AnnotationJob.ANNOTATORS, annotators);
			jobDataMap.put(AnnotationJob.USERNAME, userAccountService.getCurrentUser().getUsername());
			TriggerBuilder<?> triggerBuilder = TriggerBuilder.newTrigger().withIdentity(triggerKey)
					.usingJobData(jobDataMap).startNow();
			scheduleAnnotatorRunJob(triggerBuilder);
		}
		else
		{
			throw new RuntimeException("Index rebuild already scheduled");
		}
		return triggerKey;
	}

	private JobKey scheduleAnnotatorRunJob(TriggerBuilder<?> triggerBuilder) throws SchedulerException
	{
		JobKey jobKey = getIndexRebuildJobKey();
		JobDetail jobDetail = scheduler.getJobDetail(jobKey);
		if (jobDetail == null)
		{
			jobDetail = JobBuilder.newJob(AnnotationJob.class).withIdentity(jobKey).build();
			scheduler.scheduleJob(jobDetail, triggerBuilder.build());
		}
		else
		{
			scheduler.scheduleJob(triggerBuilder.forJob(jobDetail).build());
		}

		return jobKey;
	}

	private TriggerKey getIndexRebuildTriggerKeyCurrentUser()
	{
		String rawTriggerName = triggerNameSalt + SecurityUtils.getCurrentUsername();

		// use MD5 hash to prevent ids that are too long
		MessageDigest messageDigest;
		try
		{
			messageDigest = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
		byte[] md5Hash = messageDigest.digest(rawTriggerName.getBytes(UTF_8));

		// convert MD5 hash to string ids that can be safely used in URLs
		String triggerName = BaseEncoding.base64Url().omitPadding().encode(md5Hash);

		return new TriggerKey(triggerName, TRIGGER_GROUP);
	}

	/**
	 * Sets a map of annotators, whether they can be used by the selected data set.
	 * 
	 * @param dataSetName
	 * @return mapOfAnnotators
	 * 
	 */
	private Map<String, Map<String, Object>> setMapOfAnnotators(String dataSetName)
	{
		Map<String, Map<String, Object>> mapOfAnnotators = new HashMap<String, Map<String, Object>>();

		if (dataSetName != null)
		{
			EntityMetaData entityMetaData = dataService.getEntityMetaData(dataSetName);
			for (RepositoryAnnotator annotator : annotationService.getAllAnnotators())
			{
				List<AttributeMetaData> outputAttrs = annotator.getOutputMetaData();
				outputAttrs = getAtomicAttributesFromList(outputAttrs);
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("description", annotator.getDescription());
				map.put("canAnnotate", annotator.canAnnotate(entityMetaData));
				map.put("inputAttributes", createAttrsResponse(annotator.getRequiredAttributes()));
				map.put("inputAttributeTypes", toMap(annotator.getRequiredAttributes()));
				map.put("outputAttributes", createAttrsResponse(outputAttrs));
				map.put("outputAttributeTypes", toMap(annotator.getOutputMetaData()));

				String settingsEntityName = SettingsEntityMeta.PACKAGE_NAME
						+ org.molgenis.data.Package.PACKAGE_SEPARATOR + annotator.getInfo().getCode();
				map.put("showSettingsButton",
						molgenisPermissionService.hasPermissionOnEntity(settingsEntityName, Permission.WRITE));
				mapOfAnnotators.put(annotator.getSimpleName(), map);
			}
		}
		return mapOfAnnotators;
	}

	private JobKey getIndexRebuildJobKey()
	{
		return new JobKey(INDEX_REBUILD_JOB_KEY, JOB_GROUP);
	}

	private List<Map<String, Object>> createAttrsResponse(List<AttributeMetaData> inputMetaData)
	{
		return inputMetaData.stream().map(attr -> {
			Map<String, Object> attrMap = new HashMap<String, Object>();
			attrMap.put("name", attr.getName());
			attrMap.put("description", attr.getDescription());
			return attrMap;
		}).collect(Collectors.toList());
	}

	private List<AttributeMetaData> getAtomicAttributesFromList(List<AttributeMetaData> outputAttrs)
	{
		if (outputAttrs.size() == 1
				&& outputAttrs.get(0).getDataType().getEnumType().equals(MolgenisFieldTypes.FieldTypeEnum.COMPOUND))
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
