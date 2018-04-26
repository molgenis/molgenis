package org.molgenis.data.annotation.test.core.entity.impl.gavin;

import org.molgenis.data.annotation.core.entity.impl.gavin.Category;
import org.molgenis.data.annotation.core.entity.impl.gavin.GavinAlgorithm;
import org.molgenis.data.annotation.core.entity.impl.gavin.GavinThresholds;
import org.molgenis.data.annotation.core.entity.impl.gavin.Judgment;
import org.molgenis.data.annotation.core.entity.impl.snpeff.Impact;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.molgenis.data.annotation.core.entity.impl.gavin.Category.*;
import static org.molgenis.data.annotation.core.entity.impl.gavin.Judgment.Classification.*;
import static org.molgenis.data.annotation.core.entity.impl.gavin.Judgment.Method.calibrated;
import static org.molgenis.data.annotation.core.entity.impl.gavin.Judgment.Method.genomewide;
import static org.molgenis.data.annotation.core.entity.impl.snpeff.Impact.*;
import static org.testng.Assert.assertEquals;

@Test
public class GavinAlgorithmTest
{
	private GavinAlgorithm gavinAlgorithm;
	private GavinThresholds tfr2Thresholds;

	@BeforeClass
	public void beforeClass()
	{
		gavinAlgorithm = new GavinAlgorithm();
		tfr2Thresholds = GavinThresholds.create(1.9269599999999953E-4, 26.79, 19.8, 35.35, 18.33, C4);
	}

	@DataProvider(name = "classifyVariant")
	public Object[][] classifyVariant()
	{
		return new Object[][] {
				{ MODERATE, 33.0, 0.0, "MTOR", 0.0, 31.04, 22.49, 34.45, 26.64, C1, Pathogenic, calibrated,
						"Variant CADD score of 33.0 is greater than 26.04 in a gene for which CADD scores are informative." },
				{ LOW, 8.526, 0.008575, "NPHP4", 1.3726799999999996E-4, 33.98, 23.27, 36.00, 26.16, C1, Benign,
						calibrated,
						"Variant CADD score of 8.526 is less than 18.27 in a gene for which CADD scores are informative." },
				{ HIGH, 31.0, null, "DVL1", 0.0, 32.60, 24.30, 24.30, 29.44, C3, Pathogenic, calibrated,
						"Variant CADD score of 31.0 is greater than 19.3 for this gene." },
				{ LOW, 0.015, 3.206E-4, "SKI", 0.0, 19.13, 18.45, 27.04, 8.63, C4, Benign, calibrated,
						"Variant CADD score of 0.015 is less than 3.630000000000001 for this gene." },
				{ LOW, 0.009, 0.873, "AGRN", 0.03674999999999995, 21.4, 17.86, 32.0, 0.13, C4, Benign, calibrated,
						"Variant MAF of 0.873 is greater than 0.3674999999999995." },
				{ LOW, 0.009, 0.873, "AGRN", 0.03674999999999995, null, null, null, null, C4, Benign, calibrated,
						"Variant MAF of 0.873 is greater than 0.3674999999999995." },
				{ HIGH, 35.0, 0.0, "SF3B4", 0.0, null, null, null, null, I1, Pathogenic, calibrated,
						"Variant is of high impact, while there are no known high impact variants in the population. Also, the variant MAF of 0.0 is less than a MAF of 0.0." },
				{ MODERATE, 26.3, 0.0, "MECOM", 0.0, null, null, null, null, I2, Pathogenic, calibrated,
						"Variant is of high/moderate impact, while there are no known high/moderate impact variants in the population. Also, the variant MAF of 0.0 is less than a MAF of 0.0." },
				{ MODIFIER, 5.766, 0.0, "PARK7", 1.5815999999999996E-4, 19.68, 23.34, 33.6, 5.77, C4, Benign,
						calibrated,
						"Variant is of 'modifier' impact, and therefore unlikely to be pathogenic. However, the variant MAF of 0.0 is less than a MAF of 0.0015815999999999996." },
				{ MODERATE, 15.67, 1.654E-4, "PRDM16", 0.0028759599999999995, 23.14, 19.57, 30.99, 10.46, C4,
						Pathogenic, genomewide,
						"Variant MAF of 1.654E-4 is rare enough to be potentially pathogenic and its CADD score of 15.67 is greater than a global threshold of 15." } };
	}

	@Test(dataProvider = "classifyVariant")
	public void testClassifyVariant(Impact impact, Double caddScaled, Double exacMAF, String gene,
			Double pathoMAFThreshold, Double meanPathogenicCADDScore, Double meanPopulationCADDScore,
			Double spec95thPerCADDThreshold, Double sens95thPerCADDThreshold, Category category,
			Judgment.Classification classification, Judgment.Method method, String reason)
	{
		assertEquals(gavinAlgorithm.classifyVariant(impact, caddScaled, exacMAF, gene,
				GavinThresholds.create(pathoMAFThreshold, meanPathogenicCADDScore, meanPopulationCADDScore,
						spec95thPerCADDThreshold, sens95thPerCADDThreshold, category)),
				Judgment.create(classification, method, gene, reason));
	}

	@DataProvider(name = "genomeWideClassifyVariant")
	public Object[][] genomeWideClassifyVariant()
	{
		return new Object[][] { { MODERATE, 21.3, 0.024, "SAMD11", Benign, genomewide,
				"Variant MAF of 0.024 is not rare enough to generally be considered pathogenic." },
				{ MODIFIER, 2.561, 0.0, "BMP8A", Benign, genomewide,
						"Variant is of 'modifier' impact, and therefore unlikely to be pathogenic." },
				{ MODERATE, 15.67, 1.654E-4, "PRDM16", Pathogenic, genomewide,
						"Variant MAF of 1.654E-4 is rare enough to be potentially pathogenic and its CADD score of 15.67 is greater than a global threshold of 15." },
				{ MODERATE, 0.007, 0.001858, "SCNN1D", Benign, genomewide,
						"Variant CADD score of 0.007 is less than a global threshold of 15, although the variant MAF of 0.001858 is rare enough to be potentially pathogenic." },
				{ MODERATE, 1.626, 0.0, "PRAMEF5", Benign, genomewide,
						"Variant CADD score of 1.626 is less than a global threshold of 15, although the variant MAF of 0.0 is rare enough to be potentially pathogenic." } };
	}

	@Test(dataProvider = "genomeWideClassifyVariant")
	public void testGenomeWideClassifyVariant(Impact impact, Double caddScaled, Double exacMAF, String gene,
			Judgment.Classification classification, Judgment.Method method, String reason)
	{
		assertEquals(gavinAlgorithm.genomewideClassifyVariant(impact, caddScaled, exacMAF, gene),
				Judgment.create(classification, method, gene, reason));
	}

	@Test
	public void testHighCaddPathogenic()
	{
		assertEquals(gavinAlgorithm.classifyVariant(HIGH, 80.0, 1e-5, "TFR2", tfr2Thresholds),
				Judgment.create(Pathogenic, calibrated, "TFR2",
						"Variant CADD score of 80.0 is greater than 30.35 for this gene."));
	}

	@Test
	public void testLowVariantCaddBenign()
	{
		assertEquals(gavinAlgorithm.classifyVariant(HIGH, 6.0, 1e-5, "TFR2", tfr2Thresholds),
				Judgment.create(Benign, calibrated, "TFR2",
						"Variant CADD score of 6.0 is less than 13.329999999999998 for this gene."));
	}

	@Test
	public void testNoCaddVOUS()
	{
		assertEquals(gavinAlgorithm.classifyVariant(HIGH, null, 1e-5, "TFR2", tfr2Thresholds),
				Judgment.create(VOUS, genomewide, "TFR2",
						"Unable to classify variant as benign or pathogenic. The combination of HIGH impact, an unknown CADD score and MAF of 1.0E-5 in TFR2 is inconclusive."));
	}

	@Test
	public void testNullThresholdsZeroMAF()
	{
		//TODO: Is this intended behavior? Taken from GavinAlgorithmTest but defaulting to MAF threshold of 0 seems peculiar.
		assertEquals(gavinAlgorithm.classifyVariant(HIGH, 80.0, 1e-5, "ABCD1",
				GavinThresholds.create(0.0, null, null, null, null, I1)),
				Judgment.create(Benign, calibrated, "ABCD1", "Variant MAF of 1.0E-5 is greater than 0.0."));
	}
}
