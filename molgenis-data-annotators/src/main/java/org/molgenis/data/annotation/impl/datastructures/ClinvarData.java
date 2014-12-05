package org.molgenis.data.annotation.impl.datastructures;

public class ClinvarData
{
	String alleleid;
	String type;
	String gene_name;
	String geneid;
	String genesymbol;
	String clinicalsignificance;
	String rs_dbsnp;
	String nsv_dbvar;
	String rcvaccession;
	String testedingtr;
	String phenotypeids;
	String origin;
	String assembly;
	String clinvar_chromosome;
	String start;
	String stop;
	String cytogenetic;
	String reviewstatus;
	String hgvs_c;
	String hgvs_p;
	String numbersubmitters;
	String lastevaluated;
	String guidelines;
	String otherids;
	String variantids;

	public ClinvarData(String alleleid, String type, String gene_name, String geneid, String genesymbol,
			String clinicalsignificance, String rs_dbsnp, String nsv_dbvar, String rcvaccession, String testedingtr,
			String phenotypeids, String origin, String assembly, String clinvar_chromosome, String start, String stop,
			String cytogenetic, String reviewstatus, String hgvs_c, String hgvs_p, String numbersubmitters,
			String lastevaluated, String guidelines, String otherids, String variantids)
	{
		this.alleleid = alleleid;
		this.type = type;
		this.gene_name = gene_name;
		this.geneid = geneid;
		this.genesymbol = genesymbol;
		this.clinicalsignificance = clinicalsignificance;
		this.rs_dbsnp = rs_dbsnp;
		this.nsv_dbvar = nsv_dbvar;
		this.rcvaccession = rcvaccession;
		this.testedingtr = testedingtr;
		this.phenotypeids = phenotypeids;
		this.origin = origin;
		this.assembly = assembly;
		this.clinvar_chromosome = clinvar_chromosome;
		this.start = start;
		this.stop = stop;
		this.cytogenetic = cytogenetic;
		this.reviewstatus = reviewstatus;
		this.hgvs_c = hgvs_c;
		this.hgvs_p = hgvs_p;
		this.numbersubmitters = numbersubmitters;
		this.lastevaluated = lastevaluated;
		this.guidelines = guidelines;
		this.otherids = otherids;
		this.variantids = variantids;
	}

	public String getAlleleid()
	{
		return alleleid;
	}

	public void setAlleleid(String alleleid)
	{
		this.alleleid = alleleid;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getGene_name()
	{
		return gene_name;
	}

	public void setGene_name(String gene_name)
	{
		this.gene_name = gene_name;
	}

	public String getGeneid()
	{
		return geneid;
	}

	public void setGeneid(String geneid)
	{
		this.geneid = geneid;
	}

	public String getGenesymbol()
	{
		return genesymbol;
	}

	public void setGenesymbol(String genesymbol)
	{
		this.genesymbol = genesymbol;
	}

	public String getClinicalsignificance()
	{
		return clinicalsignificance;
	}

	public void setClinicalsignificance(String clinicalsignificance)
	{
		this.clinicalsignificance = clinicalsignificance;
	}

	public String getRs_dbsnp()
	{
		return rs_dbsnp;
	}

	public void setRs_dbsnp(String rs_dbsnp)
	{
		this.rs_dbsnp = rs_dbsnp;
	}

	public String getNsv_dbvar()
	{
		return nsv_dbvar;
	}

	public void setNsv_dbvar(String nsv_dbvar)
	{
		this.nsv_dbvar = nsv_dbvar;
	}

	public String getRcvaccession()
	{
		return rcvaccession;
	}

	public void setRcvaccession(String rcvaccession)
	{
		this.rcvaccession = rcvaccession;
	}

	public String getTestedingtr()
	{
		return testedingtr;
	}

	public void setTestedingtr(String testedingtr)
	{
		this.testedingtr = testedingtr;
	}

	public String getPhenotypeids()
	{
		return phenotypeids;
	}

	public void setPhenotypeids(String phenotypeids)
	{
		this.phenotypeids = phenotypeids;
	}

	public String getOrigin()
	{
		return origin;
	}

	public void setOrigin(String origin)
	{
		this.origin = origin;
	}

	public String getAssembly()
	{
		return assembly;
	}

	public void setAssembly(String assembly)
	{
		this.assembly = assembly;
	}

	public String getClinvar_chromosome()
	{
		return clinvar_chromosome;
	}

	public void setClinvar_chromosome(String clinvar_chromosome)
	{
		this.clinvar_chromosome = clinvar_chromosome;
	}

	public String getStart()
	{
		return start;
	}

	public void setStart(String start)
	{
		this.start = start;
	}

	public String getStop()
	{
		return stop;
	}

	public void setStop(String stop)
	{
		this.stop = stop;
	}

	public String getCytogenetic()
	{
		return cytogenetic;
	}

	public void setCytogenetic(String cytogenetic)
	{
		this.cytogenetic = cytogenetic;
	}

	public String getReviewstatus()
	{
		return reviewstatus;
	}

	public void setReviewstatus(String reviewstatus)
	{
		this.reviewstatus = reviewstatus;
	}

	public String getHgvs_c()
	{
		return hgvs_c;
	}

	public void setHgvs_c(String hgvs_c)
	{
		this.hgvs_c = hgvs_c;
	}

	public String getHgvs_p()
	{
		return hgvs_p;
	}

	public void setHgvs_p(String hgvs_p)
	{
		this.hgvs_p = hgvs_p;
	}

	public String getNumbersubmitters()
	{
		return numbersubmitters;
	}

	public void setNumbersubmitters(String numbersubmitters)
	{
		this.numbersubmitters = numbersubmitters;
	}

	public String getLastevaluated()
	{
		return lastevaluated;
	}

	public void setLastevaluated(String lastevaluated)
	{
		this.lastevaluated = lastevaluated;
	}

	public String getGuidelines()
	{
		return guidelines;
	}

	public void setGuidelines(String guidelines)
	{
		this.guidelines = guidelines;
	}

	public String getOtherids()
	{
		return otherids;
	}

	public void setOtherids(String otherids)
	{
		this.otherids = otherids;
	}

	public String getVariantids()
	{
		return variantids;
	}

	public void setVariantids(String variantids)
	{
		this.variantids = variantids;
	}
}
