package org.molgenis.util;

import java.nio.charset.Charset;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;

public abstract class BaseHttpMessageConverter<T> extends AbstractHttpMessageConverter<T>
{
	protected static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	public BaseHttpMessageConverter()
	{
		super();
	}

	public BaseHttpMessageConverter(MediaType... supportedMediaTypes)
	{
		super(supportedMediaTypes);
	}

	public BaseHttpMessageConverter(MediaType supportedMediaType)
	{
		super(supportedMediaType);
	}

	protected Charset getCharset(HttpHeaders headers)
	{
		if (headers != null && headers.getContentType() != null && headers.getContentType().getCharSet() != null)
		{
			return headers.getContentType().getCharSet();
		}
		return DEFAULT_CHARSET;
	}

}