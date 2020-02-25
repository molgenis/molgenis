package org.molgenis.data.security.auth;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.security.SessionSecurityContextUpdater;
import org.molgenis.test.AbstractMockitoTest;

class RoleMembershipDecoratorTest extends AbstractMockitoTest {
  @Mock private Repository<RoleMembership> delegateRepository;
  @Mock private RoleMembershipValidator roleMembershipValidator;
  @Mock private SessionSecurityContextUpdater sessionSecurityContextUpdater;
  private RoleMembershipDecorator roleMembershipDecorator;

  @BeforeEach
  void setUpBeforeMethod() {
    roleMembershipDecorator =
        new RoleMembershipDecorator(
            delegateRepository, roleMembershipValidator, sessionSecurityContextUpdater);
  }

  @Test
  void testAdd() {
    User user = mock(User.class);
    RoleMembership roleMembership = mock(RoleMembership.class);
    when(roleMembership.getUser()).thenReturn(user);

    roleMembershipDecorator.add(roleMembership);
    verify(roleMembershipValidator).validate(roleMembership);
    verify(sessionSecurityContextUpdater).resetAuthorities(user);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testAddStream() {
    User user = mock(User.class);
    RoleMembership roleMembership = mock(RoleMembership.class);
    when(roleMembership.getUser()).thenReturn(user);

    when(delegateRepository.add(any(Stream.class)))
        .thenAnswer(
            invocation ->
                Long.valueOf(((Stream<RoleMembership>) invocation.getArgument(0)).count())
                    .intValue());

    roleMembershipDecorator.add(Stream.of(roleMembership));
    verify(roleMembershipValidator).validate(roleMembership);
    verify(sessionSecurityContextUpdater).resetAuthorities(user);
  }

  @Test
  void testUpdate() {
    User currentUser = mock(User.class);

    RoleMembership currentRoleMembership = mock(RoleMembership.class);
    when(currentRoleMembership.getUser()).thenReturn(currentUser);

    User updatedUser = mock(User.class);

    String roleMembershipId = "MyRoleMemberShipId";
    RoleMembership updatedRoleMembership = mock(RoleMembership.class);
    when(updatedRoleMembership.getId()).thenReturn(roleMembershipId);
    when(updatedRoleMembership.getUser()).thenReturn(updatedUser);

    when(delegateRepository.findOneById(roleMembershipId)).thenReturn(currentRoleMembership);
    roleMembershipDecorator.update(updatedRoleMembership);
    verify(roleMembershipValidator).validate(updatedRoleMembership);
    verify(sessionSecurityContextUpdater).resetAuthorities(currentUser);
    verify(sessionSecurityContextUpdater).resetAuthorities(updatedUser);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testUpdateStream() {
    User currentUser = mock(User.class);

    RoleMembership currentRoleMembership = mock(RoleMembership.class);
    when(currentRoleMembership.getUser()).thenReturn(currentUser);

    User updatedUser = mock(User.class);

    String roleMemberShipId = "MyRoleMemberShipId";
    RoleMembership updatedRoleMembership = mock(RoleMembership.class);
    when(updatedRoleMembership.getId()).thenReturn(roleMemberShipId);
    when(updatedRoleMembership.getUser()).thenReturn(updatedUser);

    when(delegateRepository.findOneById(roleMemberShipId)).thenReturn(currentRoleMembership);

    roleMembershipDecorator.update(Stream.of(updatedRoleMembership));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<RoleMembership>> argumentCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).update(argumentCaptor.capture());
    argumentCaptor
        .getValue()
        .forEach(
            t -> { // consume stream
            });
    verify(roleMembershipValidator).validate(updatedRoleMembership);
    verify(sessionSecurityContextUpdater).resetAuthorities(currentUser);
    verify(sessionSecurityContextUpdater).resetAuthorities(updatedUser);
  }

  @Test
  void testDelete() {
    User user = mock(User.class);

    RoleMembership roleMembership = mock(RoleMembership.class);
    when(roleMembership.getUser()).thenReturn(user);

    roleMembershipDecorator.delete(roleMembership);
    verify(sessionSecurityContextUpdater).resetAuthorities(user);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testDeleteStream() {
    User user = mock(User.class);

    RoleMembership roleMembership = mock(RoleMembership.class);
    when(roleMembership.getUser()).thenReturn(user);

    doAnswer(invocation -> ((Stream<?>) invocation.getArguments()[0]).count())
        .when(delegateRepository)
        .delete(any(Stream.class));

    roleMembershipDecorator.delete(Stream.of(roleMembership));
    verify(sessionSecurityContextUpdater).resetAuthorities(user);
  }

  @Test
  void testDeleteById() {
    User user = mock(User.class);

    String roleMembershipId = "MyRoleMemberShipId";
    RoleMembership roleMembership = mock(RoleMembership.class);
    when(roleMembership.getUser()).thenReturn(user);

    when(delegateRepository.findOneById(roleMembershipId)).thenReturn(roleMembership);
    roleMembershipDecorator.deleteById(roleMembershipId);
    verify(sessionSecurityContextUpdater).resetAuthorities(user);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testDeleteAllStream() {
    User user = mock(User.class);

    String roleMembershipId = "MyRoleMemberShipId";
    RoleMembership roleMembership = mock(RoleMembership.class);
    when(roleMembership.getUser()).thenReturn(user);

    doAnswer(invocation -> ((Stream<?>) invocation.getArguments()[0]).count())
        .when(delegateRepository)
        .deleteAll(any(Stream.class));

    when(delegateRepository.findOneById(roleMembershipId)).thenReturn(roleMembership);
    roleMembershipDecorator.deleteAll(Stream.of(roleMembershipId));
    verify(sessionSecurityContextUpdater).resetAuthorities(user);
  }

  @Test
  void testDeleteAll() {
    User user = mock(User.class);

    RoleMembership roleMembership = mock(RoleMembership.class);
    when(roleMembership.getUser()).thenReturn(user);

    doAnswer(
            invocation -> {
              Consumer<List<Entity>> consumer = invocation.getArgument(1);
              consumer.accept(singletonList(roleMembership));
              return null;
            })
        .when(delegateRepository)
        .forEachBatched(eq(null), any(), eq(1000));
    roleMembershipDecorator.deleteAll();
    verify(sessionSecurityContextUpdater).resetAuthorities(user);
  }
}
