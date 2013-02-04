package org.molgenis.framework.ui.html;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.molgenis.util.Entity;
import org.molgenis.util.tuple.SingletonTuple;

/**
 * EntityForm extends HtmlForm optimized for showing entities.
 * <ul>
 * <li>The field names within the entity are used for input names
 * <li>Input creation can be influenced by setNewrecord setHiddenColumns and
 * setCollapsedColumns
 * <li>designed to be used with a generator to create the inputs
 * </ul>
 * 
 * @param <E>
 *            entity class
 */
public abstract class EntityForm<E extends Entity> extends HtmlForm
{
	private E entity = null;

	/** Construct a form based on a new instance of E */
	public EntityForm()
	{
		try
		{
			this.setEntity(this.getEntityClass().newInstance());
		}
		catch (InstantiationException e)
		{
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}

	/** Construct a form based on existing instance of E */
	public EntityForm(E entity)
	{
		this.setEntity(entity);
	}

	public E getEntity()
	{
		return entity;
	}

	public void setEntity(E entity)
	{
		this.entity = entity;
	}

	@Override
	public void setInputs(List<HtmlInput<?>> inputs)
	{
		throw new UnsupportedOperationException("In EntityForm the inputs cannot be changed");
	}

	/**
	 * Set the value of a particular entity field by name
	 * 
	 * @param name
	 * @param value
	 * @throws Exception
	 */
	public void set(String name, Object value) throws Exception
	{
		this.getEntity().set(new SingletonTuple<Object>(name, value), false);
	}

	/**
	 * Entity class of this form. Useful to instantiate new instances of the
	 * entity
	 */
	public abstract Class<E> getEntityClass();

	/**
	 * Names of the input labels. Useful to show headers.
	 * 
	 * @return headers
	 */
	public abstract Vector<String> getHeaders();

	public List<HtmlInput<?>> getInputs(String... fieldNames)
	{
		Entity entity = this.getEntity();
		List<HtmlInput<?>> inputs = this.getInputs();
		List<HtmlInput<?>> result = new ArrayList<HtmlInput<?>>();

		for (String fieldName : fieldNames)
		{
			if (!entity.getFields().contains(fieldName))
			{
				throw new RuntimeException(fieldName + " not known in " + this.getClass().getSimpleName());
			}

			for (HtmlInput<?> input : inputs)
			{
				// will this work always??
				if (input.getName().equalsIgnoreCase(entity.getClass().getSimpleName() + "_" + fieldName))
				{
					result.add(input);
				}
			}
		}

		return result;

	}
}
