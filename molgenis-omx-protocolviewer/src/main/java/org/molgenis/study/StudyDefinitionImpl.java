package org.molgenis.study;

import java.util.Date;
import java.util.List;

import org.molgenis.catalog.CatalogItem;

public class StudyDefinitionImpl implements StudyDefinition
{
	private String id;
	private String name;
	private String description;
	private String version;
	private Date dateCreated;
	private Status status;
	private List<String> authors;
	private String authorEmail;
	private List<CatalogItem> items;
	private String requestForm;

	public StudyDefinitionImpl()
	{
	}

	public StudyDefinitionImpl(StudyDefinition studyDefinition)
	{
		if (studyDefinition == null) throw new IllegalArgumentException("Study definition is null");
		setId(studyDefinition.getId());
		setName(studyDefinition.getName());
		setDescription(studyDefinition.getDescription());
		setVersion(studyDefinition.getVersion());
		setDateCreated(studyDefinition.getDateCreated());
		setStatus(studyDefinition.getStatus());
		setAuthors(studyDefinition.getAuthors());
		setAuthorEmail(studyDefinition.getAuthorEmail());
		setItems(studyDefinition.getItems());
		setRequestForm(studyDefinition.getRequestProposalForm());
	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public void setId(String id)
	{
		this.id = id;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	@Override
	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	@Override
	public Date getDateCreated()
	{
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated)
	{
		this.dateCreated = dateCreated;
	}

	@Override
	public Status getStatus()
	{
		return status;
	}

	public void setStatus(Status status)
	{
		this.status = status;
	}

	@Override
	public List<String> getAuthors()
	{
		return authors;
	}

	public void setAuthors(List<String> authors)
	{
		this.authors = authors;
	}

	@Override
	public String getAuthorEmail()
	{
		return authorEmail;
	}

	public void setAuthorEmail(String authorEmail)
	{
		this.authorEmail = authorEmail;
	}

	@Override
	public String getRequestProposalForm()
	{
		return requestForm;
	}

	public void setRequestForm(String requestForm)
	{
		this.requestForm = requestForm;
	}

	@Override
	public List<CatalogItem> getItems()
	{
		return items;
	}

	public void setItems(List<CatalogItem> items)
	{
		this.items = items;
	}

	@Override
	public boolean containsItem(CatalogItem item)
	{
		throw new UnsupportedOperationException(); // FIXME
	}
}
