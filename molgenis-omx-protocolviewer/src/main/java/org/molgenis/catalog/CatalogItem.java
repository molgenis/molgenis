package org.molgenis.catalog;

import java.util.List;

public interface CatalogItem
{
	public String getId();

	public String getName();

	public String getDescription();
	
	public List<String> getGroup();

	/**
	 * Return catalog item code or null if code does not exist
	 * 
	 * @return
	 */
	public String getCode();

	/**
	 * Return catalog item code system or null if code does not exist
	 * 
	 * @return
	 */
	public String getCodeSystem();

	public Iterable<CatalogFolder> getPath();
}
