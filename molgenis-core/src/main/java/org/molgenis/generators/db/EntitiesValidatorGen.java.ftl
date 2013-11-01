<#--helper functions-->
<#include "GeneratorHelper.ftl">

<#--#####################################################################-->
<#--                                                                   ##-->
<#--         START OF THE OUTPUT                                       ##-->
<#--                                                                   ##-->
<#--#####################################################################-->
/* Date:        ${date}
 * 
 * generator:   ${generator} ${version}
 *
 * 
 * THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
 */

package ${package};

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntitySource;
import org.molgenis.data.Repository;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntitiesValidator;
import org.molgenis.model.MolgenisModelException;
import org.molgenis.model.elements.Field;
import org.molgenis.data.Entity;

<#list model.entities as entity>
<#if !entity.abstract && !entity.system>
import ${entity.namespace}.${JavaName(entity)};
</#if>
</#list>

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EntitiesValidatorImpl implements EntitiesValidator 
{
	/** importable entity names (lowercase) */
	private static final Map<String, Class<? extends Entity>> ENTITIES_IMPORTABLE;

	static
	{
		// entities added in import order
		ENTITIES_IMPORTABLE = new LinkedHashMap<String, Class<? extends Entity>>();
	<#list entities as entity>
		<#if !entity.abstract && !entity.system>
		ENTITIES_IMPORTABLE.put("${entity.name?lower_case}", ${JavaName(entity)}.class);
		</#if>
	</#list>
	}
	
	private final Database database;
	private final DataService dataService;

	@Autowired
	public EntitiesValidatorImpl(Database database, DataService dataService)
	{
		if (database == null) throw new IllegalArgumentException("database is null");
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		this.database = database;
		this.dataService = dataService;
	}
	
	@Override
	public EntitiesValidationReport validate(File file) throws IOException
	{
		EntitiesValidationReport validationReport = new EntitiesValidationReportImpl();

		EntitySource entitySource = dataService.createEntitySource(file);
		try
		{
			for (String entityName : entitySource.getEntityNames())
			{
				Repository<? extends org.molgenis.data.Entity> repository = entitySource
						.getRepositoryByEntityName(entityName);
				try
				{
					boolean isImportableEntity = ENTITIES_IMPORTABLE.containsKey(entityName.toLowerCase());
					if (isImportableEntity)
					{
						Class<? extends Entity> entityClazz = ENTITIES_IMPORTABLE.get(entityName.toLowerCase());
						validateTable(entityName, repository, entityClazz, validationReport);
					}
					validationReport.getSheetsImportable().put(entityName, isImportableEntity);
				}
				finally
				{
					repository.close();
				}
			}
		}
		catch (MolgenisModelException e)
		{
			throw new IOException(e);
		}
		catch (DatabaseException e)
		{
			throw new IOException(e);
		}
		finally
		{
			entitySource.close();
		}

		return validationReport;
	}

	private void validateTable(String entityName, Repository<? extends org.molgenis.data.Entity> repository,
			Class<? extends Entity> entityClazz, EntitiesValidationReport validationReport)
			throws MolgenisModelException, DatabaseException, IOException
	{
		List<Field> entityFields = database.getMetaData().getEntity(entityClazz.getSimpleName()).getAllFields();

		// construct a list of all required and optional fields
		Map<String, Field> requiredFields = new LinkedHashMap<String, Field>();
		Map<String, Field> availableFields = new LinkedHashMap<String, Field>();

		for (Field field : entityFields)
		{
			if (!field.isSystem() && !field.isAuto())
			{
				List<String> xrefNames = getXrefNames(field);
				String fieldName = field.getName().toLowerCase();

				// determine if this field is required or optional
				Map<String, Field> fieldMap;
				if (!field.isNillable())
				{
					if (field.getDefaultValue() == null) fieldMap = requiredFields;
					else fieldMap = availableFields;
				}
				else fieldMap = availableFields;

				// add name and xref names
				fieldMap.put(fieldName, field);
				for (String xrefName : xrefNames)
					fieldMap.put(fieldName + '_' + xrefName.toLowerCase(), field);
			}
		}

		// keep track of to-be-removed required and optional fields
		List<Field> removeRequiredFields = new ArrayList<Field>();
		List<Field> removeAvailableFields = new ArrayList<Field>();

		// collect
		List<String> detectedFieldNames = new ArrayList<String>();
		List<String> unknownFieldNames = new ArrayList<String>();
		for (AttributeMetaData attr : repository.getAttributes())
		{
			String attrName = attr.getName();
			if (attrName == null || attrName.isEmpty()) continue;

			String fieldName = attrName.toLowerCase();
			if (requiredFields.containsKey(fieldName))
			{
				detectedFieldNames.add(fieldName);
				// remove all references to field
				Field removedField = requiredFields.remove(fieldName);
				removeRequiredFields.add(removedField);
			}
			else if (availableFields.containsKey(fieldName))
			{
				detectedFieldNames.add(fieldName);
				// remove all references to field
				Field removedField = availableFields.remove(fieldName);
				removeAvailableFields.add(removedField);
			}
			else
			{
				unknownFieldNames.add(fieldName);
			}
		}

		for (Field field : removeRequiredFields)
		{
			for (Iterator<Entry<String, Field>> it = requiredFields.entrySet().iterator(); it.hasNext();)
			{
				Field other = it.next().getValue();
				if (field.equals(other)) it.remove();
			}
		}
		for (Field field : removeAvailableFields)
		{
			for (Iterator<Entry<String, Field>> it = availableFields.entrySet().iterator(); it.hasNext();)
			{
				Field other = it.next().getValue();
				if (field.equals(other)) it.remove();
			}
		}

		validationReport.getImportOrder().add(entityName);
		validationReport.getFieldsImportable().put(entityName, detectedFieldNames);
		validationReport.getFieldsUnknown().put(entityName, unknownFieldNames);
		validationReport.getFieldsRequired().put(entityName, requiredFields.keySet());
		validationReport.getFieldsAvailable().put(entityName, availableFields.keySet());
	}
	private List<String> getXrefNames(Field field) throws MolgenisModelException, DatabaseException
	{
		if (!field.isXRef()) return Collections.emptyList();

		List<Field> xrefFields = field.getXrefLabels();
		List<String> fieldNames = new ArrayList<String>(xrefFields.size());
		for (Field xrefField : xrefFields)
			fieldNames.add(xrefField.getName());

		return fieldNames;
	}

	private static class EntitiesValidationReportImpl implements EntitiesValidationReport
	{
		/**
		 * map of all sheets, and whether they are importable (recognized) or
		 * not
		 */
		private final Map<String, Boolean> sheetsImportable;
		/** map of importable sheets and their importable fields */
		private final Map<String, Collection<String>> fieldsImportable;
		/** map of importable sheets and their unknown fields */
		private final Map<String, Collection<String>> fieldsUnknown;
		/** map of importable sheets and their required/missing fields */
		private final Map<String, Collection<String>> fieldsRequired;
		/** map of importable sheets and their available/optional fields */
		private final Map<String, Collection<String>> fieldsAvailable;
		/** import order of the sheets */
		private final List<String> importOrder;

		public EntitiesValidationReportImpl()
		{
			this.sheetsImportable = new LinkedHashMap<String, Boolean>();
			this.fieldsImportable = new LinkedHashMap<String, Collection<String>>();
			this.fieldsUnknown = new LinkedHashMap<String, Collection<String>>();
			this.fieldsRequired = new LinkedHashMap<String, Collection<String>>();
			this.fieldsAvailable = new LinkedHashMap<String, Collection<String>>();
			importOrder = new ArrayList<String>();
		}

		@Override
		public Map<String, Boolean> getSheetsImportable()
		{
			return sheetsImportable;
		}

		@Override
		public Map<String, Collection<String>> getFieldsImportable()
		{
			return fieldsImportable;
		}

		@Override
		public Map<String, Collection<String>> getFieldsUnknown()
		{
			return fieldsUnknown;
		}

		@Override
		public Map<String, Collection<String>> getFieldsRequired()
		{
			return fieldsRequired;
		}

		@Override
		public Map<String, Collection<String>> getFieldsAvailable()
		{
			return fieldsAvailable;
		}

		@Override
		public List<String> getImportOrder()
		{
			return importOrder;
		}
	}
}