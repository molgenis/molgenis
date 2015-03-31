package org.molgenis.ontology.controller;

import static org.molgenis.ontology.controller.SortaServiceAnonymousController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.processor.LowerCaseProcessor;
import org.molgenis.data.processor.TrimProcessor;
import org.molgenis.framework.ui.MolgenisPluginController;
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
}