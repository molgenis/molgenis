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
import org.molgenis.data.merge.RepositoryMerger;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.util.ErrorMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

@Controller
@RequestMapping(URI)
public class GeneticRepositoryMergerController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(GeneticRepositoryMergerController.class);

	public static final String ID = "geneticrepositorymerger";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	public static final DefaultAttributeMetaData CHROM = new DefaultAttributeMetaData("#CHROM",
			MolgenisFieldTypes.FieldTypeEnum.STRING);
	public static final DefaultAttributeMetaData POS = new DefaultAttributeMetaData("POS",
			MolgenisFieldTypes.FieldTypeEnum.LONG);
	public static final DefaultAttributeMetaData REF = new DefaultAttributeMetaData("REF",
			MolgenisFieldTypes.FieldTypeEnum.STRING);
	public static final DefaultAttributeMetaData ALT = new DefaultAttributeMetaData("ALT",
			MolgenisFieldTypes.FieldTypeEnum.STRING);

	private final ArrayList<AttributeMetaData> commonAttributes;
	private final RepositoryMerger repositoryMerger;
	private final DataService dataService;

	@Autowired
	public GeneticRepositoryMergerController(RepositoryMerger repositoryMerger, DataService dataService)
	{
		super(URI);

		this.repositoryMerger = repositoryMerger;
		this.dataService = dataService;

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
		if (dataService.hasRepository(resultSet))
		{
			throw new RuntimeException(
					"An entity with this name already exists. Please try again with a different name");
		}
		else
		{
			// create list of entities to merge
			List<Repository> geneticRepositories = new ArrayList<Repository>();
			for (String name : inputSets)
			{
				if (!name.equals(resultSet))
				{
					if (dataService.hasRepository(name))
					{
						geneticRepositories.add(dataService.getRepository(name));
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

			EntityMetaData mergedEntityMetaData = repositoryMerger.mergeMetaData(geneticRepositories, commonAttributes,
					resultSet);
			Repository mergedRepository = dataService.getMeta().addEntityMeta(mergedEntityMetaData);
			repositoryMerger.merge(geneticRepositories, commonAttributes, mergedRepository);
		}
		return resultSet;
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		LOG.error(e.getMessage(), e);
		return new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage(
				"An error occurred. Please contact the administrator.<br />Message:" + e.getMessage()));
	}
}
