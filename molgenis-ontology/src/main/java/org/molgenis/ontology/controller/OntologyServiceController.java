package org.molgenis.ontology.controller;

import static org.molgenis.ontology.controller.OntologyServiceController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.csv.CsvWriter;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.processor.LowerCaseProcessor;
import org.molgenis.data.processor.TrimProcessor;
import org.molgenis.data.rest.EntityCollectionResponse;
import org.molgenis.data.rest.EntityPager;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.ontology.OntologyService;
import org.molgenis.ontology.OntologyServiceResult;
import org.molgenis.ontology.beans.OntologyServiceResultImpl;
import org.molgenis.ontology.matching.AdaptedCsvRepository;
import org.molgenis.ontology.matching.MatchingTaskContentEntity;
import org.molgenis.ontology.matching.MatchingTaskEntity;
import org.molgenis.ontology.matching.ProcessInputTermService;
import org.molgenis.ontology.matching.UploadProgress;
import org.molgenis.ontology.repository.OntologyTermQueryRepository;
import org.molgenis.ontology.request.OntologyServiceRequest;
import org.molgenis.ontology.service.OntologyServiceImpl;
import org.molgenis.ontology.utils.OntologyServiceUtil;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(URI)
public class OntologyServiceController extends MolgenisPluginController
{
	@Autowired
	private UserAccountService userAccountService;

	@Autowired
	private DataService dataService;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private ProcessInputTermService processInputTermService;

	@Autowired
	private MysqlRepositoryCollection mysqlRepositoryCollection;

	@Autowired
	private UploadProgress uploadProgress;

	@Autowired
	private FileStore fileStore;

	public static final String ID = "ontologyservice";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	public static final Logger logger = Logger.getLogger(OntologyServiceController.class);
	public static final int INVALID_TOTAL_NUMBER = -1;
	private static final String ILLEGAL_PATTERN = "[^0-9a-zA-Z_]";
	private static final String ILLEGAL_PATTERN_REPLACEMENT = "_";

	public OntologyServiceController()
	{
		super(URI);
	}

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		String username = userAccountService.getCurrentUser().getUsername();

		if (uploadProgress.isUserExists(username)) return matchResult(uploadProgress.getCurrentJob(username), model);
		model.addAttribute(
				"existingTasks",
				OntologyServiceUtil.getEntityAsMap(dataService.findAll(MatchingTaskEntity.ENTITY_NAME,
						new QueryImpl().eq(MatchingTaskEntity.MOLGENIS_USER, username))));
		return "ontology-match-view";
	}

	@RequestMapping(method = GET, value = "/newtask")
	public String matchTask(Model model)
	{
		model.addAttribute("ontologies", OntologyServiceUtil.getEntityAsMap(ontologyService.getAllOntologyEntities()));
		return "ontology-match-view";
	}

	@RequestMapping(method = POST, value = "/threshold/{entityName}")
	public String updateThreshold(@RequestParam(value = "threshold", required = true) String threshold,
			@PathVariable String entityName, Model model)
	{
		if (!StringUtils.isEmpty(threshold))
		{
			Entity entity = dataService.findOne(MatchingTaskEntity.ENTITY_NAME,
					new QueryImpl().eq(MatchingTaskEntity.IDENTIFIER, entityName));
			try
			{
				Double threshold_value = Double.parseDouble(threshold);
				entity.set(MatchingTaskEntity.THRESHOLD, threshold_value);
				dataService.update(MatchingTaskEntity.ENTITY_NAME, entity);
				dataService.getCrudRepository(MatchingTaskEntity.ENTITY_NAME).flush();
			}
			catch (Exception e)
			{
				model.addAttribute("message", threshold + " is illegal threshold value!");
			}
		}

		return matchResult(entityName, model);
	}

	@RequestMapping(method = GET, value = "/result/{entityName}")
	public String matchResult(@PathVariable("entityName") String entityName, Model model)
	{
		String userName = userAccountService.getCurrentUser().getUsername();
		model.addAttribute("isRunning", uploadProgress.isUserExists(userName));
		model.addAttribute("progress", uploadProgress.getPercentage(userName));
		model.addAttribute("isMatched", uploadProgress.getUserClickMode(userName));
		model.addAttribute("entityName", entityName);

		if (dataService.hasRepository(entityName) && !uploadProgress.isUserExists(userName))
		{
			Entity entity = dataService.findOne(MatchingTaskEntity.ENTITY_NAME,
					new QueryImpl().eq(MatchingTaskEntity.IDENTIFIER, entityName));
			model.addAttribute("threshold", entity.get(MatchingTaskEntity.THRESHOLD));
			model.addAttribute("ontologyIri", entity.get(MatchingTaskEntity.CODE_SYSTEM));
			model.addAttribute(
					"numberOfMatched",
					dataService.count(
							MatchingTaskContentEntity.ENTITY_NAME,
							new QueryImpl().eq(MatchingTaskContentEntity.REF_ENTITY, entityName).and().nest()
									.eq(MatchingTaskContentEntity.VALIDATED, true).or()
									.ge(MatchingTaskContentEntity.SCORE, entity.get(MatchingTaskEntity.THRESHOLD))
									.unnest()));
			model.addAttribute(
					"numberOfUnmatched",
					dataService.count(
							MatchingTaskContentEntity.ENTITY_NAME,
							new QueryImpl().eq(MatchingTaskContentEntity.REF_ENTITY, entityName).and().nest()
									.eq(MatchingTaskContentEntity.VALIDATED, false).and()
									.lt(MatchingTaskContentEntity.SCORE, entity.get(MatchingTaskEntity.THRESHOLD))
									.unnest()));
		}

		return "ontology-match-view";
	}

	@RequestMapping(method = POST, value = "/delete")
	@ResponseStatus(value = HttpStatus.OK)
	public void deleteResult(@RequestBody String entityName)
	{
		String userName = userAccountService.getCurrentUser().getUsername();

		if (dataService.hasRepository(entityName) && !uploadProgress.isUserExists(userName))
		{
			// Remove all the matching terms from MatchingTaskContentEntity table
			Iterable<Entity> iterableMatchingEntities = dataService.findAll(MatchingTaskContentEntity.ENTITY_NAME,
					new QueryImpl().eq(MatchingTaskContentEntity.REF_ENTITY, entityName));
			dataService.delete(MatchingTaskContentEntity.ENTITY_NAME, iterableMatchingEntities);

			// Remove the matching task meta information from MatchingTaskEntity table
			Entity matchingSummaryEntity = dataService.findOne(MatchingTaskEntity.ENTITY_NAME,
					new QueryImpl().eq(MatchingTaskEntity.IDENTIFIER, entityName));
			dataService.delete(MatchingTaskEntity.ENTITY_NAME, matchingSummaryEntity);

			// Drop the table that contains the information for raw data (input terms)
			mysqlRepositoryCollection.dropEntityMetaData(entityName);

			dataService.getCrudRepository(MatchingTaskEntity.ENTITY_NAME).flush();
		}
	}

	@RequestMapping(method = POST, value = "/match/retrieve")
	@ResponseBody
	public EntityCollectionResponse matchResult(@RequestBody OntologyServiceRequest ontologyServiceRequest,
			HttpServletRequest httpServletRequest)
	{
		List<Map<String, Object>> entityMaps = new ArrayList<Map<String, Object>>();
		String entityName = ontologyServiceRequest.getEntityName();
		String ontologyIri = ontologyServiceRequest.getOntologyIri();
		EntityPager entityPager = ontologyServiceRequest.getEntityPager();
		boolean isMatched = ontologyServiceRequest.isMatched();
		Entity entity = dataService.findOne(MatchingTaskEntity.ENTITY_NAME,
				new QueryImpl().eq(MatchingTaskEntity.IDENTIFIER, entityName));

		Query query = new QueryImpl().eq(MatchingTaskContentEntity.REF_ENTITY, entityName).and().nest()
				.eq(MatchingTaskContentEntity.VALIDATED, isMatched);
		Double threshold = Double.parseDouble(entity.get(MatchingTaskEntity.THRESHOLD).toString());
		if (isMatched) query.or().ge(MatchingTaskContentEntity.SCORE, threshold).unnest();
		else query.and().lt(MatchingTaskContentEntity.SCORE, threshold).unnest();

		long count = dataService.count(MatchingTaskContentEntity.ENTITY_NAME, query);
		int start = entityPager.getStart();
		int num = entityPager.getNum();

		for (Entity mappingEntity : dataService.findAll(
				MatchingTaskContentEntity.ENTITY_NAME,
				query.offset(start).pageSize(num)
						.sort(Direction.DESC, MatchingTaskContentEntity.VALIDATED, MatchingTaskContentEntity.SCORE)))
		{
			Entity RefEntity = dataService.findOne(
					entityName,
					new QueryImpl().eq(AdaptedCsvRepository.ALLOWED_IDENTIFIER,
							mappingEntity.getString(MatchingTaskContentEntity.INPUT_TERM)));
			Map<String, Object> outputEntity = new HashMap<String, Object>();
			outputEntity.put("inputTerm", OntologyServiceUtil.getEntityAsMap(RefEntity));
			outputEntity.put("matchedTerm", OntologyServiceUtil.getEntityAsMap(mappingEntity));
			Object matchedTerm = mappingEntity.get(MatchingTaskContentEntity.MATCHED_TERM);
			if (matchedTerm != null)
			{
				outputEntity.put("ontologyTerm", OntologyServiceUtil.getEntityAsMap(ontologyService
						.getOntologyTermEntity(matchedTerm.toString(), ontologyIri)));
			}
			entityMaps.add(outputEntity);
		}

		uploadProgress.setUserClickMode(userAccountService.getCurrentUser().getUsername(), isMatched);
		EntityPager pager = new EntityPager(start, num, (long) count, null);
		return new EntityCollectionResponse(pager, entityMaps, "/match/retrieve");
	}

	@RequestMapping(method = POST, value = "/match")
	public String match(@RequestParam(value = "taskName", required = true) String entityName,
			@RequestParam(value = "selectOntologies", required = true) String ontologyIri,
			@RequestParam(value = "inputTerms", required = true) String inputTerms, Model model,
			HttpServletRequest httpServletRequest) throws Exception
	{
		if (StringUtils.isEmpty(ontologyIri) || StringUtils.isEmpty(inputTerms)) return init(model);
		String sessionId = httpServletRequest.getSession().getId();
		File uploadFile = fileStore.store(new ByteArrayInputStream(inputTerms.getBytes("UTF8")), sessionId
				+ "_input.txt");
		return startMatchJob(entityName, ontologyIri, uploadFile, model);
	}

	@RequestMapping(method = POST, value = "/match/upload", headers = "Content-Type=multipart/form-data")
	public String upload(@RequestParam(value = "taskName", required = true) String entityName,
			@RequestParam(value = "selectOntologies", required = true) String ontologyIri,
			@RequestParam(value = "file", required = true) Part file, Model model, HttpServletRequest httpServletRequest)
			throws Exception
	{
		if (StringUtils.isEmpty(ontologyIri) || file == null) return init(model);
		String sessionId = httpServletRequest.getSession().getId();
		File uploadFile = fileStore.store(file.getInputStream(), sessionId + "_input.csv");
		return startMatchJob(entityName, ontologyIri, uploadFile, model);
	}

	@RequestMapping(method = POST, value = "/match/entity")
	@ResponseBody
	public OntologyServiceResult matchResult(@RequestBody Map<String, Object> request,
			HttpServletRequest httpServletRequest)
	{
		if (request.containsKey("entityName") && !StringUtils.isEmpty(request.get("entityName").toString())
				&& request.containsKey(MatchingTaskContentEntity.IDENTIFIER)
				&& !StringUtils.isEmpty(request.get(MatchingTaskContentEntity.IDENTIFIER).toString()))
		{
			String entityName = request.get("entityName").toString();
			String inputTermIdentifier = request.get(MatchingTaskContentEntity.IDENTIFIER).toString();
			Entity matchingTaskEntity = dataService.findOne(MatchingTaskEntity.ENTITY_NAME,
					new QueryImpl().eq(MatchingTaskEntity.IDENTIFIER, entityName));
			Entity entity = dataService.findOne(entityName,
					new QueryImpl().eq(MatchingTaskContentEntity.IDENTIFIER, inputTermIdentifier));

			if (matchingTaskEntity == null || entity == null) return new OntologyServiceResultImpl(
					"entityName or inputTermIdentifier is invalid!");

			return ontologyService.searchEntity(matchingTaskEntity.getString(MatchingTaskEntity.CODE_SYSTEM), entity);
		}
		return new OntologyServiceResultImpl("Please check entityName, inputTermIdentifier exist in input!");
	}

	@RequestMapping(method = GET, value = "/match/download/{entityName}")
	public void download(@PathVariable String entityName, HttpServletResponse response, Model model) throws IOException
	{
		CsvWriter csvWriter = null;
		try
		{
			response.setContentType("text/csv");
			response.addHeader("Content-Disposition", "attachment; filename=" + getCsvFileName("match-result"));
			csvWriter = new CsvWriter(response.getOutputStream(), OntologyServiceImpl.DEFAULT_SEPARATOR);
			List<String> columnHeaders = new ArrayList<String>();
			for (AttributeMetaData attributeMetaData : dataService.getEntityMetaData(entityName).getAttributes())
			{
				if (!attributeMetaData.getName().equalsIgnoreCase(MatchingTaskEntity.IDENTIFIER)) columnHeaders
						.add(attributeMetaData.getName());
			}
			columnHeaders.addAll(Arrays.asList(OntologyTermQueryRepository.ONTOLOGY_TERM,
					OntologyTermQueryRepository.ONTOLOGY_TERM_IRI, MatchingTaskContentEntity.SCORE,
					MatchingTaskContentEntity.VALIDATED));
			csvWriter.writeAttributeNames(columnHeaders);

			Entity matchingTaskEntity = dataService.findOne(MatchingTaskEntity.ENTITY_NAME,
					new QueryImpl().eq(MatchingTaskEntity.IDENTIFIER, entityName));

			for (Entity mappingEntity : dataService.findAll(MatchingTaskContentEntity.ENTITY_NAME,
					new QueryImpl().eq(MatchingTaskContentEntity.REF_ENTITY, entityName)))
			{
				Entity inputEntity = dataService.findOne(
						entityName,
						new QueryImpl().eq(MatchingTaskEntity.IDENTIFIER,
								mappingEntity.getString(MatchingTaskContentEntity.INPUT_TERM)));
				Entity ontologyTermEntity = ontologyService.getOntologyTermEntity(
						mappingEntity.getString(MatchingTaskContentEntity.MATCHED_TERM),
						matchingTaskEntity.getString(MatchingTaskEntity.CODE_SYSTEM));
				Entity row = new MapEntity();
				for (String attributeName : inputEntity.getAttributeNames())
				{
					if (!attributeName.equals(MatchingTaskEntity.IDENTIFIER)) row.set(attributeName,
							inputEntity.get(attributeName));
				}
				row.set(OntologyTermQueryRepository.ONTOLOGY_TERM,
						ontologyTermEntity.get(OntologyTermQueryRepository.ONTOLOGY_TERM));
				row.set(OntologyTermQueryRepository.ONTOLOGY_TERM_IRI,
						ontologyTermEntity.get(OntologyTermQueryRepository.ONTOLOGY_TERM_IRI));
				row.set(MatchingTaskContentEntity.VALIDATED, mappingEntity.get(MatchingTaskContentEntity.VALIDATED));
				row.set(MatchingTaskContentEntity.SCORE, mappingEntity.get(MatchingTaskContentEntity.SCORE));
				csvWriter.add(row);
			}
		}
		finally
		{
			if (csvWriter != null) IOUtils.closeQuietly(csvWriter);
		}
	}

	@SuppressWarnings("resource")
	private String startMatchJob(String entityName, String ontologyIri, File uploadFile, Model model)
	{
		entityName = entityName.replaceAll(ILLEGAL_PATTERN, ILLEGAL_PATTERN_REPLACEMENT).toLowerCase();
		if (dataService.hasRepository(entityName))
		{
			Entity matchingTaskEntity = dataService.findOne(MatchingTaskEntity.ENTITY_NAME,
					new QueryImpl().eq(MatchingTaskEntity.IDENTIFIER, entityName));
			model.addAttribute(
					"message",
					"The task name should be case insensitive, the task name <strong>"
							+ entityName
							+ "</strong> has existed and created by user : "
							+ (matchingTaskEntity != null ? matchingTaskEntity.get(MatchingTaskEntity.MOLGENIS_USER) : StringUtils.EMPTY));
			return init(model);
		}

		CsvRepository csvRepository = new CsvRepository(uploadFile, null, OntologyServiceImpl.DEFAULT_SEPARATOR);

		if (!validateFileHeader(csvRepository))
		{
			model.addAttribute("message", "The Name header is missing!");
			return matchTask(model);
		}

		if (!validateEmptyFileHeader(csvRepository))
		{
			model.addAttribute("message", "The empty header is not allowed!");
			return matchTask(model);
		}

		if (!validateInputFileContent(csvRepository))
		{
			model.addAttribute("message", "The content of input is empty!");
			return matchTask(model);
		}

		RepositoryCollection repositoryCollection = getRepositoryCollection(entityName, uploadFile);
		uploadProgress.registerUser(userAccountService.getCurrentUser().getUsername(), entityName);
		processInputTermService.process(SecurityContextHolder.getContext(), userAccountService.getCurrentUser(),
				entityName, ontologyIri, uploadFile, repositoryCollection);

		return matchResult(entityName, model);
	}

	private RepositoryCollection getRepositoryCollection(final String name, final File file)
	{
		return new RepositoryCollection()
		{
			private String entityName = name;

			@SuppressWarnings("resource")
			@Override
			public Repository getRepositoryByEntityName(String name)
			{
				CsvRepository csvRepository = new CsvRepository(file, Arrays.<CellProcessor> asList(
						new LowerCaseProcessor(), new TrimProcessor()), OntologyServiceImpl.DEFAULT_SEPARATOR);
				return (entityName.equals(name) ? new AdaptedCsvRepository(entityName, csvRepository) : null);
			}

			@Override
			public Iterable<String> getEntityNames()
			{
				return Arrays.asList(entityName);
			}
		};
	}

	private String getCsvFileName(String dataSetName)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dataSetName + "_" + dateFormat.format(new Date()) + ".csv";
	}

	private boolean validateFileHeader(CsvRepository csvRepository)
	{
		boolean containsName = false;
		for (AttributeMetaData atomicAttributes : csvRepository.getEntityMetaData().getAtomicAttributes())
		{
			if (atomicAttributes.getName().equalsIgnoreCase(OntologyServiceImpl.DEFAULT_MATCHING_NAME_FIELD)) containsName = true;
		}
		return containsName;
	}

	private boolean validateEmptyFileHeader(CsvRepository csvRepository)
	{
		for (AttributeMetaData atomicAttributes : csvRepository.getEntityMetaData().getAtomicAttributes())
		{
			if (StringUtils.isEmpty(atomicAttributes.getName())) return false;
		}
		return true;
	}

	private boolean validateInputFileContent(CsvRepository csvRepository)
	{
		Iterator<Entity> iterator = csvRepository.iterator();
		return iterator.hasNext();
	}
}