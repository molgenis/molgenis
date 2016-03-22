package org.molgenis.ontology.controller;

import static org.molgenis.data.QueryRule.Operator.AND;
import static org.molgenis.data.QueryRule.Operator.EQUALS;
import static org.molgenis.data.QueryRule.Operator.GREATER_EQUAL;
import static org.molgenis.data.QueryRule.Operator.LESS;
import static org.molgenis.data.QueryRule.Operator.OR;
import static org.molgenis.ontology.controller.SortaServiceController.URI;
import static org.molgenis.ontology.sorta.meta.MatchingTaskContentEntityMetaData.SCORE;
import static org.molgenis.ontology.sorta.meta.MatchingTaskContentEntityMetaData.VALIDATED;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.UserAuthority;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.Repository;
import org.molgenis.data.Sort;
import org.molgenis.data.Sort.Direction;
import org.molgenis.data.csv.CsvWriter;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.jobs.JobExecution;
import org.molgenis.data.meta.MetaValidationUtils;
import org.molgenis.data.rest.EntityCollectionResponse;
import org.molgenis.data.rest.EntityPager;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.file.FileStore;
import org.molgenis.ontology.core.meta.OntologyMetaData;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.ontology.roc.MatchQualityRocService;
import org.molgenis.ontology.sorta.job.SortaJobExecution;
import org.molgenis.ontology.sorta.job.SortaJobFactory;
import org.molgenis.ontology.sorta.job.SortaJobImpl;
import org.molgenis.ontology.sorta.meta.MatchingTaskContentEntityMetaData;
import org.molgenis.ontology.sorta.meta.MatchingTaskEntityMetaData;
import org.molgenis.ontology.sorta.repo.SortaCsvRepository;
import org.molgenis.ontology.sorta.request.SortaServiceRequest;
import org.molgenis.ontology.sorta.request.SortaServiceResponse;
import org.molgenis.ontology.sorta.service.SortaService;
import org.molgenis.ontology.sorta.service.impl.SortaServiceImpl;
import org.molgenis.ontology.utils.SortaServiceUtil;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.ui.menu.MenuReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import static java.util.Objects.requireNonNull;

@Controller
@RequestMapping(URI)
public class SortaServiceController extends MolgenisPluginController
{
	private final OntologyService ontologyService;
	private final SortaService sortaService;
	private final MatchQualityRocService matchQualityRocService;
	private final DataService dataService;
	private final UserAccountService userAccountService;
	private final SortaJobFactory sortaMatchJobFactory;
	private final ExecutorService taskExecutor;
	private final FileStore fileStore;
	private final MolgenisPermissionService molgenisPermissionService;
	private final LanguageService languageService;
	private final MenuReaderService menuReaderService;

	public static final String VIEW_NAME = "sorta-match-view";
	public static final String ID = "sortaservice";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String ILLEGAL_PATTERN = "[^0-9a-zA-Z_]";
	private static final String ILLEGAL_PATTERN_REPLACEMENT = "_";
	private static final double DEFAULT_THRESHOLD = 100.0;

	@Autowired
	public SortaServiceController(OntologyService ontologyService, SortaService sortaService,
			MatchQualityRocService matchQualityRocService, SortaJobFactory sortaMatchJobFactory,
			ExecutorService taskExecutor, UserAccountService userAccountService, FileStore fileStore,
			MolgenisPermissionService molgenisPermissionService, DataService dataService,
			LanguageService languageService, MenuReaderService menuReaderService)
	{
		super(URI);
		this.ontologyService = requireNonNull(ontologyService);
		this.sortaService = requireNonNull(sortaService);
		this.matchQualityRocService = requireNonNull(matchQualityRocService);
		this.sortaMatchJobFactory = requireNonNull(sortaMatchJobFactory);
		this.taskExecutor = requireNonNull(taskExecutor);
		this.userAccountService = requireNonNull(userAccountService);
		this.fileStore = requireNonNull(fileStore);
		this.molgenisPermissionService = requireNonNull(molgenisPermissionService);
		this.dataService = requireNonNull(dataService);
		this.languageService = requireNonNull(languageService);
		this.menuReaderService = requireNonNull(menuReaderService);
	}

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		model.addAttribute("existingTasks", getJobsForCurrentUser());
		return VIEW_NAME;
	}

	@RequestMapping(method = GET, value = "/jobs")
	@ResponseBody
	public List<Entity> getJobs(Model model)
	{
		return getJobsForCurrentUser();
	}

	@RequestMapping(method = GET, value = "/newtask")
	public String matchTask(Model model)
	{
		model.addAttribute("ontologies", ontologyService.getOntologies());
		return VIEW_NAME;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/calculate/{entityName}")
	public String calculateRoc(@PathVariable String entityName, Model model)
			throws IOException, MolgenisInvalidFormatException
	{
		model.addAllAttributes(matchQualityRocService.calculateROC(entityName));
		return init(model);
	}

	@RequestMapping(method = POST, value = "/threshold/{entityName}")
	public String updateThreshold(@RequestParam(value = "threshold", required = true) String threshold,
			@PathVariable String entityName, Model model)
	{
		if (!StringUtils.isEmpty(threshold))
		{
			Entity entity = dataService.findOne(MatchingTaskEntityMetaData.ENTITY_NAME,
					new QueryImpl().eq(MatchingTaskEntityMetaData.IDENTIFIER, entityName));
			try
			{
				Double threshold_value = Double.parseDouble(threshold);
				entity.set(MatchingTaskEntityMetaData.THRESHOLD, threshold_value);
				dataService.update(MatchingTaskEntityMetaData.ENTITY_NAME, entity);
				dataService.getRepository(MatchingTaskEntityMetaData.ENTITY_NAME).flush();
			}
			catch (Exception e)
			{
				model.addAttribute("message", threshold + " is illegal threshold value!");
			}
		}

		return matchResult(entityName, model);
	}

	@RequestMapping(method = GET, value = "/result/{entityName}")
	public String matchResult(@PathVariable("entityName") String targetEntityName, Model model)
	{
		Entity entity = dataService.findOne(MatchingTaskEntityMetaData.ENTITY_NAME,
				new QueryImpl().eq(MatchingTaskEntityMetaData.IDENTIFIER, targetEntityName));
		model.addAttribute("threshold", entity.get(MatchingTaskEntityMetaData.THRESHOLD));
		model.addAttribute("ontologyIri", entity.get(MatchingTaskEntityMetaData.CODE_SYSTEM));
		model.addAttribute("numberOfMatched", countMatchedEntities(targetEntityName, true));
		model.addAttribute("numberOfUnmatched", countMatchedEntities(targetEntityName, false));

		return VIEW_NAME;
	}

	@RequestMapping(method = GET, value = "/count/{entityName}")
	@ResponseBody
	public Map<String, Object> countMatchResult(@PathVariable("entityName") String entityName)
	{
		return ImmutableMap.of("numberOfMatched", countMatchedEntities(entityName, true), "numberOfUnmatched",
				countMatchedEntities(entityName, false));
	}

	@RequestMapping(method = POST, value = "/delete/{entityName}")
	@ResponseStatus(value = HttpStatus.OK)
	public String deleteResult(@PathVariable("entityName") String entityName, Model model)
	{
		// Remove all the matching terms from MatchingTaskContentEntity table
		if (dataService.count(MatchingTaskContentEntityMetaData.ENTITY_NAME,
				new QueryImpl().eq(MatchingTaskContentEntityMetaData.REF_ENTITY, entityName)) > 0)
		{
			Stream<Entity> iterableMatchingEntities = dataService.findAll(MatchingTaskContentEntityMetaData.ENTITY_NAME,
					new QueryImpl().eq(MatchingTaskContentEntityMetaData.REF_ENTITY, entityName));
			dataService.delete(MatchingTaskContentEntityMetaData.ENTITY_NAME, iterableMatchingEntities);
		}

		// Remove the matching task meta information from MatchingTaskEntity table
		Entity matchingSummaryEntity = dataService.findOne(MatchingTaskEntityMetaData.ENTITY_NAME,
				new QueryImpl().eq(MatchingTaskEntityMetaData.IDENTIFIER, entityName));
		if (matchingSummaryEntity != null)
		{
			dataService.delete(MatchingTaskEntityMetaData.ENTITY_NAME, matchingSummaryEntity);
			dataService.getRepository(MatchingTaskEntityMetaData.ENTITY_NAME).flush();
		}

		Entity jobEntity = dataService.findOne(SortaJobExecution.ENTITY_NAME,
				QueryImpl.EQ(SortaJobExecution.USER, userAccountService.getCurrentUser()).and()
						.eq(SortaJobExecution.TARGET_ENTITY, entityName));
		// Drop the job record from the SortaJobExecuation Entity. It's not possible to delete a record from the
		// SortaJobExecution as a regular user because the user doesn't have the permission to read the table
		// MolgenisUser to which the column molgenisUser in SortaJobExecution refers
		if (jobEntity != null)
		{
			RunAsSystemProxy.runAsSystem(() -> {
				dataService.delete(SortaJobExecution.ENTITY_NAME, jobEntity);
				dataService.getRepository(SortaJobExecution.ENTITY_NAME).flush();
			});
		}

		// Drop the table that contains the information for raw data (input terms). It's not possible to delete the
		// EntityMetaData as a regular user because the ueser doesn't have the permission to change the entities and
		// attributes tables
		if (dataService.hasRepository(entityName)
				&& molgenisPermissionService.hasPermissionOnEntity(entityName, Permission.WRITEMETA))
		{
			RunAsSystemProxy.runAsSystem(() -> {
				dataService.getMeta().deleteEntityMeta(entityName);
			});
		}
		return init(model);
	}

	@RequestMapping(method = POST, value = "/match/retrieve")
	@ResponseBody
	public EntityCollectionResponse matchResult(@RequestBody SortaServiceRequest sortaServiceRequest,
			HttpServletRequest httpServletRequest)
	{
		List<Map<String, Object>> entityMaps = new ArrayList<Map<String, Object>>();
		String entityName = sortaServiceRequest.getEntityName();
		String filterQuery = sortaServiceRequest.getFilterQuery();
		String ontologyIri = sortaServiceRequest.getOntologyIri();
		EntityPager entityPager = sortaServiceRequest.getEntityPager();
		boolean isMatched = sortaServiceRequest.isMatched();
		Entity entity = dataService.findOne(MatchingTaskEntityMetaData.ENTITY_NAME,
				new QueryImpl().eq(MatchingTaskEntityMetaData.IDENTIFIER, entityName));
		Double threshold = Double.parseDouble(entity.get(MatchingTaskEntityMetaData.THRESHOLD).toString());

		QueryRule queryRuleInputEntities = new QueryRule(
				Arrays.asList(new QueryRule(VALIDATED, EQUALS, isMatched), new QueryRule(isMatched ? OR : AND),
						new QueryRule(SCORE, isMatched ? GREATER_EQUAL : LESS, threshold)));

		QueryRule queryRuleMatchingTask = new QueryRule(MatchingTaskContentEntityMetaData.REF_ENTITY, Operator.EQUALS,
				entityName);

		List<QueryRule> queryRuleInputEntitiesInOneMatchingTask = Arrays.asList(queryRuleMatchingTask,
				new QueryRule(Operator.AND), queryRuleInputEntities);

		// Add filter to the query if query string is not empty
		if (StringUtils.isNotEmpty(filterQuery))
		{
			Iterable<String> filteredInputTermIds = dataService.findAll(entityName, new QueryImpl().search(filterQuery))
					.map(inputEntity -> inputEntity.getString(SortaServiceImpl.DEFAULT_MATCHING_IDENTIFIER))
					.collect(Collectors.toList());
			QueryRule previousQueryRule = new QueryRule(queryRuleInputEntitiesInOneMatchingTask);
			QueryRule queryRuleFilterInput = new QueryRule(MatchingTaskContentEntityMetaData.INPUT_TERM, Operator.IN,
					filteredInputTermIds);
			queryRuleInputEntitiesInOneMatchingTask = Arrays.asList(previousQueryRule, new QueryRule(Operator.AND),
					queryRuleFilterInput);
		}

		Query query = new QueryImpl(queryRuleInputEntitiesInOneMatchingTask);
		long count = dataService.count(MatchingTaskContentEntityMetaData.ENTITY_NAME, query);
		int start = entityPager.getStart();
		int num = entityPager.getNum();

		Stream<Entity> findAll = dataService.findAll(MatchingTaskContentEntityMetaData.ENTITY_NAME,
				query.offset(start).pageSize(num)
						.sort(new Sort().on(MatchingTaskContentEntityMetaData.VALIDATED, Direction.DESC)
								.on(MatchingTaskContentEntityMetaData.SCORE, Direction.DESC)));
		findAll.forEach(mappingEntity -> {
			Entity RefEntity = dataService.findOne(entityName, new QueryImpl().eq(SortaCsvRepository.ALLOWED_IDENTIFIER,
					mappingEntity.getString(MatchingTaskContentEntityMetaData.INPUT_TERM)));
			Map<String, Object> outputEntity = new HashMap<String, Object>();
			outputEntity.put("inputTerm", SortaServiceUtil.getEntityAsMap(RefEntity));
			outputEntity.put("matchedTerm", SortaServiceUtil.getEntityAsMap(mappingEntity));
			Object matchedTerm = mappingEntity.get(MatchingTaskContentEntityMetaData.MATCHED_TERM);
			if (matchedTerm != null)
			{
				outputEntity.put("ontologyTerm", SortaServiceUtil
						.getEntityAsMap(sortaService.getOntologyTermEntity(matchedTerm.toString(), ontologyIri)));
			}
			entityMaps.add(outputEntity);
		});

		EntityPager pager = new EntityPager(start, num, count, null);
		return new EntityCollectionResponse(pager, entityMaps, "/match/retrieve", OntologyTermMetaData.INSTANCE,
				molgenisPermissionService, dataService, languageService);
	}

	@RequestMapping(method = POST, value = "/match")
	public String match(@RequestParam(value = "taskName", required = true) String entityName,
			@RequestParam(value = "selectOntologies", required = true) String ontologyIri,
			@RequestParam(value = "inputTerms", required = true) String inputTerms, Model model,
			HttpServletRequest httpServletRequest) throws Exception
	{
		if (StringUtils.isEmpty(ontologyIri) || StringUtils.isEmpty(inputTerms)) return init(model);
		String sessionId = httpServletRequest.getSession().getId();
		File uploadFile = fileStore.store(new ByteArrayInputStream(inputTerms.getBytes("UTF8")),
				sessionId + "_input.txt");
		return startMatchJob(entityName, ontologyIri, uploadFile, model);
	}

	@RequestMapping(method = POST, value = "/match/upload", headers = "Content-Type=multipart/form-data")
	public String upload(@RequestParam(value = "taskName", required = true) String entityName,
			@RequestParam(value = "selectOntologies", required = true) String ontologyIri,
			@RequestParam(value = "file", required = true) Part file, Model model,
			HttpServletRequest httpServletRequest) throws Exception
	{
		if (StringUtils.isEmpty(ontologyIri) || file == null) return init(model);
		String sessionId = httpServletRequest.getSession().getId();
		File uploadFile = fileStore.store(file.getInputStream(), sessionId + "_input.csv");
		return startMatchJob(entityName, ontologyIri, uploadFile, model);
	}

	@RequestMapping(method = POST, value = "/match/entity")
	@ResponseBody
	public SortaServiceResponse matchResult(@RequestBody Map<String, Object> request,
			HttpServletRequest httpServletRequest)
	{
		if (request.containsKey("entityName") && !StringUtils.isEmpty(request.get("entityName").toString())
				&& request.containsKey(MatchingTaskContentEntityMetaData.IDENTIFIER)
				&& !StringUtils.isEmpty(request.get(MatchingTaskContentEntityMetaData.IDENTIFIER).toString()))
		{
			String entityName = request.get("entityName").toString();
			String inputTermIdentifier = request.get(MatchingTaskContentEntityMetaData.IDENTIFIER).toString();
			Entity matchingTaskEntity = dataService.findOne(MatchingTaskEntityMetaData.ENTITY_NAME,
					new QueryImpl().eq(MatchingTaskEntityMetaData.IDENTIFIER, entityName));
			Entity inputEntity = dataService.findOne(entityName,
					new QueryImpl().eq(MatchingTaskContentEntityMetaData.IDENTIFIER, inputTermIdentifier));

			if (matchingTaskEntity == null || inputEntity == null)
				return new SortaServiceResponse("entityName or inputTermIdentifier is invalid!");

			return new SortaServiceResponse(inputEntity, sortaService.findOntologyTermEntities(
					matchingTaskEntity.getString(MatchingTaskEntityMetaData.CODE_SYSTEM), inputEntity));
		}
		return new SortaServiceResponse("Please check entityName, inputTermIdentifier exist in input!");
	}

	@RequestMapping(method = POST, value = "/search")
	@ResponseBody
	public SortaServiceResponse search(@RequestBody Map<String, Object> request, HttpServletRequest httpServletRequest)
	{
		if (request.containsKey("queryString") && !StringUtils.isEmpty(request.get("queryString").toString())
				&& request.containsKey(OntologyMetaData.ONTOLOGY_IRI)
				&& !StringUtils.isEmpty(request.get(OntologyMetaData.ONTOLOGY_IRI).toString()))
		{
			String queryString = request.get("queryString").toString();
			String ontologyIri = request.get(OntologyMetaData.ONTOLOGY_IRI).toString();
			Entity inputEntity = new MapEntity(
					Collections.singletonMap(SortaServiceImpl.DEFAULT_MATCHING_NAME_FIELD, queryString));

			return new SortaServiceResponse(inputEntity,
					sortaService.findOntologyTermEntities(ontologyIri, inputEntity));
		}
		return new SortaServiceResponse("Please check entityName, inputTermIdentifier exist in input!");
	}

	@RequestMapping(method = GET, value = "/match/download/{entityName}")
	public void download(@PathVariable String entityName, HttpServletResponse response, Model model) throws IOException
	{
		CsvWriter csvWriter = new CsvWriter(response.getOutputStream(), SortaServiceImpl.DEFAULT_SEPARATOR);
		try
		{
			response.setContentType("text/csv");
			response.addHeader("Content-Disposition", "attachment; filename=" + generateCsvFileName("match-result"));
			List<String> columnHeaders = new ArrayList<String>();
			for (AttributeMetaData attributeMetaData : dataService.getEntityMetaData(entityName).getAttributes())
			{
				if (!attributeMetaData.getName().equalsIgnoreCase(MatchingTaskEntityMetaData.IDENTIFIER))
					columnHeaders.add(attributeMetaData.getName());
			}
			columnHeaders.addAll(
					Arrays.asList(OntologyTermMetaData.ONTOLOGY_TERM_NAME, OntologyTermMetaData.ONTOLOGY_TERM_IRI,
							MatchingTaskContentEntityMetaData.SCORE, MatchingTaskContentEntityMetaData.VALIDATED));
			csvWriter.writeAttributeNames(columnHeaders);

			Entity matchingTaskEntity = dataService.findOne(MatchingTaskEntityMetaData.ENTITY_NAME,
					new QueryImpl().eq(MatchingTaskEntityMetaData.IDENTIFIER, entityName));

			dataService
					.findAll(MatchingTaskContentEntityMetaData.ENTITY_NAME,
							new QueryImpl().eq(MatchingTaskContentEntityMetaData.REF_ENTITY, entityName))
					.forEach(mappingEntity -> {
						Entity inputEntity = dataService.findOne(entityName,
								new QueryImpl().eq(MatchingTaskEntityMetaData.IDENTIFIER,
										mappingEntity.getString(MatchingTaskContentEntityMetaData.INPUT_TERM)));
						Entity ontologyTermEntity = sortaService.getOntologyTermEntity(
								mappingEntity.getString(MatchingTaskContentEntityMetaData.MATCHED_TERM),
								matchingTaskEntity.getString(MatchingTaskEntityMetaData.CODE_SYSTEM));
						MapEntity row = new MapEntity(inputEntity);
						row.set(OntologyTermMetaData.ONTOLOGY_TERM_NAME,
								ontologyTermEntity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME));
						row.set(OntologyTermMetaData.ONTOLOGY_TERM_IRI,
								ontologyTermEntity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI));
						row.set(MatchingTaskContentEntityMetaData.VALIDATED,
								mappingEntity.get(MatchingTaskContentEntityMetaData.VALIDATED));
						row.set(MatchingTaskContentEntityMetaData.SCORE,
								mappingEntity.get(MatchingTaskContentEntityMetaData.SCORE));
						csvWriter.add(row);
					});
		}
		finally
		{
			if (csvWriter != null) IOUtils.closeQuietly(csvWriter);
		}
	}

	private String startMatchJob(String targetEntityName, String ontologyIri, File uploadedFile, Model model)
			throws IOException
	{
		targetEntityName = targetEntityName.replaceAll(ILLEGAL_PATTERN, ILLEGAL_PATTERN_REPLACEMENT).toLowerCase();

		MetaValidationUtils.validateName(targetEntityName);

		if (dataService.hasRepository(targetEntityName))
		{
			Entity matchingTaskEntity = dataService.findOne(MatchingTaskEntityMetaData.ENTITY_NAME,
					new QueryImpl().eq(MatchingTaskEntityMetaData.IDENTIFIER, targetEntityName));
			model.addAttribute("message",
					"The task name should be case insensitive, the task name <strong>" + targetEntityName
							+ "</strong> has existed and created by user : "
							+ (matchingTaskEntity != null
									? matchingTaskEntity.get(MatchingTaskEntityMetaData.MOLGENIS_USER)
									: StringUtils.EMPTY));
			return init(model);
		}
		SortaCsvRepository repository = new SortaCsvRepository(targetEntityName, uploadedFile);

		if (!validateFileHeader(repository))
		{
			model.addAttribute("message", "The Name header is missing!");
			return matchTask(model);
		}

		if (!validateEmptyFileHeader(repository))
		{
			model.addAttribute("message", "The empty header is not allowed!");
			return matchTask(model);
		}

		if (!validateInputFileContent(repository))
		{
			model.addAttribute("message", "The content of input is empty!");
			return matchTask(model);
		}

		JobExecution jobExecution = createJobExecution(repository, ontologyIri, SecurityContextHolder.getContext());
		SortaJobImpl sortaMatchJob = sortaMatchJobFactory.create(ontologyIri, repository.getName(), jobExecution,
				SecurityContextHolder.getContext());
		taskExecutor.submit(sortaMatchJob);

		return "redirect:" + getSortaServiceMenuUrl();
	}

	private List<Entity> getJobsForCurrentUser()
	{
		final List<Entity> jobs = new ArrayList<>();
		MolgenisUser currentUser = userAccountService.getCurrentUser();
		Query query = QueryImpl.EQ(SortaJobExecution.USER, currentUser);
		query.sort().on(SortaJobExecution.START_DATE, Direction.ASC);
		RunAsSystemProxy.runAsSystem(() -> {
			dataService.findAll(SortaJobExecution.ENTITY_NAME, query).forEach(job -> {
				job.set(SortaJobExecution.USER, currentUser);
				jobs.add(job);
			});
		});
		return jobs;
	}

	private JobExecution createJobExecution(Repository repository, String ontologyIri, SecurityContext securityContext)
	{
		// Add a new entry in MatchingTask table for this new matching job
		MapEntity mapEntity = new MapEntity();
		mapEntity.set(MatchingTaskEntityMetaData.IDENTIFIER, repository.getName());
		mapEntity.set(MatchingTaskEntityMetaData.DATA_CREATED, new Date());
		mapEntity.set(MatchingTaskEntityMetaData.CODE_SYSTEM, ontologyIri);
		mapEntity.set(MatchingTaskEntityMetaData.MOLGENIS_USER, userAccountService.getCurrentUser().getUsername());
		mapEntity.set(MatchingTaskEntityMetaData.THRESHOLD, DEFAULT_THRESHOLD);

		// Create a Sorta Job Execution
		SortaJobExecution jobExecution = new SortaJobExecution(dataService);
		jobExecution.setUser(userAccountService.getCurrentUser());
		jobExecution.setResultUrl(getSortaServiceMenuUrl() + "/result/" + repository.getName());
		jobExecution.setDeleteUrl(getSortaServiceMenuUrl() + "/delete/" + repository.getName());
		jobExecution.setTargetEntityName(repository.getName());
		jobExecution.setOntologyIri(ontologyIri);

		MolgenisUser molgenisUser = userAccountService.getCurrentUser();

		RunAsSystemProxy.runAsSystem(() -> {
			// Add the original input dataset to database
			dataService.getMeta().addEntityMeta(repository.getEntityMetaData());
			dataService.getRepository(repository.getName()).add(repository.stream());
			dataService.getRepository(repository.getName()).flush();
			dataService.add(MatchingTaskEntityMetaData.ENTITY_NAME, mapEntity);
			dataService.getRepository(MatchingTaskEntityMetaData.ENTITY_NAME).flush();
			dataService.add(SortaJobExecution.ENTITY_NAME, jobExecution);

			// FIXME : temporary work around to assign write permissions to the
			// users who create the entities.
			Authentication auth = securityContext.getAuthentication();
			List<GrantedAuthority> roles = Lists.newArrayList(auth.getAuthorities());
			for (Permission permiossion : Permission.values())
			{
				UserAuthority userAuthority = new UserAuthority();
				userAuthority.setMolgenisUser(molgenisUser);
				String role = SecurityUtils.AUTHORITY_ENTITY_PREFIX + permiossion.toString() + "_"
						+ repository.getName().toUpperCase();
				userAuthority.setRole(role);
				roles.add(new SimpleGrantedAuthority(role));
				dataService.add(UserAuthority.ENTITY_NAME, userAuthority);
				dataService.getRepository(UserAuthority.ENTITY_NAME).flush();
			}
			auth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), null, roles);
			securityContext.setAuthentication(auth);
		});

		return jobExecution;
	}

	private long countMatchedEntities(String entityName, boolean isMatched)
	{
		Entity entity = dataService.findOne(MatchingTaskEntityMetaData.ENTITY_NAME,
				new QueryImpl().eq(MatchingTaskEntityMetaData.IDENTIFIER, entityName));

		double threshold = entity.getDouble(MatchingTaskEntityMetaData.THRESHOLD);

		QueryRule rule_1 = new QueryRule(MatchingTaskContentEntityMetaData.REF_ENTITY, Operator.EQUALS, entityName);

		QueryRule rule_2 = new QueryRule(MatchingTaskContentEntityMetaData.VALIDATED, Operator.EQUALS, isMatched);

		QueryRule rule_3 = new QueryRule(MatchingTaskContentEntityMetaData.SCORE,
				isMatched ? Operator.GREATER_EQUAL : Operator.LESS, threshold);

		QueryRule combinedRule = new QueryRule(
				Arrays.asList(rule_2, new QueryRule(isMatched ? Operator.OR : Operator.AND), rule_3));

		return dataService.count(MatchingTaskContentEntityMetaData.ENTITY_NAME,
				new QueryImpl(Arrays.asList(rule_1, new QueryRule(Operator.AND), combinedRule)));
	}

	private String generateCsvFileName(String dataSetName)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dataSetName + "_" + dateFormat.format(new Date()) + ".csv";
	}

	private boolean validateFileHeader(Repository repository)
	{
		boolean containsName = StreamSupport.stream(repository.getEntityMetaData().getAttributes().spliterator(), false)
				.map(AttributeMetaData::getName)
				.anyMatch(name -> name.equalsIgnoreCase(SortaServiceImpl.DEFAULT_MATCHING_NAME_FIELD));
		return containsName;
	}

	private boolean validateEmptyFileHeader(Repository repository)
	{
		boolean evaluation = StreamSupport.stream(repository.getEntityMetaData().getAttributes().spliterator(), false)
				.map(AttributeMetaData::getName).anyMatch(StringUtils::isNotBlank);
		return evaluation;
	}

	private boolean validateInputFileContent(Repository repository)
	{
		return repository.iterator().hasNext();
	}

	private String getSortaServiceMenuUrl()
	{
		return menuReaderService.getMenu().findMenuItemPath(ID);
	}
}