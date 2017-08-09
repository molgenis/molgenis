package org.molgenis.data.security.meta;

import org.mockito.Mock;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.PermissionService;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.testng.Assert.assertEquals;

public class AttributeRepositorySecurityDecoratorTest extends AbstractMockitoTest
{
	@Mock
	private Repository<Attribute> delegateRepository;
	@Mock
	private PermissionService permissionService;

	private AttributeRepositorySecurityDecorator attributeRepositorySecurityDecorator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		attributeRepositorySecurityDecorator = new AttributeRepositorySecurityDecorator(delegateRepository,
				permissionService);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testAttributeRepositorySecurityDecorator()
	{
		new AttributeRepositorySecurityDecorator(null, null);
	}

	@Test
	public void testIterator()
	{
		Attribute attribute0 = createAttributeWithPermission("id0", Permission.READ);
		Attribute attribute1 = createAttributeWithPermission("id1", Permission.WRITE);
		when(delegateRepository.iterator()).thenReturn(asList(attribute0, attribute1).iterator());

		assertEquals(newArrayList(attributeRepositorySecurityDecorator.iterator()), asList(attribute0, attribute1));
		verify(attribute0).setReadOnly(true);
		verify(attribute1, never()).setReadOnly(anyBoolean());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testForEachBatched()
	{
		Attribute attribute0 = createAttributeWithPermission("id0", Permission.READ);
		Attribute attribute1 = createAttributeWithPermission("id1", Permission.WRITE);
		Fetch fetch = mock(Fetch.class);

		int batchSize = 100;
		doAnswer(invocation ->
		{
			Consumer<List<Attribute>> delegateConsumer = invocation.getArgument(1);
			delegateConsumer.accept(asList(attribute0, attribute1));
			return null;
		}).when(delegateRepository).forEachBatched(eq(fetch), any(Consumer.class), eq(batchSize));

		Consumer<List<Attribute>> consumer = mock(Consumer.class);
		attributeRepositorySecurityDecorator.forEachBatched(fetch, consumer, batchSize);
		verify(consumer).accept(asList(attribute0, attribute1));
		verify(attribute0).setReadOnly(true);
		verify(attribute1, never()).setReadOnly(anyBoolean());
	}

	@Test
	public void testFindAllQuery()
	{
		Attribute attribute0 = createAttributeWithPermission("id0", Permission.READ);
		Attribute attribute1 = createAttributeWithPermission("id1", Permission.WRITE);
		@SuppressWarnings("unchecked")
		Query<Attribute> query = mock(Query.class);
		when(delegateRepository.findAll(query)).thenReturn(Stream.of(attribute0, attribute1));

		assertEquals(attributeRepositorySecurityDecorator.findAll(query).collect(toList()),
				asList(attribute0, attribute1));
		verify(attribute0).setReadOnly(true);
		verify(attribute1, never()).setReadOnly(anyBoolean());
	}

	@Test
	public void testFindOneQueryPermissionRead()
	{
		Attribute attribute = createAttributeWithPermission("id", Permission.READ);
		@SuppressWarnings("unchecked")
		Query<Attribute> query = mock(Query.class);
		when(delegateRepository.findOne(query)).thenReturn(attribute);

		assertEquals(attributeRepositorySecurityDecorator.findOne(query), attribute);
		verify(attribute).setReadOnly(true);
	}

	@Test
	public void testFindOneQueryPermissionWrite()
	{
		Attribute attribute = createAttributeWithPermission("id", Permission.WRITE);
		@SuppressWarnings("unchecked")
		Query<Attribute> query = mock(Query.class);
		when(delegateRepository.findOne(query)).thenReturn(attribute);

		assertEquals(attributeRepositorySecurityDecorator.findOne(query), attribute);
		verify(attribute, never()).setReadOnly(anyBoolean());
	}

	@Test
	public void testFindOneByIdObjectPermissionRead()
	{
		String attributeId = "id";
		Attribute attribute = createAttributeWithPermission(attributeId, Permission.READ);
		when(delegateRepository.findOneById(attributeId)).thenReturn(attribute);

		assertEquals(attributeRepositorySecurityDecorator.findOneById(attributeId), attribute);
		verify(attribute).setReadOnly(true);
	}

	@Test
	public void testFindOneByIdObjectPermissionWrite()
	{
		String attributeId = "id";
		Attribute attribute = createAttributeWithPermission(attributeId, Permission.WRITE);
		when(delegateRepository.findOneById(attributeId)).thenReturn(attribute);

		assertEquals(attributeRepositorySecurityDecorator.findOneById(attributeId), attribute);
		verify(attribute, never()).setReadOnly(anyBoolean());
	}

	@Test
	public void testFindOneByIdObjectFetchPermissionRead()
	{
		String attributeId = "id";
		Attribute attribute = createAttributeWithPermission(attributeId, Permission.READ);
		Fetch fetch = mock(Fetch.class);
		when(delegateRepository.findOneById(attributeId, fetch)).thenReturn(attribute);

		assertEquals(attributeRepositorySecurityDecorator.findOneById(attributeId, fetch), attribute);
		verify(attribute).setReadOnly(true);
	}

	@Test
	public void testFindOneByIdObjectFetchPermissionWrite()
	{
		String attributeId = "id";
		Attribute attribute = createAttributeWithPermission(attributeId, Permission.WRITE);
		Fetch fetch = mock(Fetch.class);
		when(delegateRepository.findOneById(attributeId, fetch)).thenReturn(attribute);

		assertEquals(attributeRepositorySecurityDecorator.findOneById(attributeId, fetch), attribute);
		verify(attribute, never()).setReadOnly(anyBoolean());
	}

	@Test
	public void testFindAllStream()
	{
		String attributeId0 = "id0";
		Attribute attribute0 = createAttributeWithPermission(attributeId0, Permission.READ);
		String attributeId1 = "id1";
		Attribute attribute1 = createAttributeWithPermission(attributeId1, Permission.WRITE);
		Stream<Object> attributeIdStream = Stream.of(attribute0, attribute1);
		when(delegateRepository.findAll(attributeIdStream)).thenReturn(Stream.of(attribute0, attribute1));

		assertEquals(attributeRepositorySecurityDecorator.findAll(attributeIdStream).collect(toList()),
				asList(attribute0, attribute1));
		verify(attribute0).setReadOnly(true);
		verify(attribute1, never()).setReadOnly(anyBoolean());
	}

	@Test
	public void testFindAllStreamFetch()
	{
		String attributeId0 = "id0";
		Attribute attribute0 = createAttributeWithPermission(attributeId0, Permission.READ);
		String attributeId1 = "id1";
		Attribute attribute1 = createAttributeWithPermission(attributeId1, Permission.WRITE);
		Fetch fetch = mock(Fetch.class);
		Stream<Object> attributeIdStream = Stream.of(attribute0, attribute1);
		when(delegateRepository.findAll(attributeIdStream, fetch)).thenReturn(Stream.of(attribute0, attribute1));

		assertEquals(attributeRepositorySecurityDecorator.findAll(attributeIdStream, fetch).collect(toList()),
				asList(attribute0, attribute1));
		verify(attribute0).setReadOnly(true);
		verify(attribute1, never()).setReadOnly(anyBoolean());
	}

	private Attribute createAttributeWithPermission(String attributeId, Permission permission)
	{
		Attribute attribute = when(mock(Attribute.class).getIdentifier()).thenReturn(attributeId).getMock();
		when(permissionService.hasPermissionOnEntity(ATTRIBUTE_META_DATA, attributeId, permission)).thenReturn(true);
		return attribute;
	}
}