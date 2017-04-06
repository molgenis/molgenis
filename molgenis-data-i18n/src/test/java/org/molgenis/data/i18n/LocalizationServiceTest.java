package org.molgenis.data.i18n;

import com.beust.jcommander.internal.Maps;
import com.google.common.collect.Sets;
import org.mockito.*;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.i18n.model.I18nString;
import org.molgenis.data.i18n.model.I18nStringFactory;
import org.molgenis.data.i18n.model.I18nStringMetaData;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;
import java.util.stream.Stream;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newTreeSet;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.molgenis.data.i18n.model.I18nStringMetaData.I18N_STRING;
import static org.molgenis.data.i18n.model.I18nStringMetaData.MSGID;
import static org.molgenis.data.i18n.model.I18nStringMetaData.NAMESPACE;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.support.AttributeUtils.getI18nAttributeName;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { LocalizationService.class, LocalizationServiceTest.Config.class })
public class LocalizationServiceTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private LocalizationService localizationService;
	@Autowired
	private DataService dataService;
	@Autowired
	private I18nStringFactory i18nStringFactory;

	@Mock
	private I18nString string1;
	@Mock
	private I18nString string2;
	@Mock
	private I18nString newString1;
	@Mock
	private I18nString newString2;

	private PropertiesMessageSource propertiesMessageSource;

	@Captor
	private ArgumentCaptor<Stream<I18nString>> updateCaptor;

	@Captor
	private ArgumentCaptor<Stream<I18nString>> addCaptor;

	@BeforeMethod
	public void setUp() throws Exception
	{
		Mockito.reset(i18nStringFactory);

		MockitoAnnotations.initMocks(this);
		when(string1.getMessageId()).thenReturn("EN_PLUS_NL");
		when(string1.getString("en")).thenReturn("string 1 - en");
		when(string1.getString("nl")).thenReturn("string 1 - nl");

		when(string2.getMessageId()).thenReturn("NL_ONLY");
		when(string2.getString("nl")).thenReturn("string 2 - nl");
		when(string2.getString("en")).thenReturn(null);

		when(i18nStringFactory.create(string1)).thenReturn(string1);
		when(i18nStringFactory.create(string2)).thenReturn(string2);

		propertiesMessageSource = new PropertiesMessageSource("test");

	}

	@AfterMethod
	public void tearDown() throws Exception
	{
	}

	@Test
	public void testGetMessage() throws Exception
	{
		Query<I18nString> query = new QueryImpl<I18nString>().eq(MSGID, "EN_PLUS_NL").and().eq(NAMESPACE, "test");
		when(dataService.findOne(I18N_STRING, query, I18nString.class)).thenReturn(string1);

		assertEquals(localizationService.getMessage("test", "EN_PLUS_NL", "nl"), "string 1 - nl");
	}

	@Test
	public void testGetMessages() throws Exception
	{
		Query<I18nString> query = new QueryImpl<I18nString>().eq(NAMESPACE, "test");
		when(dataService.findAll(I18N_STRING, query, I18nString.class)).thenAnswer((i) -> Stream.of(string1, string2));

		assertEquals(localizationService.getMessages("test", "nl"),
				Maps.newHashMap("EN_PLUS_NL", "string 1 - nl", "NL_ONLY", "string 2 - nl"));
	}

	@Test
	public void testUpdateAllLanguages() throws Exception
	{
		Query<I18nString> query = new QueryImpl<I18nString>()
				.in(MSGID, asList("EN_PLUS_NL", "EN_ONLY", "NL_ONLY", "BIOBANK_UTF8")).and().eq(NAMESPACE, "test");
		when(dataService.findAll(I18N_STRING, query, I18nString.class)).thenReturn(Stream.of(string1, string2));

		when(i18nStringFactory.create()).thenReturn(newString1, newString2);
		when(newString1.getMessageId()).thenReturn("EN_ONLY");
		when(newString2.getMessageId()).thenReturn("BIOBANK_UTF8");

		localizationService.updateAllLanguages(propertiesMessageSource);

		verify(newString1).setNamespace("test");
		verify(newString1).setMessageId("EN_ONLY");
		verify(newString1).set("en", "English only");
		verify(newString2).setNamespace("test");
		verify(newString2).setMessageId("BIOBANK_UTF8");
		verify(newString2).set("en", "Biøbånk\uD83D\uDC00");
		verify(newString2).set("nl", "\uD83D\uDC00\uD83C\uDDF3\uD83C\uDDF1");

		verify(dataService).update(eq(I18N_STRING), updateCaptor.capture());
		assertEquals(updateCaptor.getValue().collect(toSet()), Sets.newHashSet(string1, string2));
		verify(dataService).add(eq(I18N_STRING), addCaptor.capture());
		assertEquals(addCaptor.getValue().collect(toSet()), Sets.newHashSet(newString1, newString2));
	}

	@Test
	public void testAddMissingMessageIDs() throws Exception
	{
		Query<I18nString> query = new QueryImpl<I18nString>().in(MSGID, asList("EN_PLUS_NL", "NEW")).and()
				.eq(NAMESPACE, "test");
		when(dataService.findAll(I18N_STRING, query, I18nString.class)).thenReturn(Stream.of(string1));
		when(newString1.setNamespace("test")).thenReturn(newString1);
		when(newString1.setMessageId("NEW")).thenReturn(newString1);

		localizationService.addMissingMessageIDs("test", Sets.newTreeSet(asList("EN_PLUS_NL", "NEW")));

		when(i18nStringFactory.create()).thenReturn(newString1);

		verify(dataService).add(eq(I18N_STRING), addCaptor.capture());
		assertEquals(addCaptor.getValue().collect(toSet()), Collections.singleton(newString1));
		verify(newString1).setNamespace("test");
		verify(newString1).setMessageId("NEW");
	}

	@Test
	public void testGetKeys() throws Exception
	{
		Query<I18nString> query = new QueryImpl<I18nString>().eq(NAMESPACE, "test");
		when(dataService.findAll(I18N_STRING, query, I18nString.class)).thenReturn(Stream.of(string1, string2));

		Set<String> keys = localizationService.getKeys("test");
		assertEquals(keys, newHashSet("EN_PLUS_NL", "NL_ONLY"));
	}

	@Configuration
	public static class Config
	{
		@Mock
		private I18nStringFactory i18nStringFactory;

		public Config()
		{
			MockitoAnnotations.initMocks(this);
		}

		@Bean
		public I18nStringFactory i18nStringFactory()
		{
			return i18nStringFactory;
		}
	}

}