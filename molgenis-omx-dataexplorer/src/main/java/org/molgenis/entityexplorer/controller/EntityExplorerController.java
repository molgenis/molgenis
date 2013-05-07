package org.molgenis.entityexplorer.controller;

import javax.annotation.Nullable;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseAccessException;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.util.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Controller
@RequestMapping("/plugin/entityexplorer")
public class EntityExplorerController
{
	@Autowired
	private Database database;

	@Autowired
	private MolgenisSettings molgenisSettings;

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView init() throws Exception
	{
		ModelAndView model = new ModelAndView("entityexplorer");
		model.addObject("entities", Lists.newArrayList(Iterables.filter(
				Iterables.transform(database.getEntityClasses(), new Function<Class<? extends Entity>, String>()
				{
					@Override
					@Nullable
					public String apply(@Nullable Class<? extends Entity> clazz)
					{
						return clazz != null && Characteristic.class.isAssignableFrom(clazz)
								&& !clazz.equals(Characteristic.class) ? clazz.getSimpleName() : null;
					}
				}), Predicates.notNull())));
		return model;
	}

	@ExceptionHandler(DatabaseAccessException.class)
	public String handleNotAuthenticated()
	{
		return "redirect:/";
	}
}
