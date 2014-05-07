package org.molgenis.omx.decorators;

import java.net.URI;
import java.net.URISyntaxException;

import javax.validation.ValidationException;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.CrudRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.omx.observ.value.HyperlinkValue;

public class HyperlinkValueDecorator extends CrudRepositoryDecorator
{
	public HyperlinkValueDecorator(CrudRepository generatedRepository)
	{
		super(generatedRepository);
	}

	@Override
	public void add(Entity entity)
	{
		String uri = entity.getString(HyperlinkValue.VALUE);
		if (!isValidURI(uri))
		{
			throw new ValidationException("not a hyperlink [" + uri + "]");
		}

		super.add(entity);
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		for (Entity entity : entities)
		{
			String uri = entity.getString(HyperlinkValue.VALUE);
			if (!isValidURI(uri))
			{
				throw new ValidationException("not a hyperlink [" + uri + "]");
			}
		}

		return super.add(entities);
	}

	@Override
	public void update(Entity entity)
	{
		String uri = entity.getString(HyperlinkValue.VALUE);
		if (!isValidURI(uri))
		{
			throw new ValidationException("not a hyperlink [" + uri + "]");
		}

		super.update(entity);
	}

	@Override
	public void update(Iterable<? extends Entity> entities)
	{
		for (Entity entity : entities)
		{
			String uri = entity.getString(HyperlinkValue.VALUE);
			if (!isValidURI(uri))
			{
				throw new ValidationException("not a hyperlink [" + uri + "]");
			}
		}

		super.update(entities);
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
