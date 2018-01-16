package org.molgenis.core.ui.freemarker;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.apache.commons.lang3.StringEscapeUtils;
import org.molgenis.data.DataConverter;

import java.util.List;

import static freemarker.template.utility.DeepUnwrap.unwrap;

/**
 * FreeMarker limit method. For limiting text size, adds '...' if to long
 * <p>
 * usage: ${limit('text', 5)}
 */
public class LimitMethod implements TemplateMethodModelEx
{
	private String limit(String s, int nrOfCharacters, String container)
	{
		if (s.length() > nrOfCharacters)
		{
			String jsEscaped = StringEscapeUtils.escapeEcmaScript(s);

			return s.substring(0, nrOfCharacters - 8) + " [<a id='" + container
					+ "-all' href='#'> ... </a>]<script>$('#" + container + "-all').on('click', function(){$('#"
					+ container + "').html('" + jsEscaped + "')});</script>";
		}

		return s;
	}

	@Override
	public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
	{
		if (arguments.size() != 3)
		{
			throw new TemplateModelException("Expected two arguments");
		}

		String s = DataConverter.toString(unwrap((TemplateModel) arguments.get(0)));
		Integer nrOfCharacters = DataConverter.toInt(unwrap((TemplateModel) arguments.get(1)));
		String container = DataConverter.toString(arguments.get(2));

		return limit(s, nrOfCharacters, container);
	}
}
