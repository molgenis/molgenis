package org.molgenis.charts.data;

import org.molgenis.charts.MolgenisSerieType;

/**
 * Molgenis serie
 */
public class MolgenisSerie
{
	private String name = "";
	private MolgenisSerieType type;

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the type
	 */
	public MolgenisSerieType getType()
	{
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(MolgenisSerieType type)
	{
		this.type = type;
	}
}
