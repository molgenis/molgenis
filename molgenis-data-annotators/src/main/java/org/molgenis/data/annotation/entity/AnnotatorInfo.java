package org.molgenis.data.annotation.entity;

import java.util.List;

import org.molgenis.data.AttributeMetaData;

import com.google.auto.value.AutoValue;

/**
 * Informatin about an {@link Annotator}
 * 
 * @author fkelpin
 *
 */
@AutoValue
public abstract class AnnotatorInfo
{
	public static enum Status
	{
		READY, BETA, INDEV, UNKNOWN
	}

	public static enum Type
	{
		EFFECT_PREDICTION, AUTOMATED_PROTOCOL, PATHOGENICITY_ESTIMATE, POPULATION_REFERENCE, PHENOTYPE_ASSOCIATION, GENOMIC_FEATURE, UNUSED, UNKNOWN
	}

	public abstract Status getStatus();

	public abstract Type getType();

	public abstract String getCode();

	public abstract String getDescription();

	public abstract List<AttributeMetaData> getOutputAttributes();

	public static AnnotatorInfo create(Status status, Type type, String code, String description, List<AttributeMetaData> outputAttributes)
	{
		return new AutoValue_AnnotatorInfo(status, type, code, description, outputAttributes);
	}
}

/**
 * Effect predictions & gene names: snpeff Automated protocols: monogenic denovo Pathogenicity estimates & calibration:
 * cadd dann fitcon ccgg Population references: gonl 1kg exac vkgl Phenotype associations: cgd hpo omim phewascatalog
 * gwascatalog clinvar phenomizer Genomic features: ase proteinatlas vistaenhancers splicingcode Unused: kegg cosmic
 * dbnsfp
 * 
 * Bron heeft Chrom Pos en evt Ref Alt
 * 
 * Wordt altijd weer 1 rij
 * 
 * Tabix metadata kan in principe uit de file komen, zie VCFRepository
 * 
 * CADD: Query op Chrom/Pos, Post-Process de resultaten m.b.v. Ref/Alts, Dan moet het er 0 of 1 zijn.
 * 
 * 1000G: Query op Chrom/Pos, Parse de INFO kolom, Post-Process de resultaten m.b.v. Ref/Alts. Dan moet het er 0 of 1
 * zijn.
 * 
 * GoNL : Query op Chrom/Pos, Parse de INFO kolom, Post-Process de resultaten m.b.v. Ref/Alts. Dan moet het er 0 of 1
 * zijn.
 * 
 * ClinVar: Query op Chrom/Pos, Parse de INFO kolom, Post-Process de resultaten m.b.v. Ref/Alts. Dan moet het er 0 of 1
 * zijn.
 * 
 * ExAC: Query op Chrom/Pos, Parse de INFO kolom, Post-Process de resultaten m.b.v. Ref/Alts (doet iets met MAF). Dan
 * moet het er 0 of 1 zijn.
 * 
 */
