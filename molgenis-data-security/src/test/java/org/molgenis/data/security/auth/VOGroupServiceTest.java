package org.molgenis.data.security.auth;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.auth.VOGroupMetadata.NAME;
import static org.molgenis.data.security.auth.VOGroupMetadata.VO_GROUP;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.test.AbstractMockitoTest;

class VOGroupServiceTest extends AbstractMockitoTest {
  @Mock private DataService dataService;
  @Mock private VOGroupFactory voGroupFactory;
  @Mock private VOGroup voGroup;
  @Mock private VOGroupMetadata entityType;
  @Mock private Attribute attribute;

  @Mock(answer = RETURNS_SELF)
  private Query<VOGroup> query;

  @Captor private ArgumentCaptor<Stream<VOGroup>> voGroupCaptor;

  private VOGroupService voGroupService;

  @BeforeEach
  void beforeMethod() {
    voGroupService = new VOGroupService(dataService, voGroupFactory);
  }

  @Test
  void getGroupsRetrievesExistingGroup() {
    var groupName = "urn:mace:surf.nl:sram:group:molgenis:dev";
    var groupNames = Set.of(groupName);

    when(dataService.query(VO_GROUP, VOGroup.class)).thenReturn(query);
    when(query.in(NAME, groupNames).findAll()).thenReturn(Stream.of(voGroup));
    when(voGroup.getName()).thenReturn(groupName);

    assertEquals(List.of(voGroup), voGroupService.getGroups(groupNames));
  }

  @Test
  void getGroupsCreatesNewGroup() {
    var groupName = "urn:mace:surf.nl:sram:group:molgenis:dev";
    var groupNames = Set.of(groupName);

    when(dataService.query(VO_GROUP, VOGroup.class)).thenReturn(query);
    when(query.in(NAME, groupNames).findAll()).thenReturn(Stream.empty());
    when(voGroupFactory.withName(groupName)).thenReturn(voGroup);

    assertEquals(List.of(voGroup), voGroupService.getGroups(groupNames));

    verify(dataService).add(eq(VO_GROUP), voGroupCaptor.capture());
    assertEquals(List.of(voGroup), voGroupCaptor.getValue().collect(toList()));
  }

  @Test
  void getGroup() {
    when(dataService.query(VO_GROUP, VOGroup.class)).thenReturn(query);
    when(query.eq(NAME, "urn:mace:surf.nl:sram:group:molgenis:dev").findOne()).thenReturn(voGroup);

    assertEquals(voGroup, voGroupService.getGroup("urn:mace:surf.nl:sram:group:molgenis:dev"));
  }

  @Test
  void getGroupNotFound() {
    when(voGroupFactory.getEntityType()).thenReturn(entityType);
    when(entityType.getAttribute(NAME)).thenReturn(attribute);

    when(dataService.query(VO_GROUP, VOGroup.class)).thenReturn(query);

    assertThrows(
        UnknownEntityException.class,
        () -> voGroupService.getGroup("urn:mace:surf.nl:sram:group:molgenis:dev"));
  }

  @Test
  void getGroupById() {
    when(dataService.findOneById(VO_GROUP, "voID", VOGroup.class)).thenReturn(voGroup);

    assertEquals(voGroup, voGroupService.getGroupByID("voID"));
  }

  @Test
  void getGroupByIdNotFound() {
    when(voGroupFactory.getEntityType()).thenReturn(entityType);

    assertThrows(UnknownEntityException.class, () -> voGroupService.getGroupByID("voId"));
  }

  @Test
  void createGroup() {
    when(voGroupFactory.withName("urn:mace:surf.nl:sram:group:molgenis:dev")).thenReturn(voGroup);

    assertEquals(voGroup, voGroupService.createGroup("urn:mace:surf.nl:sram:group:molgenis:dev"));

    verify(dataService).add(VO_GROUP, voGroup);
  }

  @Test
  void getGroups() {
    when(dataService.findAll(VO_GROUP, VOGroup.class)).thenReturn(Stream.of(voGroup));

    assertEquals(List.of(voGroup), voGroupService.getGroups());
  }
}
