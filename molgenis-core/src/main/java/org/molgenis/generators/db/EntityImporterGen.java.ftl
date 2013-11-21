<#--helper functions-->
<#include "GeneratorHelper.ftl">

<#--#####################################################################-->
<#--                                                                   ##-->
<#--         START OF THE OUTPUT                                       ##-->
<#--                                                                   ##-->
<#--#####################################################################-->
/* File:        ${model.getName()}/model/${entity.getName()}.java
 * Copyright:   GBIC 2000-${year?c}, all rights reserved
 * Date:        ${date}
 * 
 * generator:   ${generator} ${version}
 *
 * 
 * THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
 */

package ${package};

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.db.Database.DatabaseAction;
import org.molgenis.framework.db.EntityImporter;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import ${entity.namespace}.${JavaName(entity)};

/**
 * Reads ${JavaName(entity)} from a delimited (csv) file, resolving xrefs to ids where needed, that is the tricky bit ;-)
 */
public class ${JavaName(entity)}EntityImporter implements EntityImporter<${JavaName(entity)}>
{
	private static final Logger logger = Logger.getLogger(${JavaName(entity)}EntityImporter.class);
	private static int BATCH_SIZE = 10000;
		
			
	/**
	 * Imports ${JavaName(entity)} from tab/comma delimited File
	 * @param db database to import into
	 * @param reader csv reader to load data from
	 * @param defaults to set default values for each row
	 * @param dbAction indicating wether to add,update,remove etc
	 * @param missingValues indicating what value in the csv is treated as 'null' (e.g. "" or "NA")
	 * @return number of elements imported
	 */
	@Override
	public int importEntity(Repository<? extends Entity> repository, DataService dataService, DatabaseAction dbAction)
	{
		//wrapper to count
		final AtomicInteger total = new AtomicInteger(0);
	try {
		
			// cache for entities of which xrefs couldn't be resolved (e.g. if there is a self-refence)
			// these entities can be updated with their xrefs in a second round when all entities are in the database
			List<${JavaName(entity)}> ${name(entity)}sMissingRefs = new ArrayList<${JavaName(entity)}>();
			List<Entity> entityMissingRefs = new ArrayList<Entity>();

			// cache for objects to be imported from file (in batch)
			List<${JavaName(entity)}> ${name(entity)}List = new ArrayList<${JavaName(entity)}>(BATCH_SIZE); // FIXME
			List<Entity> entityList = new ArrayList<Entity>(BATCH_SIZE);

			CrudRepository<${JavaName(entity)}> crudRepository = dataService.getCrudRepository("${entity.name}");
		
			for (Entity entity : repository)
			{
				// skip empty rows
				if (!hasValues(entity)) continue;

				// parse object, setting defaults and values from file
				${JavaName(entity)} object = new ${JavaName(entity)}();
				object.set(entity);
				${name(entity)}List.add(object);
				entityList.add(entity);

				if (!resolveForeignKeys(dataService, entity, object, crudRepository))
				{
					${name(entity)}sMissingRefs.add(object);
					entityMissingRefs.add(entity);
					${name(entity)}List.remove(object);
					entityList.remove(entity);
				}

				// add to db when batch size is reached
				if (${name(entity)}List.size() == BATCH_SIZE)
				{
					<#if entity.getXrefLabels()?exists>
					//update objects in the database using xref_label defined secondary key(s) '${csv(entity.getXrefLabels())}' defined in xref_label
					crudRepository.update(${name(entity)}List,dbAction<#list entity.getXrefLabels() as label>, "${label}"</#list>);
					<#else>
					//update objects in the database using primary key(<#list entity.getAllKeys()[0].fields as field><#if field_index != 0>,</#if>${field.name}</#list>)
					crudRepository.update(${name(entity)}List,dbAction<#list entity.getAllKeys()[0].fields as field>, "${field.name}"</#list>);
					</#if>

					// clear for next batch
					${name(entity)}List.clear();
					entityList.clear();

					// keep count
					total.set(total.get() + BATCH_SIZE);

					crudRepository.flush();
					crudRepository.clearCache();
				}
			}

			// add remaining elements to the database
			if (!${name(entity)}List.isEmpty())
			{
				total.set(total.get() + ${name(entity)}List.size());

				// resolve foreign keys, again keeping track of those entities that could not be solved
				for (int i = 0; i < ${name(entity)}List.size(); i++)
				{
					Entity entity = entityList.get(i);
					${JavaName(entity)} object = ${name(entity)}List.get(i);

					if (!resolveForeignKeys(dataService, entity, object, crudRepository))
					{
						${name(entity)}sMissingRefs.add(object);
						entityMissingRefs.add(entity);
						${name(entity)}List.remove(object);
						entityList.remove(entity);
					}
				}

				<#if entity.getXrefLabels()?exists>
				//update objects in the database using xref_label defined secondary key(s) '${csv(entity.getXrefLabels())}' defined in xref_label
				crudRepository.update(${name(entity)}List,dbAction<#list entity.getXrefLabels() as label>, "${label}"</#list>);
				<#else>
				//update objects in the database using primary key(<#list entity.getAllKeys()[0].fields as field><#if field_index != 0>,</#if>${field.name}</#list>)
				crudRepository.update(${name(entity)}List,dbAction<#list entity.getAllKeys()[0].fields as field>, "${field.name}"</#list>);
				</#if>
				${name(entity)}List.clear();
				entityList.clear();
			}

			// Try to resolve FK's for entities until all are resolved or we have more then 100 iterations
			if (!${name(entity)}sMissingRefs.isEmpty())
			{
				int iterationCount = 0;

				do
				{
					int index = new java.util.Random().nextInt(${name(entity)}sMissingRefs.size());
					Entity entity = entityMissingRefs.get(index);
					${JavaName(entity)} object = ${name(entity)}sMissingRefs.get(index);

					if (resolveForeignKeys(dataService, entity, object, crudRepository))
					{
						${name(entity)}List.add(object);
						entityMissingRefs.remove(entity);
						${name(entity)}sMissingRefs.remove(object);
					}

					if (!${name(entity)}List.isEmpty())
					{
						<#if entity.getXrefLabels()?exists>
						//update objects in the database using xref_label defined secondary key(s) '${csv(entity.getXrefLabels())}' defined in xref_label
						crudRepository.update(${name(entity)}List,dbAction<#list entity.getXrefLabels() as label>, "${label}"</#list>);
						<#else>
						//update objects in the database using primary key(<#list entity.getAllKeys()[0].fields as field><#if field_index != 0>,</#if>${field.name}</#list>)
						crudRepository.update(${name(entity)}List,dbAction<#list entity.getAllKeys()[0].fields as field>, "${field.name}"</#list>);
						</#if>
						${name(entity)}List.clear();
					}

					if (iterationCount++ > 1000)
					{
						String identifier = "";
						String name = "";
						for (${JavaName(entity)} blaat : ${name(entity)}sMissingRefs)
						{
							identifier = blaat.getString("Identifier");
							name = blaat.getString("Name");
						}
						throw new Exception(
								"Import of '${name(entity)}' entity failed:"
										+ "This is probably caused by a(n) '${name(entity)}' that has a reference but that does not exist."
										+ "(identifier:" + identifier + ", name:" + name + ")");
					}
				}
				while (!${name(entity)}sMissingRefs.isEmpty());
			}

			logger.info("imported " + total.get() + " ${name(entity)} from CSV");
		
		} 
		catch(Exception e) 
		{
			logger.error("Error importing repository [" + repository.getName() + "]", e);
			throw new MolgenisDataException(e);
		}
		
		return total.get();
	}	
	
	private boolean hasValues(Entity entity)
	{
		for (String attributeName : entity.getAttributeNames())
		{
			if (entity.get(attributeName) != null) return true;
		}
		return false;
	}
	
	/**
	 * This method tries to resolve foreign keys (i.e. xref_field) based on the secondary key/key (i.e. xref_labels).
	 */
	private boolean resolveForeignKeys(DataService dataService, Entity entity, ${JavaName(entity)} object,
			EntityMetaData metaData)
	{
		for (AttributeMetaData attr : metaData.getAttributes())
		{
			if (attr.getRefEntity() != null)
			{
				for (AttributeMetaData attrXref : attr.getRefEntity().getAttributes())
				{
					if (attr.getDataType().getEnumType() == MolgenisFieldTypes.FieldTypeEnum.XREF)
					{
						Object value = entity.get(attr.getName() + "_" + attrXref.getName());
						if (value == null)
						{
							value = entity.get(attr.getName().toLowerCase() + "_"
									+ attrXref.getName().toLowerCase());
						}

						if ((value != null) && (object.get(attr.getName()) == null))
						{
							Object xref = dataService.findOne(attr.getRefEntity().getName(),
									new QueryImpl().eq(attrXref.getName(), value));

							if (xref == null)
							{
								return false;
							}

							object.set(attr.getName(), xref);
						}
					}
					else if (attr.getDataType().getEnumType() == MolgenisFieldTypes.FieldTypeEnum.MREF)
					{
						List<String> value = entity.getList(attr.getName() + "_" + attrXref.getName());
						if (value == null || value.isEmpty())
						{
							value = entity.getList(attr.getName().toLowerCase() + "_"
									+ attrXref.getName().toLowerCase());
						}
						
						@SuppressWarnings("unchecked")
						List<Entity> xrefObjects = (List<Entity>) object.get(attr.getName());

						if (value != null && !value.isEmpty()
								&& ((xrefObjects == null) || (xrefObjects.size() < value.size())))
						{
							List<Entity> mref = dataService.findAllAsList(attr.getRefEntity().getName(),
									new QueryImpl().in(attrXref.getName(), value));

							object.set(attr.getName(), mref);

							if (mref.size() < value.size())
							{
								return false;
							}
						}
					}

				}
			}
		}

		return true;
	}
}

