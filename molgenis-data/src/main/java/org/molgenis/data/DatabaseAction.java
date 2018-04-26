package org.molgenis.data;

public enum DatabaseAction
{
	/**
	 * add records , error on duplicate records
	 */
	ADD,

	/**
	 * add, update existing records
	 */
	ADD_UPDATE_EXISTING,

	/**
	 * update records, throw an error if records are missing in the database
	 */
	UPDATE,

	/**
	 * Adds new records, ignores existing records
	 */
	ADD_IGNORE_EXISTING
}
