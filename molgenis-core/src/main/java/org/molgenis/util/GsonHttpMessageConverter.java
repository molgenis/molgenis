/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.molgenis.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

/**
 * Converts objects to json string and vica versa If logging is set to debug it will print the incoming and outgoing
 * json strings
 * 
 * @author Roy Clarkson
 * @since 1.0
 */
public class GsonHttpMessageConverter extends AbstractHttpMessageConverter<Object>
{
	private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
	private static final Logger LOG = Logger.getLogger(GsonHttpMessageConverter.class);
	private final Gson gson;
	private Type type = null;
	private boolean prefixJson = false;

	/**
	 * Construct a new {@code GsonHttpMessageConverter} with a default {@link Gson#Gson() Gson}.
	 */
	public GsonHttpMessageConverter()
	{
		super(new MediaType("application", "json", DEFAULT_CHARSET));

		GsonBuilder builder = new GsonBuilder().setDateFormat(MolgenisDateFormat.DATEFORMAT_DATETIME)
				.disableHtmlEscaping();
		if (LOG.isDebugEnabled())
		{
			builder = builder.setPrettyPrinting();
		}

		gson = builder.create();
	}

	/**
	 * Construct a new {@code GsonHttpMessageConverter}.
	 * 
	 * @param gson
	 *            a customized {@link Gson#Gson() Gson}
	 */
	public GsonHttpMessageConverter(Gson gson)
	{
		super(new MediaType("application", "json", DEFAULT_CHARSET));
		this.gson = gson;
	}

	public void setType(Type type)
	{
		this.type = type;
	}

	public Type getType()
	{
		return type;
	}

	public Gson getGson()
	{
		return gson;
	}

	/**
	 * Indicates whether the JSON output by this view should be prefixed with "{} &&". Default is false.
	 * <p>
	 * Prefixing the JSON string in this manner is used to help prevent JSON Hijacking. The prefix renders the string
	 * syntactically invalid as a script so that it cannot be hijacked. This prefix does not affect the evaluation of
	 * JSON, but if JSON validation is performed on the string, the prefix would need to be ignored.
	 */
	public void setPrefixJson(boolean prefixJson)
	{
		this.prefixJson = prefixJson;
	}

	@Override
	public boolean canRead(Class<?> clazz, MediaType mediaType)
	{
		return canRead(mediaType);
	}

	@Override
	public boolean canWrite(Class<?> clazz, MediaType mediaType)
	{
		return canWrite(mediaType);
	}

	@Override
	protected boolean supports(Class<?> clazz)
	{
		// should not be called, since we override canRead/Write instead
		throw new UnsupportedOperationException();
	}

	@Override
	protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException,
			HttpMessageNotReadableException
	{
		Reader json = new InputStreamReader(inputMessage.getBody(), getCharset(inputMessage.getHeaders()));
		try
		{
			Type typeOfT = getType();
			if (LOG.isDebugEnabled())
			{
				String jsonStr = IOUtils.toString(json);
				LOG.debug("Json request:\n" + jsonStr);

				if (typeOfT != null)
				{
					return this.gson.fromJson(jsonStr, typeOfT);
				}

				return this.gson.fromJson(jsonStr, clazz);
			}
			else
			{
				if (typeOfT != null)
				{
					return this.gson.fromJson(json, typeOfT);
				}

				return this.gson.fromJson(json, clazz);
			}
		}
		catch (JsonSyntaxException ex)
		{
			throw new HttpMessageNotReadableException("Could not read JSON: " + ex.getMessage(), ex);
		}
		catch (JsonIOException ex)
		{
			throw new HttpMessageNotReadableException("Could not read JSON: " + ex.getMessage(), ex);
		}
		catch (JsonParseException ex)
		{
			throw new HttpMessageNotReadableException("Could not read JSON: " + ex.getMessage(), ex);
		}
		finally
		{
			IOUtils.closeQuietly(json);
		}
	}

	@Override
	protected void writeInternal(Object o, HttpOutputMessage outputMessage) throws IOException,
			HttpMessageNotWritableException
	{

		OutputStreamWriter writer = new OutputStreamWriter(outputMessage.getBody(),
				getCharset(outputMessage.getHeaders()));

		try
		{
			Type typeOfSrc = getType();

			if (LOG.isDebugEnabled())
			{
				StringBuilder sb = new StringBuilder();
				if (this.prefixJson)
				{
					sb.append("{} && ");
				}

				if (typeOfSrc != null)
				{
					sb.append(gson.toJson(o, typeOfSrc));
				}
				else
				{
					sb.append(gson.toJson(o));
				}

				LOG.debug("Json response:\n" + sb.toString());
				writer.write(sb.toString());
			}
			else
			{
				if (this.prefixJson)
				{
					writer.append("{} && ");
				}

				if (typeOfSrc != null)
				{
					this.gson.toJson(o, typeOfSrc, writer);
				}
				else
				{
					this.gson.toJson(o, writer);
				}
			}
		}
		catch (JsonIOException ex)
		{
			throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getMessage(), ex);
		}
		finally
		{
			IOUtils.closeQuietly(writer);
		}

	}

	// helpers

	private Charset getCharset(HttpHeaders headers)
	{
		if (headers != null && headers.getContentType() != null && headers.getContentType().getCharSet() != null)
		{
			return headers.getContentType().getCharSet();
		}
		return DEFAULT_CHARSET;
	}

}
