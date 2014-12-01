package org.molgenis.study;

import java.util.Date;
import java.util.List;

import org.molgenis.catalog.CatalogFolder;

public interface StudyDefinition
{
	String getId();

	void setId(String id);

	String getName();

	void setName(String name);

	String getDescription();

	String getVersion();

	Date getDateCreated();

	Status getStatus();

	Iterable<CatalogFolder> getItems();

	void setItems(Iterable<CatalogFolder> items);

	boolean containsItem(CatalogFolder item);

	List<String> getAuthors();

	String getAuthorEmail();

	/**
	 * Get the request proposal form filename
	 * 
	 * @return
	 */
	String getRequestProposalForm();

	void setRequestProposalForm(String fileName);

	String getExternalId();

	void setExternalId(String externalId);

	public enum Status
	{
		DRAFT, SUBMITTED, APPROVED, REJECTED, EXPORTED
	}
}
