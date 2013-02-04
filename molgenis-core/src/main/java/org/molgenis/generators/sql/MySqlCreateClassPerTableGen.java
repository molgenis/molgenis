package org.molgenis.generators.sql;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisOptions;
import org.molgenis.generators.Generator;
import org.molgenis.model.MolgenisModel;
import org.molgenis.model.elements.Entity;
import org.molgenis.model.elements.Model;

import freemarker.template.Template;

public class MySqlCreateClassPerTableGen extends Generator
{
	private static final Logger logger = Logger.getLogger(MySqlCreateClassPerTableGen.class);

	@Override
	public String getDescription()
	{
		return "Generates create tables and views for each entity.";
	}

	@Override
	public void generate(Model model, MolgenisOptions options) throws Exception
	{
		Template template = createTemplate("/" + this.getClass().getSimpleName() + ".mysql.ftl");
		Map<String, Object> templateArgs = createTemplateArguments(options);

		List<Entity> entityList = model.getEntities();
		entityList = MolgenisModel.sortEntitiesByDependency(entityList, model); // side
																				// effect?

		File target = new File(this.getSqlPath(options) + "/create_tables.mysql");
		boolean created = target.getParentFile().mkdirs();
		if (!created && !target.getParentFile().exists())
		{
			throw new IOException("could not create " + target.getParentFile());
		}

		templateArgs.put("model", model);
		templateArgs.put("entities", entityList);
		OutputStream targetOut = new FileOutputStream(target);
		template.process(templateArgs, new OutputStreamWriter(targetOut, Charset.forName("UTF-8")));
		targetOut.close();

		logger.info("generated " + target);
	}

	// public void sortEntitiesByXref(List<Entity> entityList, Model model)
	// throws Exception
	// {
	// if(true)
	// return;
	// //buble sort
	// int count = 0;
	// int maxcount = entityList.size() * entityList.size();
	// for(int i = 0; i < entityList.size() - 1; i++)
	// {
	// if (count == maxcount)
	// throw new
	// Exception("you have a cyclic relationship in you database scheme. check the 'swapped .. with ..' messages for clues.");
	// Entity currentEntity = entityList.get(i);
	//
	// Vector<String> dependencies = new Vector<String>();
	// // if (currentEntity.hasAncestor())
	// // dependencies.add(currentEntity.getAncestor().getName()); //ancestor
	//
	// // if (currentEntity.hasImplements())
	// // {
	// // for (Entity e : currentEntity.getImplements())
	// // dependencies.add(e.getName()); //imlements
	// // }
	// for (Field field : currentEntity.getFields())
	// {
	// if (field.getType().toString() == "xref" || field.getType().toString() ==
	// "mref")
	// {
	// // Field xref_field =
	// model.getEntity(field.getXRefEntity()).getField(field.getXRefField());
	// // if( !field.getName().equals(xref_field.getName()) &&
	// !(field.getEntity().isAncestor(xref_field.getXRefEntity()) ||
	// field.getEntity().getImplements().contains(xref_field.getXRefEntity())))
	// dependencies.add(field.getXRefEntity()); //xref fields
	// }
	// }
	//
	// for(String entity: dependencies)
	// {
	// int xrefPosition = this.indexOf(entityList,entity);
	// if(xrefPosition > i)
	// //need to swap when index of referenced table is larger than own
	// //this is endless in case of cyclic relationship!
	// {
	// //swap
	// Entity xrefEntity = entityList.get(xrefPosition);
	// entityList.set(i,xrefEntity);
	// entityList.set(xrefPosition,currentEntity);
	// logger.debug("swapped " + entityList.get(xrefPosition).getName() +
	// " with " + entityList.get(i).getName());
	// i--; //check swapped entity
	// break;
	// }
	// }
	// count++;
	// }
	// }
	//
	// private int indexOf(List<Entity> entityList, String entityName)
	// {
	// for(int i = 0; i< entityList.size(); i++)
	// {
	// if(entityList.get(i).getName().equals(entityName))
	// return i;
	// }
	// return 0;
	// }
}
