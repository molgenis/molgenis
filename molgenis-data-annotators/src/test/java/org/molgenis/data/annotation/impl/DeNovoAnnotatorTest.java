package org.molgenis.data.annotation.impl;

import org.molgenis.data.annotation.AbstractAnnotatorTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Created by charbonb on 04/05/15.
 */
public class DeNovoAnnotatorTest extends AbstractAnnotatorTest
{
	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		annotator = new DeNovoAnnotator(settings, null);
	}

    @Test
    public void annotateTest()
    {
        annotator.annotate(entities);
    }
}
