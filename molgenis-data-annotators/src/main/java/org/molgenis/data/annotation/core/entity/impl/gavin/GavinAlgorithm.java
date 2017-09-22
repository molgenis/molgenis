package org.molgenis.data.annotation.core.entity.impl.gavin;

import org.molgenis.data.annotation.core.entity.impl.snpeff.Impact;

import static org.molgenis.data.annotation.core.entity.impl.gavin.Category.*;
import static org.molgenis.data.annotation.core.entity.impl.gavin.Judgment.Classification.*;
import static org.molgenis.data.annotation.core.entity.impl.gavin.Judgment.Method.calibrated;
import static org.molgenis.data.annotation.core.entity.impl.gavin.Judgment.Method.genomewide;
import static org.molgenis.data.annotation.core.entity.impl.snpeff.Impact.*;

public class GavinAlgorithm
{
	public static final String NAME = "GavinAnnotator";

	public static final double GENOMEWIDE_MAF_THRESHOLD = 0.003456145;
	public static final int GENOMEWIDE_CADD_THRESHOLD = 15;
	/**
	 * Sensitivity is more important than specificity, so we can adjust this parameter to globally adjust the thresholds.
	 * <ul>
	 * <li>At a setting of 1, ie. default behaviour, GAVIN is 89% sensitive and 83% specific</li>
	 * <li>at a setting of 5, a more sensitive setting, GAVIN is 92% sensitive and 78% specific</li>
	 * </ul>
	 * Technically we lose more than we gain, but having >90% sensitivity is really important.
	 * Better to have a few more false positives than to miss a true positive.
	 */
	public static final int EXTRA_SENSITIVITY_FACTOR = 5;

	/**
	 * Classifies a variant using the given GavinThresholds.
	 *
	 * @param impact          putative impact of the variant, determined by snpEff
	 * @param caddScaled      scaled cadd score for the variant
	 * @param exacMAF         maf of the variant, determined by exac
	 * @param gene            gene of the variant
	 * @param gavinThresholds the {@link GavinThresholds} for this gene
	 * @return the {@link Judgment} of the gavin algorithm
	 */
	public Judgment classifyVariant(Impact impact, Double caddScaled, Double exacMAF, String gene,
			GavinThresholds gavinThresholds)
	{
		if (gavinThresholds == null)
		{
			//if we have no data for this gene, immediately fall back to the genomewide method
			return genomewideClassifyVariant(impact, caddScaled, exacMAF, gene);
		}

		gavinThresholds = gavinThresholds.withExtraSensitivity(EXTRA_SENSITIVITY_FACTOR);

		// CADD score based classification, calibrated
		if (caddScaled != null)
		{
			switch (gavinThresholds.getCategory())
			{
				case C1:
				case C2:
					if (gavinThresholds.isAboveMeanPathogenicCADDScore(caddScaled))
					{
						return Judgment.create(Pathogenic, calibrated, gene,
								"Variant CADD score of " + caddScaled + " is greater than "
										+ gavinThresholds.getMeanPathogenicCADDScore()
										+ " in a gene for which CADD scores are informative.");
					}
					if (gavinThresholds.isBelowMeanPopulationCADDScore(caddScaled))
					{
						return Judgment.create(Benign, calibrated, gene,
								"Variant CADD score of " + caddScaled + " is less than "
										+ gavinThresholds.getMeanPopulationCADDScore()
										+ " in a gene for which CADD scores are informative.");
					}
					//this rule does not classify apparently, just continue onto the next rules
					break;
				case C3:
				case C4:
				case C5:
					if (gavinThresholds.isAboveSpec95thPerCADDThreshold(caddScaled))
					{
						return Judgment.create(Pathogenic, calibrated, gene,
								"Variant CADD score of " + caddScaled + " is greater than "
										+ gavinThresholds.getSpec95thPerCADDThreshold() + " for this gene.");
					}
					if (gavinThresholds.isBelowSens95PerCADDThreshold(caddScaled))
					{
						return Judgment.create(Benign, calibrated, gene,
								"Variant CADD score of " + caddScaled + " is less than "
										+ gavinThresholds.getSens95thPerCADDThreshold() + " for this gene.");
					}
					//this rule does not classify apparently, just continue onto the next rules
					break;
			}
		}

		// MAF-based classification, calibrated
		if (gavinThresholds.isAbovePathoMAFThreshold(exacMAF))

		{
			return Judgment.create(Benign, calibrated, gene,
					"Variant MAF of " + exacMAF + " is greater than " + gavinThresholds.getPathoMAFThreshold() + ".");
		}

		String mafReason =
				"the variant MAF of " + exacMAF + " is less than a MAF of " + gavinThresholds.getPathoMAFThreshold()
						+ ".";

		// Impact based classification, calibrated
		if (impact != null)

		{
			if (gavinThresholds.getCategory() == I1 && impact == HIGH)
			{
				return Judgment.create(Pathogenic, calibrated, gene,
						"Variant is of high impact, while there are no known high impact variants in the population. Also, "
								+ mafReason);
			}
			if (gavinThresholds.getCategory() == I2 && (impact == MODERATE || impact == HIGH))
			{
				return Judgment.create(Pathogenic, calibrated, gene,
						"Variant is of high/moderate impact, while there are no known high/moderate impact variants in the population. Also, "
								+ mafReason);
			}
			if (gavinThresholds.getCategory() == I3 && (impact == LOW || impact == MODERATE || impact == HIGH))
			{
				return Judgment.create(Pathogenic, calibrated, gene,
						"Variant is of high/moderate/low impact, while there are no known high/moderate/low impact variants in the population. Also, "
								+ mafReason);
			}
			if (impact == MODIFIER)
			{
				return Judgment.create(Benign, calibrated, gene,
						"Variant is of 'modifier' impact, and therefore unlikely to be pathogenic. However, "
								+ mafReason);
			}
		}

		//if everything so far has failed, we can still fall back to the genome-wide method
		return genomewideClassifyVariant(impact, caddScaled, exacMAF, gene);
	}

	/**
	 * Classifies a variant based on genome-wide thresholds
	 *
	 * @param impact     putative impact of the variant, determined by snpEff
	 * @param caddScaled scaled cadd score for the variant
	 * @param exacMAF    maf of the variant, determined by exac
	 * @param gene       gene of the variant
	 * @return the {@link Judgment} of the gavin algorithm
	 */

	public Judgment genomewideClassifyVariant(Impact impact, Double caddScaled, Double exacMAF, String gene)
	{
		exacMAF = exacMAF != null ? exacMAF : 0;

		if (exacMAF > GENOMEWIDE_MAF_THRESHOLD)
		{
			return Judgment.create(Benign, genomewide, gene,
					"Variant MAF of " + exacMAF + " is not rare enough to generally be considered pathogenic.");
		}
		if (impact == MODIFIER)
		{
			return Judgment.create(Benign, genomewide, gene,
					"Variant is of 'modifier' impact, and therefore unlikely to be pathogenic.");
		}

		if (caddScaled != null)
		{
			if (caddScaled > GENOMEWIDE_CADD_THRESHOLD)
			{
				return Judgment.create(Pathogenic, genomewide, gene, "Variant MAF of " + exacMAF
						+ " is rare enough to be potentially pathogenic and its CADD score of " + caddScaled
						+ " is greater than a global threshold of " + GENOMEWIDE_CADD_THRESHOLD + ".");
			}
			else
			{
				return Judgment.create(Benign, genomewide, gene,
						"Variant CADD score of " + caddScaled + " is less than a global threshold of "
								+ GENOMEWIDE_CADD_THRESHOLD + ", although the variant MAF of " + exacMAF
								+ " is rare enough to be potentially pathogenic.");
			}
		}
		else
		{
			return Judgment.create(VOUS, genomewide, gene,
					"Unable to classify variant as benign or pathogenic. The combination of " + impact
							+ " impact, an unknown CADD score and MAF of " + exacMAF + " in " + gene
							+ " is inconclusive.");
		}
	}
}