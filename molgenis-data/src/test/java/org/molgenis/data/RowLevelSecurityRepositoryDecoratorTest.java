// package org.molgenis.data;
//
// import static com.google.common.collect.Lists.newArrayList;
// import static org.mockito.Matchers.any;
// import static org.mockito.Mockito.doAnswer;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.times;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.verifyNoMoreInteractions;
// import static org.mockito.Mockito.when;
// import static org.molgenis.data.RowLevelSecurityRepositoryDecorator.PERMISSIONS_ATTRIBUTE;
// import static org.molgenis.data.RowLevelSecurityRepositoryDecorator.UPDATE_ATTRIBUTE;
// import static org.testng.Assert.assertEquals;
// import static org.testng.Assert.assertNotNull;
// import static org.testng.Assert.assertTrue;
//
// import java.util.List;
// import java.util.stream.Collectors;
// import java.util.stream.Stream;
//
// import org.mockito.ArgumentCaptor;
// import org.mockito.invocation.InvocationOnMock;
// import org.mockito.stubbing.Answer;
// import org.molgenis.MolgenisFieldTypes;
// import org.molgenis.security.core.Permission;
// import org.springframework.security.authentication.TestingAuthenticationToken;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.testng.annotations.BeforeMethod;
// import org.testng.annotations.Test;
//
// public class RowLevelSecurityRepositoryDecoratorTest
// {
// private String entityName;
// private EntityMetaData entityMetaData;
// private Repository decoratedRepository;
// private AttributeMetaData updatePermissionAttribute;
// private RowLevelSecurityRepositoryDecorator repositoryDecorator;
// private RowLevelSecurityUtils permissionValidator;
// private Entity entity1;
// private Entity entity2;
// private Stream<Entity> entityStream;
// private TestingAuthenticationToken adminAuthentication;
// private TestingAuthenticationToken systemAuthentication;
// private TestingAuthenticationToken userAuthentication;
//
// private List<String> permissionOptions = newArrayList("", UPDATE_ATTRIBUTE);
//
// @SuppressWarnings("unchecked")
// @BeforeMethod
// public void beforeMethod()
// {
// entityName = "entity";
// entityMetaData = mock(EntityMetaData.class);
// when(entityMetaData.getName()).thenReturn(entityName);
//
// updatePermissionAttribute = mock(AttributeMetaData.class);
// when(updatePermissionAttribute.getName()).thenReturn(UPDATE_ATTRIBUTE);
// when(updatePermissionAttribute.getDataType()).thenReturn(MolgenisFieldTypes.STRING);
//
// AttributeMetaData idAttribute = mock(AttributeMetaData.class);
// when(idAttribute.getName()).thenReturn("id");
// when(idAttribute.getDataType()).thenReturn(MolgenisFieldTypes.STRING);
//
// AttributeMetaData valueAttribute = mock(AttributeMetaData.class);
// when(valueAttribute.getName()).thenReturn("value");
// when(valueAttribute.getDataType()).thenReturn(MolgenisFieldTypes.STRING);
//
// entityMetaData = mock(EntityMetaData.class);
// when(entityMetaData.getName()).thenReturn(entityName);
// when(entityMetaData.isRowLevelSecured()).thenReturn(true);
// when(entityMetaData.getIdAttribute()).thenReturn(idAttribute);
// when(entityMetaData.getAttribute(UPDATE_ATTRIBUTE)).thenReturn(updatePermissionAttribute);
// when(entityMetaData.getOwnAttributes())
// .thenReturn(newArrayList(idAttribute, valueAttribute, updatePermissionAttribute));
// when(entityMetaData.getAtomicAttributes())
// .thenReturn(newArrayList(idAttribute, valueAttribute, updatePermissionAttribute));
// when(entityMetaData.getOwnIdAttribute()).thenReturn(idAttribute);
// when(entityMetaData.getOwnLabelAttribute()).thenReturn(valueAttribute);
// when(entityMetaData.getOwnLookupAttributes()).thenReturn(newArrayList(valueAttribute));
//
// decoratedRepository = mock(Repository.class);
// when(decoratedRepository.getName()).thenReturn(entityName);
// when(decoratedRepository.getEntityMetaData()).thenReturn(entityMetaData);
// when(decoratedRepository.stream(any(Fetch.class))).thenReturn(entityStream);
//
// RowLevelSecurityUtils permissionValidator = mock(RowLevelSecurityUtils.class);
// repositoryDecorator = new RowLevelSecurityRepositoryDecorator(decoratedRepository, permissionValidator);
// entity1 = mock(Entity.class);
// when(entity1.getEntityMetaData()).thenReturn(entityMetaData);
// entity2 = mock(Entity.class);
// when(entity2.getEntityMetaData()).thenReturn(entityMetaData);
//
// entityStream = Stream.of(entity1, entity2);
//
// adminAuthentication = new TestingAuthenticationToken("admin", null, "ROLE_SU");
// adminAuthentication.setAuthenticated(false);
// systemAuthentication = new TestingAuthenticationToken("system", null, "ROLE_SYSTEM");
// systemAuthentication.setAuthenticated(false);
// userAuthentication = new TestingAuthenticationToken("user", null);
// userAuthentication.setAuthenticated(false);
//
// permissionValidator = mock(RowLevelSecurityUtils.class);
// when(permissionValidator.validatePermission(entity1, Permission.UPDATE, userAuthentication)).thenReturn(true);
// when(permissionValidator.validatePermission(entity2, Permission.UPDATE, userAuthentication))
// .thenThrow(MolgenisDataAccessException.class);
// when(permissionValidator.hasPermission(entity1, Permission.UPDATE, userAuthentication)).thenReturn(true);
// when(permissionValidator.hasPermission(entity2, Permission.UPDATE, userAuthentication)).thenReturn(false);
//
// when(permissionValidator.validatePermission(entity1, Permission.UPDATE, adminAuthentication)).thenReturn(true);
// when(permissionValidator.validatePermission(entity2, Permission.UPDATE, adminAuthentication)).thenReturn(true);
// when(permissionValidator.hasPermission(entity1, Permission.UPDATE, adminAuthentication)).thenReturn(true);
// when(permissionValidator.hasPermission(entity2, Permission.UPDATE, adminAuthentication)).thenReturn(true);
//
// when(permissionValidator.validatePermission(entity1, Permission.UPDATE, systemAuthentication)).thenReturn(true);
// when(permissionValidator.validatePermission(entity2, Permission.UPDATE, systemAuthentication)).thenReturn(true);
// when(permissionValidator.hasPermission(entity1, Permission.UPDATE, systemAuthentication)).thenReturn(true);
// when(permissionValidator.hasPermission(entity2, Permission.UPDATE, systemAuthentication)).thenReturn(true);
//
// repositoryDecorator = new RowLevelSecurityRepositoryDecorator(decoratedRepository, permissionValidator);
//
// // SecurityContextHolder.getContext().setAuthentication(authentication);
//
// Answer<Void> streamConsumer = new Answer<Void>()
// {
// @Override
// public Void answer(InvocationOnMock invocation) throws Throwable
// {
// Stream<Entity> entities = (Stream<Entity>) invocation.getArguments()[0];
// entities.collect(Collectors.toList());
// return null;
// }
// };
//
// doAnswer(streamConsumer).when(decoratedRepository).update(any(Stream.class));
// }
//
// @Test
// public void stream()
// {
// when(decoratedRepository.stream(any(Fetch.class))).thenReturn(entityStream);
// Stream<Entity> stream = repositoryDecorator.stream(new Fetch());
// verify(decoratedRepository, times(1)).stream(any(Fetch.class));
//
// stream.forEach((entity) -> {
// assertNotNull(entity.getEntityMetaData().getAttribute(PERMISSIONS_ATTRIBUTE));
// assertTrue(permissionOptions.contains(entity.get(PERMISSIONS_ATTRIBUTE)));
// });
// }
//
// @Test
// public void getEntityMetaDataAsSu()
// {
// TestingAuthenticationToken authentication = new TestingAuthenticationToken("admin", null, "ROLE_SU");
// authentication.setAuthenticated(false);
// SecurityContextHolder.getContext().setAuthentication(authentication);
//
// assertEquals(repositoryDecorator.getEntityMetaData().getAttribute(UPDATE_ATTRIBUTE), updatePermissionAttribute);
// }
//
// @Test
// public void getEntityMetaDataAsUser()
// {
// TestingAuthenticationToken authentication = new TestingAuthenticationToken("user", null);
// authentication.setAuthenticated(false);
// SecurityContextHolder.getContext().setAuthentication(authentication);
//
// assertEquals(repositoryDecorator.getEntityMetaData().getAttribute(UPDATE_ATTRIBUTE), null);
// }
//
// @SuppressWarnings("unchecked")
// @Test(expectedExceptions = MolgenisDataAccessException.class)
// public void updateEntityNoPermission()
// {
//
// when(permissionValidator.validatePermission(entity1, Permission.UPDATE, userAuthentication))
// .thenThrow(MolgenisDataAccessException.class);
//
// try
// {
// repositoryDecorator.update(entity1);
// }
// catch (MolgenisDataAccessException e)
// {
// verify(decoratedRepository, times(1)).getEntityMetaData();
// verifyNoMoreInteractions(decoratedRepository);
// throw e;
// }
// }
//
// @Test
// public void updateEntity()
// {
// ArgumentCaptor<Entity> argumentCaptor = ArgumentCaptor.forClass(Entity.class);
//
// repositoryDecorator.update(entity1);
// verify(decoratedRepository, times(1)).update(argumentCaptor.capture());
// assertEquals(argumentCaptor.getValue().getEntityMetaData().getAttribute(UPDATE_ATTRIBUTE),
// updatePermissionAttribute);
// }
//
// @SuppressWarnings("unchecked")
// @Test(expectedExceptions = MolgenisDataAccessException.class)
// public void updateStreamNoPermission()
// {
// try
// {
// repositoryDecorator.update(entityStream);
// }
// catch (MolgenisDataAccessException e)
// {
// verify(decoratedRepository, times(1)).getEntityMetaData();
// verify(decoratedRepository, times(1)).update(any(Stream.class));
// verifyNoMoreInteractions(decoratedRepository);
// throw e;
// }
// }
//
// @SuppressWarnings("unchecked")
// @Test
// public void updateStream()
// {
// Answer<Void> streamConsumer = new Answer<Void>()
// {
// @Override
// public Void answer(InvocationOnMock invocation) throws Throwable
// {
// Stream<Entity> entities = (Stream<Entity>) invocation.getArguments()[0];
// entities.forEach(entity -> {
// assertEquals(entity.getEntityMetaData().getAttribute(UPDATE_ATTRIBUTE), updatePermissionAttribute);
// });
// return null;
// }
// };
// doAnswer(streamConsumer).when(decoratedRepository).update(any(Stream.class));
//
// repositoryDecorator.update(Stream.of(entity1, entity1));
// verify(decoratedRepository, times(1)).getEntityMetaData();
// verify(decoratedRepository, times(1)).update(any(Stream.class));
// }
// }
