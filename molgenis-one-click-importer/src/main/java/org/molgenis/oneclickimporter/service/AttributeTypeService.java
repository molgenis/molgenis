package org.molgenis.oneclickimporter.service;

import org.molgenis.data.meta.AttributeType;

import java.util.List;

public interface AttributeTypeService
{
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
