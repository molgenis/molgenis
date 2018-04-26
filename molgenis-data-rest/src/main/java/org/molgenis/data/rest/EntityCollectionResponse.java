package org.molgenis.data.rest;

import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.UserPermissionEvaluator;

import java.util.List;
import java.util.Map;

public class EntityCollectionResponse
{
	private final String href;
	private final EntityTypeResponse meta;
	private final int start;
	private final int num;
	private final long total;
	private final String prevHref;
	private final String nextHref;
	private final List<Map<String, Object>> items;

	public EntityCollectionResponse(EntityPager entityPager, List<Map<String, Object>> items, String href,
			EntityType meta, UserPermissionEvaluator permissionService, DataService dataService)
	{
		this.href = href;
		this.meta = meta != null ? new EntityTypeResponse(meta, permissionService, dataService) : null;
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

	public EntityTypeResponse getMeta()
	{
		return meta;
	}

	public int getStart()
	{
		return start;
	}

	public int getNum()
	{
		return num;
	}

	public long getTotal()
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