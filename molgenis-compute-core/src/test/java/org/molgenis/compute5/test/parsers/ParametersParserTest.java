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
		File f1 = new File("workflows/copyFile/constants.csv");
		File f2 = new File("workflows/copyFile/parameters.csv");
		File f3 = new File("workflows/copyFile/moreParameters.csv");
		
		Parameters parameters = ParametersCsvParser.parse(f1,f2,f3);
		
		for(Tuple t: parameters.getValues())
		{
			System.out.println(t);
		}
	}
}
