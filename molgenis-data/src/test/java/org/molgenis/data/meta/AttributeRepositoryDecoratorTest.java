package org.molgenis.data.meta;

import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static org.mockito.Mockito.*;

public class AttributeRepositoryDecoratorTest extends AbstractMockitoTest
{
	private AttributeRepositoryDecorator repo;
	@Mock
	private Repository<Attribute> delegateRepository;
	@Mock
	private DataService dataService;
	@Mock
	private MetaDataService metadataService;
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

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		repo = new AttributeRepositoryDecorator(delegateRepository, dataService);
	}

	@Test
	public void updateNonSystemAbstractEntity()
	{
		when(dataService.getMeta()).thenReturn(metadataService);
		when(metadataService.getConcreteChildren(abstractEntityType)).thenReturn(
				Stream.of(concreteEntityType1, concreteEntityType2));
		doReturn(backend1).when(metadataService).getBackend(concreteEntityType1);
		doReturn(backend2).when(metadataService).getBackend(concreteEntityType2);
		String attributeId = "SDFSADFSDAF";
		when(attribute.getIdentifier()).thenReturn(attributeId);

		Attribute currentAttribute = mock(Attribute.class);
		when(delegateRepository.findOneById(attributeId)).thenReturn(currentAttribute);
		when(currentAttribute.getEntity()).thenReturn(abstractEntityType);

		repo.update(attribute);

		verify(delegateRepository).update(attribute);
		verify(backend1).updateAttribute(concreteEntityType1, currentAttribute, attribute);
		verify(backend2).updateAttribute(concreteEntityType2, currentAttribute, attribute);
	}
}