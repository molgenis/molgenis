package org.molgenis.data.annotation.impl.datastructures;

public class HpoData
{
	String diseaseId;
	String geneSymbol;
	String geneEntrezId;
	String hpoId;
	String hpoTerm;

	public HpoData(String diseaseId, String geneSymbol, String geneEntrezId, String hpoId, String hpoTerm)
	{
		super();
		this.diseaseId = diseaseId;
		this.geneSymbol = geneSymbol;
		this.geneEntrezId = geneEntrezId;
		this.hpoId = hpoId;
		this.hpoTerm = hpoTerm;
	}

	public String getDiseaseId()
	{
		return diseaseId;
	}

	public void setDiseaseId(String diseaseId)
	{
		this.diseaseId = diseaseId;
	}

	public String getGeneSymbol()
	{
		return geneSymbol;
	}

	public void setGeneSymbol(String geneSymbol)
	{
		this.geneSymbol = geneSymbol;
	}

	public String getGeneEntrezId()
	{
		return geneEntrezId;
	}

	public void setGeneEntrezId(String geneEntrezId)
	{
		this.geneEntrezId = geneEntrezId;
	}

	public String getHpoId()
	{
		return hpoId;
	}

	public void setHpoId(String hpoId)
	{
		this.hpoId = hpoId;
	}

	public String getHpoTerm()
	{
		return hpoTerm;
	}

	public void setHpoTerm(String hpoTerm)
	{
		this.hpoTerm = hpoTerm;
	}

}
