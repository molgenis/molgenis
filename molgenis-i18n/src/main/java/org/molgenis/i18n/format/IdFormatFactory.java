package org.molgenis.i18n.format;

import org.molgenis.i18n.Identifiable;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Locale;

/**
 * {@link org.apache.commons.lang3.text.FormatFactory} for {@link Format}s that invokes and prints the value of
 * the Identifiable#getId method.
 */
class IdFormatFactory implements org.apache.commons.lang3.text.FormatFactory
{
	private static final IdFormat ID_FORMAT = new IdFormat();

	@Override
	public Format getFormat(String name, String arguments, Locale locale)
	{
		return ID_FORMAT;
	}

	static class IdFormat extends Format
	{
		@Override
		public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos)
		{
			if (obj instanceof Identifiable)
			{
				return toAppendTo.append(((Identifiable) obj).getIdValue());
			}
			return toAppendTo.append(obj);
		}

		@Override
		public Object parseObject(String source, ParsePosition pos)
		{
			throw new UnsupportedOperationException();
		}
	}
}
