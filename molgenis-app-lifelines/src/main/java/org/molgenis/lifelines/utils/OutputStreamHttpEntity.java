package org.molgenis.lifelines.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.entity.AbstractHttpEntity;

/**
 * Write content out with HttpClient without intermediate buffering in memory.
 */
public abstract class OutputStreamHttpEntity extends AbstractHttpEntity
{
	@Override
	public boolean isRepeatable()
	{
		return false;
	}

	@Override
	public long getContentLength()
	{
		return -1;
	}

	@Override
	public boolean isStreaming()
	{
		return false;
	}

	@Override
	public InputStream getContent() throws IOException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public abstract void writeTo(OutputStream outstream) throws IOException;
}
