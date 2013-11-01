package org.molgenis.omx.decorators;

import java.net.URI;
import java.net.URISyntaxException;

import javax.validation.ValidationException;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.CrudRepositoryDecorator;
import org.molgenis.omx.observ.value.HyperlinkValue;

public class HyperlinkValueDecorator<E extends HyperlinkValue> extends CrudRepositoryDecorator<E>
{
	public HyperlinkValueDecorator(CrudRepository<E> generatedRepository)
	{
		super(generatedRepository);
	}

	@Override
	public void add(E entity)
	{
		String uri = entity.getValue();
		if (!isValidURI(uri))
		{
			throw new ValidationException("not a hyperlink [" + uri + "]");
		}

		super.add(entity);
	}

	@Override
	public void add(Iterable<E> entities)
	{
		for (E entity : entities)
		{
			String uri = entity.getValue();
			if (!isValidURI(uri))
			{
				throw new ValidationException("not a hyperlink [" + uri + "]");
			}
		}

		super.add(entities);
	}

	@Override
	public void update(E entity)
	{
		String uri = entity.getValue();
		if (!isValidURI(uri))
		{
			throw new ValidationException("not a hyperlink [" + uri + "]");
		}

		super.update(entity);
	}

	@Override
	public void update(Iterable<E> entities)
	{
		for (E entity : entities)
		{
			String uri = entity.getValue();
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
