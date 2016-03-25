package org.molgenis.ontology.controller;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.molgenis.data.QueryRule.Operator.AND;
import static org.molgenis.data.QueryRule.Operator.EQUALS;
import static org.molgenis.data.QueryRule.Operator.GREATER_EQUAL;
import static org.molgenis.data.QueryRule.Operator.LESS;
import static org.molgenis.data.QueryRule.Operator.OR;
import static org.molgenis.data.Sort.Direction.DESC;
import static org.molgenis.ontology.controller.SortaServiceController.URI;
import static org.molgenis.ontology.sorta.meta.MatchingTaskContentEntityMetaData.INPUT_TERM;
import static org.molgenis.ontology.sorta.meta.MatchingTaskContentEntityMetaData.MATCHED_TERM;
import static org.molgenis.ontology.sorta.meta.MatchingTaskContentEntityMetaData.SCORE;
import static org.molgenis.ontology.sorta.meta.MatchingTaskContentEntityMetaData.VALIDATED;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.Repository;
import org.molgenis.data.Sort;
import org.molgenis.data.Sort.Direction;
import org.molgenis.data.csv.CsvWriter;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.rest.EntityCollectionResponse;
import org.molgenis.data.rest.EntityPager;
import org.molgenis.data.support.DefaultEntityMetaData;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private final IdGenerator idGenerator;

	public static final String MATCH_VIEW_NAME = "sorta-match-view";
	public static final String ID = "sortaservice";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final double DEFAULT_THRESHOLD = 100.0;

	private static final Logger LOG = LoggerFactory.getLogger(SortaServiceController.class);

	@Autowired
	public SortaServiceController(OntologyService ontologyService, SortaService sortaService,
			MatchQualityRocService matchQualityRocService, SortaJobFactory sortaMatchJobFactory,
			ExecutorService taskExecutor, UserAccountService userAccountService, FileStore fileStore,
			MolgenisPermissionService molgenisPermissionService, DataService dataService,
			LanguageService languageService, MenuReaderService menuReaderService, IdGenerator idGenerator)
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
		this.idGenerator = idGenerator;
	}

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		model.addAttribute("existingTasks", getJobsForCurrentUser());
		return MATCH_VIEW_NAME;
	}
	
	private SortaJobExecution findSortaJobExecution(String sortaJobExecutionId)
	{
		return dataService.findOne(SortaJobExecution.ENTITY_NAME, sortaJobExecutionId, SortaJobExecution.class);
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
		return MATCH_VIEW_NAME;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/calculate/{sortaJobExecutionId}")
	public String calculateRoc(@PathVariable String sortaJobExecutionId, Model model)
			throws IOException, MolgenisInvalidFormatException
	{
		model.addAllAttributes(matchQualityRocService.calculateROC(sortaJobExecutionId));
		return init(model);
	}

	@RequestMapping(method = POST, value = "/threshold/{sortaJobExecutionId}")
	public String updateThreshold(@RequestParam(value = "threshold", required = true) String threshold,
			@PathVariable String sortaJobExecutionId, Model model)
	{
		if (!StringUtils.isEmpty(threshold))
		{
			SortaJobExecution sortaJobExecution = findSortaJobExecution(sortaJobExecutionId);
			try
			{
				Double thresholdValue = Double.parseDouble(threshold);
				sortaJobExecution.setThreshold(thresholdValue);
				dataService.update(SortaJobExecution.ENTITY_NAME, sortaJobExecution);
			}
			catch (NumberFormatException e)
			{
				model.addAttribute("message", threshold + " is illegal threshold value!");
			}
			catch (Exception other)
			{
				model.addAttribute("message", "Error updating threshold: " + other.getMessage());
			}
		}

		return matchResult(sortaJobExecutionId, model);
	}

	@RequestMapping(method = GET, value = "/result/{sortaJobExecutionId}")
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
			LOG.info("Job execution with id " + sortaJobExecutionId + " not found.");
			model.addAttribute("message", "Job execution not found.");
			return init(model);
		}
	}

	@RequestMapping(method = GET, value = "/count/{sortaJobExecutionId}")
	@ResponseBody
	public Map<String, Object> countMatchResult(@PathVariable("sortaJobExecutionId") String sortaJobExecutionId)
	{
		SortaJobExecution sortaJobExecution = findSortaJobExecution(sortaJobExecutionId);
		return ImmutableMap.of("numberOfMatched", countMatchedEntities(sortaJobExecution, true), "numberOfUnmatched",
				countMatchedEntities(sortaJobExecution, false));
	}

	@RequestMapping(method = POST, value = "/delete/{sortaJobExecutionId}")
	@ResponseStatus(value = HttpStatus.OK)
	public String deleteResult(@PathVariable("sortaJobExecutionId") String sortaJobExecutionId, Model model)
	{
		SortaJobExecution sortaJobExecution = findSortaJobExecution(sortaJobExecutionId);
		if (sortaJobExecution != null)
		{
			MolgenisUser currentUser = userAccountService.getCurrentUser();
			if (currentUser.isSuperuser()
					|| sortaJobExecution.getUser().getUsername().equals(currentUser.getUsername()))
			{
				// Remove result repository
				String resultEntityName = sortaJobExecution.getResultEntityName();
				dataService.getMeta().deleteEntityMeta(resultEntityName);

				// Remove SortaJobExecution. It's not possible to delete a record from the
				// SortaJobExecution as a regular user because the user doesn't have the permission to read the table
				// MolgenisUser to which the column molgenisUser in SortaJobExecution refers

				RunAsSystemProxy.runAsSystem(() -> {
					dataService.delete(SortaJobExecution.ENTITY_NAME, sortaJobExecution.getIdentifier());
					dataService.getRepository(SortaJobExecution.ENTITY_NAME).flush();
				});

				// Drop the table that contains the information for raw data (input terms). It's not possible to delete
				// the
				// EntityMetaData as a regular user because the user doesn't have the permission to change the entities
				// and
				// attributes tables
				if (dataService.hasRepository(resultEntityName)
						&& molgenisPermissionService.hasPermissionOnEntity(resultEntityName, Permission.WRITEMETA))
				{
					RunAsSystemProxy.runAsSystem(() -> dataService.getMeta().deleteEntityMeta(resultEntityName));
				}
			}
		}
		return init(model);
	}

	@RequestMapping(method = POST, value = "/match/retrieve")
	@ResponseBody
	public EntityCollectionResponse retrieveSortaJobResults(@RequestBody SortaServiceRequest sortaServiceRequest,
			HttpServletRequest httpServletRequest)
	{
		List<Map<String, Object>> entityMaps = new ArrayList<Map<String, Object>>();
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

		List<QueryRule> queryRuleInputEntitiesInOneMatchingTask = Arrays.asList(queryRuleInputEntities);

		// Add filter to the query if query string is not empty
		if (isNotEmpty(filterQuery))
		{
			Iterable<String> filteredInputTermIds = dataService
					.findAll(sortaJobExecution.getSourceEntityName(), new QueryImpl().search(filterQuery))
					.map(inputEntity -> inputEntity.getString(SortaServiceImpl.DEFAULT_MATCHING_IDENTIFIER))
					.collect(Collectors.toList());
			QueryRule previousQueryRule = new QueryRule(queryRuleInputEntitiesInOneMatchingTask);
			QueryRule queryRuleFilterInput = new QueryRule(MatchingTaskContentEntityMetaData.INPUT_TERM, Operator.IN,
					filteredInputTermIds);
			queryRuleInputEntitiesInOneMatchingTask = Arrays.asList(previousQueryRule, new QueryRule(Operator.AND),
					queryRuleFilterInput);
		}

		Query query = new QueryImpl(queryRuleInputEntitiesInOneMatchingTask);
		long count = dataService.count(resultEntityName, query);
		int start = entityPager.getStart();
		int num = entityPager.getNum();

		Stream<Entity> findAll = dataService.findAll(sortaJobExecution.getResultEntityName(),
				query.offset(start).pageSize(num).sort(new Sort().on(VALIDATED, DESC).on(SCORE, DESC)));
		findAll.forEach(mappingEntity -> {
			Entity RefEntity = dataService.findOne(sortaJobExecution.getSourceEntityName(),
					mappingEntity.getString(INPUT_TERM));
			Map<String, Object> outputEntity = new HashMap<String, Object>();
			outputEntity.put("inputTerm", SortaServiceUtil.getEntityAsMap(RefEntity));
			outputEntity.put("matchedTerm", SortaServiceUtil.getEntityAsMap(mappingEntity));
			Object matchedTerm = mappingEntity.get(MATCHED_TERM);
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
	public String match(@RequestParam(value = "taskName", required = true) String jobName,
			@RequestParam(value = "selectOntologies", required = true) String ontologyIri,
			@RequestParam(value = "inputTerms", required = true) String inputTerms, Model model,
			HttpServletRequest httpServletRequest) throws Exception
	{
		if (isEmpty(ontologyIri) || isEmpty(inputTerms)) return init(model);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(inputTerms.getBytes("UTF8"));
		return startMatchJob(jobName, ontologyIri, model, httpServletRequest, inputStream);
	}

	@RequestMapping(method = POST, value = "/match/upload", headers = "Content-Type=multipart/form-data")
	public String upload(@RequestParam(value = "taskName", required = true) String jobName,
			@RequestParam(value = "selectOntologies", required = true) String ontologyIri,
			@RequestParam(value = "file", required = true) Part file, Model model,
			HttpServletRequest httpServletRequest) throws Exception
	{
		if (isEmpty(ontologyIri) || file == null) return init(model);
		InputStream inputStream = file.getInputStream();
		return startMatchJob(jobName, ontologyIri, model, httpServletRequest, inputStream);
	}

	@RequestMapping(method = POST, value = "/match/entity")
	@ResponseBody
	public SortaServiceResponse findMatchingOntologyTerms(@RequestBody Map<String, Object> request,
			HttpServletRequest httpServletRequest)
	{
		// TODO: less obfuscated request object, let Spring do the matching
		if (request.containsKey("sortaJobExecutionId") && !isEmpty(request.get("sortaJobExecutionId").toString())
				&& request.containsKey(MatchingTaskContentEntityMetaData.IDENTIFIER)
				&& !isEmpty(request.get(MatchingTaskContentEntityMetaData.IDENTIFIER).toString()))
		{
			String sortaJobExecutionId = request.get("sortaJobExecutionId").toString();
			SortaJobExecution sortaJobExecution = findSortaJobExecution(sortaJobExecutionId);
			if (sortaJobExecution == null) return new SortaServiceResponse("sortaJobExecutionId is invalid!");

			String inputTermIdentifier = request.get(MatchingTaskContentEntityMetaData.IDENTIFIER).toString();
			Entity inputEntity = dataService.findOne(sortaJobExecution.getSourceEntityName(), inputTermIdentifier);

			if (inputEntity == null) return new SortaServiceResponse("inputTerm identifier is invalid!");

			return new SortaServiceResponse(inputEntity,
					sortaService.findOntologyTermEntities(sortaJobExecution.getOntologyIri(), inputEntity));
		}
		return new SortaServiceResponse("Please check that sortaJobExecutionId and identifier keys exist in input and have nonempty value!");
	}

	@RequestMapping(method = POST, value = "/search")
	@ResponseBody
	public SortaServiceResponse search(@RequestBody Map<String, Object> request, HttpServletRequest httpServletRequest)
	{
		// TODO: less obfuscated request object, let Spring do the matching
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
		return new SortaServiceResponse("Please check that queryString and ontologyIRI keys exist in input and have nonempty value!");
	}

	private MapEntity toDownloadRow(SortaJobExecution sortaJobExecution, Entity resultEntity)
	{
		String inputTermId = resultEntity.getString(MatchingTaskContentEntityMetaData.INPUT_TERM);
		// TODO: make an xref and fetch it in one go!
		Entity inputEntity = dataService.findOne(sortaJobExecution.getSourceEntityName(), inputTermId);
		Entity ontologyTermEntity = sortaService.getOntologyTermEntity(
				resultEntity.getString(MatchingTaskContentEntityMetaData.MATCHED_TERM),
				sortaJobExecution.getOntologyIri());
		MapEntity row = new MapEntity(inputEntity);
		row.set(OntologyTermMetaData.ONTOLOGY_TERM_NAME,
				ontologyTermEntity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME));
		row.set(OntologyTermMetaData.ONTOLOGY_TERM_IRI, ontologyTermEntity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI));
		row.set(MatchingTaskContentEntityMetaData.VALIDATED,
				resultEntity.get(MatchingTaskContentEntityMetaData.VALIDATED));
		row.set(MatchingTaskContentEntityMetaData.SCORE, resultEntity.get(MatchingTaskContentEntityMetaData.SCORE));
		return row;
	}

	
	@RequestMapping(method = GET, value = "/match/download/{sortaJobExecutionId}")
	public void download(@PathVariable String sortaJobExecutionId, HttpServletResponse response, Model model)
			throws IOException
	{
		CsvWriter csvWriter = new CsvWriter(response.getOutputStream(), SortaServiceImpl.DEFAULT_SEPARATOR);
		try
		{
			SortaJobExecution sortaJobExecution = findSortaJobExecution(sortaJobExecutionId);

			response.setContentType("text/csv");
			response.addHeader("Content-Disposition", "attachment; filename=" + generateCsvFileName("match-result"));
			List<String> columnHeaders = new ArrayList<String>();
			
			EntityMetaData resultMetaData = dataService.getEntityMetaData(sortaJobExecution.getResultEntityName());
			for (AttributeMetaData attributeMetaData : resultMetaData.getAttributes())
			{
				if (!attributeMetaData.getName().equalsIgnoreCase(SortaCsvRepository.ALLOWED_IDENTIFIER))
					columnHeaders.add(attributeMetaData.getName());
			}
			columnHeaders.addAll(
					Arrays.asList(OntologyTermMetaData.ONTOLOGY_TERM_NAME, OntologyTermMetaData.ONTOLOGY_TERM_IRI,
							MatchingTaskContentEntityMetaData.SCORE, MatchingTaskContentEntityMetaData.VALIDATED));
			csvWriter.writeAttributeNames(columnHeaders);

			dataService.findAll(sortaJobExecution.getResultEntityName(), new QueryImpl())
					.forEach(resultEntity -> csvWriter.add(toDownloadRow(sortaJobExecution, resultEntity)));
		}
		finally
		{
			if (csvWriter != null) IOUtils.closeQuietly(csvWriter);
		}
	}

	private String startMatchJob(String jobName, String ontologyIri, Model model,
			HttpServletRequest httpServletRequest, InputStream inputStream) throws IOException
	{
		String sessionId = httpServletRequest.getSession().getId();
		File uploadFile = fileStore.store(inputStream, sessionId + "_input.csv");
		String inputRepositoryName = idGenerator.generateId();
		SortaCsvRepository inputRepository = new SortaCsvRepository(inputRepositoryName, jobName + " input",
				uploadFile);
		
		if (!validateFileHeader(inputRepository))
		{
			model.addAttribute("message", "The Name header is missing!");
			return matchTask(model);
		}
		
		if (!validateEmptyFileHeader(inputRepository))
		{
			model.addAttribute("message", "The empty header is not allowed!");
			return matchTask(model);
		}
		
		if (!validateInputFileContent(inputRepository))
		{
			model.addAttribute("message", "The content of input is empty!");
			return matchTask(model);
		}
		
		SortaJobExecution jobExecution = createJobExecution(inputRepository, jobName, ontologyIri,
				SecurityContextHolder.getContext());
		SortaJobImpl sortaMatchJob = sortaMatchJobFactory.create(jobExecution, SecurityContextHolder.getContext());
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
				// TODO: fetch the user as well
				job.set(SortaJobExecution.USER, currentUser);
				jobs.add(job);
			});
		});
		return jobs;
	}

	private SortaJobExecution createJobExecution(Repository inputRepository, String jobName, String ontologyIri,
			SecurityContext securityContext)
	{
		String resultEntityName = idGenerator.generateId();

		// Create a Sorta Job Execution
		SortaJobExecution sortaJobExecution = new SortaJobExecution(dataService);
		sortaJobExecution.setIdentifier(resultEntityName);
		sortaJobExecution.setUser(userAccountService.getCurrentUser());
		sortaJobExecution.setSourceEntityName(inputRepository.getName());
		sortaJobExecution.setResultUrl(getSortaServiceMenuUrl() + "/result/" + resultEntityName);
		sortaJobExecution.setDeleteUrl(getSortaServiceMenuUrl() + "/delete/" + resultEntityName);
		sortaJobExecution.setResultEntityName(resultEntityName);
		sortaJobExecution.setThreshold(DEFAULT_THRESHOLD);
		sortaJobExecution.setOntologyIri(ontologyIri);

		MolgenisUser molgenisUser = userAccountService.getCurrentUser();

		RunAsSystemProxy.runAsSystem(() -> {
			// Add the original input dataset to database
			dataService.getMeta().addEntityMeta(inputRepository.getEntityMetaData());
			dataService.getRepository(inputRepository.getName()).add(inputRepository.stream());

			// Create empty result repository
			DefaultEntityMetaData resultEntityMetaData = new DefaultEntityMetaData(resultEntityName,
					MatchingTaskContentEntityMetaData.INSTANCE);
			resultEntityMetaData.setAbstract(false);
			resultEntityMetaData.setLabel(jobName + " output");
			dataService.getMeta().addEntityMeta(resultEntityMetaData);

			// Add job execution entity
			dataService.add(SortaJobExecution.ENTITY_NAME, sortaJobExecution);

			// FIXME : temporary work around to assign write permissions to the
			// users who create the entities.
			Authentication auth = securityContext.getAuthentication();
			List<GrantedAuthority> roles = Lists.newArrayList(auth.getAuthorities());
			for (Permission permission : Permission.values())
			{
				UserAuthority userAuthority = new UserAuthority();
				userAuthority.setMolgenisUser(molgenisUser);
				String role = SecurityUtils.AUTHORITY_ENTITY_PREFIX + permission.toString() + "_"
						+ inputRepository.getName().toUpperCase();
				userAuthority.setRole(role);
				roles.add(new SimpleGrantedAuthority(role));
				dataService.add(UserAuthority.ENTITY_NAME, userAuthority);
				dataService.getRepository(UserAuthority.ENTITY_NAME).flush();
			}
			auth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), null, roles);
			// TODO: what arcane trickery is this?
			securityContext.setAuthentication(auth);
		});

		return sortaJobExecution;
	}

	private long countMatchedEntities(SortaJobExecution sortaJobExecution, boolean isMatched)
	{
		double threshold = sortaJobExecution.getThreshold();
		QueryRule validatedRule = new QueryRule(MatchingTaskContentEntityMetaData.VALIDATED, Operator.EQUALS,
				isMatched);
		QueryRule thresholdRule = new QueryRule(MatchingTaskContentEntityMetaData.SCORE,
				isMatched ? Operator.GREATER_EQUAL : Operator.LESS, threshold);
		QueryRule combinedRule = new QueryRule(
				Arrays.asList(validatedRule, new QueryRule(isMatched ? Operator.OR : Operator.AND), thresholdRule));

		return dataService.count(sortaJobExecution.getResultEntityName(), new QueryImpl(combinedRule));
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