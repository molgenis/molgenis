package org.molgenis.vkgl.api;

public class VkglQuery
{
	private VkglCoordinateQuery[] coordinate;
	private VkglAlleleQuery[] allele;
	private String queryStatement; 
	
	public VkglCoordinateQuery[] getCoordinate()
	{
		return coordinate;
	}
	public void setCoordinate(VkglCoordinateQuery[] coordinate)
	{
		this.coordinate = coordinate;
	}
	public VkglAlleleQuery[] getAllele()
	{
		return allele;
	}
	public void setAllele(VkglAlleleQuery[] allele)
	{
		this.allele = allele;
	}
	public String getQueryStatement()
	{
		return queryStatement;
	}
	public void setQueryStatement(String queryStatement)
	{
		this.queryStatement = queryStatement;
	}

}
