package org.molgenis.data.csv.typed;

import org.molgenis.data.Entity;

/**
 * Convert a row in a csv file to an entity
 * 
 * @param <T>
 */
public interface LineMapper<T extends Entity>
{
	T mapLine(String[] values, int lineNumber);
}
