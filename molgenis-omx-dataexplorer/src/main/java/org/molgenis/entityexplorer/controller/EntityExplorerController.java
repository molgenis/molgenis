package org.molgenis.entityexplorer.controller;

import static org.molgenis.entityexplorer.controller.EntityExplorerController.URI;

import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.framework.db.DatabaseAccessException;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(URI)
public class EntityExplorerController extends MolgenisPluginController
{
	private static final Logger logger = Logger.getLogger(EntityExplorerController.class);

	private static final String KEY_APP_HREF_CSS = "app.href.css";

	public static final String URI = "/plugin/entityexplorer";

	@Autowired
	private DataService dataService;

	@Autowired
	private MolgenisSettings molgenisSettings;

	public EntityExplorerController()
	{
		super(URI);
	}

	// TODO
	// @SuppressWarnings("unchecked")
	// @RequestMapping(method = RequestMethod.GET)
	// public String init(@RequestParam(required = false)
	// String entity, @RequestParam(required = false)
	// String identifier, @RequestParam(required = false)
	// String query, Model model) throws Exception
	// {
	//
	// // select all characteristic entities
	// Iterable<Class<? extends Entity>> entityClazzes = Iterables.filter(database.getEntityClasses(),
	// new Predicate<Class<? extends Entity>>()
	// {
	// @Override
	// public boolean apply(@Nullable
	// Class<? extends Entity> clazz)
	// {
	// return clazz != null && Characteristic.class.isAssignableFrom(clazz)
	// && !clazz.equals(Characteristic.class);
	// }
	// });
	//
	// Map<String, Class<? extends Characteristic>> clazzMap = new LinkedHashMap<String, Class<? extends
	// Characteristic>>();
	// for (Class<? extends Entity> clazz : entityClazzes)
	// {
	// if (database.count(clazz) > 0) clazzMap.put(clazz.getSimpleName(), (Class<? extends Characteristic>) clazz);
	// }
	//
	// // select initial entity
	// Class<? extends Characteristic> selectedClazz = clazzMap.get(entity);
	// if (selectedClazz == null)
	// {
	// if (!clazzMap.isEmpty()) selectedClazz = clazzMap.entrySet().iterator().next().getValue();
	// }
	//
	// String appHrefCss = molgenisSettings.getProperty(KEY_APP_HREF_CSS);
	// if (appHrefCss != null) model.addAttribute(KEY_APP_HREF_CSS.replaceAll("\\.", "_"), appHrefCss);
	// model.addAttribute("selectedQuery", query);
	// ArrayList<String> entities = new ArrayList<String>(clazzMap.keySet());
	// Collections.sort(entities);
	// model.addAttribute("entities", entities);
	//
	// // determine instances for selected entity
	// if (selectedClazz != null)
	// {
	// List<? extends Characteristic> characteristics = database.find(selectedClazz);
	// Collections.sort(characteristics, new Comparator<Characteristic>()
	// {
	// @Override
	// public int compare(Characteristic o1, Characteristic o2)
	// {
	// return o1.getName().compareTo(o2.getName());
	// }
	//
	// });
	//
	// Characteristic selectedCharacteristic = null;
	// if (identifier != null)
	// {
	// List<? extends Characteristic> selectedCharacteristics = database.find(selectedClazz, new QueryRule(
	// Characteristic.IDENTIFIER, Operator.EQUALS, identifier));
	// if (selectedCharacteristics != null && !selectedCharacteristics.isEmpty())
	// {
	// selectedCharacteristic = selectedCharacteristics.get(0);
	// }
	// else
	// {
	// logger.warn(selectedClazz.getSimpleName() + " with identifier " + identifier + " does not exist");
	// }
	// }
	// else
	// {
	// if (characteristics != null && !characteristics.isEmpty()) selectedCharacteristic = characteristics
	// .get(0);
	// }
	//
	// model.addAttribute("selectedEntity", selectedClazz.getSimpleName());
	// model.addAttribute("entityInstances", characteristics);
	// model.addAttribute("selectedEntityInstance", selectedCharacteristic);
	// }
	//
	// return "view-entityexplorer";
	// }

	@ExceptionHandler(DatabaseAccessException.class)
	public String handleNotAuthenticated()
	{
		return "redirect:/";
	}
}
