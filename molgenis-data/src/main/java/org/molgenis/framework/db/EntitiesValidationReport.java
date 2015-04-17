package org.molgenis.framework.db;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface EntitiesValidationReport
{
	/** Returns true for importable sheets and false for unimportable sheets */
	public Map<String, Boolean> getSheetsImportable();

	/** lists per entity what fields can be imported */
	public Map<String, Collection<String>> getFieldsImportable();

	/** lists per entity what fields cannot be imported */
	public Map<String, Collection<String>> getFieldsUnknown();

	/** lists per entity what fields should have been filled in */
	public Map<String, Collection<String>> getFieldsRequired();

	/** lists per entity what fields could have been filled in but where not provided */
	public Map<String, Collection<String>> getFieldsAvailable();

	/** provides import order based on dependency */
	public List<String> getImportOrder();

	/** Returns list of packages **/
	public List<String> getPackages();

	boolean valid();
}
