package org.molgenis.i18n.format;

import org.molgenis.i18n.Labeled;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Locale;

/**
 * {@link org.apache.commons.lang3.text.FormatFactory} for {@link Format}s that invoke and print the value of
 * the Labeled#getLabel method for a given {@link Locale}'s language.
 */
class LabelFormatFactory implements org.apache.commons.lang3.text.FormatFactory
{
	@Override
	public Format getFormat(String name, String arguments, Locale locale)
	{
		return new LabelFormat(locale.getLanguage());
	}

	private static class LabelFormat extends Format
	{
		private final String languageCode;

		private LabelFormat(String languageCode)
		{
			this.languageCode = languageCode;
		}

		@Override
		public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos)
		{
			if (obj instanceof Labeled)
			{
				return toAppendTo.append(((Labeled) obj).getLabel(languageCode));
			}
			else
			{
				return toAppendTo.append(obj);
			}
		}

		@Override
		public Object parseObject(String source, ParsePosition pos)
		{
			throw new UnsupportedOperationException();
		}
	}
}
