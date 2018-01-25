package org.molgenis.oneclickimporter.service.impl;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.util.MolgenisDateFormat;
import org.molgenis.oneclickimporter.service.AttributeTypeService;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.molgenis.data.meta.AttributeType.*;

@Component
public class AttributeTypeServiceImpl implements AttributeTypeService
{
	private static final int MAX_STRING_LENGTH = 255;

	@Override
	public AttributeType guessAttributeType(List<Object> dataValues)
	{
		boolean guessCompleted = false;
		int rowCount = dataValues.size();
		int currentRowIndex = 0;

		AttributeType currentGuess = getBasicAttributeType(dataValues.get(0));
		currentGuess = getEnrichedType(currentGuess, dataValues.get(currentRowIndex));
		while (currentRowIndex < rowCount && !guessCompleted)
		{
			Object value = dataValues.get(currentRowIndex);
			AttributeType basicType = getBasicAttributeType(value);

			AttributeType basicTypeGuess = getCommonType(currentGuess, basicType);
			AttributeType enrichedTypeGuess = getEnrichedType(basicTypeGuess, value);

			// If the newly found type is not narrower than the current type, do not update
			// e.g. a long does not fit into an integer
			if (isBroader(enrichedTypeGuess, currentGuess))
			{
				currentGuess = enrichedTypeGuess;
			}

			// If a guess is TEXT, there is no other type option suitable
			if (TEXT.equals(currentGuess))
			{
				guessCompleted = true;
			}

			currentRowIndex++;
		}

		if (currentGuess == null)
		{
			currentGuess = STRING;
		}
		return currentGuess;
	}

	/**
	 * Check if the new enriched type is broader the the previously found type
	 *
	 * @return
	 */
	private boolean isBroader(AttributeType enrichedTypeGuess, AttributeType columnTypeGuess)
	{
		if (columnTypeGuess == null && enrichedTypeGuess != null || columnTypeGuess == null)
		{
			return true;
		}

		switch (columnTypeGuess)
		{
			case INT:
				return enrichedTypeGuess.equals(INT) || enrichedTypeGuess.equals(LONG) || enrichedTypeGuess.equals(
						DECIMAL) || enrichedTypeGuess.equals(STRING) || enrichedTypeGuess.equals(TEXT);

			case DECIMAL:
				return enrichedTypeGuess.equals(DECIMAL) || enrichedTypeGuess.equals(DATE) || enrichedTypeGuess.equals(
						DATE_TIME) || enrichedTypeGuess.equals(STRING) || enrichedTypeGuess.equals(TEXT);
			case LONG:
				return enrichedTypeGuess.equals(LONG) || enrichedTypeGuess.equals(DECIMAL) || enrichedTypeGuess.equals(
						DATE) || enrichedTypeGuess.equals(DATE_TIME) || enrichedTypeGuess.equals(STRING)
						|| enrichedTypeGuess.equals(TEXT);
			case BOOL:
				return enrichedTypeGuess.equals(STRING) || enrichedTypeGuess.equals(TEXT);
			case STRING:
				return enrichedTypeGuess.equals(STRING) || enrichedTypeGuess.equals(TEXT);
			case DATE_TIME:
				return enrichedTypeGuess.equals(STRING) || enrichedTypeGuess.equals(TEXT);
			case DATE:
				return enrichedTypeGuess.equals(STRING) || enrichedTypeGuess.equals(TEXT);
			default:
				return false;

		}
	}

	/**
	 * Returns an enriched AttributeType for when the value meets certain criteria
	 * i.e. if a string value is longer dan 255 characters, the type should be TEXT
	 */
	private AttributeType getEnrichedType(AttributeType guess, Object value)
	{
		if (value == null)
		{
			return guess;
		}

		if (guess.equals(STRING))
		{
			String stringValue = value.toString();
			if (stringValue.length() > MAX_STRING_LENGTH)
			{
				return TEXT;
			}

			if (canValueBeUsedAsDate(value))
			{
				return DATE;
			}
		}
		else if (guess.equals(DECIMAL))
		{
			if (value instanceof Integer)
			{
				return INT;
			}
			else if (value instanceof Long)
			{
				Long longValue = (Long) value;
				return longValue > Integer.MIN_VALUE && longValue < Integer.MAX_VALUE ? INT : LONG;
			}
			else if (value instanceof Double)
			{
				Double doubleValue = (Double) value;

				if (doubleValue != Math.rint(doubleValue))
				{
					return DECIMAL;
				}

				if (doubleValue > Integer.MIN_VALUE && doubleValue < Integer.MAX_VALUE)
				{
					return INT;
				}

				if (doubleValue > Long.MIN_VALUE && doubleValue < Long.MAX_VALUE)
				{
					return LONG;
				}
			}
		}
		return guess;
	}

	/**
	 * Returns the AttributeType shared by both types
	 */
	private AttributeType getCommonType(AttributeType existingGuess, AttributeType newGuess)
	{
		if (existingGuess == null && newGuess == null)
		{
			return null;
		}

		if (existingGuess == null)
		{
			return newGuess;
		}

		if (newGuess == null)
		{
			return existingGuess;
		}

		if (existingGuess.equals(newGuess))
		{
			return existingGuess;
		}

		switch (existingGuess)
		{
			case INT:
				//noinspection Duplicates
				if (newGuess.equals(DECIMAL))
				{
					return DECIMAL;
				}
				else if (newGuess.equals(LONG))
				{
					return LONG;
				}
				else
				{
					return STRING;
				}
			case DECIMAL:
				if (newGuess.equals(INT) || newGuess.equals(LONG))
				{
					return DECIMAL;
				}
				else
				{
					return STRING;
				}
			case LONG:
				//noinspection Duplicates
				if (newGuess.equals(INT))
				{
					return LONG;
				}
				else if (newGuess.equals(DECIMAL))
				{
					return DECIMAL;
				}
				else
				{
					return STRING;
				}
			case BOOL:
				if (!newGuess.equals(BOOL))
				{
					return STRING;
				}
			case DATE:
				if (!newGuess.equals(DATE))
				{
					return STRING;
				}
			default:
				return STRING;
		}
	}

	/**
	 * Sets the basic type based on instance of the value Object
	 */
	private AttributeType getBasicAttributeType(Object value)
	{
		if (value == null)
		{
			return null;
		}

		if (value instanceof Integer)
		{
			return INT;
		}
		else if (value instanceof Double || value instanceof Float)
		{
			return DECIMAL;
		}
		else if (value instanceof Long)
		{
			return LONG;
		}
		else if (value instanceof Boolean)
		{
			return BOOL;
		}
		else
		{
			return STRING;
		}
	}

	private Boolean canValueBeUsedAsDate(Object value)
	{
		try
		{
			MolgenisDateFormat.parseLocalDate(value.toString());
		}
		catch (Exception e)
		{
			return false;
		}
		return true;
	}
}
