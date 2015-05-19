package org.molgenis.data.annotation.impl.datastructures;

import java.util.List;

/**
 * Created by jvelde on 1/30/14. Defines a KEGG gene
 *
 */
public class KeggGene
{

	public KeggGene(String id, List<String> symbols, List<String> proteins)
	{
		this.id = id;
		this.symbols = symbols;
		this.proteins = proteins;
	}

	@Override
	public String toString()
	{
		return "KeggGene{" + "id='" + id + '\'' + ", symbols=" + symbols + ", proteins=" + proteins + '}';
	}

	private String id;
	private List<String> symbols;
	private List<String> proteins;

	public String getId()
	{
		return id;
	}

	public void setId(String Id)
	{
		this.id = Id;
	}

	public List<String> getSymbols()
	{
		return symbols;
	}

	public void setSymbols(List<String> symbols)
	{
		this.symbols = symbols;
	}

	public List<String> getProteins()
	{
		return proteins;
	}

	public void setProteins(List<String> proteins)
	{
		this.proteins = proteins;
	}
}
