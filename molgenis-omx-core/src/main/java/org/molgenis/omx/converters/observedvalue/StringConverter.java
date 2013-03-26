package org.molgenis.omx.converters.observedvalue;

import org.molgenis.framework.db.Database;
import org.molgenis.omx.observ.ObservableFeature;

/**
 * Converts a string value to a java type
 * 
 * @author erwin
 * 
 * @param <T>
 */
public interface StringConverter<T>
{
	T fromString(String value, Database db, ObservableFeature feature);
}
