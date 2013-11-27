package org.molgenis.framework.db;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface EntitiesValidationReport
{
	public Map<String, Boolean> getSheetsImportable();

	public Map<String, Collection<String>> getFieldsImportable();

	public Map<String, Collection<String>> getFieldsUnknown();

	public Map<String, Collection<String>> getFieldsRequired();

	public Map<String, Collection<String>> getFieldsAvailable();

	public List<String> getImportOrder();
}
