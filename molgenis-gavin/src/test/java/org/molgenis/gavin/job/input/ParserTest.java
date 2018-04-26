package org.molgenis.gavin.job.input;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.molgenis.gavin.job.input.model.CaddVariant;
import org.molgenis.gavin.job.input.model.LineType;
import org.molgenis.gavin.job.input.model.Variant;
import org.molgenis.gavin.job.input.model.VcfVariant;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.molgenis.gavin.job.input.model.LineType.*;

public class ParserTest extends AbstractMockitoTest
{
	private Parser lineParser = new Parser();
	@Mock
	private LineSink output;
	@Mock
	private LineSink error;

	@DataProvider(name = "chrom")
	public Object[][] createParseChromTestcases()
	{
		return new Object[][] { { "X", "X" }, { "chr1", "1" }, { "2", "2" }, { "3", "3" }, { "4", "4" }, { "5", "5" },
				{ "6", "6" }, { "7", "7" }, { "8", "8" }, { "9", "9" }, { "10", "10" }, { "11", "11" }, { "12", "12" },
				{ "13", "13" }, { "14", "14" }, { "15", "15" }, { "16", "16" }, { "17", "17" }, { "18", "18" },
				{ "19", "19" }, { "20", "20" }, { "21", "21" }, { "22", "22" }, { "chr22", "22" }, { "CHR-X", null },
				{ "chrx", "X" }, { "MT", null }, { "0", null }, { "01", null }, { "23", null }, { "chrY", "Y" } };
	}

	@Test(dataProvider = "chrom")
	public void testParseChrom(String input, String expected)
	{
		Assert.assertEquals(lineParser.parseChrom(input), expected);
	}

	@DataProvider(name = "line")
	public Object[][] createParseVariantTestcases()
	{
		return new Object[][] { { "X\t123\tA\tC", null },
				{ "X\t123\t.\tA\tC", VcfVariant.create("X", 123, ".", "A", "C") },
				{ "20\t14370\trs6054257\tG\tA\t29\tPASS\tNS=3;DP=14;AF=0.5;DB;H2\tGT:GQ:DP:HQ",
						VcfVariant.create("20", 14370, "rs6054257", "G", "A") },
				{ "20\t14370\t.\tG\tA\t29\tPASS\tNS=3;DP=14;AF=0.5;DB;H2\tGT:GQ:DP:HQ",
						VcfVariant.create("20", 14370, ".", "G", "A") }, { "", null },
				{ "X\t123\tA\tC\t213.23\tblah", null }, { "X\t123\tA\tC\t213.23", null },
				{ " X \t 123 \t rsblah \t A \t C \t 213.23", VcfVariant.create("X", 123, "rsblah", "A", "C") },
				{ "20\t14370\tG\t.\t2.2\t3.234", CaddVariant.create("20", 14370, "G", ".", 2.2, 3.234) },
				{ "X\t123\t\tA\tC\t213.23", VcfVariant.create("X", 123, ".", "A", "C") } };
	}

	@Test(dataProvider = "line")
	public void testParseVariant(String input, Variant expected)
	{
		Assert.assertEquals(lineParser.tryParseVariant(input), expected);
	}

	@Test
	public void testTransform()
	{
		List<String> lines = Arrays.asList("#Comment line", "#Another comment line",
				"11\t47359281\t.\tC\tG\t.\t.\tCADD_SCALED=33.0", "11\t47359281\tC\tCC\t2.3\t33.0",
				"11\t47359281\t.\tC\tCG\t.\t.\tCADD_SCALED=33.0", "11\t47359281\t.\tCG\tC\t.\t.\tCADD_SCALED=33.0");

		Multiset<LineType> expected = ImmutableMultiset.of(COMMENT, COMMENT, VCF, CADD, INDEL_NOCADD, INDEL_NOCADD);
		Assert.assertEquals(lineParser.transformLines(lines.stream(), output, error), expected);

		verify(output).accept("##fileformat=VCFv4.0");
		verify(output).accept("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO");
		verify(output).accept("11\t47359281\t.\tC\tG\t.\t.\t.");
		verify(output).accept("11\t47359281\t.\tC\tCC\t.\t.\tCADD=2.3;CADD_SCALED=33.0");
		verify(error).accept("Line 5:\t11\t47359281\t.\tC\tCG\t.\t.\tCADD_SCALED=33.0");
		verify(error).accept("Line 6:\t11\t47359281\t.\tCG\tC\t.\t.\tCADD_SCALED=33.0");
	}

	@Test
	public void testTransformSkipsLinesWhenMaxLinesProcessed()
	{
		Assert.assertEquals(lineParser.transformLine("", 120000, 100000, output, error), LineType.SKIPPED);
		Mockito.verifyZeroInteractions(output, error);
	}

}
