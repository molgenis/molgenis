package org.molgenis.ontology.controller;

import static org.molgenis.ontology.controller.SortaServiceAnonymousController.URI;
import static org.molgenis.ontology.sorta.meta.OntologyTermHitEntityMetaData.COMBINED_SCORE;
import static org.molgenis.ontology.sorta.meta.OntologyTermHitEntityMetaData.SCORE;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.csv.CsvWriter;
import org.molgenis.data.support.MapEntity;
import org.molgenis.file.FileStore;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.ontology.sorta.repo.SortaCsvRepository;
import org.molgenis.ontology.sorta.service.SortaService;
import org.molgenis.ontology.sorta.service.impl.SortaServiceImpl;
import org.molgenis.ontology.utils.SortaServiceUtil;
import org.molgenis.ui.MolgenisPluginController;
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
	private SortaService sortaService;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private FileStore fileStore;
	
	public static final String VIEW_NAME = "sorta-match-anonymous-view";
	public static final String ID = "sorta_anonymous";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	public SortaServiceAnonymousController()
	{
		super(URI);
	}

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		model.addAttribute("ontologies", ontologyService.getOntologies());
		return VIEW_NAME;
	}

	@RequestMapping(method = POST, value = "/match")
	public String match(@RequestParam(value = "selectOntologies", required = true) String ontologyIri,
			@RequestParam(value = "inputTerms", required = true) String inputTerms,
			HttpServletRequest httpServletRequest, Model model) throws UnsupportedEncodingException, IOException
	{
		String fileName = httpServletRequest.getSession().getId() + "_input.txt";
		File uploadFile = fileStore.store(new ByteArrayInputStream(inputTerms.getBytes("UTF8")), fileName);

		validateSortaInput(ontologyIri, uploadFile, httpServletRequest, model);

		return init(model);
	}

	@RequestMapping(method = POST, value = "/match/upload")
	public String upload(@RequestParam(value = "selectOntologies", required = true) String ontologyIri,
			@RequestParam(value = "file", required = true) Part file, HttpServletRequest httpServletRequest,
			Model model) throws UnsupportedEncodingException, IOException
	{

		String fileName = httpServletRequest.getSession().getId() + "_input.csv";
		File uploadFile = fileStore.store(file.getInputStream(), fileName);

		validateSortaInput(ontologyIri, uploadFile, httpServletRequest, model);

		return init(model);
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
			SortaCsvRepository csvRepository = new SortaCsvRepository(uploadFile.getName(), uploadFile.getName(), uploadFile);

			if (validateUserInputHeader(csvRepository) && validateUserInputContent(csvRepository))
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
				SortaCsvRepository csvRepository = new SortaCsvRepository(uploadFile);

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

						mapEntity.set(SCORE, df.format(ontologyTermEntity.getDouble(SCORE)));

						mapEntity.set(COMBINED_SCORE, df.format(ontologyTermEntity.getDouble(COMBINED_SCORE)));

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

	private List<String> createDownloadTableHeaders(SortaCsvRepository csvRepository)
	{
		List<String> inputAttributeNames = FluentIterable.from(csvRepository.getEntityMetaData().getAtomicAttributes())
				.transform(new Function<AttributeMetaData, String>()
				{
					public String apply(AttributeMetaData attributeMetaData)
					{
						return attributeMetaData.getName();
					}
				}).filter(attrName -> !StringUtils.equalsIgnoreCase(attrName, SortaCsvRepository.ALLOWED_IDENTIFIER))
				.toList();

		List<String> columnHeaders = new ArrayList<String>(inputAttributeNames);
		columnHeaders.addAll(Arrays.asList(OntologyTermMetaData.ONTOLOGY_TERM_NAME,
				OntologyTermMetaData.ONTOLOGY_TERM_IRI, SCORE, COMBINED_SCORE));
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

	private void validateSortaInput(String ontologyIri, File uploadFile, HttpServletRequest httpServletRequest,
			Model model)
	{
		SortaCsvRepository csvRepository = new SortaCsvRepository(uploadFile);

		HttpSession session = httpServletRequest.getSession();
		session.setAttribute("filePath", uploadFile.getAbsoluteFile());
		session.setAttribute("ontologyIri", ontologyIri);
		model.addAttribute("showResult", true);

		if (!validateUserInputHeader(csvRepository))
		{
			model.addAttribute("message", "The input header is wrong!");
			model.addAttribute("showResult", false);
			session.removeAttribute("filePath");
			session.removeAttribute("ontologyIri");
		}
		else if (!validateUserInputContent(csvRepository))
		{
			model.addAttribute("message", "There are no terms in the input!");
			model.addAttribute("showResult", false);
			session.removeAttribute("filePath");
			session.removeAttribute("ontologyIri");
		}
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

	private boolean validateUserInputContent(SortaCsvRepository csvRepository)
	{
		Iterator<Entity> iterator = csvRepository.iterator();
		return iterator.hasNext();
	}

	private String generateCsvFileName(String dataSetName)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dataSetName + "_" + dateFormat.format(new Date()) + ".csv";
	}
}