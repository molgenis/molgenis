package org.molgenis.standardsregistry.model;

import java.util.List;

/**
 * @author sido
 */
public class PackageSearchResponse
{

	private final String query;
	private final int offset;
	private final int num;
	private final int total;
	private final List<PackageResponse> packages;

	public PackageSearchResponse(String query, int offset, int num, int total, List<PackageResponse> packages)
	{
		this.query = query;
		this.offset = offset;
		this.num = num;
		this.total = total;
		this.packages = packages;
	}

	@SuppressWarnings("unused")
	public String getQuery()
	{
		return query;
	}

	@SuppressWarnings("unused")
	public int getOffset()
	{
		return offset;
	}

	@SuppressWarnings("unused")
	public int getNum()
	{
		return num;
	}

	@SuppressWarnings("unused")
	public int getTotal()
	{
		return total;
	}

	@SuppressWarnings("unused")
	public List<PackageResponse> getPackages()
	{
		return packages;
	}
}
