package org.molgenis.data.annotation.test.core.entity.impl.gavin;

import org.molgenis.data.annotation.core.entity.impl.gavin.GavinAlgorithm;
import org.molgenis.data.annotation.core.entity.impl.gavin.GavinThresholds;
import org.molgenis.data.annotation.core.entity.impl.gavin.Judgment;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.molgenis.data.annotation.core.entity.impl.gavin.Category.C4;
import static org.molgenis.data.annotation.core.entity.impl.gavin.Category.I1;
import static org.molgenis.data.annotation.core.entity.impl.gavin.Judgment.Classification.*;
import static org.molgenis.data.annotation.core.entity.impl.gavin.Judgment.Method.calibrated;
import static org.molgenis.data.annotation.core.entity.impl.gavin.Judgment.Method.genomewide;
import static org.molgenis.data.annotation.core.entity.impl.snpeff.Impact.HIGH;
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
						"Unable to classify variant as benign or pathogenic. The combination of HIGH impact, a CADD score of null and MAF of 1.0E-5 in TFR2 is inconclusive."));
	}

	@Test
	public void testNullThresholdsZeroMAF()
	{
		//TODO: Is this intended behavior? Taken from GavinAlgorithmTest but defaulting to MAF threshold of 0 seems peculiar.
		assertEquals(gavinAlgorithm
						.classifyVariant(HIGH, 80.0, 1e-5, "ABCD1", GavinThresholds.create(0.0, null, null, null, null, I1)),
				Judgment.create(Benign, calibrated, "ABCD1", "Variant MAF of 1.0E-5 is greater than 0.0."));
	}

	//TODO: Add more unit tests for GavinAlgorithm, now that we easily can!

}
