package org.molgenis.ontology.controller;

import static org.molgenis.ontology.controller.SortaServiceAnonymousController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import org.molgenis.data.Repository;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.csv.CsvWriter;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.processor.LowerCaseProcessor;
import org.molgenis.data.processor.TrimProcessor;
import org.molgenis.data.support.MapEntity;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.ontology.sorta.MatchingTaskEntityMetaData;
import org.molgenis.ontology.sorta.SortaModifiableCsvRepository;
import org.molgenis.ontology.sorta.SortaService;
import org.molgenis.ontology.sorta.SortaServiceImpl;
import org.molgenis.ontology.utils.SortaServiceUtil;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

@Controller
@RequestMapping(URI)
public class SortaServiceAnonymousController extends MolgenisPluginController
{
	@Autowired
	private DataService dataService;

	@Autowired
	private SortaService sortaService;

	@Autowired
	private FileStore fileStore;

	public static final String VIEW_NAME = "ontology-match-annonymous-view";
	public static final String ID = "sorta_anonymous";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	public SortaServiceAnonymousController()
	{
		super(URI);
	}

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		model.addAttribute("ontologies", SortaServiceUtil.getEntityAsMap(sortaService.getAllOntologyEntities()));
		return VIEW_NAME;
	}

	@RequestMapping(method = POST, value = "/match")
	public String match(@RequestParam(value = "selectOntologies", required = true) String ontologyIri,
			@RequestParam(value = "inputTerms", required = true) String inputTerms,
			HttpServletRequest httpServletRequest, Model model) throws UnsupportedEncodingException, IOException
	{

		String sessionId = httpServletRequest.getSession().getId();
		File uploadFile = fileStore.store(new ByteArrayInputStream(inputTerms.getBytes("UTF8")), sessionId
				+ "_input.txt");
		httpServletRequest.getSession().setAttribute("filePath", uploadFile.getAbsoluteFile());
		httpServletRequest.getSession().setAttribute("ontologyIri", ontologyIri);
		model.addAttribute("showResult", true);
		return VIEW_NAME;
	}

	@RequestMapping(method = POST, value = "/match/upload")
	public String upload(@RequestParam(value = "selectOntologies", required = true) String ontologyIri,
			@RequestParam(value = "file", required = true) Part file, HttpServletRequest httpServletRequest, Model model)
			throws UnsupportedEncodingException, IOException
	{

		String sessionId = httpServletRequest.getSession().getId();
		File uploadFile = fileStore.store(file.getInputStream(), sessionId + "_input.csv");
		httpServletRequest.getSession().setAttribute("filePath", uploadFile.getAbsoluteFile());
		httpServletRequest.getSession().setAttribute("ontologyIri", ontologyIri);
		model.addAttribute("showResult", true);
		return VIEW_NAME;
	}

	@RequestMapping(method = GET, value = "/retrieve")
	@ResponseBody
	public List<Map<String, Object>> matchResult(HttpServletRequest httpServletRequest)
			throws UnsupportedEncodingException, IOException
	{
		Object filePath = httpServletRequest.getSession().getAttribute("filePath");
		Object ontologyIriObject = httpServletRequest.getSession().getAttribute("ontologyIri");
		List<Map<String, Object>> responseResults = new ArrayList<Map<String, Object>>();
		if (filePath != null && ontologyIriObject != null)
		{
			File uploadFile = new File(filePath.toString());
			SortaModifiableCsvRepository csvRepository = new SortaModifiableCsvRepository(uploadFile.getName(),
					new CsvRepository(uploadFile, Arrays.<CellProcessor> asList(new LowerCaseProcessor(),
							new TrimProcessor()), SortaServiceImpl.DEFAULT_SEPARATOR));

			if (validateUserInputHeader(csvRepository))
			{
				responseResults.addAll(matchInputWithOntologyTerm(csvRepository, ontologyIriObject.toString()));
			}
		}
		return responseResults;
	}

	@RequestMapping(method = GET, value = "/download")
	public void download(HttpServletRequest httpServletRequest, HttpServletResponse response)
			throws UnsupportedEncodingException, IOException
	{
		CsvWriter csvWriter = null;
		try
		{
			response.setContentType("text/csv");
			response.addHeader("Content-Disposition", "attachment; filename=" + generateCsvFileName("match-result"));

			Object filePath = httpServletRequest.getSession().getAttribute("filePath");
			Object ontologyIriObject = httpServletRequest.getSession().getAttribute("ontologyIri");

			if (filePath != null && ontologyIriObject != null)
			{
				DecimalFormat df = new DecimalFormat("###.##");

				String ontologyIri = ontologyIriObject.toString();
				File uploadFile = new File(filePath.toString());
				SortaModifiableCsvRepository csvRepository = new SortaModifiableCsvRepository(uploadFile.getName(),
						new CsvRepository(uploadFile, Arrays.<CellProcessor> asList(new LowerCaseProcessor(),
								new TrimProcessor()), SortaServiceImpl.DEFAULT_SEPARATOR));

				List<String> columnHeaders = createDownloadTableHeaders(csvRepository);
				csvWriter = new CsvWriter(response.getOutputStream(), SortaServiceImpl.DEFAULT_SEPARATOR);
				csvWriter.writeAttributeNames(columnHeaders);

				for (Entity inputEntity : csvRepository)
				{
					int count = 0;
					for (Entity ontologyTermEntity : sortaService.findOntologyTermEntities(ontologyIri, inputEntity))
					{
						MapEntity mapEntity = new MapEntity();
						if (count == 0)
						{
							mapEntity = new MapEntity(inputEntity);
						}

						if (count >= 20)
						{
							break;
						}

						mapEntity.set(OntologyTermMetaData.ONTOLOGY_TERM_NAME,
								ontologyTermEntity.getString(OntologyTermMetaData.ONTOLOGY_TERM_NAME));

						mapEntity.set(OntologyTermMetaData.ONTOLOGY_TERM_IRI,
								ontologyTermEntity.getString(OntologyTermMetaData.ONTOLOGY_TERM_IRI));

						mapEntity.set(SortaServiceImpl.SCORE,
								df.format(ontologyTermEntity.getDouble(SortaServiceImpl.SCORE)));

						mapEntity.set(SortaServiceImpl.COMBINED_SCORE,
								df.format(ontologyTermEntity.getDouble(SortaServiceImpl.COMBINED_SCORE)));

						csvWriter.add(mapEntity);

						count++;
					}
				}
			}
		}
		finally
		{
			if (csvWriter != null) IOUtils.closeQuietly(csvWriter);
		}
	}

	private List<String> createDownloadTableHeaders(SortaModifiableCsvRepository csvRepository)
	{
		List<String> inputAttributeNames = FluentIterable.from(csvRepository.getEntityMetaData().getAtomicAttributes())
				.transform(new Function<AttributeMetaData, String>()
				{
					public String apply(AttributeMetaData attributeMetaData)
					{
						return attributeMetaData.getName();
					}
				}).filter(attrName -> !StringUtils.equalsIgnoreCase(attrName, MatchingTaskEntityMetaData.IDENTIFIER))
				.toList();

		List<String> columnHeaders = new ArrayList<String>(inputAttributeNames);
		columnHeaders.addAll(Arrays.asList(OntologyTermMetaData.ONTOLOGY_TERM_NAME,
				OntologyTermMetaData.ONTOLOGY_TERM_IRI, SortaServiceImpl.SCORE, SortaServiceImpl.COMBINED_SCORE));
		return columnHeaders;
	}

	private List<Map<String, Object>> matchInputWithOntologyTerm(Repository repository, String ontologyIri)
	{
		return FluentIterable.from(repository).transform(new Function<Entity, Map<String, Object>>()
		{
			public Map<String, Object> apply(Entity inputEntity)
			{
				Iterable<Entity> findOntologyTermEntities = sortaService.findOntologyTermEntities(ontologyIri,
						inputEntity);

				return ImmutableMap.of("inputTerm", SortaServiceUtil.getEntityAsMap(inputEntity), "ontologyTerm",
						SortaServiceUtil.getEntityAsMap(findOntologyTermEntities));
			}
		}).toList();
	}

	private boolean validateUserInputHeader(Repository repository)
	{
		return Iterables.any(repository.getEntityMetaData().getAtomicAttributes(), new Predicate<AttributeMetaData>()
		{
			public boolean apply(AttributeMetaData attr)
			{
				return StringUtils.isNotEmpty(attr.getName())
						&& StringUtils.equalsIgnoreCase(attr.getName(), SortaServiceImpl.DEFAULT_MATCHING_NAME_FIELD);
			}
		});
	}

	private String generateCsvFileName(String dataSetName)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dataSetName + "_" + dateFormat.format(new Date()) + ".csv";
	}
}