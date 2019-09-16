package org.molgenis.data.i18n;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.i18n.model.L10nStringMetadata.L10N_STRING;
import static org.molgenis.data.i18n.model.L10nStringMetadata.MSGID;
import static org.molgenis.data.i18n.model.L10nStringMetadata.NAMESPACE;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.i18n.model.L10nString;
import org.molgenis.data.i18n.model.L10nStringFactory;
import org.molgenis.test.AbstractMockitoTest;

class LocalizationServiceTest extends AbstractMockitoTest {
  private static final Locale DUTCH = new Locale("nl");

  private LocalizationService localizationService;

  @Mock private DataService dataService;

  @Mock private L10nStringFactory l10nStringFactory;

  @Mock private L10nString enPlusNl;
  @Mock private L10nString nlOnly;
  @Mock private L10nString newString1;
  @Mock private L10nString newString2;

  @Captor private ArgumentCaptor<Stream<L10nString>> updateCaptor;
  @Captor private ArgumentCaptor<Stream<L10nString>> addCaptor;
  @Captor private ArgumentCaptor<Stream<L10nString>> deleteCaptor;

  @BeforeEach
  void setUp() throws Exception {
    localizationService = new LocalizationService(dataService, l10nStringFactory);
  }

  @Test
  void testGetMessage() {
    doReturn("string 1 - nl").when(enPlusNl).getString(DUTCH);
    @SuppressWarnings("unchecked")
    Query<L10nString> query = mock(Query.class, RETURNS_SELF);
    when(dataService.query(L10N_STRING, L10nString.class)).thenReturn(query);
    when(query.eq(MSGID, "EN_PLUS_NL").findOne()).thenReturn(enPlusNl);

    assertEquals(
        "string 1 - nl", localizationService.resolveCodeWithoutArguments("EN_PLUS_NL", DUTCH));
  }

  @Test
  void testGetMessages() {
    @SuppressWarnings("unchecked")
    Query<L10nString> query = mock(Query.class, RETURNS_SELF);
    when(dataService.query(L10N_STRING, L10nString.class)).thenReturn(query);
    when(query.eq(NAMESPACE, "test").findAll()).thenReturn(Stream.of(enPlusNl, nlOnly));
    when(enPlusNl.getMessageID()).thenReturn("EN_PLUS_NL");
    doReturn("string 1 - nl").when(enPlusNl).getString(DUTCH);
    when(nlOnly.getMessageID()).thenReturn("NL_ONLY");
    doReturn("string 2 - nl").when(nlOnly).getString(DUTCH);

    assertEquals(
        of("EN_PLUS_NL", "string 1 - nl", "NL_ONLY", "string 2 - nl"),
        localizationService.getMessages("test", DUTCH));
  }

  @Test
  void testAddMissingMessageIds() {
    Set<String> messageIds = ImmutableSet.of("EN_PLUS_NL", "NEW");
    @SuppressWarnings("unchecked")
    Query<L10nString> query = mock(Query.class, RETURNS_SELF);
    when(dataService.query(L10N_STRING, L10nString.class)).thenReturn(query);
    when(query.in(MSGID, messageIds).and().eq(NAMESPACE, "test").findAll())
        .thenReturn(Stream.of(enPlusNl));
    when(enPlusNl.getMessageID()).thenReturn("EN_PLUS_NL");

    when(l10nStringFactory.create("NEW")).thenReturn(newString1);
    when(newString1.setMessageID("NEW")).thenReturn(newString1);
    when(newString1.setNamespace("test")).thenReturn(newString1);

    localizationService.addMissingMessageIds("test", messageIds);

    verify(dataService).add(eq(L10N_STRING), addCaptor.capture());
    assertEquals(singleton(newString1), addCaptor.getValue().collect(toSet()));
  }

  @Test
  void testDeleteNamespace() {
    @SuppressWarnings("unchecked")
    Query<L10nString> query = mock(Query.class, RETURNS_SELF);
    when(dataService.query(L10N_STRING, L10nString.class)).thenReturn(query);
    when(query.eq(NAMESPACE, "test").findAll()).thenReturn(Stream.of(enPlusNl, nlOnly));

    localizationService.deleteNamespace("test");
    verify(dataService).delete(eq(L10N_STRING), deleteCaptor.capture());
    assertEquals(newArrayList(enPlusNl, nlOnly), deleteCaptor.getValue().collect(toList()));
  }

  @Test
  void testStore() {
    List<L10nString> toAdd = singletonList(newString1);
    List<L10nString> toUpdate = singletonList(enPlusNl);
    localizationService.store(toUpdate, toAdd);

    verify(dataService).add(eq(L10N_STRING), addCaptor.capture());
    verify(dataService).update(eq(L10N_STRING), updateCaptor.capture());

    assertEquals(toAdd, addCaptor.getValue().collect(toList()));
    assertEquals(toUpdate, updateCaptor.getValue().collect(toList()));
  }

  @Test
  void testGetAllMessageIds() {
    when(dataService.findAll(L10N_STRING, L10nString.class))
        .thenReturn(Stream.of(enPlusNl, nlOnly));
    when(enPlusNl.getMessageID()).thenReturn("A");
    when(nlOnly.getMessageID()).thenReturn("B");
    assertEquals(asList("A", "B"), localizationService.getAllMessageIds());
  }
}
