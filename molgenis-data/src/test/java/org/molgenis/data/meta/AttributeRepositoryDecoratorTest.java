package org.molgenis.data.meta;

import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.AttributeMetadata.CHILDREN;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ATTRIBUTES;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;

public class AttributeRepositoryDecoratorTest extends AbstractMockitoTest
{
	private AttributeRepositoryDecorator repo;
	@Mock
	private Repository<Attribute> decoratedRepo;
	@Mock
	private DataService dataService;
	@Mock
	private MetaDataService metadataService;
	@Mock
	private SystemEntityTypeRegistry systemEntityTypeRegistry;
	@Mock
	private Attribute attribute;
	@Mock
	private EntityType abstractEntityType;
	@Mock
	private EntityType concreteEntityType1;
	@Mock
	private EntityType concreteEntityType2;
	@Mock
	private RepositoryCollection backend1;
	@Mock
	private RepositoryCollection backend2;
	private String attributeId = "SDFSADFSDAF";

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		when(attribute.getEntity()).thenReturn(abstractEntityType);
		when(attribute.getName()).thenReturn("attributeName");
		when(dataService.getMeta()).thenReturn(metadataService);
		when(metadataService.getConcreteChildren(abstractEntityType)).thenReturn(
				Stream.of(concreteEntityType1, concreteEntityType2));
		when(metadataService.getBackend(concreteEntityType1)).thenReturn(backend1);
		when(metadataService.getBackend(concreteEntityType2)).thenReturn(backend2);
		when(attribute.getIdentifier()).thenReturn(attributeId);
		repo = new AttributeRepositoryDecorator(decoratedRepo, dataService);
	}

	@Test
	public void delete()
	{
		String attrName = "attrName";
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn(attrName).getMock();
		String attrIdentifier = "id";
		when(attr.getIdentifier()).thenReturn(attrIdentifier);
		when(systemEntityTypeRegistry.hasSystemAttribute(attrIdentifier)).thenReturn(false);

		@SuppressWarnings("unchecked")
		Query<EntityType> entityQ = mock(Query.class);
		when(entityQ.eq(ATTRIBUTES, attr)).thenReturn(entityQ);
		when(entityQ.findOne()).thenReturn(null);
		when(dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(entityQ);

		@SuppressWarnings("unchecked")
		Query<Attribute> attrQ = mock(Query.class);
		when(dataService.query(ATTRIBUTE_META_DATA, Attribute.class)).thenReturn(attrQ);
		when(attrQ.eq(CHILDREN, attr)).thenReturn(attrQ);
		when(attrQ.findOne()).thenReturn(null);

		repo.delete(attr);

		verify(decoratedRepo).delete(attr);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void deleteCompoundAttribute()
	{
		// Compound parent attribute
		Attribute compound = when(mock(Attribute.class).getName()).thenReturn("compound").getMock();
		when(compound.getDataType()).thenReturn(AttributeType.COMPOUND);

		// Child
		Attribute child = when(mock(Attribute.class).getName()).thenReturn("child").getMock();
		when(compound.getChildren()).thenReturn(newArrayList(child));
		when(child.getParent()).thenReturn(mock(Attribute.class));
		MetaDataService mds = mock(MetaDataService.class);
		when(dataService.getMeta()).thenReturn(mds);
		when(mds.getRepository(AttributeMetadata.ATTRIBUTE_META_DATA)).thenReturn(mock(Repository.class));

		repo.delete(compound);

		//Test
		verify(child).setParent(null);
		verify(decoratedRepo).delete(compound);
	}

	@Test
	public void deleteStream()
	{
		AttributeRepositoryDecorator repoSpy = spy(repo);
		doNothing().when(repoSpy).delete(any(Attribute.class));
		Attribute attr0 = mock(Attribute.class);
		Attribute attr1 = mock(Attribute.class);
		repoSpy.delete(Stream.of(attr0, attr1));
		verify(repoSpy).delete(attr0);
		verify(repoSpy).delete(attr1);
	}

	@Test
	public void updateNonSystemAbstractEntity()
	{
		Attribute currentAttribute = mock(Attribute.class);
		when(systemEntityTypeRegistry.getSystemAttribute(attributeId)).thenReturn(null);
		when(decoratedRepo.findOneById(attributeId)).thenReturn(currentAttribute);
		when(currentAttribute.getEntity()).thenReturn(abstractEntityType);

		repo.update(attribute);

		verify(decoratedRepo).update(attribute);
		verify(backend1).updateAttribute(concreteEntityType1, currentAttribute, attribute);
		verify(backend2).updateAttribute(concreteEntityType2, currentAttribute, attribute);
	}
}