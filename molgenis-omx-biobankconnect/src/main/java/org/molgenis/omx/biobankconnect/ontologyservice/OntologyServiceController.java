package org.molgenis.omx.biobankconnect.ontologyservice;

import static org.molgenis.omx.biobankconnect.ontologyservice.OntologyServiceController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.processor.LowerCaseProcessor;
import org.molgenis.data.processor.TrimProcessor;
import org.molgenis.data.rest.EntityCollectionResponse;
import org.molgenis.data.rest.EntityPager;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchResult;
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
	private OntologyService ontologyService;

	@Autowired
	private OntologyServiceSessionData ontologyServiceSessionData;

	@Autowired
	private FileStore fileStore;

	public static final String ID = "ontologyservice";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	// private String ontologyUrl = null;
	// private List<String> inputLines = new ArrayList<String>();

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
						new TrimProcessor()), OntologyService.DEFAULT_SEPARATOR));

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
							new TrimProcessor()), OntologyService.DEFAULT_SEPARATOR));

			model.addAttribute("ontologyUrl", ontologyServiceSessionData.getOntologyIriBySession(sessionId));
			model.addAttribute("total", ontologyServiceSessionData.getTotalNumberBySession(sessionId));
		}
		finally
		{
			if (reader != null) IOUtils.closeQuietly(reader);
		}
		return "ontology-match-view-result";
	}

	// @RequestMapping(method = GET, value = "/match/download")
	// public void download(HttpServletResponse response, Model model) throws
	// IOException
	// {
	// if (ontologyUrl != null && inputLines != null)
	// {
	// ExcelWriter excelWriter = null;
	// try
	// {
	// response.setContentType("application/vnd.ms-excel");
	// response.addHeader("Content-Disposition", "attachment; filename=" +
	// getCsvFileName("match-result"));
	// excelWriter = new ExcelWriter(response.getOutputStream());
	// excelWriter.addCellProcessor(new LowerCaseProcessor(true, false));
	// int iteration = inputLines.size() / 1000 + 1;
	// List<String> columnHeaders = Arrays.asList("InputTerm", "OntologyTerm",
	// "Synonym used for matching",
	// "OntologyTermUrl", "OntologyUrl", "CombinedScore", "LuceneScore");
	// for (int i = 0; i < iteration; i++)
	// {
	// Writable sheetWriter = excelWriter.createWritable("result" + (i + 1),
	// columnHeaders);
	// int lowerBound = i * 1000;
	// int upperBound = (i + 1) * 1000 < inputLines.size() ? (i + 1) * 1000 :
	// inputLines.size();
	//
	// for (String term : inputLines.subList(lowerBound, upperBound))
	// {
	// for (Hit hit : ontologyService.search(ontologyUrl, term).getSearchHits())
	// {
	// Entity row = new MapEntity();
	// row.set("InputTerm", term);
	// row.set("OntologyTerm", hit.getColumnValueMap().get("ontologyTerm"));
	// row.set("Synonym used for matching",
	// hit.getColumnValueMap().get("ontologyTermSynonym"));
	// row.set("OntologyTermUrl",
	// hit.getColumnValueMap().get("ontologyTermIRI"));
	// row.set("OntologyUrl", hit.getColumnValueMap().get("ontologyIRI"));
	// row.set("CombinedScore", hit.getColumnValueMap().get("combinedScore"));
	// row.set("LuceneScore", hit.getColumnValueMap().get("score"));
	// sheetWriter.add(row);
	// }
	// }
	// }
	// }
	// finally
	// {
	// if (excelWriter != null) IOUtils.closeQuietly(excelWriter);
	// }
	// }
	// }

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

		int count = ontologyServiceSessionData.getTotalNumberBySession(sessionId);
		int start = entityPager.getStart();
		int num = entityPager.getNum();
		int toIndex = start + num;

		for (Entity entity : ontologyServiceSessionData.getSubList(sessionId, start, toIndex > count ? count : toIndex))
		{
			Map<String, Object> outputEntity = new HashMap<String, Object>();
			outputEntity.put("term", gatherInfo(entity));
			outputEntity
					.put("results", ontologyService.searchEntity(
							ontologyServiceSessionData.getOntologyIriBySession(sessionId), entity));
			entities.add(outputEntity);
		}
		EntityPager pager = new EntityPager(start, num, (long) count, null);
		return new EntityCollectionResponse(pager, entities, "/match/retrieve");
	}

	@RequestMapping(method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public SearchResult query(@RequestBody
	OntologyServiceRequest ontologyTermRequest)
	{
		String ontologyUrl = ontologyTermRequest.getOntologyIri();
		String queryString = ontologyTermRequest.getQueryString();
		if (ontologyUrl == null || queryString == null) return new SearchResult(0, Collections.<Hit> emptyList());
		return ontologyService.search(ontologyUrl, queryString);
	}

	private String gatherInfo(Entity entity)
	{
		StringBuilder stringBuilder = new StringBuilder();

		for (String attributeName : entity.getAttributeNames())
		{
			stringBuilder.append(entity.get(attributeName));
			break;
		}
		return stringBuilder.toString();
	}

	private List<String> collectAllLinesFromFile(File uploadFile) throws IOException
	{
		List<String> terms = new ArrayList<String>();
		InputStream inputStream = null;
		BufferedReader bufferedReader = null;
		String line = null;
		try
		{
			inputStream = new FileInputStream(uploadFile);
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
			while ((line = bufferedReader.readLine()) != null)
			{
				if (!StringUtils.isEmpty(line)) terms.add(line.trim());
			}
		}
		finally
		{
			if (bufferedReader != null) bufferedReader.close();
		}
		return terms;
	}

	private String getCsvFileName(String dataSetName)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dataSetName + "_" + dateFormat.format(new Date()) + ".xls";
	}
}