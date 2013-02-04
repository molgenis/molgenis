package org.molgenis.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisOptions;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.model.elements.Entity;
import org.molgenis.model.elements.Field;
import org.molgenis.model.elements.Model;
import org.molgenis.model.elements.Module;

public class MolgenisModel
{
	private static final Logger logger = Logger.getLogger(MolgenisModel.class.getSimpleName());

	public static Model parse(MolgenisOptions options) throws Exception
	{
		Model model = null;

		try
		{
			logger.info("parsing db-schema from " + options.model_database);

			model = MolgenisModelParser.parseDbSchema(options.model_database);

			Model importedModel = MolgenisModelParser.parseDbSchema(options.import_model_database, "imported");
			MolgenisModelValidator.validate(importedModel, options); // Create
																		// mref
																		// links

			for (Module importedModule : importedModel.getModules())
			{
				model.getModules().add(importedModule);
			}

			for (Entity importedEntity : importedModel.getEntities())
			{
				importedEntity.setImported(true);
				importedEntity.setModel(model);

				// Prevent duplicate elements (elements of parent are also in
				// the tree)
				model.getDatabase().getTreeElements().remove(importedEntity.getName());

				importedEntity.setParent(model.getDatabase());
			}

			// Get the strings of the property 'authorizable' and add the entity
			// name
			// 'Authorizable' to the list of Implements. Solves datamodel
			// duplication
			// in molgenis_apps suite. Possible future work: put auth
			// dependencies into
			// molgenis itself so it becomes generic across projects.
			for (String eName : options.authorizable)
			{
				eName = eName.trim(); // allow e.g. 'Observation, Investigation'
				Vector<String> implNames = model.getEntity(eName).getImplementsNames();
				if (!implNames.contains("Authorizable"))
				{
					implNames.add("Authorizable");
					model.getEntity(eName).setImplements(implNames);
				}
			}

			logger.info("read model");
			if (logger.isTraceEnabled()) logger.trace(model);

			// if (!options.exclude_system) Model.createSystemTables(model);
			MolgenisModelValidator.validate(model, options);

			logger.info("parsing ui-schema");
			model = MolgenisModelParser.parseUiSchema(options.path + options.model_userinterface, model);
			// if (options.force_molgenis_package == true)
			// model.setName("molgenis");

			MolgenisModelValidator.validateUI(model, options);

			logger.info("validated model");
			if (logger.isTraceEnabled()) logger.trace(model);
		}
		catch (MolgenisModelException e)
		{
			logger.error("Parsing failed: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
		return model;
	}

	public static Model parse(Properties p) throws Exception
	{
		MolgenisOptions options = new MolgenisOptions(p);
		return parse(options);
	}

	public static List<Entity> sortEntitiesByDependency(List<Entity> entityList, final Model model)
			throws MolgenisModelException
	{
		List<Entity> result = new ArrayList<Entity>();

		boolean found = true;
		List<Entity> toBeMoved = new ArrayList<Entity>();
		while (entityList.size() > 0 && found)
		{
			found = false;
			for (Entity entity : entityList)
			{
				List<String> deps = getDependencies(entity, model);

				// check if all deps are there
				boolean missing = false;
				for (String dep : deps)
				{
					if (indexOf(result, dep) < 0)
					{
						missing = true;
						break;
					}
				}

				if (!missing)
				{
					toBeMoved.add(entity);
					result.add(entity);
					found = true;
					break;
				}
			}

			for (Entity e : toBeMoved)
				entityList.remove(e);
			toBeMoved.clear();
		}

		// list not empty, cyclic?
		for (Entity e : entityList)
		{
			logger.error("cyclic relations to '" + e.getName() + "' depends on " + getDependencies(e, model));
			result.add(e);
		}

		// result
		for (Entity e : result)
		{
			logger.info(e.getName());
		}

		return result;
	}

	private static int indexOf(List<Entity> entityList, String entityName)
	{
		for (int i = 0; i < entityList.size(); i++)
		{
			if (entityList.get(i).getName().equals(entityName)) return i;
		}
		return -1;
	}

	private static List<String> getDependencies(Entity currentEntity, Model model) throws MolgenisModelException
	{
		Set<String> dependencies = new HashSet<String>();

		for (Field field : currentEntity.getAllFields())
		{
			if (field.getType() instanceof XrefField)
			{
				dependencies.add(model.getEntity(field.getXrefEntityName()).getName());

				Entity xrefEntity = field.getXrefEntity();

				// also all subclasses have this xref!!!!
				for (Entity e : xrefEntity.getAllDescendants())
				{
					if (!dependencies.contains(e.getName())) dependencies.add(e.getName());
				}
			}
			if (field.getType() instanceof MrefField)
			{
				dependencies.add(field.getXrefEntity().getName()); // mref
				// fields
				// including super classes
				for (String name : model.getEntity(field.getXrefEntity().getName()).getParents())
				{
					dependencies.add(name);
				}
			}
		}

		dependencies.remove(currentEntity.getName());
		return new ArrayList<String>(dependencies);
	}
}
