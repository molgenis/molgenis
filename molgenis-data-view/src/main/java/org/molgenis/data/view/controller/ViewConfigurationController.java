package org.molgenis.data.view.controller;

import static com.google.common.collect.Lists.newArrayList;
import static org.molgenis.data.view.controller.ViewConfigurationController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;
import java.util.stream.Collectors;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.view.meta.EntityViewMetaData;
import org.molgenis.data.view.response.EntityViewCollectionResponse;
import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(URI)
public class ViewConfigurationController extends MolgenisPluginController
{
	@Autowired
	DataService dataService;

	@Autowired
	IdGenerator idGenerator;

	public static final String NAME = "configureview";
	public static final String URI = PLUGIN_URI_PREFIX + NAME;

	public ViewConfigurationController()
	{
		super(URI);
	}

	/**
	 * Init view
	 * 
	 * @param masterEntityName
	 * @return
	 */
	@RequestMapping
	public String init(Model model)
	{
		return "view-configure-view";
	}

	@RequestMapping(value = "/add-entity-view", method = POST)
	@ResponseBody
	public String addEntityView(@RequestParam(value = "viewName") String viewName,
			@RequestParam(value = "masterEntityName") String masterEntityName)
	{
		Entity newViewEntity = new MapEntity(new EntityViewMetaData());
		newViewEntity.set(EntityViewMetaData.VIEW_NAME, viewName);
		newViewEntity.set(EntityViewMetaData.MASTER_ENTITY, masterEntityName);
		dataService.add(EntityViewMetaData.ENTITY_NAME, newViewEntity);

		// Can I get the ID of this entity?
		String id = newViewEntity.getIdValue().toString();

		return id;
	}

	/**
	 * parses the {@link EntityView} table into a {@link List} of {@link EntityViewCollectionResponse} with View Names,
	 * Master Entity names, and a list of Joined Entity names
	 * 
	 * @param masterEntityName
	 * @return a {@link List} of {@link EntityViewCollectionResponse}
	 */
	@RequestMapping(value = "/get-entity-views", method = GET)
	@ResponseBody
	public List<EntityViewCollectionResponse> getEntityViews()
	{
		List<EntityViewCollectionResponse> entityViewCollectionResponses = newArrayList();
		List<String> joinedEntities = newArrayList();
		List<Entity> entityViewEntities = dataService.findAll(EntityViewMetaData.ENTITY_NAME)
				.collect(Collectors.toList());

		String currentViewName = null;
		for (Entity entity : entityViewEntities)
		{
			String viewName = entity.getString(EntityViewMetaData.VIEW_NAME);
			String masterEntityName = entity.getString(EntityViewMetaData.MASTER_ENTITY);

			if (currentViewName == null)
			{
				currentViewName = viewName;
			}

			if (viewName.equals(currentViewName))
			{
				joinedEntities.add(entity.getString(EntityViewMetaData.JOIN_ENTITY));
			}
			else
			{
				entityViewCollectionResponses.add(new EntityViewCollectionResponse(idGenerator.generateId(), viewName,
						masterEntityName, joinedEntities));
				joinedEntities = newArrayList();
				currentViewName = viewName;
			}
		}

		return entityViewCollectionResponses;
	}

	@RequestMapping(value = "/delete-entity-view", method = POST)
	@ResponseBody
	public void deleteEntityView(@RequestParam(value = "viewName") String viewName)
	{
		dataService.delete(EntityViewMetaData.ENTITY_NAME, dataService.findAll(EntityViewMetaData.ENTITY_NAME,
				new QueryImpl().eq(EntityViewMetaData.VIEW_NAME, viewName)));
	}
}
