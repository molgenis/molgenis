package org.molgenis.test.data;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityFactoryRegistry;
import org.molgenis.data.EntityReferenceCreator;
import org.molgenis.data.EntityReferenceCreatorImpl;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.util.GenericDependencyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;

import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

@ContextConfiguration(classes = { AbstractMolgenisSpringTest.Config.class })
public abstract class AbstractMolgenisSpringTest extends AbstractTestNGSpringContextTests
{
	@BeforeClass
	public void bootstrap()
	{
		// bootstrap meta data
		EntityTypeMetadata entityTypeMeta = applicationContext.getBean(EntityTypeMetadata.class);
		entityTypeMeta.setBackendEnumOptions(newArrayList("test"));
		applicationContext.getBean(AttributeMetadata.class).bootstrap(entityTypeMeta);
		Map<String, SystemEntityType> systemEntityTypeMap = applicationContext.getBeansOfType(SystemEntityType.class);
		new GenericDependencyResolver().resolve(systemEntityTypeMap.values(), SystemEntityType::getDependencies)
				.stream().forEach(systemEntityType -> systemEntityType.bootstrap(entityTypeMeta));

	}

	@Configuration
	@ComponentScan({ "org.molgenis.data.meta.model", "org.molgenis.data.system.model", "org.molgenis.data.populate",
			"org.molgenis.test.data" })
	public static class Config
	{
		@Bean
		public GenericDependencyResolver genericDependencyResolver()
		{
			return new GenericDependencyResolver();
		}

		@Bean
		public DataService dataService()
		{
			return new DataServiceImpl();
		}

		@Bean
		public EntityFactoryRegistry entityFactoryRegistry()
		{
			return new EntityFactoryRegistry();
		}

		@Bean
		public EntityReferenceCreator entityReferenceCreator()
		{
			return new EntityReferenceCreatorImpl(dataService(), entityFactoryRegistry());
		}
	}
}
