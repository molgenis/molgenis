package org.molgenis.omx.converters.observedvalue;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.ObservableFeature;

/**
 * Xref field contains an identifier that references a subclass of
 * Characteristic. This class finds the object by identifier and returns the
 * labelValue of that object
 * 
 * @author erwin
 * 
 */
public class StringToXrefLabelConverter implements StringConverter<String>
{

	@Override
	public String fromString(String value, Database db, ObservableFeature feature)
	{
		try
		{
			Characteristic characteristic = Characteristic.findByIdentifier(db, value);
			if (characteristic == null)
			{
				throw new RuntimeException("Unknown identifier [" + value + "]");
			}

			return characteristic.getLabelValue();
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}

	}

}
