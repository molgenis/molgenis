package org.molgenis.study;

import java.util.Date;
import java.util.List;

import org.molgenis.catalog.CatalogItem;

public interface StudyDefinition
{
	String getId();

	void setId(String id);

	String getName();

	String getDescription();

	String getVersion();

	Date getDateCreated();

	Status getStatus();

	List<CatalogItem> getItems();

	void setItems(List<CatalogItem> items);

	boolean containsItem(CatalogItem item);

	List<String> getAuthors();

	String getAuthorEmail();

	/**
	 * Get the request proposal form filename
	 * 
	 * @return
	 */
	String getRequestProposalForm();

	public enum Status
	{
		DRAFT, SUBMITTED, APPROVED, REJECTED
	}
}
