package org.molgenis.ontology.service;

import static org.molgenis.ontology.service.OntologyServiceController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.processor.LowerCaseProcessor;
import org.molgenis.data.processor.TrimProcessor;
import org.molgenis.data.rest.EntityCollectionResponse;
import org.molgenis.data.rest.EntityPager;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.ontology.matching.AdaptedCsvRepository;
import org.molgenis.ontology.matching.MatchingTaskEntity;
import org.molgenis.ontology.matching.MathcingTaskContentEntity;
import org.molgenis.ontology.matching.ProcessInputTermService;
import org.molgenis.ontology.matching.UploadProgress;
import org.molgenis.ontology.utils.OntologyServiceUtil;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(URI)
public class OntologyServiceController extends MolgenisPluginController
{
	@Autowired
	private UserAccountService userAccountService;

	@Autowired
	private DataService dataService;

	@Autowired
	private OntologyServiceImpl ontologyService;

	@Autowired
	private ProcessInputTermService processInputTermService;

	@Autowired
	private UploadProgress uploadProgress;

	@Autowired
	private FileStore fileStore;

	public static final String ID = "ontologyservice";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	public static final int INVALID_TOTAL_NUMBER = -1;
	private static final String EXCEL_NEWLINE_CHAR = "\n";
	private static final String ILLEGAL_PATTERN = "[^0-9a-zA-Z_]";
	private static final String ILLEGAL_PATTERN_REPLACEMENT = "_";

	public OntologyServiceController()
	{
		super(URI);
	}

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		model.addAttribute("existingTasks",
				OntologyServiceUtil.getEntityAsMap(dataService.findAll(MatchingTaskEntity.ENTITY_NAME)));
		return "ontology-match-view";
	}

	@RequestMapping(method = GET, value = "/newtask")
	public String matchTask(Model model)
	{
		model.addAttribute("ontologies", OntologyServiceUtil.getEntityAsMap(ontologyService.getAllOntologyEntities()));
		return "ontology-match-view";
	}

	@RequestMapping(method = GET, value = "/result/{entityName}")
	public String matchResult(@PathVariable
	String entityName, Model model)
	{
		String userName = userAccountService.getCurrentUser().getUsername();
		model.addAttribute("isRunning", uploadProgress.isUserExists(userName));

		if (dataService.hasRepository(entityName) && !uploadProgress.isUserExists(userName))
		{
			Entity entity = dataService.findOne(MatchingTaskEntity.ENTITY_NAME,
					new QueryImpl().eq(MatchingTaskEntity.IDENTIFIER, entityName));

			model.addAttribute("entityName", entityName);
			model.addAttribute("threshold", entity.get(MatchingTaskEntity.THRESHOLD));
			model.addAttribute("ontologyIri", entity.get(MatchingTaskEntity.CODE_SYSTEM));
			model.addAttribute(
					"numberOfMatched",
					dataService.count(
							MathcingTaskContentEntity.ENTITY_NAME,
							new QueryImpl().eq(MathcingTaskContentEntity.REF_ENTITY, entityName).and()
									.eq(MathcingTaskContentEntity.VALIDATED, true)));
			model.addAttribute(
					"numberOfUnmatched",
					dataService.count(
							MathcingTaskContentEntity.ENTITY_NAME,
							new QueryImpl().eq(MathcingTaskContentEntity.REF_ENTITY, entityName).and()
									.eq(MathcingTaskContentEntity.VALIDATED, false)));
		}
		return "ontology-match-view";
	}

	@RequestMapping(method = POST, value = "/match")
	public String match(@RequestParam(value = "selectOntologies", required = true)
	String ontologyIri, @RequestParam(value = "inputTerms", required = true)
	String inputTerms, Model model, HttpServletRequest httpServletRequest) throws UnsupportedEncodingException,
			IOException
	{
		// if (StringUtils.isEmpty(ontologyIri) ||
		// StringUtils.isEmpty(inputTerms)) return init(model);
		// String sessionId = httpServletRequest.getSession().getId();
		//
		// File uploadFile = fileStore.store(new
		// ByteArrayInputStream(inputTerms.getBytes("UTF8")), sessionId
		// + "_input.txt");
		// CsvRepository csvRepository = new CsvRepository(uploadFile,
		// Arrays.<CellProcessor> asList(
		// new LowerCaseProcessor(), new TrimProcessor()),
		// OntologyServiceImpl.DEFAULT_SEPARATOR);
		// ontologyServiceSessionData.addDataBySession(sessionId, ontologyIri,
		// csvRepository);
		//
		// model.addAttribute("ontologyUrl",
		// ontologyServiceSessionData.getOntologyIriBySession(sessionId));
		// model.addAttribute("total",
		// ontologyServiceSessionData.getTotalNumberBySession(sessionId));

		return init(model);
	}

	@RequestMapping(method = POST, value = "/match/upload", headers = "Content-Type=multipart/form-data")
	public String upload(@RequestParam(value = "taskName", required = true)
	String entityName, @RequestParam(value = "selectOntologies", required = true)
	String ontologyIri, @RequestParam(value = "file", required = true)
	Part file, Model model, HttpServletRequest httpServletRequest) throws Exception
	{
		if (StringUtils.isEmpty(ontologyIri) || file == null) return init(model);
		entityName = entityName.replaceAll(ILLEGAL_PATTERN, ILLEGAL_PATTERN_REPLACEMENT);
		if (dataService.hasRepository(entityName))
		{
			model.addAttribute("message", "The task name has existed!");
			return init(model);
		}

		String sessionId = httpServletRequest.getSession().getId();
		File uploadFile = fileStore.store(file.getInputStream(), sessionId + "_input.csv");
		RepositoryCollection repositoryCollection = getRepositoryCollection(entityName, uploadFile);

		String userName = userAccountService.getCurrentUser().getUsername();
		uploadProgress.registerUser(userName, 0);
		processInputTermService.process(userName, entityName, ontologyIri, uploadFile, repositoryCollection);
		model.addAttribute("isRunning", uploadProgress.isUserExists(userName));

		return matchResult(entityName, model);
	}

	@RequestMapping(method = GET, value = "/match/download")
	public void download(HttpServletResponse response, Model model, HttpServletRequest httpServletRequest)
			throws IOException
	{
		// String sessionId = httpServletRequest.getSession().getId();
		// if
		// (!StringUtils.isEmpty(ontologyServiceSessionData.getOntologyIriBySession(sessionId))
		// && ontologyServiceSessionData.getCsvRepositoryBySession(sessionId) !=
		// null)
		// {
		// ExcelWriter excelWriter = null;
		// try
		// {
		// response.setContentType("application/vnd.ms-excel");
		// response.addHeader("Content-Disposition", "attachment; filename=" +
		// getCsvFileName("match-result"));
		// excelWriter = new ExcelWriter(response.getOutputStream());
		// excelWriter.addCellProcessor(new LowerCaseProcessor(true, false));
		// int totalNumberBySession =
		// ontologyServiceSessionData.getTotalNumberBySession(sessionId);
		// int iteration = totalNumberBySession / 1000 + 1;
		// List<String> columnHeaders = Arrays.asList("InputTerm",
		// "OntologyTerm", "Synonym", "OntologyTermUrl",
		// "OntologyUrl", "Score");
		// for (int i = 0; i < iteration; i++)
		// {
		// Writable sheetWriter = excelWriter.createWritable("result" + (i + 1),
		// columnHeaders);
		// int lowerBound = i * 1000;
		// int upperBound = (i + 1) * 1000 < totalNumberBySession ? (i + 1) *
		// 1000 : totalNumberBySession;
		//
		// for (Entity entity : ontologyServiceSessionData.getSubList(sessionId,
		// lowerBound, upperBound))
		// {
		// int count = 0;
		// for (Map<String, Object> ontologyTermEntity :
		// ontologyService.searchEntity(
		// ontologyServiceSessionData.getOntologyIriBySession(sessionId),
		// entity)
		// .getOntologyTerms())
		// {
		// Entity row = new MapEntity();
		// if (count == 0)
		// {
		// row.set("InputTerm",
		// gatherInfo(OntologyServiceUtil.getEntityAsMap(entity)));
		// }
		// row.set("OntologyTerm",
		// ontologyTermEntity.get(OntologyTermQueryRepository.ONTOLOGY_TERM));
		// row.set("Synonym",
		// ontologyTermEntity.get(OntologyTermQueryRepository.SYNONYMS));
		// row.set("OntologyTermUrl",
		// ontologyTermEntity.get(OntologyTermQueryRepository.ONTOLOGY_TERM_IRI));
		// row.set("OntologyUrl",
		// ontologyTermEntity.get(OntologyTermQueryRepository.ONTOLOGY_IRI));
		// row.set("Score",
		// ontologyTermEntity.get(OntologyServiceImpl.COMBINED_SCORE));
		// sheetWriter.add(row);
		// count++;
		// }
		// }
		// }
		// }
		// finally
		// {
		// if (excelWriter != null) IOUtils.closeQuietly(excelWriter);
		// }
		// }
	}

	@RequestMapping(method = POST, value = "/match/retrieve")
	@ResponseBody
	public EntityCollectionResponse matchResult(@RequestBody
	OntologyServiceRequest ontologyServiceRequest, HttpServletRequest httpServletRequest)
	{
		List<Map<String, Object>> entityMaps = new ArrayList<Map<String, Object>>();
		String entityName = ontologyServiceRequest.getEntityName();
		String ontologyIri = ontologyServiceRequest.getOntologyIri();
		EntityPager entityPager = ontologyServiceRequest.getEntityPager();
		boolean isMatched = ontologyServiceRequest.isMatched();

		long count = dataService.count(
				MathcingTaskContentEntity.ENTITY_NAME,
				new QueryImpl().eq(MathcingTaskContentEntity.REF_ENTITY, entityName).and()
						.eq(MathcingTaskContentEntity.VALIDATED, isMatched));

		int start = entityPager.getStart();
		int num = entityPager.getNum();

		Iterable<Entity> entities = dataService.findAll(
				MathcingTaskContentEntity.ENTITY_NAME,
				new QueryImpl().eq(MathcingTaskContentEntity.REF_ENTITY, entityName).and()
						.eq(MathcingTaskContentEntity.VALIDATED, isMatched).offset(start).pageSize(num));
		for (Entity entity : entities)
		{
			Entity RefEntity = dataService.findOne(
					entityName,
					new QueryImpl().eq(AdaptedCsvRepository.ALLOWED_IDENTIFIER,
							entity.getString(MathcingTaskContentEntity.INPUT_TERM)));
			Map<String, Object> outputEntity = new HashMap<String, Object>();
			outputEntity.put("inputTerm", OntologyServiceUtil.getEntityAsMap(RefEntity));
			outputEntity.put("matchedTerm", OntologyServiceUtil.getEntityAsMap(entity));
			outputEntity.put(
					"ontologyTerm",
					OntologyServiceUtil.getEntityAsMap(ontologyService.getOntologyTermEntity(
							entity.getString(MathcingTaskContentEntity.MATCHED_TERM), ontologyIri)));
			entityMaps.add(outputEntity);
		}
		EntityPager pager = new EntityPager(start, num, (long) count, null);
		return new EntityCollectionResponse(pager, entityMaps, "/match/retrieve");
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

	private String gatherInfo(Map<String, Object> inputData)
	{
		StringBuilder stringBuilder = new StringBuilder();
		for (Entry<String, Object> entry : inputData.entrySet())
		{
			if (stringBuilder.length() != 0)
			{
				stringBuilder.append(EXCEL_NEWLINE_CHAR);
			}
			stringBuilder.append(entry.getKey()).append(':').append(entry.getValue());
		}
		return stringBuilder.toString();
	}

	private String firstAttributeValue(Entity entity)
	{
		StringBuilder stringBuilder = new StringBuilder();

		for (String attributeName : entity.getAttributeNames())
		{
			stringBuilder.append(entity.get(attributeName));
			break;
		}
		return stringBuilder.toString();
	}

	private String getCsvFileName(String dataSetName)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dataSetName + "_" + dateFormat.format(new Date()) + ".xls";
	}
}