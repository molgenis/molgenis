package org.molgenis.data.matrix.impl;

import org.molgenis.util.ResourceUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class MatrixMapperImplTest {
    private MatrixMapperImpl matrixMapper;

    @BeforeTest
    public void setup() {
        matrixMapper = new MatrixMapperImpl(ResourceUtils.getFile(getClass(), "/mapping.txt"));
    }

    @Test
    public void testMapping() {
        assertEquals(matrixMapper.map("mapping1"), "matrix1");
        assertEquals(matrixMapper.map("mapping4"), "matrix4");
        assertNull(matrixMapper.map("foo"));
    }
}
