package org.molgenis.data.annotation;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Created by charbonb on 30/04/15.
 */
public abstract class AbstractAnnotatorTest extends AnnotatorTestData {

    @Test
    public void canAnnotateTrueTest()
    {
        assertEquals(annotator.canAnnotate(metaDataCanAnnotate), "true");
    }

    @Test
    public void canAnnotateFalseTest()
    {
        assertEquals(annotator.canAnnotate(metaDataCantAnnotate), "a required attribute has the wrong datatype");
    }
}
