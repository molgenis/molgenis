package org.molgenis.data.annotation.cmd;

public interface AnnotatorInfo
{
	public enum status {READY, BETA, INDEV, UNKNOWN}
	public enum type {EFFECT_PREDICTION, AUTOMATED_PROTOCOL, PATHOGENICITY_ESTIMATE, POPULATION_REFERENCE, PHENOTYPE_ASSOCIATION, GENOMIC_FEATURE, UNUSED, UNKNOWN}
	String code = null; //e.g. 'cgd', 'gonl', 'cadd'

	public status getStatus();
	public type getType();
	public String getCode();
}



/**

Effect predictions & gene names:
	snpeff
Automated protocols:
	monogenic
	denovo
Pathogenicity estimates & calibration:
	cadd
	dann
	fitcon
	ccgg
Population references:
	gonl
	1kg
	exac
	vkgl
Phenotype associations:
	cgd
	hpo
	omim
	phewascatalog
	gwascatalog
	clinvar
	phenomizer
Genomic features:
	ase
	proteinatlas
	vistaenhancers
	splicingcode
Unused:
	kegg
	cosmic
	dbnsfp
	

*/