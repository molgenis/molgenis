package org.molgenis.data.annotation.impl.datastructures;

import org.molgenis.data.annotation.provider.CgdDataProvider.generalizedInheritance;

public class CgdData
{
	String references;
	String intervention_rationale;
	String comments;
	String intervention_categories;
	String manifestation_categories;
	String allelic_conditions;
	String age_group;
	String inheritance;
	String condition;
	String entrez_gene_id;
	String hgnc_id;
	private generalizedInheritance generalizedInheritance; // used for gene inheritance matching!

	public CgdData(String hgnc_id, String entrez_gene_id, String condition, String inheritance, String age_group,
			String allelic_conditions, String manifestation_categories, String intervention_categories,
			String comments, String intervention_rationale, String references)
	{
		this.references = references;
		this.intervention_rationale = intervention_rationale;
		this.comments = comments;
		this.intervention_categories = intervention_categories;
		this.manifestation_categories = manifestation_categories;
		this.allelic_conditions = allelic_conditions;
		this.age_group = age_group;
		this.inheritance = inheritance;
		this.condition = condition;
		this.entrez_gene_id = entrez_gene_id;
		this.hgnc_id = hgnc_id;
	}

	public generalizedInheritance getGeneralizedInheritance()
	{
		return generalizedInheritance;
	}

	public void setGeneralizedInheritance(generalizedInheritance generalizedInheritance)
	{
		this.generalizedInheritance = generalizedInheritance;
	}

	public String getReferences()
	{
		return references;
	}

	public void setReferences(String references)
	{
		this.references = references;
	}

	public String getIntervention_rationale()
	{
		return intervention_rationale;
	}

	public void setIntervention_rationale(String intervention_rationale)
	{
		this.intervention_rationale = intervention_rationale;
	}

	public String getComments()
	{
		return comments;
	}

	public void setComments(String comments)
	{
		this.comments = comments;
	}

	public String getIntervention_categories()
	{
		return intervention_categories;
	}

	public void setIntervention_categories(String intervention_categories)
	{
		this.intervention_categories = intervention_categories;
	}

	public String getManifestation_categories()
	{
		return manifestation_categories;
	}

	public void setManifestation_categories(String manifestation_categories)
	{
		this.manifestation_categories = manifestation_categories;
	}

	public String getAllelic_conditions()
	{
		return allelic_conditions;
	}

	public void setAllelic_conditions(String allelic_conditions)
	{
		this.allelic_conditions = allelic_conditions;
	}

	public String getAge_group()
	{
		return age_group;
	}

	public void setAge_group(String age_group)
	{
		this.age_group = age_group;
	}

	public String getInheritance()
	{
		return inheritance;
	}

	public void setInheritance(String inheritance)
	{
		this.inheritance = inheritance;
	}

	public String getCondition()
	{
		return condition;
	}

	public void setCondition(String condition)
	{
		this.condition = condition;
	}

	public String getEntrez_gene_id()
	{
		return entrez_gene_id;
	}

	public void setEntrez_gene_id(String entrez_gene_id)
	{
		this.entrez_gene_id = entrez_gene_id;
	}

	public String getHgnc_id()
	{
		return hgnc_id;
	}

	public void setHgnc_id(String hgnc_id)
	{
		this.hgnc_id = hgnc_id;
	}
}
