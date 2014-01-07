package org.molgenis.studymanager;

import java.util.Date;

import org.molgenis.catalogmanager.CatalogMetaModel;

public class StudyDefinitionMetaModel extends CatalogMetaModel
{
	private final String email;
	private final Date date;

	/**
	 * 
	 * @param id
	 * @param name
	 * @param email
	 *            email of the user related to this study definition
	 * @param loaded
	 */
	public StudyDefinitionMetaModel(String id, String name, String email, Date date, boolean loaded)
	{
		super(id, name, loaded);
		this.email = email;
		this.date = date != null ? new Date(date.getTime()) : null; // do not store externally mutable object
	}

	public String getUser()
	{
		return email;
	}

	public Date getDate()
	{
		return date != null ? new Date(date.getTime()) : null; // do not expose mutual object
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		StudyDefinitionMetaModel other = (StudyDefinitionMetaModel) obj;
		if (date == null)
		{
			if (other.date != null) return false;
		}
		else if (!date.equals(other.date)) return false;
		if (email == null)
		{
			if (other.email != null) return false;
		}
		else if (!email.equals(other.email)) return false;
		return true;
	}
}
