package org.molgenis.omx.protocolviewer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.Part;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.protocolviewer.ProtocolViewerService;
import org.molgenis.omx.protocolviewer.ProtocolViewerServiceImpl;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.study.StudyDefinition;
import org.molgenis.studymanager.StudyManagerService;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
//FIXME
//@ContextConfiguration
public class ProtocolViewerServiceImplTest extends AbstractTestNGSpringContextTests
{
//	@Configuration
//	static class Config
//	{
//		@Bean
//		public ProtocolViewerService orderStudyDataService()
//		{
//			return new ProtocolViewerServiceImpl();
//		}
//
//		@Bean
//		public DataService dataService()
//		{
//			return mock(DataService.class);
//		}
//
//		@Bean
//		public MolgenisSettings molgenisSettings()
//		{
//			return mock(MolgenisSettings.class);
//		}
//
//		@Bean
//		public FileStore fileStore() throws IOException
//		{
//			File requestForm = mock(File.class);
//			when(requestForm.getPath()).thenReturn("requestform.doc");
//			FileStore fileStore = mock(FileStore.class);
//			when(fileStore.store(any(InputStream.class), any(String.class))).thenReturn(requestForm);
//			return fileStore;
//		}
//
//		@Bean
//		public JavaMailSender mailSender()
//		{
//			MimeMessage mimeMessage = mock(MimeMessage.class);
//			JavaMailSender javaMailSender = mock(JavaMailSender.class);
//			when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
//			return javaMailSender;
//		}
//
//		@Bean
//		public StudyManagerService studyManagerService()
//		{
//			return mock(StudyManagerService.class);
//		}
//
//		@Bean
//		public MolgenisUserService molgenisUserService()
//		{
//			return mock(MolgenisUserService.class);
//		}
//	}
//
//	private static final String USERNAME_USER = "user";
//
//	private static Authentication AUTHENTICATION_PREVIOUS;
//	private Authentication authentication;
//	private MolgenisUser molgenisUser;
//	private final String protocol1Identifier = "1";
//	private Protocol protocol1;
//	private ObservableFeature feature0, feature1;
//
//	@Autowired
//	private ProtocolViewerService protocolViewerService;
//
//	@Autowired
//	private StudyManagerService studyManagerService;
//
//	@Autowired
//	private MolgenisUserService molgenisUserService;
//
//	@Autowired
//	private DataService dataService;
//
//	@Autowired
//	private JavaMailSender javaMailSender;
//
//	@BeforeClass
//	public void setUpBeforeClass()
//	{
//		AUTHENTICATION_PREVIOUS = SecurityContextHolder.getContext().getAuthentication();
//		authentication = mock(Authentication.class);
//		SecurityContextHolder.getContext().setAuthentication(authentication);
//	}
//
//	@AfterClass
//	public static void tearDownAfterClass()
//	{
//		SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_PREVIOUS);
//	}
//
//	@SuppressWarnings(
//	{ "deprecation" })
//	@BeforeMethod
//	public void setUp()
//	{
//		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
//		molgenisUser = when(mock(MolgenisUser.class).getId()).thenReturn(1).getMock();
//		when(molgenisUser.getUsername()).thenReturn(USERNAME_USER);
//		when(molgenisUser.getEmail()).thenReturn("email@user.com");
//		when(molgenisUserService.getUser(USERNAME_USER)).thenReturn(molgenisUser);
//
//		feature0 = mock(ObservableFeature.class);
//		when(feature0.getId()).thenReturn(0);
//		when(feature0.getName()).thenReturn("feature #0");
//		when(feature0.getDescription()).thenReturn("feature #0 description");
//
//		feature1 = mock(ObservableFeature.class);
//		when(feature1.getId()).thenReturn(1);
//		when(feature1.getName()).thenReturn("feature #1");
//		when(feature1.getDescription()).thenReturn("feature #1 description");
//
//		Query q0 = new QueryImpl(new QueryRule(ObservableFeature.ID, Operator.IN, Arrays.asList(0, 1)));
//		when(dataService.findAll(ObservableFeature.ENTITY_NAME, q0)).thenReturn(
//				Arrays.<Entity> asList(feature0, feature1));
//
//		Query q0b = new QueryImpl(new QueryRule(ObservableFeature.ID, Operator.IN, Arrays.asList(-2, -1)));
//		when(dataService.findAll(ObservableFeature.ENTITY_NAME, q0b)).thenReturn(Collections.<Entity> emptyList());
//
//		protocol1 = when(mock(Protocol.class).getIdentifier()).thenReturn(protocol1Identifier).getMock();
//		Query q1 = new QueryImpl(new QueryRule(Protocol.ID, Operator.EQUALS, protocol1Identifier));
//		when(dataService.findOne(Protocol.ENTITY_NAME, q1)).thenReturn(protocol1);
//
//		Query q2 = new QueryImpl(new QueryRule(ObservableFeature.ENTITY_NAME, Operator.IN, Arrays.asList(-2, -1)));
//		when(dataService.findAll(ObservableFeature.ENTITY_NAME, q2)).thenReturn(Collections.<Entity> emptyList());
//	}
//
//	@Test
//	public void orderStudyData() throws MessagingException, IOException
//	{
//		StudyDefinition studyDefinition = when(mock(StudyDefinition.class).getId()).thenReturn("1").getMock();
//		when(studyManagerService.persistStudyDefinition((StudyDefinition) any())).thenReturn(studyDefinition);
//
//		String studyDataRequestName = "study #1";
//		Part requestForm = mock(Part.class);
//		when(requestForm.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]
//		{ 0, 1, 2 }));
//		protocolViewerService.submitStudyDefinitionDraftForCurrentUser(studyDataRequestName, requestForm, protocol1Identifier,
//                Arrays.asList(Integer.valueOf(0), Integer.valueOf(1)));
//
//		ArgumentCaptor<StudyDataRequest> argument = ArgumentCaptor.forClass(StudyDataRequest.class);
//		verify(dataService).add(eq(StudyDataRequest.ENTITY_NAME), argument.capture());
//		StudyDataRequest studyDataRequest = argument.getValue();
//		assertEquals(studyDataRequest.getMolgenisUser(), molgenisUser);
//		assertEquals(studyDataRequest.getName(), studyDataRequestName);
//		assertEquals(studyDataRequest.getProtocol(), protocol1);
//		assertEquals(studyDataRequest.getFeatures(), Arrays.asList(feature0, feature1));
//		verify(javaMailSender).send(any(MimeMessage.class));
//	}
//
//	@Test(expectedExceptions = IllegalArgumentException.class)
//	public void orderStudyData_noStudyName() throws MessagingException, IOException
//	{
//		protocolViewerService.submitStudyDefinitionDraftForCurrentUser(null, mock(Part.class), protocol1Identifier,
//                Arrays.asList(Integer.valueOf(0), Integer.valueOf(1)));
//	}
//
//	@Test(expectedExceptions = IllegalArgumentException.class)
//	public void orderStudyData_noRequestForm() throws MessagingException, IOException
//	{
//		protocolViewerService.submitStudyDefinitionDraftForCurrentUser("study #1", null, protocol1Identifier,
//                Arrays.asList(Integer.valueOf(0), Integer.valueOf(1)));
//	}
//
//	@Test(expectedExceptions = IllegalArgumentException.class)
//	public void orderStudyData_noFeatures() throws MessagingException, IOException
//	{
//		protocolViewerService.submitStudyDefinitionDraftForCurrentUser("study #1", mock(Part.class), protocol1Identifier, null);
//	}
//
//	@Test(expectedExceptions = IllegalArgumentException.class)
//	public void orderStudyData_emptyFeatures() throws MessagingException, IOException
//	{
//		protocolViewerService.submitStudyDefinitionDraftForCurrentUser("study #1", mock(Part.class), protocol1Identifier,
//                Collections.<Integer>emptyList());
//	}
//
//	@Test(expectedExceptions = MolgenisDataException.class)
//	public void orderStudyData_invalidFeatures() throws MessagingException, IOException
//	{
//		protocolViewerService.submitStudyDefinitionDraftForCurrentUser("study #1", mock(Part.class), protocol1Identifier,
//                Arrays.asList(Integer.valueOf(-2), Integer.valueOf(-1)));
//	}
//
//	@Test
//	public void getOrders()
//	{
//		StudyDataRequest studyDataRequest0 = mock(StudyDataRequest.class);
//		StudyDataRequest studyDataRequest1 = mock(StudyDataRequest.class);
//		when(dataService.findAll(StudyDataRequest.ENTITY_NAME, new QueryImpl())).thenReturn(
//				Arrays.<Entity> asList(studyDataRequest0, studyDataRequest1));
//		assertEquals(protocolViewerService.getStudyDefinitionsForCurrentUser(), Arrays.asList(studyDataRequest0, studyDataRequest1));
//	}
//
//	@Test
//	public void getOrdersString()
//	{
//		StudyDataRequest studyDataRequest0 = mock(StudyDataRequest.class);
//		when(dataService.findOne(StudyDataRequest.ENTITY_NAME, 1)).thenReturn(studyDataRequest0);
//		assertEquals(protocolViewerService.getOrder(1), studyDataRequest0);
//	}
}
