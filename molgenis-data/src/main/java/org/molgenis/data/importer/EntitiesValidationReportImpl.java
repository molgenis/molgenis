package org.molgenis.data.importer;

import java.util.*;

import org.molgenis.framework.db.EntitiesValidationReport;

/**
 * Created by mswertz on 03/05/14.
 */
public class EntitiesValidationReportImpl implements EntitiesValidationReport
{
	/**
	 * map of all sheets, and whether they are importable (recognized) or not
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

	@Override
	public boolean valid()
	{// determine if validation succeeded
		boolean ok = true;
		if (sheetsImportable != null)
		{
			for (Boolean b : sheetsImportable.values())
			{
				ok = ok & b;
			}

			for (Collection<String> fields : getFieldsRequired().values())
			{
				ok = ok & (fields == null || fields.isEmpty());
			}
		}
		return ok;
	}
}
