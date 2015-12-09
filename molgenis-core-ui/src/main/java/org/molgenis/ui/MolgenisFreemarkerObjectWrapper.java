package org.molgenis.ui;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.Version;

public class MolgenisFreemarkerObjectWrapper extends DefaultObjectWrapper
{
	public MolgenisFreemarkerObjectWrapper(Version incompatibleImprovements)
	{
		super(incompatibleImprovements);
	}

	@Override
	protected TemplateModel handleUnknownType(Object obj) throws TemplateModelException
	{
		if (obj instanceof Iterable<?>)
		{
			// Fix for https://github.com/molgenis/molgenis/issues/4227
			// If a method returning an Iterable in some cases returns a List and in other cases a e.g. FluentIterable
			// then it is unclear whether <#list iterable> or <#list iterable.iterator()> should be used.
			obj = ((Iterable<?>) obj).iterator();
		}
		return super.handleUnknownType(obj);
	}
}
