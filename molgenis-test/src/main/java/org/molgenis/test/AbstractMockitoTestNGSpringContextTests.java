package org.molgenis.test;

import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import static java.util.Objects.requireNonNull;
import static org.mockito.Mockito.mockitoSession;

public class AbstractMockitoTestNGSpringContextTests extends AbstractTestNGSpringContextTests
{
	private final Strictness strictness;

	private MockitoSession mockitoSession;

	public AbstractMockitoTestNGSpringContextTests()
	{
		this(Strictness.STRICT_STUBS);
	}

	public AbstractMockitoTestNGSpringContextTests(Strictness strictness)
	{
		this.strictness = requireNonNull(strictness);
	}

	@BeforeMethod
	public void initMocks()
	{
		mockitoSession = mockitoSession().initMocks(this).strictness(strictness).startMocking();
	}

	@AfterMethod
	public void tearDownAfterMethod()
	{
		mockitoSession.finishMocking();
	}
}
