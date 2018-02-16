package org.molgenis.ontology.sorta.controller;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.data.*;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.csv.CsvWriter;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.rest.EntityCollectionResponse;
import org.molgenis.data.rest.EntityPager;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.permission.PermissionSystemService;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.ontology.sorta.job.SortaJobExecution;
import org.molgenis.ontology.sorta.job.SortaJobExecutionFactory;
import org.molgenis.ontology.sorta.job.SortaJobFactory;
import org.molgenis.ontology.sorta.job.SortaJobImpl;
import org.molgenis.ontology.sorta.meta.MatchingTaskContentMetaData;
import org.molgenis.ontology.sorta.meta.SortaJobExecutionMetaData;
import org.molgenis.ontology.sorta.repo.SortaCsvRepository;
import org.molgenis.ontology.sorta.request.SortaServiceRequest;
import org.molgenis.ontology.sorta.request.SortaServiceResponse;
import org.molgenis.ontology.sorta.service.SortaService;
import org.molgenis.ontology.sorta.service.impl.SortaServiceImpl;
import org.molgenis.ontology.utils.SortaServiceUtil;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.core.runas.RunAsSystemAspect;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.web.PluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.molgenis.data.QueryRule.Operator.*;
import static org.molgenis.data.Sort.Direction.DESC;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.EntityType.AttributeCopyMode.DEEP_COPY_ATTRS;
import static org.molgenis.ontology.sorta.controller.SortaController.URI;
import static org.molgenis.ontology.sorta.meta.MatchingTaskContentMetaData.*;
import static org.molgenis.ontology.sorta.meta.SortaJobExecutionMetaData.SORTA_JOB_EXECUTION;
import static org.molgenis.ontology.utils.SortaServiceUtil.getEntityAsMap;

@Controller
@RequestMapping(URI)
public class SortaController extends PluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(SortaController.class);

	public static final String ID = "sorta";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	private static final int BATCH_SIZE = 1000;
	private static final String MODEL_KEY_MESSAGE = "message";
	public static final String MATCH_VIEW_NAME = "sorta-match-view";
	public static final double DEFAULT_THRESHOLD = 100.0;

	private final OntologyService ontologyService;
	private final SortaService sortaService;
	private final DataService dataService;
	private final UserAccountService userAccountService;
	private final SortaJobFactory sortaMatchJobFactory;
	private final ExecutorService taskExecutor;
	private final FileStore fileStore;
	private final UserPermissionEvaluator permissionService;
	private final MenuReaderService menuReaderService;
	private final IdGenerator idGenerator;
	private final PermissionSystemService permissionSystemService;
	private final MatchingTaskContentMetaData matchingTaskContentMetaData;
	private final SortaJobExecutionMetaData sortaJobExecutionMetaData;
	private final OntologyTermMetaData ontologyTermMetaData;
	private final SortaJobExecutionFactory sortaJobExecutionFactory;
	private final EntityTypeFactory entityTypeFactory;
	private final AttributeFactory attrMetaFactory;

	public SortaController(OntologyService ontologyService, SortaService sortaService,
			SortaJobFactory sortaMatchJobFactory, ExecutorService taskExecutor, UserAccountService userAccountService,
			FileStore fileStore, UserPermissionEvaluator permissionService, DataService dataService,
			MenuReaderService menuReaderService, IdGenerator idGenerator,
			PermissionSystemService permissionSystemService, MatchingTaskContentMetaData matchingTaskContentMetaData,
			SortaJobExecutionMetaData sortaJobExecutionMetaData, OntologyTermMetaData ontologyTermMetaData,
			SortaJobExecutionFactory sortaJobExecutionFactory, EntityTypeFactory entityTypeFactory,
			AttributeFactory attrMetaFactory)
	{
		super(URI);
		this.ontologyService = requireNonNull(ontologyService);
		this.sortaService = requireNonNull(sortaService);
		this.sortaMatchJobFactory = requireNonNull(sortaMatchJobFactory);
		this.taskExecutor = requireNonNull(taskExecutor);
		this.userAccountService = requireNonNull(userAccountService);
		this.fileStore = requireNonNull(fileStore);
		this.permissionService = requireNonNull(permissionService);
		this.dataService = requireNonNull(dataService);
		this.menuReaderService = requireNonNull(menuReaderService);
		this.idGenerator = requireNonNull(idGenerator);
		this.permissionSystemService = requireNonNull(permissionSystemService);
		this.matchingTaskContentMetaData = requireNonNull(matchingTaskContentMetaData);
		this.sortaJobExecutionMetaData = requireNonNull(sortaJobExecutionMetaData);
		this.ontologyTermMetaData = requireNonNull(ontologyTermMetaData);
		this.sortaJobExecutionFactory = requireNonNull(sortaJobExecutionFactory);
		this.entityTypeFactory = requireNonNull(entityTypeFactory);
		this.attrMetaFactory = requireNonNull(attrMetaFactory);
	}

	@GetMapping
	public String init(Model model)
	{
		model.addAttribute("existingTasks", getJobsForCurrentUser());
		return MATCH_VIEW_NAME;
	}

	private SortaJobExecution findSortaJobExecution(String sortaJobExecutionId)
	{
		Fetch fetch = new Fetch();
		sortaJobExecutionMetaData.getAtomicAttributes().forEach(attr -> fetch.field(attr.getName()));
		return RunAsSystemAspect.runAsSystem(
				() -> dataService.findOneById(SORTA_JOB_EXECUTION, sortaJobExecutionId, fetch,
						SortaJobExecution.class));
	}

	@GetMapping("/jobs")
	@ResponseBody
	public List<SortaJobExecution> getJobs()
	{
		return getJobsForCurrentUser();
	}

	@GetMapping("/newtask")
	public String matchTask(Model model)
	{
		model.addAttribute("ontologies", ontologyService.getOntologies());
		return MATCH_VIEW_NAME;
	}

	@PostMapping("/threshold/{sortaJobExecutionId}")
	public String updateThreshold(@RequestParam(value = "threshold") String threshold,
			@PathVariable String sortaJobExecutionId, Model model)
	{
		if (!StringUtils.isEmpty(threshold))
		{
			SortaJobExecution sortaJobExecution = findSortaJobExecution(sortaJobExecutionId);
			try
			{
				User currentUser = userAccountService.getCurrentUser();
				if (currentUser.isSuperuser() || sortaJobExecution.getUser().equals(currentUser.getUsername()))
				{
					RunAsSystemAspect.runAsSystem(() ->
					{
						Double thresholdValue = Double.parseDouble(threshold);
						sortaJobExecution.setThreshold(thresholdValue);
						dataService.update(SORTA_JOB_EXECUTION, sortaJobExecution);
					});
				}
			}
			catch (NumberFormatException e)
			{
				model.addAttribute(MODEL_KEY_MESSAGE, threshold + " is illegal threshold value!");
			}
			catch (Exception other)
			{
				model.addAttribute(MODEL_KEY_MESSAGE, "Error updating threshold: " + other.getMessage());
			}
		}

		return matchResult(sortaJobExecutionId, model);
	}

	@GetMapping("/result/{sortaJobExecutionId}")
	public String matchResult(@PathVariable("sortaJobExecutionId") String sortaJobExecutionId, Model model)
	{
		SortaJobExecution sortaJobExecution = findSortaJobExecution(sortaJobExecutionId);
		if (sortaJobExecution != null)
		{
			model.addAttribute("sortaJobExecutionId", sortaJobExecution.getIdentifier());
			model.addAttribute("threshold", sortaJobExecution.getThreshold());
			model.addAttribute("ontologyIri", sortaJobExecution.getOntologyIri());
			model.addAttribute("numberOfMatched", countMatchedEntities(sortaJobExecution, true));
			model.addAttribute("numberOfUnmatched", countMatchedEntities(sortaJobExecution, false));
			return MATCH_VIEW_NAME;
		}
		else
		{
			LOG.info("Job execution with id {} not found.", sortaJobExecutionId);
			model.addAttribute(MODEL_KEY_MESSAGE, "Job execution not found.");
			return init(model);
		}
	}

	@GetMapping("/count/{sortaJobExecutionId}")
	@ResponseBody
	public Map<String, Object> countMatchResult(@PathVariable("sortaJobExecutionId") String sortaJobExecutionId)
	{
		SortaJobExecution sortaJobExecution = findSortaJobExecution(sortaJobExecutionId);
		return ImmutableMap.of("numberOfMatched", countMatchedEntities(sortaJobExecution, true), "numberOfUnmatched",
				countMatchedEntities(sortaJobExecution, false));
	}

	@PostMapping("/delete/{sortaJobExecutionId}")
	@ResponseStatus(value = HttpStatus.OK)
	public String deleteResult(@PathVariable("sortaJobExecutionId") String sortaJobExecutionId, Model model)
	{
		SortaJobExecution sortaJobExecution = findSortaJobExecution(sortaJobExecutionId);
		if (sortaJobExecution != null)
		{
			User currentUser = userAccountService.getCurrentUser();
			if (currentUser.isSuperuser() || sortaJobExecution.getUser().equals(currentUser.getUsername()))
			{
				RunAsSystemAspect.runAsSystem(
						() -> dataService.deleteById(SORTA_JOB_EXECUTION, sortaJobExecution.getIdentifier()));
				tryDeleteRepository(sortaJobExecution.getResultEntityName());
				tryDeleteRepository(sortaJobExecution.getSourceEntityName());
			}
		}
		return init(model);
	}

	private void tryDeleteRepository(String entityTypeId)
	{
		if (dataService.hasRepository(entityTypeId) && permissionService.hasPermission(
				new EntityTypeIdentity(entityTypeId), EntityTypePermission.WRITEMETA))
		{
			RunAsSystemAspect.runAsSystem(() -> deleteRepository(entityTypeId));
		}
		else
		{
			LOG.info("Unable to delete repository {}", entityTypeId);
		}
	}

	private void deleteRepository(String entityTypeId)
	{
		try
		{
			dataService.getMeta().deleteEntityType(entityTypeId);
			LOG.info("Deleted repository {}", entityTypeId);
		}
		catch (Exception ex)
		{
			LOG.error("Failed to delete existing writable repository {}", entityTypeId);
		}
	}

	@PostMapping("/match/retrieve")
	@ResponseBody
	public EntityCollectionResponse retrieveSortaJobResults(@RequestBody SortaServiceRequest sortaServiceRequest)
	{
		List<Map<String, Object>> entityMaps = new ArrayList<>();
		String sortaJobExecutionId = sortaServiceRequest.getSortaJobExecutionId();
		String filterQuery = sortaServiceRequest.getFilterQuery();
		String ontologyIri = sortaServiceRequest.getOntologyIri();
		EntityPager entityPager = sortaServiceRequest.getEntityPager();
		SortaJobExecution sortaJobExecution = findSortaJobExecution(sortaJobExecutionId);
		String resultEntityName = sortaJobExecution.getResultEntityName();
		double threshold = sortaJobExecution.getThreshold();

		boolean isMatched = sortaServiceRequest.isMatched();

		QueryRule queryRuleInputEntities = new QueryRule(
				Arrays.asList(new QueryRule(VALIDATED, EQUALS, isMatched), new QueryRule(isMatched ? OR : AND),
						new QueryRule(SCORE, isMatched ? GREATER_EQUAL : LESS, threshold)));

		List<QueryRule> queryRuleInputEntitiesInOneMatchingTask = singletonList(queryRuleInputEntities);

		// Add filter to the query if query string is not empty
		if (isNotEmpty(filterQuery))
		{
			Iterable<String> filteredInputTermIds = dataService.findAll(sortaJobExecution.getSourceEntityName(),
					new QueryImpl<>().search(filterQuery))
															   .map(inputEntity -> inputEntity.getString(
																	   SortaServiceImpl.DEFAULT_MATCHING_IDENTIFIER))
															   .collect(Collectors.toList());
			QueryRule previousQueryRule = new QueryRule(queryRuleInputEntitiesInOneMatchingTask);
			QueryRule queryRuleFilterInput = new QueryRule(MatchingTaskContentMetaData.INPUT_TERM, Operator.IN,
					filteredInputTermIds);
			queryRuleInputEntitiesInOneMatchingTask = Arrays.asList(previousQueryRule, new QueryRule(Operator.AND),
					queryRuleFilterInput);
		}

		Query<Entity> query = new QueryImpl<>(queryRuleInputEntitiesInOneMatchingTask);
		long count = dataService.count(resultEntityName, query);
		int start = entityPager.getStart();
		int num = entityPager.getNum();

		Stream<Entity> findAll = dataService.findAll(sortaJobExecution.getResultEntityName(),
				query.offset(start).pageSize(num).sort(new Sort().on(VALIDATED, DESC).on(SCORE, DESC)));
		findAll.forEach(mappingEntity ->
		{
			Map<String, Object> outputEntity = new HashMap<>();
			outputEntity.put("inputTerm", getEntityAsMap(mappingEntity.getEntity(INPUT_TERM)));
			outputEntity.put("matchedTerm", getEntityAsMap(mappingEntity));
			Object matchedTerm = mappingEntity.get(MATCHED_TERM);
			if (matchedTerm != null)
			{
				outputEntity.put("ontologyTerm", SortaServiceUtil.getEntityAsMap(
						sortaService.getOntologyTermEntity(matchedTerm.toString(), ontologyIri)));
			}
			entityMaps.add(outputEntity);
		});

		EntityPager pager = new EntityPager(start, num, count, null);
		return new EntityCollectionResponse(pager, entityMaps, "/match/retrieve", ontologyTermMetaData,
				permissionService, dataService);
	}

	@PostMapping("/match")
	public String match(@RequestParam(value = "taskName") String jobName,
			@RequestParam(value = "selectOntologies") String ontologyIri,
			@RequestParam(value = "inputTerms") String inputTerms, Model model, HttpServletRequest httpServletRequest)
			throws IOException
	{
		if (isEmpty(ontologyIri) || isEmpty(inputTerms)) return init(model);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(inputTerms.getBytes("UTF8"));
		return startMatchJob(jobName, ontologyIri, model, httpServletRequest, inputStream);
	}

	@PostMapping(value = "/match/upload", headers = "Content-Type=multipart/form-data")
	public String upload(@RequestParam(value = "taskName") String jobName,
			@RequestParam(value = "selectOntologies") String ontologyIri,
			@RequestParam(value = "file") MultipartFile file, Model model, HttpServletRequest httpServletRequest)
			throws IOException
	{
		if (isEmpty(ontologyIri) || file == null) return init(model);
		InputStream inputStream = file.getInputStream();
		return startMatchJob(jobName, ontologyIri, model, httpServletRequest, inputStream);
	}

	@PostMapping("/match/entity")
	@ResponseBody
	public SortaServiceResponse findMatchingOntologyTerms(@RequestBody Map<String, Object> request)
	{
		// TODO: less obfuscated request object, let Spring do the matching
		if (request.containsKey("sortaJobExecutionId") && !isEmpty(request.get("sortaJobExecutionId").toString())
				&& request.containsKey(IDENTIFIER) && !isEmpty(request.get(IDENTIFIER).toString()))
		{
			String sortaJobExecutionId = request.get("sortaJobExecutionId").toString();
			SortaJobExecution sortaJobExecution = findSortaJobExecution(sortaJobExecutionId);
			if (sortaJobExecution == null) return new SortaServiceResponse("sortaJobExecutionId is invalid!");

			String inputTermIdentifier = request.get(IDENTIFIER).toString();
			Entity inputEntity = dataService.findOneById(sortaJobExecution.getSourceEntityName(), inputTermIdentifier);

			if (inputEntity == null) return new SortaServiceResponse("inputTerm identifier is invalid!");

			return new SortaServiceResponse(inputEntity,
					sortaService.findOntologyTermEntities(sortaJobExecution.getOntologyIri(), inputEntity));
		}
		return new SortaServiceResponse(
				"Please check that sortaJobExecutionId and identifier keys exist in input and have nonempty value!");
	}

	private Entity toDownloadRow(SortaJobExecution sortaJobExecution, Entity resultEntity,
			EntityType downloadEntityType)
	{
		NumberFormat format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(2);
		Entity inputEntity = resultEntity.getEntity(MatchingTaskContentMetaData.INPUT_TERM);
		Entity ontologyTermEntity = sortaService.getOntologyTermEntity(
				resultEntity.getString(MatchingTaskContentMetaData.MATCHED_TERM), sortaJobExecution.getOntologyIri());

		Entity row = new DynamicEntity(downloadEntityType);
		inputEntity.getAttributeNames().forEach(attributeName ->
		{
			if (!attributeName.equalsIgnoreCase(SortaCsvRepository.ALLOWED_IDENTIFIER))
			{
				row.set(attributeName, inputEntity.get(attributeName));
			}
		});
		if (ontologyTermEntity != null)
		{
			row.set(OntologyTermMetaData.ONTOLOGY_TERM_NAME,
					ontologyTermEntity.getString(OntologyTermMetaData.ONTOLOGY_TERM_NAME));
			row.set(OntologyTermMetaData.ONTOLOGY_TERM_IRI,
					ontologyTermEntity.getString(OntologyTermMetaData.ONTOLOGY_TERM_IRI));
		}
		row.set(MatchingTaskContentMetaData.VALIDATED, resultEntity.getBoolean(MatchingTaskContentMetaData.VALIDATED));
		Double score = resultEntity.getDouble(MatchingTaskContentMetaData.SCORE);
		if (score != null)
		{
			row.set(MatchingTaskContentMetaData.SCORE, format.format(score));
		}
		return row;
	}

	@GetMapping("/match/download/{sortaJobExecutionId}")
	public void download(@PathVariable String sortaJobExecutionId, HttpServletResponse response) throws IOException
	{
		try (CsvWriter csvWriter = new CsvWriter(response.getOutputStream(), SortaServiceImpl.DEFAULT_SEPARATOR))
		{
			SortaJobExecution sortaJobExecution = findSortaJobExecution(sortaJobExecutionId);

			response.setContentType("text/csv");
			response.addHeader("Content-Disposition", "attachment; filename=" + generateCsvFileName());

			List<String> columnHeaders = new ArrayList<>();
			EntityType targetMetadata = entityTypeFactory.create("SortaDownload" + sortaJobExecutionId);

			EntityType sourceMetaData = dataService.getEntityType(sortaJobExecution.getSourceEntityName());
			for (Attribute attribute : sourceMetaData.getAttributes())
			{
				if (!attribute.getName().equalsIgnoreCase(SortaCsvRepository.ALLOWED_IDENTIFIER))
				{
					columnHeaders.add(attribute.getName());
					targetMetadata.addAttribute(attrMetaFactory.create().setName(attribute.getName()));
				}
			}
			columnHeaders.addAll(
					Arrays.asList(OntologyTermMetaData.ONTOLOGY_TERM_NAME, OntologyTermMetaData.ONTOLOGY_TERM_IRI,
							MatchingTaskContentMetaData.SCORE, MatchingTaskContentMetaData.VALIDATED));
			targetMetadata.addAttribute(ontologyTermMetaData.getAttribute(OntologyTermMetaData.ONTOLOGY_TERM_NAME));
			targetMetadata.addAttribute(ontologyTermMetaData.getAttribute(OntologyTermMetaData.ONTOLOGY_TERM_IRI));
			targetMetadata.addAttribute(
					Attribute.newInstance(matchingTaskContentMetaData.getAttribute(MatchingTaskContentMetaData.SCORE),
							EntityType.AttributeCopyMode.SHALLOW_COPY_ATTRS, attrMetaFactory)
							 .setDataType(AttributeType.STRING));
			targetMetadata.addAttribute(
					matchingTaskContentMetaData.getAttribute(MatchingTaskContentMetaData.VALIDATED));

			csvWriter.writeAttributeNames(columnHeaders);

			dataService.findAll(sortaJobExecution.getResultEntityName(), new QueryImpl<>())
					   .forEach(resultEntity -> csvWriter.add(
							   toDownloadRow(sortaJobExecution, resultEntity, targetMetadata)));
		}
	}

	private String startMatchJob(String jobName, String ontologyIri, Model model, HttpServletRequest httpServletRequest,
			InputStream inputStream) throws IOException
	{
		String sessionId = httpServletRequest.getSession().getId();
		File uploadFile = fileStore.store(inputStream, sessionId
				+ ".csv"); // TODO determine whether multiple match jobs during the same session results in wrong file usage
		String inputRepositoryName = idGenerator.generateId();
		SortaCsvRepository inputRepository = new SortaCsvRepository(inputRepositoryName, jobName + " input", uploadFile,
				entityTypeFactory, attrMetaFactory);

		if (!validateFileHeader(inputRepository))
		{
			model.addAttribute(MODEL_KEY_MESSAGE, "The Name header is missing!");
			return matchTask(model);
		}

		if (!validateEmptyFileHeader(inputRepository))
		{
			model.addAttribute(MODEL_KEY_MESSAGE, "The empty header is not allowed!");
			return matchTask(model);
		}

		if (!validateInputFileContent(inputRepository))
		{
			model.addAttribute(MODEL_KEY_MESSAGE, "The content of input is empty!");
			return matchTask(model);
		}

		SortaJobExecution jobExecution = createJobExecution(inputRepository, jobName, ontologyIri);
		SortaJobImpl sortaMatchJob = sortaMatchJobFactory.create(jobExecution);
		taskExecutor.submit(sortaMatchJob);

		return "redirect:" + getSortaServiceMenuUrl();
	}

	private List<SortaJobExecution> getJobsForCurrentUser()
	{
		final List<SortaJobExecution> jobs = new ArrayList<>();
		User currentUser = userAccountService.getCurrentUser();
		Query<SortaJobExecution> query = dataService.query(SORTA_JOB_EXECUTION, SortaJobExecution.class)
													.eq(JobExecutionMetaData.USER, currentUser.getUsername());
		query.sort().on(JobExecutionMetaData.START_DATE, DESC);
		RunAsSystemAspect.runAsSystem(() -> query.findAll().forEach(job ->
		{
			// TODO: fetch the user as well
			job.set(JobExecutionMetaData.USER, currentUser.getUsername());
			jobs.add(job);
		}));
		return jobs;
	}

	private SortaJobExecution createJobExecution(Repository<Entity> inputData, String jobName, String ontologyIri)
	{
		String resultEntityName = idGenerator.generateId();

		SortaJobExecution sortaJobExecution = sortaJobExecutionFactory.create();
		sortaJobExecution.setIdentifier(resultEntityName);
		sortaJobExecution.setName(jobName);
		sortaJobExecution.setUser(userAccountService.getCurrentUser());
		sortaJobExecution.setSourceEntityName(inputData.getName());
		sortaJobExecution.setDeleteUrl(getSortaServiceMenuUrl() + "/delete/" + resultEntityName);
		sortaJobExecution.setResultEntityName(resultEntityName);
		sortaJobExecution.setThreshold(DEFAULT_THRESHOLD);
		sortaJobExecution.setOntologyIri(ontologyIri);

		RunAsSystemAspect.runAsSystem(() ->
		{
			createInputRepository(inputData);
			createEmptyResultRepository(jobName, resultEntityName, inputData.getEntityType());
			dataService.add(SORTA_JOB_EXECUTION, sortaJobExecution);
		});

		EntityType resultEntityType = entityTypeFactory.create(resultEntityName);
		permissionSystemService.giveUserWriteMetaPermissions(asList(inputData.getEntityType(), resultEntityType));

		return sortaJobExecution;
	}

	private void createEmptyResultRepository(String jobName, String resultEntityName, EntityType sourceMetaData)
	{
		EntityType resultEntityType = EntityType.newInstance(matchingTaskContentMetaData, DEEP_COPY_ATTRS,
				attrMetaFactory);
		resultEntityType.setId(resultEntityName);
		resultEntityType.setPackage(null);
		resultEntityType.setAbstract(false);
		resultEntityType.addAttribute(attrMetaFactory.create()
													 .setName(INPUT_TERM)
													 .setDataType(XREF)
													 .setRefEntity(sourceMetaData)
													 .setDescription("Reference to the input term")
													 .setNillable(false));
		resultEntityType.setLabel(jobName + " output");
		dataService.getMeta().addEntityType(resultEntityType);
	}

	private void createInputRepository(Repository<Entity> inputRepository)
	{
		// Add the original input dataset to database
		dataService.getMeta().addEntityType(inputRepository.getEntityType());

		Repository<Entity> target = dataService.getRepository(inputRepository.getName());
		inputRepository.forEachBatched(entities -> target.add(entities.stream()), BATCH_SIZE);
	}

	private long countMatchedEntities(SortaJobExecution sortaJobExecution, boolean isMatched)
	{
		double threshold = sortaJobExecution.getThreshold();
		QueryRule validatedRule = new QueryRule(MatchingTaskContentMetaData.VALIDATED, EQUALS, isMatched);
		QueryRule thresholdRule = new QueryRule(MatchingTaskContentMetaData.SCORE, isMatched ? GREATER_EQUAL : LESS,
				threshold);
		QueryRule combinedRule = new QueryRule(
				asList(validatedRule, new QueryRule(isMatched ? OR : AND), thresholdRule));

		return dataService.count(sortaJobExecution.getResultEntityName(), new QueryImpl<>(combinedRule));
	}

	private String generateCsvFileName()
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return "match-result" + "_" + dateFormat.format(new Date()) + ".csv";
	}

	private boolean validateFileHeader(Repository<Entity> repository)
	{
		return StreamSupport.stream(repository.getEntityType().getAttributes().spliterator(), false)
							.map(Attribute::getName)
							.anyMatch(name -> name.equalsIgnoreCase(SortaServiceImpl.DEFAULT_MATCHING_NAME_FIELD));
	}

	private boolean validateEmptyFileHeader(Repository<Entity> repository)
	{
		return StreamSupport.stream(repository.getEntityType().getAttributes().spliterator(), false)
							.map(Attribute::getName)
							.anyMatch(StringUtils::isNotBlank);
	}

	private boolean validateInputFileContent(Repository<Entity> repository)
	{
		return repository.iterator().hasNext();
	}

	private String getSortaServiceMenuUrl()
	{
		return menuReaderService.getMenu().findMenuItemPath(ID);
	}
}