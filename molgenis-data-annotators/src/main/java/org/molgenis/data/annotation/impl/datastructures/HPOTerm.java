package org.molgenis.data.annotation.impl.datastructures;

/**
 * Created by jvelde on 2/12/14.
 */
public class HPOTerm
{

	// OMIM:614887 PEX14 5195 HP:0002240 Hepatomegaly

	String id;
	String description;
	String diseaseDb;
	Integer diseaseDbEntry;
	String geneName;
	Integer geneEntrezID;

	public HPOTerm(String id, String description, String diseaseDb, Integer diseaseDbEntry, String geneName,
			Integer geneEntrezID)
	{
		this.id = id;
		this.description = description;
		this.diseaseDb = diseaseDb;
		this.diseaseDbEntry = diseaseDbEntry;
		this.geneName = geneName;
		this.geneEntrezID = geneEntrezID;
	}

	@Override
	public String toString()
	{
		return "HPOTerm{" + "id='" + id + '\'' + ", description='" + description + '\'' + ", diseaseDb='" + diseaseDb
				+ '\'' + ", diseaseDbEntry=" + diseaseDbEntry + ", geneName='" + geneName + '\'' + ", geneEntrezID="
				+ geneEntrezID + '}';
	}

	public String getId()
	{
		return id;
	}

	public String getDescription()
	{
		return description;
	}

	public String getDiseaseDb()
	{
		return diseaseDb;
	}

	public Integer getDiseaseDbEntry()
	{
		return diseaseDbEntry;
	}

	public String getGeneName()
	{
		return geneName;
	}

	public Integer getGeneEntrezID()
	{
		return geneEntrezID;
	}
}
