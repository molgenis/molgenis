package org.molgenis.util;

import java.text.ParseException;
import java.util.List;
import java.util.Vector;

import org.molgenis.util.tuple.Tuple;

/**
 * Typed data objects that enforce data structure.
 * <p>
 * Entity just behaves like a "bean" or "POJO" and poses very little
 * restrictions on classes stored. Entity is the interface for any entity stored
 * within a <a href="org.molgenis.framework.data.Database">Database</a>.
 * <p>
 * Advantage of using a typed data object is a) that it can be inspected to see
 * what is in there, b) it is much easier to use the data as is intended and c)
 * the compiler will inform you if you make a change that breaks other
 * components depending on the Entity.
 * <p>
 * FIXME: make a common superclass for Tuple and Entity.
 */
public interface Entity
{

	/**
	 * Generic setter
	 * 
	 * @param fieldName
	 * @param value
	 * @throws ParseException
	 */
	public void set(String fieldName, Object value) throws Exception;

	/**
	 * Set the properties of this entity using the values from a Tuple.
	 * 
	 * @param values
	 * @throws ParseException
	 */
	public void set(Tuple values) throws Exception;

	/**
	 * Set the properties of this entity using the values from a Tuple.
	 * <p>
	 * Newly created Entity objects may have default values. The 'strict'
	 * parameter indicates whether these defaults should be overwritten by Tuple
	 * values in the case that these values are 'null'. Typically, this is only
	 * desired when one is reconstructing an existing Entity, and not when
	 * instantiating an Entity for the first time.
	 * 
	 * @param values
	 *            a tuple map of values
	 * @param strict
	 *            whether null values should be mapped also from Tuple to Entity
	 * @throws ParseException
	 */
	public void set(Tuple values, boolean strict) throws Exception;

	/**
	 * Generic getter (by fieldname)
	 */
	public Object get(String columnName);

	/**
	 * Primary key field name
	 */
	public String getIdField();

	public Object getIdValue();

	/**
	 * Secondary key field names
	 */
	public List<String> getLabelFields();

	/**
	 * Get the values as a Tuple.
	 */
	public Tuple getValues();

	/**
	 * Get the names used as aliases for the field indexes (e.g. for use in
	 * {@link #get(String)}).
	 * 
	 * @return column names.
	 */
	public Vector<String> getFields();

	/**
	 * Get the names used as aliases for the field indexes (e.g. for use in
	 * {@link #get(String)}).
	 * 
	 * @skipAutoIds removes autoid fields
	 * @return column names.
	 */
	public Vector<String> getFields(boolean skipAutoIds);

	/**
	 * Mark an Entity as readonly.
	 * 
	 * @param readonly
	 *            to set an Entity to be readonly.
	 */
	public void setReadonly(boolean readonly);

	/**
	 * Whether this Entity is readonly.
	 * 
	 * @return boolean indicating whether this Entity is readonly.
	 */
	public boolean isReadonly();

	/**
	 * @deprecated This method will be removed because it is too specific.
	 */
	@Deprecated
	public String getValues(String sep);

	/**
	 * @deprecated This method will be removed because it is too specific.
	 */
	@Deprecated
	public String getFields(String sep);

	/**
	 * Validates attribute and tupel constraints (such as not null).
	 */
	public void validate() throws Exception;

	Entity create(Tuple tuple) throws Exception;

	// for JPA only
	public String getXrefIdFieldName(String fieldName);

	public String getLabelValue();

}
