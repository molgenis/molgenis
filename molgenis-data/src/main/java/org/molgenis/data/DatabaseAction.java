package org.molgenis.data;

public enum DatabaseAction
{
	/** add records , error on duplicate records */
	ADD,
	/** add, ignore existing records */
	ADD_IGNORE_EXISTING,
	/** add, update existing records */
	ADD_UPDATE_EXISTING,
	/**
	 * update records, throw an error if records are missing in the database
	 */
	UPDATE,
	/** update records, ignore missing records */
	UPDATE_IGNORE_MISSING,
	/**
	 * remove records in the list from database; throw an exception of records are missing in the database
	 */
	REMOVE,
	/** remove records in the list from database; ignore missing records */
	REMOVE_IGNORE_MISSING,
};
