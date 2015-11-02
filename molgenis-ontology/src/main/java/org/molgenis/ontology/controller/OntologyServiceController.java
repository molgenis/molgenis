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
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.Sort;
import org.molgenis.data.Sort.Direction;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.csv.CsvWriter;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.processor.LowerCaseProcessor;
import org.molgenis.data.processor.TrimProcessor;
import org.molgenis.data.rest.EntityCollectionResponse;
import org.molgenis.data.rest.EntityPager;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.file.FileStore;
import org.molgenis.ontology.beans.OntologyServiceResult;
import org.molgenis.ontology.core.meta.OntologyMetaData;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.ontology.matching.AdaptedCsvRepository;
import org.molgenis.ontology.matching.MatchInputTermBatchService;
import org.molgenis.ontology.matching.MatchingTaskContentEntityMetaData;
import org.molgenis.ontology.matching.MatchingTaskEntityMetaData;
import org.molgenis.ontology.matching.OntologyService;
import org.molgenis.ontology.matching.OntologyServiceImpl;
import org.molgenis.ontology.matching.UploadProgress;
import org.molgenis.ontology.request.OntologyServiceRequest;
import org.molgenis.ontology.roc.MatchQualityRocService;
import org.molgenis.ontology.utils.OntologyServiceUtil;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
	private MatchInputTermBatchService matchInputTermBatchService;

	@Autowired
	private UploadProgress uploadProgress;

	@Autowired
	private MatchQualityRocService matchQualityRocService;

	@Autowired
	private FileStore fileStore;

	@Autowired
	private MolgenisPermissionService molgenisPermissionService;

	public static final String ID = "ontologyservice";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
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
		model.addAttribute("existingTasks", OntologyServiceUtil.getEntityAsMap(dataService.findAll(
				MatchingTaskEntityMetaData.ENTITY_NAME,
				new QueryImpl().eq(MatchingTaskEntityMetaData.MOLGENIS_USER, username))));
		return "ontology-match-view";
	}

	@RequestMapping(method = GET, value = "/newtask")
	public String matchTask(Model model)
	{
		model.addAttribute("ontologies", OntologyServiceUtil.getEntityAsMap(ontologyService.getAllOntologyEntities()));
		return "ontology-match-view";
	}

	@RequestMapping(method = RequestMethod.GET, value = "/calculate/{entityName}")
	public String calculateRoc(@PathVariable String entityName, Model model) throws IOException,
			MolgenisInvalidFormatException
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
	public String matchResult(@PathVariable("entityName") String entityName, Model model)
	{
		String userName = userAccountService.getCurrentUser().getUsername();
		model.addAttribute("isRunning", uploadProgress.isUserExists(userName));
		model.addAttribute("progress", uploadProgress.getPercentage(userName));
		model.addAttribute("isMatched", uploadProgress.getUserClickMode(userName));
		model.addAttribute("entityName", entityName);

		if (dataService.hasRepository(entityName) && !uploadProgress.isUserExists(userName))
		{
			Entity entity = dataService.findOne(MatchingTaskEntityMetaData.ENTITY_NAME,
					new QueryImpl().eq(MatchingTaskEntityMetaData.IDENTIFIER, entityName));
			model.addAttribute("threshold", entity.get(MatchingTaskEntityMetaData.THRESHOLD));
			model.addAttribute("ontologyIri", entity.get(MatchingTaskEntityMetaData.CODE_SYSTEM));
			model.addAttribute(
					"numberOfMatched",
					dataService.count(
							MatchingTaskContentEntityMetaData.ENTITY_NAME,
							new QueryImpl()
									.eq(MatchingTaskContentEntityMetaData.REF_ENTITY, entityName)
									.and()
									.nest()
									.eq(MatchingTaskContentEntityMetaData.VALIDATED, true)
									.or()
									.ge(MatchingTaskContentEntityMetaData.SCORE,
											entity.get(MatchingTaskEntityMetaData.THRESHOLD)).unnest()));
			model.addAttribute(
					"numberOfUnmatched",
					dataService.count(
							MatchingTaskContentEntityMetaData.ENTITY_NAME,
							new QueryImpl()
									.eq(MatchingTaskContentEntityMetaData.REF_ENTITY, entityName)
									.and()
									.nest()
									.eq(MatchingTaskContentEntityMetaData.VALIDATED, false)
									.and()
									.lt(MatchingTaskContentEntityMetaData.SCORE,
											entity.get(MatchingTaskEntityMetaData.THRESHOLD)).unnest()));
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
			Iterable<Entity> iterableMatchingEntities = dataService.findAll(
					MatchingTaskContentEntityMetaData.ENTITY_NAME,
					new QueryImpl().eq(MatchingTaskContentEntityMetaData.REF_ENTITY, entityName));
			dataService.delete(MatchingTaskContentEntityMetaData.ENTITY_NAME, iterableMatchingEntities);

			// Remove the matching task meta information from MatchingTaskEntity table
			Entity matchingSummaryEntity = dataService.findOne(MatchingTaskEntityMetaData.ENTITY_NAME,
					new QueryImpl().eq(MatchingTaskEntityMetaData.IDENTIFIER, entityName));
			dataService.delete(MatchingTaskEntityMetaData.ENTITY_NAME, matchingSummaryEntity);

			// Drop the table that contains the information for raw data (input terms)
			dataService.deleteAll(entityName);
			dataService.getMeta().deleteEntityMeta(entityName);

			dataService.getRepository(MatchingTaskEntityMetaData.ENTITY_NAME).flush();
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
		Entity entity = dataService.findOne(MatchingTaskEntityMetaData.ENTITY_NAME,
				new QueryImpl().eq(MatchingTaskEntityMetaData.IDENTIFIER, entityName));

		Query query = new QueryImpl().eq(MatchingTaskContentEntityMetaData.REF_ENTITY, entityName).and().nest()
				.eq(MatchingTaskContentEntityMetaData.VALIDATED, isMatched);
		Double threshold = Double.parseDouble(entity.get(MatchingTaskEntityMetaData.THRESHOLD).toString());
		if (isMatched) query.or().ge(MatchingTaskContentEntityMetaData.SCORE, threshold).unnest();
		else query.and().lt(MatchingTaskContentEntityMetaData.SCORE, threshold).unnest();

		long count = dataService.count(MatchingTaskContentEntityMetaData.ENTITY_NAME, query);
		int start = entityPager.getStart();
		int num = entityPager.getNum();

		for (Entity mappingEntity : dataService.findAll(
				MatchingTaskContentEntityMetaData.ENTITY_NAME,
				query.offset(start)
						.pageSize(num)
						.sort(new Sort().on(MatchingTaskContentEntityMetaData.VALIDATED, Direction.DESC).on(
								MatchingTaskContentEntityMetaData.SCORE, Direction.DESC))))
		{
			Entity RefEntity = dataService.findOne(
					entityName,
					new QueryImpl().eq(AdaptedCsvRepository.ALLOWED_IDENTIFIER,
							mappingEntity.getString(MatchingTaskContentEntityMetaData.INPUT_TERM)));
			Map<String, Object> outputEntity = new HashMap<String, Object>();
			outputEntity.put("inputTerm", OntologyServiceUtil.getEntityAsMap(RefEntity));
			outputEntity.put("matchedTerm", OntologyServiceUtil.getEntityAsMap(mappingEntity));
			Object matchedTerm = mappingEntity.get(MatchingTaskContentEntityMetaData.MATCHED_TERM);
			if (matchedTerm != null)
			{
				outputEntity.put("ontologyTerm", OntologyServiceUtil.getEntityAsMap(ontologyService
						.getOntologyTermEntity(matchedTerm.toString(), ontologyIri)));
			}
			entityMaps.add(outputEntity);
		}

		uploadProgress.setUserClickMode(userAccountService.getCurrentUser().getUsername(), isMatched);
		EntityPager pager = new EntityPager(start, num, count, null);
		return new EntityCollectionResponse(pager, entityMaps, "/match/retrieve", OntologyTermMetaData.INSTANCE,
				molgenisPermissionService, dataService);
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
				&& request.containsKey(MatchingTaskContentEntityMetaData.IDENTIFIER)
				&& !StringUtils.isEmpty(request.get(MatchingTaskContentEntityMetaData.IDENTIFIER).toString()))
		{
			String entityName = request.get("entityName").toString();
			String inputTermIdentifier = request.get(MatchingTaskContentEntityMetaData.IDENTIFIER).toString();
			Entity matchingTaskEntity = dataService.findOne(MatchingTaskEntityMetaData.ENTITY_NAME,
					new QueryImpl().eq(MatchingTaskEntityMetaData.IDENTIFIER, entityName));
			Entity entity = dataService.findOne(entityName,
					new QueryImpl().eq(MatchingTaskContentEntityMetaData.IDENTIFIER, inputTermIdentifier));

			if (matchingTaskEntity == null || entity == null) return new OntologyServiceResult(
					"entityName or inputTermIdentifier is invalid!");

			return ontologyService.searchEntity(matchingTaskEntity.getString(MatchingTaskEntityMetaData.CODE_SYSTEM),
					entity);
		}
		return new OntologyServiceResult("Please check entityName, inputTermIdentifier exist in input!");
	}

	@RequestMapping(method = POST, value = "/search")
	@ResponseBody
	public OntologyServiceResult search(@RequestBody Map<String, Object> request, HttpServletRequest httpServletRequest)
	{
		if (request.containsKey("queryString") && !StringUtils.isEmpty(request.get("queryString").toString())
				&& request.containsKey(OntologyMetaData.ONTOLOGY_IRI)
				&& !StringUtils.isEmpty(request.get(OntologyMetaData.ONTOLOGY_IRI).toString()))
		{
			String queryString = request.get("queryString").toString();
			String ontologyIri = request.get(OntologyMetaData.ONTOLOGY_IRI).toString();
			Entity entity = new MapEntity();
			entity.set(OntologyServiceImpl.DEFAULT_MATCHING_NAME_FIELD, queryString);
			return ontologyService.searchEntity(ontologyIri, entity);
		}
		return new OntologyServiceResult("Please check entityName, inputTermIdentifier exist in input!");
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
				if (!attributeMetaData.getName().equalsIgnoreCase(MatchingTaskEntityMetaData.IDENTIFIER)) columnHeaders
						.add(attributeMetaData.getName());
			}
			columnHeaders.addAll(Arrays.asList(OntologyTermMetaData.ONTOLOGY_TERM_NAME,
					OntologyTermMetaData.ONTOLOGY_TERM_IRI, MatchingTaskContentEntityMetaData.SCORE,
					MatchingTaskContentEntityMetaData.VALIDATED));
			csvWriter.writeAttributeNames(columnHeaders);

			Entity matchingTaskEntity = dataService.findOne(MatchingTaskEntityMetaData.ENTITY_NAME,
					new QueryImpl().eq(MatchingTaskEntityMetaData.IDENTIFIER, entityName));

			for (Entity mappingEntity : dataService.findAll(MatchingTaskContentEntityMetaData.ENTITY_NAME,
					new QueryImpl().eq(MatchingTaskContentEntityMetaData.REF_ENTITY, entityName)))
			{
				Entity inputEntity = dataService.findOne(
						entityName,
						new QueryImpl().eq(MatchingTaskEntityMetaData.IDENTIFIER,
								mappingEntity.getString(MatchingTaskContentEntityMetaData.INPUT_TERM)));
				Entity ontologyTermEntity = ontologyService.getOntologyTermEntity(
						mappingEntity.getString(MatchingTaskContentEntityMetaData.MATCHED_TERM),
						matchingTaskEntity.getString(MatchingTaskEntityMetaData.CODE_SYSTEM));
				Entity row = new MapEntity();
				for (String attributeName : inputEntity.getAttributeNames())
				{
					if (!attributeName.equals(MatchingTaskEntityMetaData.IDENTIFIER)) row.set(attributeName,
							inputEntity.get(attributeName));
				}

				row.set(OntologyTermMetaData.ONTOLOGY_TERM_NAME,
						ontologyTermEntity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME));
				row.set(OntologyTermMetaData.ONTOLOGY_TERM_IRI,
						ontologyTermEntity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI));
				row.set(MatchingTaskContentEntityMetaData.VALIDATED,
						mappingEntity.get(MatchingTaskContentEntityMetaData.VALIDATED));
				row.set(MatchingTaskContentEntityMetaData.SCORE,
						mappingEntity.get(MatchingTaskContentEntityMetaData.SCORE));
				csvWriter.add(row);
			}
		}
		finally
		{
			if (csvWriter != null) IOUtils.closeQuietly(csvWriter);
		}
	}

	private String startMatchJob(String entityName, String ontologyIri, File uploadFile, Model model)
			throws IOException
	{
		entityName = entityName.replaceAll(ILLEGAL_PATTERN, ILLEGAL_PATTERN_REPLACEMENT).toLowerCase();
		if (dataService.hasRepository(entityName))
		{
			Entity matchingTaskEntity = dataService.findOne(MatchingTaskEntityMetaData.ENTITY_NAME,
					new QueryImpl().eq(MatchingTaskEntityMetaData.IDENTIFIER, entityName));
			model.addAttribute(
					"message",
					"The task name should be case insensitive, the task name <strong>"
							+ entityName
							+ "</strong> has existed and created by user : "
							+ (matchingTaskEntity != null ? matchingTaskEntity
									.get(MatchingTaskEntityMetaData.MOLGENIS_USER) : StringUtils.EMPTY));
			return init(model);
		}
		AdaptedCsvRepository csvRepository = new AdaptedCsvRepository(entityName, new CsvRepository(uploadFile,
				Arrays.<CellProcessor> asList(new LowerCaseProcessor(), new TrimProcessor()),
				OntologyServiceImpl.DEFAULT_SEPARATOR));

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

		uploadProgress.registerUser(userAccountService.getCurrentUser().getUsername(), entityName);
		matchInputTermBatchService.process(SecurityContextHolder.getContext(), userAccountService.getCurrentUser(),
				ontologyIri, csvRepository);

		return matchResult(entityName, model);
	}

	private String getCsvFileName(String dataSetName)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dataSetName + "_" + dateFormat.format(new Date()) + ".csv";
	}

	private boolean validateFileHeader(Repository repository)
	{
		boolean containsName = false;
		for (AttributeMetaData atomicAttributes : repository.getEntityMetaData().getAtomicAttributes())
		{
			if (atomicAttributes.getName().equalsIgnoreCase(OntologyServiceImpl.DEFAULT_MATCHING_NAME_FIELD)) containsName = true;
		}
		return containsName;
	}

	private boolean validateEmptyFileHeader(Repository repository)
	{
		for (AttributeMetaData atomicAttributes : repository.getEntityMetaData().getAtomicAttributes())
		{
			if (StringUtils.isEmpty(atomicAttributes.getName())) return false;
		}
		return true;
	}

	private boolean validateInputFileContent(Repository repository)
	{
		Iterator<Entity> iterator = repository.iterator();
		return iterator.hasNext();
	}
}