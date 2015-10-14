package org.molgenis.rdconnect;

import org.springframework.context.ApplicationEvent;

public class IdCardBiobankIndexingEnabledEvent extends ApplicationEvent
{
	private static final long serialVersionUID = 1L;

	private final boolean biobankIndexingEnabled;

	public IdCardBiobankIndexingEnabledEvent(Object source, boolean biobankIndexingEnabled)
	{
		super(source);
		this.biobankIndexingEnabled = biobankIndexingEnabled;
	}

	public boolean getBiobankIndexingEnabled()
	{
		return biobankIndexingEnabled;
	}
}
