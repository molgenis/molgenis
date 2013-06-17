package org.molgenis.omx.decorators;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.MapperDecorator;
import org.molgenis.omx.observ.value.HyperlinkValue;

public class HyperlinkValueDecorator<E extends HyperlinkValue> extends MapperDecorator<E>
{
	public HyperlinkValueDecorator(Mapper<E> generatedMapper)
	{
		super(generatedMapper);
	}

	@Override
	public int add(List<E> entities) throws DatabaseException
	{
		for (E entity : entities)
		{
			String uri = entity.getValue();
			if (!isValidURI(uri))
			{
				throw new DatabaseException("not a hyperlink [" + uri + "]");
			}
		}
		return super.add(entities);
	}

	@Override
	public int update(List<E> entities) throws DatabaseException
	{
		for (E entity : entities)
		{
			String uri = entity.getValue();
			if (!isValidURI(uri))
			{
				throw new DatabaseException("not a hyperlink [" + uri + "]");
			}
		}
		return super.update(entities);
	}

	private boolean isValidURI(String uri)
	{
		try
		{
			new URI(uri);
			return true;
		}
		catch (URISyntaxException e)
		{
			return false;
		}
	}
}
