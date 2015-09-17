package org.molgenis.data.rest.v2;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;

import org.molgenis.data.EntityMetaData;
import org.molgenis.data.rest.EntityPager;
import org.molgenis.security.core.MolgenisPermissionService;

class EntityCollectionResponseV2
{
	private final String href;
	private final EntityMetaDataResponseV2 meta;
	private final Integer start;
	private final Integer num;
	private final Long total;
	private final String prevHref;
	private final String nextHref;
	private final List<Map<String, Object>> items;

	public EntityCollectionResponseV2(String href)
	{
		this.href = requireNonNull(href);
		this.meta = null;
		this.start = null;
		this.num = null;
		this.total = null;
		this.prevHref = null;
		this.nextHref = null;
		this.items = null;
	}

	public EntityCollectionResponseV2(EntityPager entityPager, List<Map<String, Object>> items,
			AttributeFilter attributes, String href, EntityMetaData meta, MolgenisPermissionService permissionService)
	{
		this.href = href;
		this.meta = new EntityMetaDataResponseV2(meta, attributes, permissionService);
		this.start = entityPager.getStart();
		this.num = entityPager.getNum();
		this.total = entityPager.getTotal();

		Integer prevStart = entityPager.getPrevStart();
		this.prevHref = prevStart != null ? this.href + "?start=" + prevStart + "&num=" + this.num : null;

		Integer nextStart = entityPager.getNextStart();
		this.nextHref = nextStart != null ? this.href + "?start=" + nextStart + "&num=" + this.num : null;

		this.items = items;
	}

	public String getHref()
	{
		return href;
	}

	public EntityMetaDataResponseV2 getMeta()
	{
		return meta;
	}

	public Integer getStart()
	{
		return start;
	}

	public Integer getNum()
	{
		return num;
	}

	public Long getTotal()
	{
		return total;
	}

	public String getPrevHref()
	{
		return prevHref;
	}

	public String getNextHref()
	{
		return nextHref;
	}

	public List<Map<String, Object>> getItems()
	{
		return items;
	}

}