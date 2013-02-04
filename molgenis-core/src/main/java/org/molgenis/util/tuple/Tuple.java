package org.molgenis.util.tuple;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

/**
 * A row of values
 */
public interface Tuple extends Serializable
{
	/**
	 * Return the number of columns
	 * 
	 * @return
	 */
	public int getNrCols();

	/**
	 * Returns whether the values have corresponding column names
	 * 
	 * @return
	 */
	public boolean hasColNames();

	/**
	 * Returns all column names
	 * 
	 * @return
	 */
	public Iterable<String> getColNames();

	/**
	 * Returns whether the value of the designated column is null
	 * 
	 * @param columnName
	 * @return
	 */
	public boolean isNull(String colName);

	public boolean isNull(int col);

	/**
	 * Retrieves the value of the designated column as Object.
	 * 
	 * @param columnName
	 * @return
	 */
	public Object get(String colName);

	public Object get(int col);

	/**
	 * Retrieves the value of the designated column as String.
	 * 
	 * @param columnName
	 * @return
	 */
	public String getString(String colName);

	public String getString(int col);

	/**
	 * Retrieves the value of the designated column as Integer.
	 * 
	 * @param columnName
	 * @return
	 */
	public Integer getInt(String colName);

	public Integer getInt(int col);

	/**
	 * Retrieves the value of the designated column as Long.
	 * 
	 * @param columnName
	 * @return
	 */
	public Long getLong(String colName);

	public Long getLong(int col);

	/**
	 * Retrieves the value of the designated column as Boolean.
	 * 
	 * @param columnName
	 * @return
	 */
	public Boolean getBoolean(String colName);

	public Boolean getBoolean(int col);

	/**
	 * Retrieves the value of the designated column as Double.
	 * 
	 * @param columnName
	 * @return
	 */
	public Double getDouble(String colName);

	public Double getDouble(int col);

	/**
	 * Retrieves the value of the designated column as {@link java.sql.Date}.
	 * 
	 * @param columnName
	 * @return
	 */
	public Date getDate(String colName);

	public Date getDate(int col);

	/**
	 * Retrieves the value of the designated column as
	 * {@link java.sql.Timestamp}.
	 * 
	 * @param columnName
	 * @return
	 */
	public Timestamp getTimestamp(String colName);

	public Timestamp getTimestamp(int col);

	/**
	 * Retrieves the value of the designated column as List<?>.
	 * 
	 * TODO make generic
	 * 
	 * @param columnName
	 * @return
	 */
	public List<String> getList(String colName);

	public List<String> getList(int col);
}
