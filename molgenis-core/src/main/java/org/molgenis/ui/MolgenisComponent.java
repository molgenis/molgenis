package org.molgenis.ui;

import java.util.UUID;

import org.molgenis.framework.ui.html.HtmlInputException;
import org.molgenis.util.tuple.Tuple;

public abstract class MolgenisComponent<T>
{
	public static final String ID = "id";
	public static final String CLASS = "class";

	/** unique id of this input */
	private String id;

	/** The css class of this input. */
	private String clazz;

	/** Constructor using id only */
	public MolgenisComponent(String id)
	{
		this.id = id;
	}

	/** No-arg constructor. Will autogenerate id */
	public MolgenisComponent()
	{
		this.id = UUID.randomUUID().toString().replace("-", "");
	}

	public void set(Tuple params) throws HtmlInputException
	{
		this.id(params.getString(ID));
		this.setClazz(params.getString(CLASS));
	}

	// PROPERTIES
	public String getId()
	{
		return id;
	}

	@SuppressWarnings("unchecked")
	public T id(String id)
	{
		this.id = id;
		return (T) this;
	}

	public String getClazz()
	{
		return this.clazz;
	}

	@SuppressWarnings("unchecked")
	public T setClazz(String clazz)
	{
		this.clazz = clazz;
		return (T) this;
	}
}
