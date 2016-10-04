package org.molgenis.pathways.model;

/**
 * Impact of a variant in a VCF file.
 */
public enum Impact
{
	NONE("219AD7"), LOW("FFFF00"), MODERATE("FFA500"), HIGH("FF0000");

	private final String color;

	Impact(String color)
	{
		this.color = color;
	}

	public String getColor()
	{
		return color;
	}
}