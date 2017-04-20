package org.molgenis.integrationtest.platform.importservice;

import com.google.common.collect.ImmutableSet;
import org.mockito.Mockito;
import org.molgenis.auth.User;
import org.molgenis.auth.UserFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.csv.CsvDataConfig;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.importer.ImportServiceRegistrar;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.vcf.VcfDataConfig;
import org.molgenis.data.vcf.importer.VcfImporterService;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.framework.ui.MolgenisPluginRegistryImpl;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.molgenis.ontology.OntologyDataConfig;
import org.molgenis.ontology.core.config.OntologyTestConfig;
import org.molgenis.ontology.importer.OntologyImportService;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeClass;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.auth.UserMetaData.USER;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.util.EntityUtils.asStream;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { PlatformITConfig.class, ImportServiceIT.Config.class })
@TestExecutionListeners(listeners = { WithSecurityContextTestExecutionListener.class })
@Transactional
@Rollback
public abstract class ImportServiceIT extends AbstractTransactionalTestNGSpringContextTests
{
	private final static Logger LOG = LoggerFactory.getLogger(ImportServiceIT.class);

	static final String ROLE_SU = "SU";
	static final String ROLE_READ_PACKAGE = "ENTITY_READ_sys_md_Package";
	static final String ROLE_READ_ENTITY_TYPE = "ENTITY_READ_sys_md_EntityType";
	static final String ROLE_READ_ATTRIBUTE = "ENTITY_READ_sys_md_Attribute";
	static final String ROLE_READ_TAG = "ENTITY_READ_sys_md_Tag";
	static final String ROLE_READ_OWNED = "ENTITY_READ_sys_sec_Owned";
	static final String ROLE_READ_FILE_META = "ENTITY_READ_sys_FileMeta";

	@Autowired
	UserFactory userFactory;

	@Autowired
	ImportServiceFactory importServiceFactory;

	@Autowired
	FileRepositoryCollectionFactory fileRepositoryCollectionFactory;

	@Autowired
	ImportServiceRegistrar importServiceRegistrar;

	@Autowired
	DataService dataService;

	@BeforeClass
	public void beforeClass()
	{
		ContextRefreshedEvent contextRefreshedEvent = Mockito.mock(ContextRefreshedEvent.class);
		Mockito.when(contextRefreshedEvent.getApplicationContext()).thenReturn(applicationContext);
		importServiceRegistrar.register(contextRefreshedEvent);
		RunAsSystemProxy.runAsSystem(() -> dataService.add(USER, getTestUser()));
	}

	abstract User getTestUser();

	static void validateImportReport(EntityImportReport importReport, Map<String, Integer> entityTypeCountMap,
			Set<String> addedEntityTypeIds)
	{
		assertEquals(ImmutableSet.copyOf(importReport.getNewEntities()), addedEntityTypeIds);
		assertEquals(importReport.getNrImportedEntitiesMap(), entityTypeCountMap);
	}

	static File getFile(String resourceName)
	{
		requireNonNull(resourceName);

		try
		{
			File file = ResourceUtils.getFile(ImportServiceIT.class, resourceName);
			LOG.trace("emx import integration test file: [{}]", file);
			return file;
		}
		catch (Exception e)
		{
			LOG.error("File name: [{}]", resourceName);
			throw new MolgenisDataAccessException(e);
		}
	}

	void verifyFirstAndLastRows(String entityName, Map<String, Object> expectedFirstRow,
			Map<String, Object> expectedLastRow)
	{
		List<Entity> importedEntities = findAllAsList(entityName);
		assertEquals(entityToMap(importedEntities.get(0)), expectedFirstRow);
		assertEquals(entityToMap(getLast(importedEntities)), expectedLastRow);
	}

	List<Entity> findAllAsList(String entityName)
	{
		return dataService.findAll(entityName).collect(Collectors.toList());
	}

	static Map<String, Object> entityToMap(Entity entity)
	{
		Map<String, Object> entityMap = newHashMap();
		Iterable<Attribute> attributes = entity.getEntityType().getAllAttributes();

		for (Attribute attribute : attributes)
		{
			if (attribute.getDataType().equals(COMPOUND))
			{
				continue;
			}

			String attributeName = attribute.getName();
			Object value = null;
			switch (attribute.getDataType())
			{
				case CATEGORICAL:
				case FILE:
				case XREF:
					if (entity.getEntity(attributeName) != null)
					{
						value = entity.getEntity(attributeName).getIdValue();
					}
					break;
				case CATEGORICAL_MREF:
				case MREF:
				case ONE_TO_MANY:
					value = getIdsAsSet(entity.getEntities(attributeName));
					break;
				default:
					value = entity.get(attributeName);
					break;
			}
			entityMap.put(attributeName, value);
		}

		return entityMap;
	}

	static Set<Object> getIdsAsSet(Iterable<? extends Entity> entities)
	{
		return asStream(entities).map(Entity::getIdValue).collect(toSet());
	}

	@Import(value = { VcfDataConfig.class, VcfImporterService.class, VcfAttributes.class, OntologyDataConfig.class,
			OntologyTestConfig.class, OntologyImportService.class, MolgenisPluginRegistryImpl.class,
			CsvDataConfig.class })
	static class Config
	{

	}
}
