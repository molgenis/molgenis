package org.molgenis.omx.genomebrowser;

public class GenomeBrowserPluginModel
{
	private String startPosition = "9600000";
	private String endPosition = "9700000";
	private String chromosome = "chrI";
	
	private final DallianceDataSource source1 = new DallianceDataSource("Genome","http://genome.ucsc.edu/cgi-bin/das/ce10/","dna",true,true,true,true);
	private final DallianceDataSource source2 = new DallianceDataSource("age2_qtl","http://localhost:8900/das/age2_qtl");


	public DallianceDataSource getSource()
	{
		System.out.println("getSource()");
		return source1;
	}



	public String getStartPosition()
	{
		return startPosition;
	}



	public void setStartPosition(String startPosition)
	{
		this.startPosition = startPosition;
	}



	public String getEndPosition()
	{
		return endPosition;
	}



	public void setEndPosition(String endPosition)
	{
		this.endPosition = endPosition;
	}



	public String getChromosome()
	{
		return chromosome;
	}



	public void setChromosome(String chromosome)
	{
		this.chromosome = chromosome;
	}
	
}
