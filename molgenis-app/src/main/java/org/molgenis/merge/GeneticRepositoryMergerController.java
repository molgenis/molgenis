package org.molgenis.merge;

import static org.molgenis.merge.GeneticRepositoryMergerController.URI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.ElasticsearchRepository;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.merge.RepositoryMerger;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

@Controller
@RequestMapping(URI)
public class GeneticRepositoryMergerController extends MolgenisPluginController
{
	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(GeneticRepositoryMergerController.class);

	public static final String ID = "geneticrepositorymerger";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	public static final String ID_FIELD = "ID";
	public static final DefaultAttributeMetaData CHROM = new DefaultAttributeMetaData("#CHROM",
			MolgenisFieldTypes.FieldTypeEnum.STRING);
	public static final DefaultAttributeMetaData POS = new DefaultAttributeMetaData("POS",
			MolgenisFieldTypes.FieldTypeEnum.LONG);
	public static final DefaultAttributeMetaData REF = new DefaultAttributeMetaData("REF",
			MolgenisFieldTypes.FieldTypeEnum.STRING);
	public static final DefaultAttributeMetaData ALT = new DefaultAttributeMetaData("ALT",
			MolgenisFieldTypes.FieldTypeEnum.STRING);

	private final ArrayList<AttributeMetaData> commonAttributes;
	private RepositoryMerger repositoryMerger;
	private DataService dataService;
	private SearchService searchService;

	@Autowired
	public GeneticRepositoryMergerController(RepositoryMerger repositoryMerger, DataService dataService,
			SearchService searchService)
	{
		super(URI);

		this.repositoryMerger = repositoryMerger;
		this.dataService = dataService;
		this.searchService = searchService;

		commonAttributes = new ArrayList<AttributeMetaData>();
		commonAttributes.add(CHROM);
		commonAttributes.add(POS);
		commonAttributes.add(REF);
		commonAttributes.add(ALT);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model) throws Exception
	{
		dataService.getEntityNames();
		List<String> geneticRepositories = new ArrayList<String>();
		for (String name : dataService.getEntityNames())
		{
			if (dataService.getEntityMetaData(name).getAttribute(CHROM.getName()) != null
					&& dataService.getEntityMetaData(name).getAttribute(POS.getName()) != null
					&& dataService.getEntityMetaData(name).getAttribute(REF.getName()) != null
					&& dataService.getEntityMetaData(name).getAttribute(ALT.getName()) != null)
			{
				geneticRepositories.add(name);
			}
		}

		Iterable<EntityMetaData> entitiesMeta = Iterables.transform(geneticRepositories,
				new Function<String, EntityMetaData>()
				{
					@Override
					public EntityMetaData apply(String entityName)
					{
						return dataService.getEntityMetaData(entityName);
					}
				});
		model.addAttribute("entitiesMeta", entitiesMeta);

		return "view-geneticrepositorymerger";
	}

	@RequestMapping(method = RequestMethod.POST, value = "mergeRepositories")
	@ResponseBody
	public String merge(@RequestParam("resultDataset") String resultSet, @RequestParam("datasets") String[] inputSets)
			throws IOException
	{
		// create list of entities to merge
		List<Repository> geneticRepositories = new ArrayList<Repository>();
		for (String name : inputSets)
		{
			if (!name.equals(resultSet))
			{
				if (dataService.hasRepository(name))
				{
					geneticRepositories.add(dataService.getRepositoryByEntityName(name));
				}
				else
				{
					throw new RuntimeException("Cannot merge Repository: " + name + " it does not exist");
				}
			}
			else
			{
				throw new RuntimeException("Cannot merge Repository with itself");
			}
		}
		// Delete if exists
		if (dataService.hasRepository(resultSet))
		{
			if (searchService.documentTypeExists(resultSet))
			{
				searchService.deleteDocumentsByType(resultSet);
				dataService.removeRepository(resultSet);
			}
			else
			{
				throw new RuntimeException("Repository " + resultSet + " is not a ElasticSearchRepository");
			}
		}

		EntityMetaData mergedEntityMetaData = repositoryMerger.mergeMetaData(geneticRepositories, commonAttributes,
				resultSet);
		searchService.createMappings(mergedEntityMetaData, true, true, true, true);

		ElasticsearchRepository mergedRepository = new ElasticsearchRepository(mergedEntityMetaData, searchService);
		repositoryMerger.merge(geneticRepositories, commonAttributes, mergedRepository, ID_FIELD);

		return resultSet;
	}
}
