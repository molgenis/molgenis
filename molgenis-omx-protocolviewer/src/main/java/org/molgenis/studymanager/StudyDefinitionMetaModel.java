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
		this.date = date;
	}

	public String getUser()
	{
		return email;
	}

	public Date getDate()
	{
		return date;
	}
}
