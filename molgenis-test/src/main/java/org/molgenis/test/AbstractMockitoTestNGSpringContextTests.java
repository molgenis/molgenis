package org.molgenis.test;

import org.mockito.MockitoAnnotations;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;

public class AbstractMockitoTestNGSpringContextTests extends AbstractTestNGSpringContextTests
{
	@BeforeMethod
	public void initMocks()
	{
		MockitoAnnotations.initMocks(this);
	}
}
