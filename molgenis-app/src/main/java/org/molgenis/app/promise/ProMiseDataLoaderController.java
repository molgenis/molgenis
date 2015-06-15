package org.molgenis.app.promise;

import static org.molgenis.app.promise.ProMiseDataLoaderController.URI;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(URI)
public class ProMiseDataLoaderController extends MolgenisPluginController
{
	public static final String ID = "promiseloader";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final ProMiseDataParser promiseDataParser;
	private final DataService dataService;

	@Autowired
	public ProMiseDataLoaderController(ProMiseDataParser proMiseDataParser, DataService dataService)
	{
		super(URI);
		this.promiseDataParser = proMiseDataParser;
		this.dataService = dataService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init()
	{
		return "view-promiseloader";
	}

	@RequestMapping(value = "load", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void load() throws IOException
	{
		Iterable<Entity> entities = promiseDataParser.parse();

		String promiseEntityName = "promise";
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData(promiseEntityName);
		entityMetaData.setLabel("ProMISe");
		entityMetaData.addAttribute("_id").setIdAttribute(true).setAuto(true).setVisible(false).setNillable(false);

		Set<String> attrNames = new HashSet<String>();
		for (Entity entity : entities)
		{
			for (String attrName : entity.getAttributeNames())
			{
				if (!attrNames.contains(attrName))
				{
					entityMetaData.addAttribute(attrName).setNillable(true);
					attrNames.add(attrName);
				}
			}
		}

		if (dataService.getMeta().getEntityMetaData(promiseEntityName) != null)
		{
			dataService.getMeta().deleteEntityMeta(promiseEntityName);
		}
		dataService.getMeta().addEntityMeta(entityMetaData);
		dataService.add(promiseEntityName, entities);
	}
}
