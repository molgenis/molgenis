package org.molgenis.geneticrepositorymerger.controller;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.merge.RepositoryMerger;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.ArrayList;

import static org.molgenis.geneticrepositorymerger.controller.GeneticRepositoryMergerController.URI;

@Controller
@RequestMapping(URI)
public class GeneticRepositoryMergerController extends MolgenisPluginController
{
	private static final Logger logger = Logger.getLogger(GeneticRepositoryMergerController.class);
	public static final String ID = "geneticrepositorymerger";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	public static final DefaultAttributeMetaData CHROM = new DefaultAttributeMetaData("#CHROM",
			MolgenisFieldTypes.FieldTypeEnum.STRING);
	public static final DefaultAttributeMetaData POS = new DefaultAttributeMetaData("POS",
			MolgenisFieldTypes.FieldTypeEnum.STRING);
	public static final DefaultAttributeMetaData REF = new DefaultAttributeMetaData("REF",
			MolgenisFieldTypes.FieldTypeEnum.STRING);
	public static final DefaultAttributeMetaData ALT = new DefaultAttributeMetaData("ALT",
			MolgenisFieldTypes.FieldTypeEnum.STRING);

	public static final String VKGL = "VKGL";

	private final ArrayList<AttributeMetaData> commonAttributes;

	@Autowired
	private RepositoryMerger repositoryMerger;

	@Autowired
	private DataService dataService;

	@Autowired
	private SearchService searchService;

	public GeneticRepositoryMergerController()
	{
		super(URI);
		commonAttributes = new ArrayList<AttributeMetaData>();
		commonAttributes.add(CHROM);
		commonAttributes.add(POS);
		commonAttributes.add(REF);
		commonAttributes.add(ALT);
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.GET)
	public String init() throws Exception
	{
		return "view-vcfrepositorymerger";
	}

	@RequestMapping(method = RequestMethod.POST, value = "mergeRepositories")
	@ResponseStatus(HttpStatus.OK)
	public void merge()
	{
		if (dataService.hasRepository(VKGL))
		{
			if (searchService.documentTypeExists(VKGL))
			{
				searchService.deleteDocumentsByType(VKGL);
			}
			else
			{
				throw new RuntimeException("Repository " + VKGL + " is not a ElasticSearchRepository");
			}
		}
		dataService.getEntityNames();
		List<Repository> geneticRepositories = new ArrayList<Repository>();
		for (String name : dataService.getEntityNames())
		{
			if (dataService.getEntityMetaData(name).getAttribute(CHROM.getName()) != null
					&& dataService.getEntityMetaData(name).getAttribute(POS.getName()) != null
					&& dataService.getEntityMetaData(name).getAttribute(REF.getName()) != null
					&& dataService.getEntityMetaData(name).getAttribute(ALT.getName()) != null)
			{
				if (!name.equals(VKGL))
				{
					geneticRepositories.add(dataService.getRepositoryByEntityName(name));
				}
			}
		}
		repositoryMerger.merge(geneticRepositories, commonAttributes, VKGL);
	}
}
