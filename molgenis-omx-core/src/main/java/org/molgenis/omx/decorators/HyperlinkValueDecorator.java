package org.molgenis.omx.decorators;

import java.util.List;

import org.hibernate.validator.constraints.impl.URLValidator;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.MapperDecorator;
import org.molgenis.omx.observ.value.HyperlinkValue;

public class HyperlinkValueDecorator<E extends HyperlinkValue> extends MapperDecorator<E>
{
	private URLValidator urlValidator;

	public HyperlinkValueDecorator(Mapper<E> generatedMapper)
	{
		super(generatedMapper);
		urlValidator = new URLValidator();
	}

	@Override
	public int add(List<E> entities) throws DatabaseException
	{
		for (E entity : entities)
		{
			String url = entity.getValue();
			if (!urlValidator.isValid(url, null))
			{
				throw new DatabaseException("not a hyperlink [" + url + "]");
			}
		}
		return super.add(entities);
	}

	@Override
	public int update(List<E> entities) throws DatabaseException
	{
		for (E entity : entities)
		{
			String url = entity.getValue();
			if (!urlValidator.isValid(url, null))
			{
				throw new DatabaseException("not a hyperlink [" + url + "]");
			}
		}
		return super.update(entities);
	}
}
