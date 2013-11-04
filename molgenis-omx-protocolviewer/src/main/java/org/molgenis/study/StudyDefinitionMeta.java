package org.molgenis.study;

import java.util.Date;

import org.molgenis.catalog.CatalogMeta;

/**
 * Study definition meta data
 * 
 * @author erwin
 */
public class StudyDefinitionMeta extends CatalogMeta
{
	private final String email;
	private final Date date;

	/**
	 * 
	 * @param id
	 * @param name
	 * @param email
	 *            email of the user related to this study definition
	 */
	public StudyDefinitionMeta(String id, String name, String email, Date date)
	{
		super(id, name);
		this.email = email;
		this.date = date != null ? new Date(date.getTime()) : null; // do not store externally mutable object
	}

	public String getEmail()
	{
		return email;
	}

	public Date getDate()
	{
		return date != null ? new Date(date.getTime()) : null; // do not expose mutable object
	}
}
