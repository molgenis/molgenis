package org.molgenis.omx.biobankconnect.ontologyservice;

import static org.molgenis.omx.biobankconnect.ontologyservice.OntologyTreeController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;
import java.util.Map;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.biobankconnect.utils.OntologyTermRepository;
import org.molgenis.search.Hit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(URI)
public class OntologyTreeController extends MolgenisPluginController
{
	@Autowired
	private OntologyService ontologyService;

	public static final String ID = "ontologytree";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	public OntologyTreeController()
	{
		super(URI);
	}

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		model.addAttribute("ontologies", ontologyService.getAllOntologies());
		return "ontology-tree-view";
	}

	@RequestMapping(value = "/build", method = POST, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public OntologyMetaResponse getEntityMetaData(@RequestBody
	OntologyTermMetaRequest request)
	{
		Hit ontology = ontologyService.getOntologyByUrl(request.getOntologyUrl());
		OntologyMetaResponse response = new OntologyMetaResponse(ontology);
		for (Hit hit : ontologyService.getRootOntologyTerms(request.getOntologyUrl()))
		{
			response.addAttribute(new OntologyTermMetaResponse(hit));
		}
		return response;
	}

	@RequestMapping(value = "/info", method = POST, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public OntologyTermMetaResponse getOntologyTermInfo(@RequestBody
	OntologyTermMetaRequest request)
	{
		List<Hit> ontologyTerms = ontologyService.getOntologyTermInfo(request.getOntologyUrl(),
				request.getOntologyTermUrl());
		OntologyTermMetaResponse response = new OntologyTermMetaResponse(ontologyTerms.get(0));
		for (Hit hit : ontologyTerms)
		{
			Map<String, Object> columnValueMap = hit.getColumnValueMap();
			if (columnValueMap.containsKey(OntologyTermRepository.SYNONYMS))
			{
				response.addSynonyms(columnValueMap.get(OntologyTermRepository.SYNONYMS).toString());
			}
		}
		return response;
	}

	@RequestMapping(value = "/meta", method = POST, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public OntologyTermMetaResponse children(@RequestBody
	OntologyTermMetaRequest request)
	{
		Hit ontologyTerms = ontologyService.findOntologyTerm(request.getOntologyUrl(), request.getOntologyTermUrl(),
				request.getNodePath());
		OntologyTermMetaResponse response = new OntologyTermMetaResponse(ontologyTerms);
		for (Hit hit : ontologyService.getChildren(request.getOntologyUrl(), request.getOntologyTermUrl(),
				request.getNodePath()))
		{
			response.addAttribute(new OntologyTermMetaResponse(hit));
		}
		return response;
	}
}