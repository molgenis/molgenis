package org.molgenis.data.annotation.provider;

public interface Pinger
{
	public abstract boolean ping(String url, int timeout);
}
