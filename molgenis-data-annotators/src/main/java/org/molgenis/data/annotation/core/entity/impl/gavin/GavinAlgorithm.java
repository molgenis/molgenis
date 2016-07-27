package org.molgenis.data.annotation.core.entity.impl.gavin;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.entity.impl.gavin.Judgment.Method;
import org.molgenis.data.annotation.core.entity.impl.snpeff.Impact;

public class GavinAlgorithm
{
	public static final String PATHOMAFTHRESHOLD = "PathoMAFThreshold";
	public static final String MEANPATHOGENICCADDSCORE = "MeanPathogenicCADDScore";
	public static final String SPEC95THPERCADDTHRESHOLD = "Spec95thPerCADDThreshold";

	/**
	 * @param impact
	 * @param caddScaled
	 * @param exacMAF
	 * @param category
	 * @param gene
	 * @param annotationSourceEntity
	 * @return
	 */
	public Judgment classifyVariant(Impact impact, Double caddScaled, Double exacMAF, Category category, String gene,
			Entity annotationSourceEntity)
	{

		String pathoMAFThresholdString = annotationSourceEntity.getString(PATHOMAFTHRESHOLD);
		String meanPathogenicCADDScoreString = annotationSourceEntity.getString(MEANPATHOGENICCADDSCORE);
		String spec95thPerCADDThresholdString = annotationSourceEntity.getString(SPEC95THPERCADDTHRESHOLD);

		Double pathoMAFThreshold = pathoMAFThresholdString != null ? Double.parseDouble(pathoMAFThresholdString) : null;
		Double meanPathogenicCADDScore =
				meanPathogenicCADDScoreString != null ? Double.parseDouble(meanPathogenicCADDScoreString) : null;
		Double spec95thPerCADDThreshold =
				spec95thPerCADDThresholdString != null ? Double.parseDouble(spec95thPerCADDThresholdString) : null;

		// MAF based classification, calibrated
		if (exacMAF > pathoMAFThreshold)
		{
			return new Judgment(Judgment.Classification.Benign, Judgment.Method.calibrated,
					"Variant MAF of " + exacMAF + " is greater than the pathogenic 95th percentile MAF of "
							+ pathoMAFThreshold + ".");
		}

		String mafReason = "the variant MAF of " + exacMAF + " is lesser than the pathogenic 95th percentile MAF of "
				+ pathoMAFThreshold + ".";

		// Impact based classification, calibrated
		if (impact != null)
		{
			if (category.equals(Category.I1) && impact.equals(Impact.HIGH))
			{
				return new Judgment(Judgment.Classification.Pathognic, Judgment.Method.calibrated,
						"Variant is of high impact, while there are no known high impact variants in the population. Also, "
								+ mafReason);
			}
			else if (category.equals(Category.I2) && (impact.equals(Impact.MODERATE) || impact.equals(Impact.HIGH)))
			{
				return new Judgment(Judgment.Classification.Pathognic, Judgment.Method.calibrated,
						"Variant is of high/moderate impact, while there are no known high/moderate impact variants in the population. Also, "
								+ mafReason);
			}
			else if (category.equals(Category.I3) && (impact.equals(Impact.LOW) || impact.equals(Impact.MODERATE)
					|| impact.equals(Impact.HIGH)))
			{
				return new Judgment(Judgment.Classification.Pathognic, Judgment.Method.calibrated,
						"Variant is of high/moderate/low impact, while there are no known high/moderate/low impact variants in the population. Also, "
								+ mafReason);
			}
			else if (impact.equals(Impact.MODIFIER))
			{
				return new Judgment(Judgment.Classification.Benign, Judgment.Method.calibrated,
						"Variant is of 'modifier' impact, and therefore unlikely to be pathogenic. However, "
								+ mafReason);
			}
		}

		// CADD score based classification, calibrated
		if (caddScaled != null)
		{
			if ((category.equals(Category.C1) || category.equals(Category.C2)))
			{
				if (caddScaled > meanPathogenicCADDScore)
				{
					return new Judgment(Judgment.Classification.Pathognic, Judgment.Method.calibrated,
							"Variant CADD score of " + caddScaled + " is greater than the mean pathogenic score of "
									+ meanPathogenicCADDScore
									+ " in a gene for which CADD scores are informative. Also, " + mafReason);
				}
				else if (caddScaled < meanPathogenicCADDScore)
				{
					return new Judgment(Judgment.Classification.Benign, Judgment.Method.calibrated,
							"Variant CADD score of " + caddScaled + " is lesser than the mean population score of "
									+ meanPathogenicCADDScore
									+ " in a gene for which CADD scores are informative, although " + mafReason);
				}
			}
			else if ((category.equals(Category.C3) || category.equals(Category.C4) || category.equals(Category.C5)))
			{
				if (caddScaled > spec95thPerCADDThreshold)
				{
					return new Judgment(Judgment.Classification.Pathognic, Judgment.Method.calibrated,
							"Variant CADD score of " + caddScaled + " is greater than the 95% specificity threshold of "
									+ spec95thPerCADDThreshold + " for this gene. Also, " + mafReason);
				}
				else if (caddScaled < spec95thPerCADDThreshold)
				{
					return new Judgment(Judgment.Classification.Benign, Judgment.Method.calibrated,
							"Variant CADD score of " + caddScaled + " is lesser than the 95% sensitivity threshold of "
									+ spec95thPerCADDThreshold + " for this gene, although " + mafReason);
				}
			}
		}

		// if everything so far has failed, we can still fall back to the naive method
		return genomewideClassifyVariant(impact, caddScaled, exacMAF, gene);
	}

	/**
	 * @param impact
	 * @param caddScaled
	 * @param exacMAF
	 * @param gene
	 * @return
	 */
	public Judgment genomewideClassifyVariant(Impact impact, Double caddScaled, Double exacMAF, String gene)
	{

		exacMAF = exacMAF != null ? exacMAF : 0;

		if (exacMAF > GavinAnnotator.MAF_THRESHOLD)
		{
			return new Judgment(Judgment.Classification.Benign, Method.genomewide,
					"MAF > " + GavinAnnotator.MAF_THRESHOLD);
		}
		if (Impact.MODIFIER.equals(impact))
		{
			return new Judgment(Judgment.Classification.Benign, Method.genomewide, "Impact is MODIFIER");
		}
		else
		{
			if (caddScaled != null && caddScaled > GavinAnnotator.CADD_MAXIMUM_THRESHOLD)
			{
				return new Judgment(Judgment.Classification.Pathognic, Method.genomewide,
						"CADDscore > " + GavinAnnotator.CADD_MAXIMUM_THRESHOLD);
			}
			else if (caddScaled != null && caddScaled <= GavinAnnotator.CADD_MINIMUM_THRESHOLD)
			{
				return new Judgment(Judgment.Classification.Benign, Method.genomewide,
						"CADDscore <= " + GavinAnnotator.CADD_MINIMUM_THRESHOLD);
			}
			else
			{
				return new Judgment(Judgment.Classification.VOUS, Method.genomewide,
						"Unable to classify variant as benign or pathogenic. The combination of " + impact
								+ " impact, a CADD score " + (caddScaled != null ? caddScaled : "[missing]")
								+ " and MAF of " + exacMAF + " in " + gene + " is inconclusive.");
			}
		}
	}
}