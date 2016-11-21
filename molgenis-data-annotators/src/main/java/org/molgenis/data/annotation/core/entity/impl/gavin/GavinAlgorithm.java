package org.molgenis.data.annotation.core.entity.impl.gavin;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.entity.impl.gavin.GavinEntry.Category;
import org.molgenis.data.annotation.core.entity.impl.snpeff.Impact;

import java.util.HashMap;

import static org.molgenis.data.annotation.core.entity.impl.gavin.GavinEntry.Category.*;
import static org.molgenis.data.annotation.core.entity.impl.gavin.Judgment.Classification.*;
import static org.molgenis.data.annotation.core.entity.impl.gavin.Judgment.Method.calibrated;
import static org.molgenis.data.annotation.core.entity.impl.gavin.Judgment.Method.genomewide;
import static org.molgenis.data.annotation.core.entity.impl.snpeff.Impact.*;

public class GavinAlgorithm
{
	public static final String NAME = "GavinAnnotator";
	public static final String PATHOMAFTHRESHOLD = "PathoMAFThreshold";
	public static final String MEANPATHOGENICCADDSCORE = "MeanPathogenicCADDScore";
	public static final String MEANPOPULATIONCADDSCORE = "MeanPopulationCADDScore";
	public static final String SPEC95THPERCADDTHRESHOLD = "Spec95thPerCADDThreshold";
	public static final String SENS95THPERCADDTHRESHOLD = "Sens95thPerCADDThreshold";
	private static final String CATEGORY = "Category";

	public static final double GENOMEWIDE_MAF_THRESHOLD = 0.003456145;
	public static final int GENOMEWIDE_CADD_THRESHOLD = 15;

	/**
	 * @param impact
	 * @param caddScaled
	 * @param exacMAF
	 * @param gene
	 * @param annotationSourceEntity
	 * @return
	 */
	public Judgment classifyVariant(Impact impact, Double caddScaled, Double exacMAF, String gene,
			Entity annotationSourceEntity, HashMap<String, GavinEntry> geneToEntry)
	{
		Double pathoMAFThreshold, meanPathogenicCADDScore, meanPopulationCADDScore, spec95thPerCADDThreshold, sens95thPerCADDThreshold = null;
		Category category = null;

		// sensitivity is more important than specificity, so we can adjust this parameter to globally adjust the thresholds
		// at a setting of 1, ie. default behaviour, GAVIN is 89% sensitive and 83% specific
		// at a setting of 5, a more sensitive setting, GAVIN is 92% sensitive and 78% specific
		// technically we lose more than we gain, but having >90% sensitivity is really important
		// better to have a few more false positives than to miss a true positive
		int extraSensitivityFactor = 5;

		if (geneToEntry == null)
		{
			//get data from entity for the annotator
			pathoMAFThreshold = annotationSourceEntity.getString(PATHOMAFTHRESHOLD) != null ? Double
					.valueOf(annotationSourceEntity.getString(PATHOMAFTHRESHOLD)) : null;
			if (pathoMAFThreshold != null)
			{
				pathoMAFThreshold = pathoMAFThreshold * extraSensitivityFactor * 2;
			}

			meanPathogenicCADDScore = annotationSourceEntity.getString(MEANPATHOGENICCADDSCORE) != null ? Double
					.valueOf(annotationSourceEntity.getString(MEANPATHOGENICCADDSCORE)) : null;
			if (meanPathogenicCADDScore != null)
			{
				meanPathogenicCADDScore = meanPathogenicCADDScore - extraSensitivityFactor;
			}

			meanPopulationCADDScore = annotationSourceEntity.getString(MEANPOPULATIONCADDSCORE) != null ? Double
					.valueOf(annotationSourceEntity.getString(MEANPOPULATIONCADDSCORE)) : null;
			if (meanPopulationCADDScore != null)
			{
				meanPopulationCADDScore = meanPopulationCADDScore - extraSensitivityFactor;
			}

			spec95thPerCADDThreshold = annotationSourceEntity.getString(SPEC95THPERCADDTHRESHOLD) != null ? Double
					.valueOf(annotationSourceEntity.getString(SPEC95THPERCADDTHRESHOLD)) : null;
			if (spec95thPerCADDThreshold != null)
			{
				spec95thPerCADDThreshold = spec95thPerCADDThreshold - extraSensitivityFactor;
			}

			sens95thPerCADDThreshold = annotationSourceEntity.getString(SENS95THPERCADDTHRESHOLD) != null ? Double
					.valueOf(annotationSourceEntity.getString(SENS95THPERCADDTHRESHOLD)) : null;
			if (sens95thPerCADDThreshold != null)
			{
				sens95thPerCADDThreshold = sens95thPerCADDThreshold - extraSensitivityFactor;
			}

			category = Category.valueOf(annotationSourceEntity.getString(CATEGORY));
		}
		else
		{
			//get data from map, for reuse in GAVIN-related tools other than the annotator
			if (!geneToEntry.containsKey(gene))
			{
				//if we have no data for this gene, immediately fall back to the genomewide method
				return genomewideClassifyVariant(impact, caddScaled, exacMAF, gene);
			}
			else
			{
				pathoMAFThreshold = geneToEntry.get(gene).PathoMAFThreshold != null ?
						geneToEntry.get(gene).PathoMAFThreshold * extraSensitivityFactor * 2 : null;
				meanPathogenicCADDScore = geneToEntry.get(gene).MeanPathogenicCADDScore != null ?
						geneToEntry.get(gene).MeanPathogenicCADDScore - extraSensitivityFactor : null;
				meanPopulationCADDScore = geneToEntry.get(gene).MeanPopulationCADDScore != null ?
						geneToEntry.get(gene).MeanPopulationCADDScore - extraSensitivityFactor : null;
				spec95thPerCADDThreshold = geneToEntry.get(gene).Spec95thPerCADDThreshold != null ?
						geneToEntry.get(gene).Spec95thPerCADDThreshold - extraSensitivityFactor : null;
				sens95thPerCADDThreshold = geneToEntry.get(gene).Sens95thPerCADDThreshold != null ?
						geneToEntry.get(gene).Sens95thPerCADDThreshold - extraSensitivityFactor : null;
				category = geneToEntry.get(gene).category;
			}
		}

		// CADD score based classification, calibrated
		if (caddScaled != null)
		{
			switch (category)
			{
				case C1:
				case C2:
					if (caddScaled > meanPathogenicCADDScore)
					{
						return new Judgment(Pathogenic, calibrated, gene,
								"Variant CADD score of " + caddScaled + " is greater than " + meanPathogenicCADDScore
										+ " in a gene for which CADD scores are informative.");
					}
					else if (caddScaled < meanPopulationCADDScore)
					{
						return new Judgment(Benign, calibrated, gene,
								"Variant CADD score of " + caddScaled + " is less than " + meanPopulationCADDScore
										+ " in a gene for which CADD scores are informative.");
					}
					else
					{
						//this rule does not classify apparently, just continue onto the next rules
					}
					break;
				case C3:
				case C4:
				case C5:
					if (caddScaled > spec95thPerCADDThreshold)
					{
						return new Judgment(Pathogenic, calibrated, gene,
								"Variant CADD score of " + caddScaled + " is greater than " + spec95thPerCADDThreshold
										+ " for this gene.");
					}
					else if (caddScaled < sens95thPerCADDThreshold)
					{
						return new Judgment(Benign, calibrated, gene,
								"Variant CADD score of " + caddScaled + " is less than " + sens95thPerCADDThreshold
										+ " for this gene.");
					}
					else
					{
						//this rule does not classify apparently, just continue onto the next rules
					}
					break;
			}
		}

		// MAF-based classification, calibrated
		if (pathoMAFThreshold != null && exacMAF > pathoMAFThreshold)
		{
			return new Judgment(Benign, calibrated, gene,
					"Variant MAF of " + exacMAF + " is greater than " + pathoMAFThreshold + ".");
		}

		String mafReason = "the variant MAF of " + exacMAF + " is less than a MAF of " + pathoMAFThreshold + ".";

		// Impact based classification, calibrated
		if (impact != null)
		{
			if (category == I1 && impact == HIGH)
			{
				return new Judgment(Pathogenic, calibrated, gene,
						"Variant is of high impact, while there are no known high impact variants in the population. Also, "
								+ mafReason);
			}
			else if (category == I2 && (impact == MODERATE || impact == HIGH))
			{

				return new Judgment(Pathogenic, calibrated, gene,
						"Variant is of high/moderate impact, while there are no known high/moderate impact variants in the population. Also, "
								+ mafReason);
			}
		}
		else if (category == I3 && (impact == LOW || impact == MODERATE || impact == HIGH))
		{
			return new Judgment(Pathogenic, calibrated, gene,
					"Variant is of high/moderate/low impact, while there are no known high/moderate/low impact variants in the population. Also, "
							+ mafReason);
		}
		else if (impact == MODIFIER)
		{
			return new Judgment(Benign, calibrated, gene,
					"Variant is of 'modifier' impact, and therefore unlikely to be pathogenic. However, " + mafReason);
		}

		//if everything so far has failed, we can still fall back to the genome-wide method
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

		if (exacMAF > GENOMEWIDE_MAF_THRESHOLD)
		{
			return new Judgment(Benign, genomewide, gene,
					"Variant MAF of " + exacMAF + " is not rare enough to generally be considered pathogenic.");
		}
		if (impact == MODIFIER)
		{
			return new Judgment(Benign, genomewide, gene,
					"Variant is of 'modifier' impact, and therefore unlikely to be pathogenic.");
		}
		else
		{
			if (caddScaled != null && caddScaled > GENOMEWIDE_CADD_THRESHOLD)
			{
				return new Judgment(Pathogenic, genomewide, gene, "Variant MAF of " + exacMAF
						+ " is rare enough to be potentially pathogenic and its CADD score of " + caddScaled
						+ " is greater than a global threshold of " + GENOMEWIDE_CADD_THRESHOLD + ".");
			}
			else if (caddScaled != null && caddScaled <= GENOMEWIDE_CADD_THRESHOLD)
			{
				return new Judgment(Benign, genomewide, gene,
						"Variant CADD score of " + caddScaled + " is less than a global threshold of "
								+ GENOMEWIDE_CADD_THRESHOLD + ", although the variant MAF of " + exacMAF
								+ " is rare enough to be potentially pathogenic.");
			}
			else
			{
				return new Judgment(VOUS, genomewide, gene,
						"Unable to classify variant as benign or pathogenic. The combination of " + impact
								+ " impact, a CADD score of " + caddScaled + " and MAF of " + exacMAF + " in " + gene
								+ " is inconclusive.");
			}
		}
	}
}