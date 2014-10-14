package org.molgenis.ontology.service;

import static org.molgenis.ontology.service.OntologyServiceController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.Writable;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.excel.ExcelWriter;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.processor.LowerCaseProcessor;
import org.molgenis.data.processor.TrimProcessor;
import org.molgenis.data.rest.EntityCollectionResponse;
import org.molgenis.data.rest.EntityPager;
import org.molgenis.data.semantic.OntologyServiceResult;
import org.molgenis.data.semantic.OntologyTerm;
import org.molgenis.data.support.MapEntity;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(URI)
public class OntologyServiceController extends MolgenisPluginController
{
	@Autowired
	private OntologyServiceImpl ontologyService;

	@Autowired
	private OntologyServiceSessionData ontologyServiceSessionData;

	@Autowired
	private FileStore fileStore;

	public static final String ID = "ontologyservice";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	public static final int INVALID_TOTAL_NUMBER = -1;
	private static final String EXCEL_NEWLINE_CHAR = "\n";

	public OntologyServiceController()
	{
		super(URI);
	}

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		model.addAttribute("ontologies", ontologyService.getAllOntologies());
		return "ontology-match-view";
	}

	@RequestMapping(method = POST, value = "/match")
	public String match(@RequestParam(value = "selectOntologies", required = true)
	String ontologyIri, @RequestParam(value = "inputTerms", required = true)
	String inputTerms, Model model, HttpServletRequest httpServletRequest) throws UnsupportedEncodingException,
			IOException
	{
		if (StringUtils.isEmpty(ontologyIri) || StringUtils.isEmpty(inputTerms)) return init(model);
		String sessionId = httpServletRequest.getSession().getId();

		File uploadFile = fileStore.store(new ByteArrayInputStream(inputTerms.getBytes("UTF8")), sessionId
				+ "_input.txt");
		ontologyServiceSessionData.addDataBySession(
				sessionId,
				ontologyIri,
				new CsvRepository(uploadFile, Arrays.<CellProcessor> asList(new LowerCaseProcessor(),
						new TrimProcessor()), OntologyServiceImpl.DEFAULT_SEPARATOR));

		model.addAttribute("ontologyUrl", ontologyServiceSessionData.getOntologyIriBySession(sessionId));
		model.addAttribute("total", ontologyServiceSessionData.getTotalNumberBySession(sessionId));

		return "ontology-match-view-result";
	}

	@RequestMapping(method = POST, value = "/match/upload", headers = "Content-Type=multipart/form-data")
	public String upload(@RequestParam(value = "selectOntologies", required = true)
	String ontologyIri, @RequestParam(value = "file", required = true)
	Part file, Model model, HttpServletRequest httpServletRequest) throws IOException
	{
		if (StringUtils.isEmpty(ontologyIri) || file == null) return init(model);
		String sessionId = httpServletRequest.getSession().getId();
		CsvRepository reader = null;
		try
		{
			File uploadFile = fileStore.store(file.getInputStream(), sessionId + "_input.txt");
			ontologyServiceSessionData.addDataBySession(
					sessionId,
					ontologyIri,
					new CsvRepository(uploadFile, Arrays.<CellProcessor> asList(new LowerCaseProcessor(),
							new TrimProcessor()), OntologyServiceImpl.DEFAULT_SEPARATOR));

			model.addAttribute("ontologyUrl", ontologyServiceSessionData.getOntologyIriBySession(sessionId));
			model.addAttribute("total", ontologyServiceSessionData.getTotalNumberBySession(sessionId));
		}
		finally
		{
			if (reader != null) IOUtils.closeQuietly(reader);
		}
		return "ontology-match-view-result";
	}

	@RequestMapping(method = GET, value = "/match/download")
	public void download(HttpServletResponse response, Model model, HttpServletRequest httpServletRequest)
			throws IOException
	{
		String sessionId = httpServletRequest.getSession().getId();
		if (!StringUtils.isEmpty(ontologyServiceSessionData.getOntologyIriBySession(sessionId))
				&& ontologyServiceSessionData.getCsvRepositoryBySession(sessionId) != null)
		{
			ExcelWriter excelWriter = null;
			try
			{
				response.setContentType("application/vnd.ms-excel");
				response.addHeader("Content-Disposition", "attachment; filename=" + getCsvFileName("match-result"));
				excelWriter = new ExcelWriter(response.getOutputStream());
				excelWriter.addCellProcessor(new LowerCaseProcessor(true, false));
				int totalNumberBySession = ontologyServiceSessionData.getTotalNumberBySession(sessionId);
				int iteration = totalNumberBySession / 1000 + 1;
				List<String> columnHeaders = Arrays.asList("InputTerm", "OntologyTerm", "Synonym", "OntologyTermUrl",
						"OntologyUrl", "Score");
				for (int i = 0; i < iteration; i++)
				{
					Writable sheetWriter = excelWriter.createWritable("result" + (i + 1), columnHeaders);
					int lowerBound = i * 1000;
					int upperBound = (i + 1) * 1000 < totalNumberBySession ? (i + 1) * 1000 : totalNumberBySession;

					for (Entity entity : ontologyServiceSessionData.getSubList(sessionId, lowerBound, upperBound))
					{
						OntologyServiceResult searchEntity = ontologyService.searchEntity(
								ontologyServiceSessionData.getOntologyIriBySession(sessionId), entity);

						int count = 0;
						for (OntologyTerm ontologyTerm : searchEntity.getOntologyTerms())
						{
							Entity row = new MapEntity();
							if (count == 0)
							{
								row.set("InputTerm", gatherInfo(searchEntity.getInputData()));
							}
							row.set("OntologyTerm", ontologyTerm.getLabel());
							for (String synonym : ontologyTerm.getSynonyms())
							{
								row.set("Synonym", synonym);
								break;
							}
							row.set("OntologyTermUrl", ontologyTerm.getIRI());
							row.set("OntologyUrl", ontologyTerm.getOntology().getIri());
							row.set("Score", ontologyTerm.getScore().doubleValue());
							sheetWriter.add(row);
							count++;
						}
					}
				}
			}
			finally
			{
				if (excelWriter != null) IOUtils.closeQuietly(excelWriter);
			}
		}
	}

	@RequestMapping(method = POST, value = "/match/retrieve")
	@ResponseBody
	public EntityCollectionResponse matchResult(@RequestBody
	EntityPager entityPager, HttpServletRequest httpServletRequest)
	{
		String sessionId = httpServletRequest.getSession().getId();

		if (StringUtils.isEmpty(ontologyServiceSessionData.getOntologyIriBySession(sessionId))) throw new RuntimeException(
				"The ontologyUrl is empty!");
		if (ontologyServiceSessionData.getCsvRepositoryBySession(sessionId) == null) throw new RuntimeException(
				"The ontologyUrl is empty!");

		List<Map<String, Object>> entities = new ArrayList<Map<String, Object>>();

		if (ontologyServiceSessionData.validationAttributesBySession(sessionId)
				&& ontologyServiceSessionData.getTotalNumberBySession(sessionId) != INVALID_TOTAL_NUMBER)
		{
			int count = ontologyServiceSessionData.getTotalNumberBySession(sessionId);
			int start = entityPager.getStart();
			int num = entityPager.getNum();
			int toIndex = start + num;

			for (Entity entity : ontologyServiceSessionData.getSubList(sessionId, start,
					toIndex > count ? count : toIndex))
			{
				Map<String, Object> outputEntity = new HashMap<String, Object>();
				outputEntity.put("term", firstAttributeValue(entity));
				outputEntity.put("results", ontologyService.searchEntity(
						ontologyServiceSessionData.getOntologyIriBySession(sessionId), entity));
				entities.add(outputEntity);
			}
			EntityPager pager = new EntityPager(start, num, (long) count, null);
			return new EntityCollectionResponse(pager, entities, "/match/retrieve");
		}
		return new EntityCollectionResponse(new EntityPager(0, 0, (long) 0, null),
				Collections.<Map<String, Object>> emptyList(), "/match/retrieve");
	}

	@RequestMapping(method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public OntologyServiceResult query(@RequestBody
	OntologyServiceRequest ontologyTermRequest)
	{
		String ontologyUrl = ontologyTermRequest.getOntologyIri();
		String queryString = ontologyTermRequest.getQueryString();
		if (ontologyUrl == null || queryString == null) return new OntologyServiceResult("Your input cannot be null!");
		return ontologyService.search(ontologyUrl, queryString);
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