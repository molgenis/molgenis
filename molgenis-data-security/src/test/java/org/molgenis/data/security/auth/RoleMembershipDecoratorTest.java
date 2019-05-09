package org.molgenis.data.security.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.Repository;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RoleMembershipDecoratorTest extends AbstractMockitoTest {
  @Mock private Repository<RoleMembership> delegateRepository;
  @Mock private RoleMembershipValidator roleMembershipValidator;
  private RoleMembershipDecorator roleMembershipDecorator;

  @BeforeMethod
  public void setUpBeforeMethod() {
    roleMembershipDecorator =
        new RoleMembershipDecorator(delegateRepository, roleMembershipValidator);
  }

  @Test
  public void testAdd() {
    RoleMembership roleMembership = mock(RoleMembership.class);
    roleMembershipDecorator.add(roleMembership);
    verify(roleMembershipValidator).validate(roleMembership);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testAddStream() {
    RoleMembership roleMembership = mock(RoleMembership.class);

    when(delegateRepository.add(any(Stream.class)))
        .thenAnswer(
            invocation ->
                Long.valueOf(((Stream<RoleMembership>) invocation.getArgument(0)).count())
                    .intValue());

    roleMembershipDecorator.add(Stream.of(roleMembership));
    verify(roleMembershipValidator).validate(roleMembership);
  }

  @Test
  public void testUpdate() {
    RoleMembership roleMembership = mock(RoleMembership.class);
    roleMembershipDecorator.update(roleMembership);
    verify(roleMembershipValidator).validate(roleMembership);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testUpdateStream() {
    RoleMembership roleMembership = mock(RoleMembership.class);

    roleMembershipDecorator.update(Stream.of(roleMembership));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<RoleMembership>> argumentCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).update(argumentCaptor.capture());
    argumentCaptor
        .getValue()
        .forEach(
            t -> { // consume stream
            });
    verify(roleMembershipValidator).validate(roleMembership);
  }
}
