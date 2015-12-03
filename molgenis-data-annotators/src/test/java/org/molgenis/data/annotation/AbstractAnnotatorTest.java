package org.molgenis.data.annotation;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

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
