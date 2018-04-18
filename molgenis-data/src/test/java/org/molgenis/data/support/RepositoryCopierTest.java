package org.molgenis.data.support;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.assertEquals;

public class RepositoryCopierTest extends AbstractMockitoTest
{
	private RepositoryCopier repositoryCopier;
	@Mock
	private MetaDataService metaDataService;
	@Mock
	private AttributeFactory attributeFactory;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		repositoryCopier = new RepositoryCopier(metaDataService, attributeFactory);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testRepositoryCopier()
	{
		new RepositoryCopier(null, null);
	}

	@Test
	public void testCopyRepository()
	{
		Repository<Entity> repository = getMockRepository();
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		@SuppressWarnings("unchecked")
		Stream<Entity> entitiesStream = mock(Stream.class);
		when(query.findAll()).thenReturn(entitiesStream);
		when(repository.query()).thenReturn(query);
		String entityTypeId = "copiedEntityTypeId";
		Package package_ = mock(Package.class);
		when(package_.toString()).thenReturn("Package");
		String entityTypeLabel = "copiedEntityTypeLabel";

		@SuppressWarnings("unchecked")
		Repository<Entity> copiedRepository = mock(Repository.class);
		when(metaDataService.createRepository(any(EntityType.class))).thenReturn(copiedRepository);

		assertEquals(repositoryCopier.copyRepository(repository, entityTypeId, package_, entityTypeLabel),
				copiedRepository);

		ArgumentCaptor<EntityType> entityTypeCaptor = ArgumentCaptor.forClass(EntityType.class);
		verify(metaDataService).createRepository(entityTypeCaptor.capture());
		EntityType copiedEntityType = entityTypeCaptor.getValue();
		assertEquals(copiedEntityType.getId(), entityTypeId);
		assertEquals(copiedEntityType.getLabel(), entityTypeLabel);
		assertEquals(copiedEntityType.getPackage(), package_);

		verify(copiedRepository).add(entitiesStream);
	}

	private Repository<Entity> getMockRepository()
	{
		EntityType entityTypeMeta = createEntityTypeMeta();
		EntityType entityType = mock(EntityType.class);
		when(entityType.getEntityType()).thenReturn(entityTypeMeta);
		when(entityType.getOwnAllAttributes()).thenReturn(emptyList());
		when(entityType.getTags()).thenReturn(emptyList());

		@SuppressWarnings("unchecked")
		Repository<Entity> repository = mock(Repository.class);
		when(repository.getName()).thenReturn("Repository");
		when(repository.getEntityType()).thenReturn(entityType);
		return repository;
	}

	private static EntityType createEntityTypeMeta()
	{
		EntityType entityTypeMeta = mock(EntityType.class);
		Attribute strAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
		Attribute intAttr = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
		Attribute boolAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
		Attribute xrefAttr = when(mock(Attribute.class).getDataType()).thenReturn(XREF).getMock();
		Attribute mrefAttr = when(mock(Attribute.class).getDataType()).thenReturn(MREF).getMock();
		doReturn(strAttr).when(entityTypeMeta).getAttribute(EntityTypeMetadata.ID);
		doReturn(xrefAttr).when(entityTypeMeta).getAttribute(EntityTypeMetadata.PACKAGE);
		doReturn(strAttr).when(entityTypeMeta).getAttribute(EntityTypeMetadata.LABEL);
		doReturn(mrefAttr).when(entityTypeMeta).getAttribute(EntityTypeMetadata.ATTRIBUTES);
		doReturn(boolAttr).when(entityTypeMeta).getAttribute(EntityTypeMetadata.IS_ABSTRACT);
		doReturn(mrefAttr).when(entityTypeMeta).getAttribute(EntityTypeMetadata.TAGS);
		doReturn(intAttr).when(entityTypeMeta).getAttribute(EntityTypeMetadata.INDEXING_DEPTH);
		return entityTypeMeta;
	}
}