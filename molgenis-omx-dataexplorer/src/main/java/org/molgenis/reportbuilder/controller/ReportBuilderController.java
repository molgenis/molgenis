package org.molgenis.reportbuilder.controller;

import static org.molgenis.reportbuilder.controller.ReportBuilderController.URI;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Queryable;
import org.molgenis.framework.ui.MolgenisPluginController;
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

/**
 * Controller for the report builder
 * 
 * @author mdehaan
 * 
 */
@Controller
@RequestMapping(URI)
public class ReportBuilderController extends MolgenisPluginController
{

	private static final Logger logger = Logger.getLogger(ReportBuilderController.class);

	@Autowired
	private DataService dataService;

	public static final String ID = "reportbuilder";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	public ReportBuilderController()
	{
		super(URI);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(@RequestParam(value = "entityName") String entityName,
			@RequestParam(value = "entityId") String entityId,
			@RequestParam(value = "parameters", required = false) String parameterString,
			@RequestParam(value = "view") String view, Model model) throws Exception
	{

		if (dataService.hasRepository(entityName))
		{
			Queryable queryableRepository = dataService.getQueryableRepository(entityName);
			Entity entity = queryableRepository.findOne(entityId);

			if (entity != null)
			{
				model.addAttribute("entityName", entityName);
				model.addAttribute("entityId", entityId);
				model.addAttribute("entityMap", getMapFromEntity(entity));

				if (parameterString != null)
				{
					Map<String, String> parameterMap = getMapFromRequestMappingUrl(parameterString);
					model.addAttribute("parameterMap", parameterMap);
				}
				else
				{
					model.addAttribute("parameterMap", null);
				}
			}
			else
			{
				throw new RuntimeException(entityName + " does not contain a row with id: " + entityId);
			}
		}
		else
		{
			throw new RuntimeException("unknown entity: " + entityName);
		}
		return "view-" + view;
	}

	/**
	 * Translates a single entity its attributes and respective values to a map
	 * 
	 * @param entity
	 * @return A map with entity attribute as key and respective value as value
	 */
	private Map<String, String> getMapFromEntity(Entity entity)
	{
		Map<String, String> entityValueMap = new HashMap<String, String>();
		Iterator<String> entityAttributes = entity.getAttributeNames().iterator();

		if (entityAttributes != null)
		{
			while (entityAttributes.hasNext())
			{
				String entityAttribute = entityAttributes.next();
				entityValueMap.put(entityAttribute, entity.get(entityAttribute).toString());
			}
		}
		else
		{
			throw new RuntimeException("the selected row did not have any attributes");
		}

		return entityValueMap;
	}

	private Map<String, String> getMapFromRequestMappingUrl(String parameterString)
	{
		Map<String, String> parameterMap = new HashMap<String, String>();

		try
		{
			// expected key value pairs to be seperated by ','
			String[] parameters = parameterString.split(",");
			if (parameters.length > 0)
			{
				for (int i = 0; i < parameters.length; i++)
				{
					// expected key and value to be seperated by ':'
					parameterMap.put(parameters[i].split(":")[0], parameters[i].split(":")[1]);
				}
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(
					"the parameterMap key value pairs need to be in the following format: key:value,key:value");
		}

		return parameterMap;
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Map<String, String> handleRuntimeException(RuntimeException e)
	{
		logger.error(null, e);
		return Collections.singletonMap("errorMessage",
				"An error occured. Please contact the administrator.<br />Message:" + e.getMessage());
	}
}
