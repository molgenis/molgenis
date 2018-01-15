package org.molgenis.data;

import org.mockito.Mock;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;
import org.molgenis.data.config.MetadataTestConfig;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.util.GenericDependencyResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.mockito.Mockito.mockitoSession;
import static org.mockito.Mockito.reset;
import static org.mockito.MockitoAnnotations.initMocks;

@ContextConfiguration(classes = { AbstractMolgenisSpringTest.Config.class })
@TestExecutionListeners(listeners = { WithSecurityContextTestExecutionListener.class })
public abstract class AbstractMolgenisSpringTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private Config config;

	private final Strictness strictness;

	private MockitoSession mockitoSession;

	public AbstractMolgenisSpringTest()
	{
		this(Strictness.STRICT_STUBS);
	}

	public AbstractMolgenisSpringTest(Strictness strictness)
	{
		this.strictness = requireNonNull(strictness);
	}

	// long method name, because if a method annotated with @BeforeClass and the same method name exists in a subclass then this method is ignored.
	@BeforeClass
	public void abstractMolgenisSpringTestBeforeClass()
	{
		// bootstrap meta data
		EntityTypeMetadata entityTypeMeta = applicationContext.getBean(EntityTypeMetadata.class);
		entityTypeMeta.setBackendEnumOptions(newArrayList("test"));
		applicationContext.getBean(AttributeMetadata.class).bootstrap(entityTypeMeta);
		Map<String, SystemEntityType> systemEntityTypeMap = applicationContext.getBeansOfType(SystemEntityType.class);
		new GenericDependencyResolver().resolve(systemEntityTypeMap.values(), SystemEntityType::getDependencies)
									   .forEach(systemEntityType -> systemEntityType.bootstrap(entityTypeMeta));
	}

	// long method name, because if a method annotated with @BeforeMethod and the same method name exists in a subclass then this method is ignored.
	@BeforeMethod
	public void abstractMolgenisSpringTestBeforeMethod() throws Exception
	{
		mockitoSession = mockitoSession().initMocks(this).strictness(strictness).startMocking();
		config.resetMocks();
	}

	@AfterMethod
	public void abstractMolgenisSpringTestAfterMethod()
	{
		mockitoSession.finishMocking();
	}

	@Configuration
	@Import(MetadataTestConfig.class)
	public static class Config
	{
		@Mock
		private DataService dataService;

		public Config()
		{
			initMocks(this);
		}

		public void resetMocks()
		{
			reset(dataService);
		}

		@Bean
		public DataService dataService()
		{
			return dataService;
		}
	}
}
