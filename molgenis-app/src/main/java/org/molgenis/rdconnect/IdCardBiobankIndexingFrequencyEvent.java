package org.molgenis.rdconnect;

import org.springframework.context.ApplicationEvent;

public class IdCardBiobankIndexingFrequencyEvent extends ApplicationEvent
{
	private static final long serialVersionUID = 1L;

	private final String biobankIndexingFrequency;

	public IdCardBiobankIndexingFrequencyEvent(Object source, String biobankIndexingFrequency)
	{
		super(source);
		this.biobankIndexingFrequency = biobankIndexingFrequency;
	}

	public String getBiobankIndexingFrequency()
	{
		return biobankIndexingFrequency;
	}
}
