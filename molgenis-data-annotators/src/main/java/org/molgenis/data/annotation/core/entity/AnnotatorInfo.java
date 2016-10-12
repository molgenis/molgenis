package org.molgenis.data.annotation.core.entity;

import com.google.auto.value.AutoValue;
import org.molgenis.data.meta.model.Attribute;

import java.util.List;

/**
 * Informatin about an {@link Annotator}
 *
 * @author fkelpin
 */
@AutoValue
public abstract class AnnotatorInfo
{
	public enum Status
	{
		READY, BETA, INDEV, UNKNOWN
	}

	public enum Type
	{
		EFFECT_PREDICTION, AUTOMATED_PROTOCOL, PATHOGENICITY_ESTIMATE, POPULATION_REFERENCE, PHENOTYPE_ASSOCIATION, GENOMIC_FEATURE, UNUSED, UNKNOWN
	}

	public abstract Status getStatus();

	public abstract Type getType();

	public abstract String getCode();

	public abstract String getDescription();

	public abstract List<Attribute> getOutputAttributes();

	public static AnnotatorInfo create(Status status, Type type, String code, String description,
			List<Attribute> outputAttributes)
	{
		return new AutoValue_AnnotatorInfo(status, type, code, description, outputAttributes);
	}
}

/**
 * Effect predictions & gene names: snpeff Automated protocols: monogenic denovo Pathogenicity estimates & calibration:
 * cadd dann fitcon ccgg Population references: gonl 1kg exac vkgl Phenotype associations: cgd hpo omim phewascatalog
 * gwascatalog clinvar phenomizer Genomic features: ase proteinatlas vistaenhancers splicingcode Unused: kegg cosmic
 * dbnsfp
 * <p>
 * Bron heeft Chrom Pos en evt Ref Alt
 * <p>
 * Wordt altijd weer 1 rij
 * <p>
 * Tabix metadata kan in principe uit de file komen, zie VCFRepository
 * <p>
 * CADD: Query op Chrom/Pos, Post-Process de resultaten m.b.v. Ref/Alts, Dan moet het er 0 of 1 zijn.
 * <p>
 * 1000G: Query op Chrom/Pos, Parse de INFO kolom, Post-Process de resultaten m.b.v. Ref/Alts. Dan moet het er 0 of 1
 * zijn.
 * <p>
 * GoNL : Query op Chrom/Pos, Parse de INFO kolom, Post-Process de resultaten m.b.v. Ref/Alts. Dan moet het er 0 of 1
 * zijn.
 * <p>
 * ClinVar: Query op Chrom/Pos, Parse de INFO kolom, Post-Process de resultaten m.b.v. Ref/Alts. Dan moet het er 0 of 1
 * zijn.
 * <p>
 * ExAC: Query op Chrom/Pos, Parse de INFO kolom, Post-Process de resultaten m.b.v. Ref/Alts (doet iets met MAF). Dan
 * moet het er 0 of 1 zijn.
 */
