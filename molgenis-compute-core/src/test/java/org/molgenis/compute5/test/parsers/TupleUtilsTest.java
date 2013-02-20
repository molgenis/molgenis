package org.molgenis.compute5.test.parsers;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.molgenis.compute5.generators.TupleUtils;
import org.molgenis.compute5.model.Parameters;
import org.molgenis.compute5.parsers.ParametersCsvParser;
import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.WritableTuple;
import org.testng.annotations.Test;


public class TupleUtilsTest
{
	@Test
	public void test() throws IOException
	{
		Parameters p = ParametersCsvParser.parse(new File("src/main/resources/workflows/ngs/settings.csv"), new File("src/main/resources/workflows/ngs/parameters.csv"));

		System.out.println("orginal:\n" + print(p.getValues()));

		List<WritableTuple> collapsed = TupleUtils.collapse(p.getValues(), Arrays.asList(new String[]
		{ "user.bwaVersion" }));

		System.out.println("collapsed:\n" + print(collapsed));
		
		List<WritableTuple> uncollapsed = TupleUtils.uncollapse(collapsed, Parameters.ID_COLUMN);
		
		System.out.println("uncollapsed:\n" + print(uncollapsed));
	}

	private String print(List<? extends Tuple> collapsed)
	{
		String result = "";
		for (Tuple t : collapsed)
			result += t + "\n";
		return result;
	}
}
