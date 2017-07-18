package org.molgenis.oneclickimporter.service;

import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.oneclickimporter.model.DataCollection;

import java.util.List;

public interface OneClickImporterService
{
	/**
	 * Generate {@link DataCollection} with sheet contents
	 *
	 * @param dataCollectionName
	 * @param sheet
	 */
	DataCollection buildDataCollection(String dataCollectionName, Sheet sheet);

	/**
	 * Guess the datatype of the given list.
	 * <p>
	 * The data type is based on the lowest common data type of the given list of values.
	 * <p>
	 * Of example is the list is composed of a set of Date's and a single String the data type will be String
	 * <p>
	 * The type hierarchies are:
	 * DateTime -> String -> Text
	 * Date -> String -> Text,
	 * Boolean -> String -> Text
	 * Int -> Long -> Decimal -> String -> Text
	 * String -> Text
	 *
	 * @param dataValues
	 */
	AttributeType guessAttributeType(List<Object> dataValues);
}
