package org.molgenis.ui.freemarker;

import static freemarker.template.utility.DeepUnwrap.unwrap;

import java.util.List;

import org.molgenis.data.DataConverter;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * FreeMarker limit method. For limiting text size, adds '...' if to long
 * 
 * usage: ${limit('text', 5)}
 */
public class LimitMethod implements TemplateMethodModelEx
{
	private String limit(String s, int nrOfCharacters)
	{
		if (s.length() > nrOfCharacters)
		{
			return s.substring(0, nrOfCharacters - 6) + " [...]";
		}

		return s;
	}

	@Override
	public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
	{
		if (arguments.size() != 2)
		{
			throw new TemplateModelException("Expected two arguments");
		}

		String s = DataConverter.toString(unwrap((TemplateModel) arguments.get(0)));
		Integer nrOfCharacters = DataConverter.toInt(unwrap((TemplateModel) arguments.get(1)));

		return limit(s, nrOfCharacters);
	}
}
