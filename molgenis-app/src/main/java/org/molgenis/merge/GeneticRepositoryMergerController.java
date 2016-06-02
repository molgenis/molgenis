package org.molgenis.merge;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.LONG;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.merge.GeneticRepositoryMergerController.URI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.merge.RepositoryMerger;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.AttributeMetaDataFactory;
import org.molgenis.data.meta.EntityMetaData;
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

	public final AttributeMetaData chromAttr;
	public final AttributeMetaData posAttr;
	public final AttributeMetaData refAttr;
	public final AttributeMetaData altAttr;

	private final List<AttributeMetaData> commonAttributes;
	private final RepositoryMerger repositoryMerger;
	private final DataService dataService;
	private final AttributeMetaDataFactory attributeMetaDataFactory;

	@Autowired
	public GeneticRepositoryMergerController(RepositoryMerger repositoryMerger, DataService dataService,
			AttributeMetaDataFactory attributeMetaDataFactory)
	{
		super(URI);

		this.repositoryMerger = requireNonNull(repositoryMerger);
		this.dataService = requireNonNull(dataService);
		this.attributeMetaDataFactory = requireNonNull(attributeMetaDataFactory);

		chromAttr = attributeMetaDataFactory.create().setName("#CHROM").setDataType(STRING);
		posAttr = attributeMetaDataFactory.create().setName("POS").setDataType(LONG);
		refAttr = attributeMetaDataFactory.create().setName("REF").setDataType(TEXT);
		altAttr = attributeMetaDataFactory.create().setName("ALT").setDataType(TEXT);

		commonAttributes = new ArrayList<>();
		commonAttributes.add(chromAttr);
		commonAttributes.add(posAttr);
		commonAttributes.add(refAttr);
		commonAttributes.add(altAttr);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model) throws Exception
	{
		List<String> geneticRepositories = new ArrayList<>();
		dataService.getEntityNames().forEach(name -> {
			EntityMetaData entityMetaData = dataService.getEntityMetaData(name);
			if (entityMetaData.getAttribute(chromAttr.getName()) != null
					&& entityMetaData.getAttribute(posAttr.getName()) != null
					&& entityMetaData.getAttribute(refAttr.getName()) != null
					&& entityMetaData.getAttribute(altAttr.getName()) != null)
			{
				geneticRepositories.add(name);
			}
		});

		Iterable<EntityMetaData> entitiesMeta = Iterables
				.transform(geneticRepositories, new Function<String, EntityMetaData>()
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
			List<Repository<Entity>> geneticRepositories = new ArrayList<Repository<Entity>>();
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

			EntityMetaData mergedEntityMetaData = repositoryMerger
					.mergeMetaData(geneticRepositories, commonAttributes, null, resultSet);
			Repository<Entity> mergedRepository = dataService.getMeta().addEntityMeta(mergedEntityMetaData);
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
