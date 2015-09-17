package org.molgenis.data.rest.v2;

import static com.google.common.base.Preconditions.checkNotNull;

import org.molgenis.data.AggregateResult;

public class EntityAggregatesResponse extends EntityCollectionResponseV2
{
	private final AggregateResult aggs;

	public EntityAggregatesResponse(AggregateResult aggs, String href)
	{
		super(href);
		this.aggs = checkNotNull(aggs);
	}

	public AggregateResult getAggs()
	{
		return aggs;
	}
}
