package org.molgenis.oneclickimporter.service;

import org.molgenis.data.validation.meta.NameValidator;

/**
 * Service that offers methods to:
 * - Generate valid ids by removing illegal characters
 * - Generate labels that are not duplicate
 */
public interface OneClickImporterNamingService
{
	// matches everything that is not 'a-z', 'A-Z', '0-9', '_' or '#'
	String ILLEGAL_CHARACTER_REGEX = "[^a-zA-Z0-9_#]+";

	/**
	 * Generates a label with a postfix if the supplied label is already present in the database
	 * <p>
	 * if the database contains an EntityType with label: 'label' then uploading a CSV file with the name label.csv
	 * result in the newly created EntityType being labeled with label (1)
	 *
	 * @param label
	 * @return a label with postfix if the label already existed
	 */
	String getLabelWithPostFix(String label);

	/**
	 * Valid Identifiers are not allowed to contain illegal characters
	 * This method generates a valid ID from a file name by replacing illegal characters with '_'
	 * <p>
	 * e.g. file-!!name.csv becomes file___name
	 * <p>
	 * Illegal character rules can be found in @{@link NameValidator#checkForIllegalCharacters}
	 *
	 * @param filename
	 * @return id without illegal characters
	 */
	String createValidIdFromFileName(String filename);

	/**
	 * Valid column names are not allowed to contain illegal characters
	 * <p>
	 * e.g. name#!3 becomes name#_3
	 * <p>
	 * Illegal character rules can be found in @{@link NameValidator#checkForIllegalCharacters}
	 *
	 * @param column
	 * @return column name without illegal characters
	 */
	String asValidColumnName(String column);
}
