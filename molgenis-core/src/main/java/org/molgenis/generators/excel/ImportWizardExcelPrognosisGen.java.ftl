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
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.io.excel.ExcelReader;
import org.molgenis.io.excel.ExcelSheetReader;
import org.molgenis.model.MolgenisModelException;
import org.molgenis.model.elements.Field;
<#list model.entities as entity><#if !entity.abstract>
import ${entity.namespace}.${JavaName(entity)};
</#if></#list>

import com.google.common.collect.Lists;

public class ImportWizardExcelPrognosis {

	// map of all sheets, and whether they are importable (recognized) or not
	private Map<String, Boolean> sheetsImportable = new LinkedHashMap<String, Boolean>();

	// map of importable sheets and their importable fields
	private Map<String, Collection<String>> fieldsImportable = new LinkedHashMap<String, Collection<String>>();

	// map of importable sheets and their unknown fields
	private Map<String, Collection<String>> fieldsUnknown = new LinkedHashMap<String, Collection<String>>();

	// map of importable sheets and their required/missing fields
	private Map<String, Collection<String>> fieldsRequired = new LinkedHashMap<String, Collection<String>>();
	
	// map of importable sheets and their available/optional fields
	private Map<String, Collection<String>> fieldsAvailable = new LinkedHashMap<String, Collection<String>>();
	
	// import order of the sheets
	private List<String> importOrder = new ArrayList<String>();

	public ImportWizardExcelPrognosis(Database db, File excelFile) throws Exception {
		ExcelReader excelReader = new ExcelReader(excelFile);
		
		ArrayList<String> lowercasedSheetNames = new ArrayList<String>();
		Map<String, String> lowerToOriginalName = new LinkedHashMap<String, String>();

		try {

			for (int i = 0; i < excelReader.getNumberOfSheets(); i++) {
				String sheetName = excelReader.getSheetName(i);
				lowercasedSheetNames.add(sheetName.toLowerCase());
				lowerToOriginalName.put(sheetName.toLowerCase(), sheetName);
			}

			<#list entities as entity><#if !entity.abstract>
			if (lowercasedSheetNames.contains("${entity.name?lower_case}")) {
				String originalSheetname = lowerToOriginalName.get("${entity.name?lower_case}");
				ExcelSheetReader sheetReader = excelReader.getSheet(originalSheetname);
				List<String> colNames = Lists.newArrayList(sheetReader.colNamesIterator());
				List<Field> entityFields = db.getMetaData().getEntity(${JavaName(entity)}.class.getSimpleName()).getAllFields();
				headersToMaps(originalSheetname, colNames, entityFields);
			}
			</#if></#list>
			
			for(String sheetName : lowerToOriginalName.values()){
				if(importOrder.contains(sheetName)){
					sheetsImportable.put(sheetName, true);
				}else{
					sheetsImportable.put(sheetName, false);
				}
			}

		} 
		finally 
		{
			excelReader.close();
		}
	}
	
	public void headersToMaps(String originalSheetname, List<String> allHeaders, List<Field> entityFields)
			throws MolgenisModelException, DatabaseException
	{
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
					else
						fieldMap = availableFields;
				}
				else
					fieldMap = availableFields;

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
		for (String header : allHeaders)
		{
			if (header == null || header.isEmpty()) continue;
			
			String fieldName = header.toLowerCase();
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

		importOrder.add(originalSheetname);
		fieldsImportable.put(originalSheetname, detectedFieldNames);
		fieldsUnknown.put(originalSheetname, unknownFieldNames);
		fieldsRequired.put(originalSheetname, requiredFields.keySet());
		fieldsAvailable.put(originalSheetname, availableFields.keySet());
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

	public Map<String, Boolean> getSheetsImportable()
	{
		return sheetsImportable;
	}

	public Map<String, Collection<String>> getFieldsImportable()
	{
		return fieldsImportable;
	}

	public Map<String, Collection<String>> getFieldsUnknown()
	{
		return fieldsUnknown;
	}

	public Map<String, Collection<String>> getFieldsRequired()
	{
		return fieldsRequired;
	}

	public Map<String, Collection<String>> getFieldsAvailable()
	{
		return fieldsAvailable;
	}

	public List<String> getImportOrder()
	{
		return importOrder;
	}
}