package org.molgenis.compute5.test.parsers;

import java.io.File;
import java.io.IOException;

import org.molgenis.compute5.model.Parameters;
import org.molgenis.compute5.parsers.ParametersCsvParser;
import org.molgenis.util.tuple.Tuple;
import org.testng.annotations.Test;


public class ParametersParserTest {

	@Test
	public void test() throws IOException
	{
		File f = new File("src/main/resources/workflows/includeParams/parameters.csv");
		
		Parameters parameters = ParametersCsvParser.parse(f);
		
		for(Tuple t: parameters.getValues())
		{
			System.out.println(t);
		}
	}
}
