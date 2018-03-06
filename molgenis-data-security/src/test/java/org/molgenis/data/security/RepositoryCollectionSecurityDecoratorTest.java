package org.molgenis.data.security;

import org.mockito.Mock;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.molgenis.data.security.RepositoryPermission.*;

@ContextConfiguration(classes = { RepositoryCollectionSecurityDecoratorTest.Config.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class RepositoryCollectionSecurityDecoratorTest extends AbstractMockitoTestNGSpringContextTests
{
	private static final String USERNAME = "user";

	@Mock
	private RepositoryCollection delegateRepositoryCollection;
	@Mock
	private MutableAclService mutableAclService;
	@Mock
	private UserPermissionEvaluator userPermissionEvaluator;
	private RepositoryCollectionSecurityDecorator repositoryCollectionSecurityDecorator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		repositoryCollectionSecurityDecorator = new RepositoryCollectionSecurityDecorator(delegateRepositoryCollection,
				mutableAclService, userPermissionEvaluator);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testRepositoryCollectionSecurityDecorator()
	{
		new RepositoryCollectionSecurityDecorator(null, null, null);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testCreateRepository()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("MyEntityType").getMock();
		MutableAcl acl = mock(MutableAcl.class);
		when(mutableAclService.createAcl(new RepositoryIdentity(entityType))).thenReturn(acl);
		repositoryCollectionSecurityDecorator.createRepository(entityType);

		verify(acl).insertAce(0, new CumulativePermission().set(CREATE).set(WRITEMETA).set(WRITE).set(READ).set(COUNT),
				new PrincipalSid(USERNAME), true);
		verify(mutableAclService).updateAcl(acl);
		verify(delegateRepositoryCollection).createRepository(entityType);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testCreateRepositoryEntityTypeWithPackage()
	{
		new RuntimeException("TODO write test");
	}

	@Test
	public void testUpdateRepository()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("MyEntityType").getMock();
		EntityType updatedEntityType = mock(EntityType.class);
		when(userPermissionEvaluator.hasPermission(new RepositoryIdentity(entityType), WRITEMETA)).thenReturn(true);
		repositoryCollectionSecurityDecorator.updateRepository(entityType, updatedEntityType);
		verify(delegateRepositoryCollection).updateRepository(entityType, updatedEntityType);
	}

	@Test
	public void testUpdateRepositoryEntityTypePackageChange()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("MyEntityType").getMock();
		Package entityTypePackage = when(mock(Package.class).getId()).thenReturn("MyPackage").getMock();
		when(entityType.getPackage()).thenReturn(entityTypePackage);
		Package updatedEntityTypePackage = when(mock(Package.class).getId()).thenReturn("MyOtherPackage").getMock();
		EntityType updatedEntityType = mock(EntityType.class);
		when(updatedEntityType.getPackage()).thenReturn(updatedEntityTypePackage);
		when(userPermissionEvaluator.hasPermission(new RepositoryIdentity(entityType), WRITEMETA)).thenReturn(true);
		MutableAcl acl = mock(MutableAcl.class);
		RepositoryIdentity repositoryIdentity = new RepositoryIdentity(entityType);
		doReturn(acl).when(mutableAclService).readAclById(repositoryIdentity);
		Acl parentAcl = mock(Acl.class);
		PackageIdentity packageIdentity = new PackageIdentity(updatedEntityTypePackage);
		doReturn(parentAcl).when(mutableAclService).readAclById(packageIdentity);
		repositoryCollectionSecurityDecorator.updateRepository(entityType, updatedEntityType);

		verify(delegateRepositoryCollection).updateRepository(entityType, updatedEntityType);
		verify(acl).setParent(parentAcl);
		verify(mutableAclService).updateAcl(acl);
	}

	@Test
	public void testUpdateRepositoryEntityTypePackageRemoved()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("MyEntityType").getMock();
		Package entityTypePackage = mock(Package.class);
		when(entityType.getPackage()).thenReturn(entityTypePackage);
		EntityType updatedEntityType = mock(EntityType.class);
		when(userPermissionEvaluator.hasPermission(new RepositoryIdentity(entityType), WRITEMETA)).thenReturn(true);
		MutableAcl acl = mock(MutableAcl.class);
		RepositoryIdentity repositoryIdentity = new RepositoryIdentity(entityType);
		doReturn(acl).when(mutableAclService).readAclById(repositoryIdentity);
		repositoryCollectionSecurityDecorator.updateRepository(entityType, updatedEntityType);

		verify(delegateRepositoryCollection).updateRepository(entityType, updatedEntityType);
		verify(acl).setParent(null);
		verify(mutableAclService).updateAcl(acl);
	}

	@SuppressWarnings("deprecation")
	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void testUpdateRepositoryPermissionDenied()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("MyEntityType").getMock();
		EntityType updatedEntityType = mock(EntityType.class);
		repositoryCollectionSecurityDecorator.updateRepository(entityType, updatedEntityType);
	}

	@Test
	public void testAddAttribute()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("MyEntityType").getMock();
		Attribute attribute = mock(Attribute.class);
		when(userPermissionEvaluator.hasPermission(new RepositoryIdentity(entityType), WRITEMETA)).thenReturn(true);
		repositoryCollectionSecurityDecorator.addAttribute(entityType, attribute);
		verify(delegateRepositoryCollection).addAttribute(entityType, attribute);
	}

	@SuppressWarnings("deprecation")
	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void testAddAttributePermissionDenied()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("MyEntityType").getMock();
		Attribute attribute = mock(Attribute.class);
		repositoryCollectionSecurityDecorator.addAttribute(entityType, attribute);
	}

	@Test
	public void testUpdateAttribute()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("MyEntityType").getMock();
		Attribute attribute = mock(Attribute.class);
		Attribute updatedAttribute = mock(Attribute.class);
		when(userPermissionEvaluator.hasPermission(new RepositoryIdentity(entityType), WRITEMETA)).thenReturn(true);
		repositoryCollectionSecurityDecorator.updateAttribute(entityType, attribute, updatedAttribute);
		verify(delegateRepositoryCollection).updateAttribute(entityType, attribute, updatedAttribute);
	}

	@SuppressWarnings("deprecation")
	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void testUpdateAttributePermissionDenied()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("MyEntityType").getMock();
		Attribute attribute = mock(Attribute.class);
		Attribute updatedAttribute = mock(Attribute.class);
		repositoryCollectionSecurityDecorator.updateAttribute(entityType, attribute, updatedAttribute);
	}

	@Test
	public void testDeleteAttribute()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("MyEntityType").getMock();
		Attribute attribute = mock(Attribute.class);
		when(userPermissionEvaluator.hasPermission(new RepositoryIdentity(entityType), WRITEMETA)).thenReturn(true);
		repositoryCollectionSecurityDecorator.deleteAttribute(entityType, attribute);
		verify(delegateRepositoryCollection).deleteAttribute(entityType, attribute);
	}

	@SuppressWarnings("deprecation")
	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void testDeleteAttributePermissionDenied()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("MyEntityType").getMock();
		Attribute attribute = mock(Attribute.class);
		repositoryCollectionSecurityDecorator.deleteAttribute(entityType, attribute);
	}

	@Test
	public void testDeleteRepository()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("MyEntityType").getMock();
		when(userPermissionEvaluator.hasPermission(new RepositoryIdentity(entityType), WRITEMETA)).thenReturn(true);
		repositoryCollectionSecurityDecorator.deleteRepository(entityType);
		verify(delegateRepositoryCollection).deleteRepository(entityType);
	}

	@SuppressWarnings("deprecation")
	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void testDeleteRepositoryPermissionDenied()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("MyEntityType").getMock();
		repositoryCollectionSecurityDecorator.deleteRepository(entityType);
	}

	static class Config
	{
	}
}