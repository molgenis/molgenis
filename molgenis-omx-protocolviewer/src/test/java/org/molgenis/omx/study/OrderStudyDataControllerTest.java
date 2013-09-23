package org.molgenis.omx.study;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Arrays;
import java.util.Date;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.order.OrderStudyDataController;
import org.molgenis.omx.order.OrderStudyDataService;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.studymanager.StudyManagerService;
import org.molgenis.util.FileStore;
import org.molgenis.util.GsonHttpMessageConverter;
import org.molgenis.util.HandleRequestDelegationException;
import org.molgenis.util.ShoppingCart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration
public class OrderStudyDataControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private OrderStudyDataController orderStudyDataController;

	@Autowired
	private OrderStudyDataService orderStudyDataService;

	private MockMvc mockMvc;
	private Authentication authentication;

	@BeforeMethod
	public void setUp() throws HandleRequestDelegationException, Exception
	{
		mockMvc = MockMvcBuilders.standaloneSetup(orderStudyDataController)
				.setMessageConverters(new GsonHttpMessageConverter(), new FormHttpMessageConverter()).build();

		authentication = mock(Authentication.class);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@Test
	public void getOrderDataForm() throws Exception
	{
		this.mockMvc.perform(get(OrderStudyDataController.URI + "/order")).andExpect(status().isOk())
				.andExpect(view().name("orderdata-modal"));
	}

	@Test
	public void getOrdersForm() throws Exception
	{
		this.mockMvc.perform(get(OrderStudyDataController.URI + "/orders/view")).andExpect(status().isOk())
				.andExpect(view().name("orderlist-modal"));
	}

	@Test
	public void getOrders() throws Exception
	{
		when(authentication.isAuthenticated()).thenReturn(true);
		UserDetails userDetails = when(mock(UserDetails.class).getUsername()).thenReturn("user0").getMock();
		when(authentication.getPrincipal()).thenReturn(userDetails);

		this.mockMvc
				.perform(get(OrderStudyDataController.URI + "/orders"))
				.andExpect(status().isOk())
				.andExpect(
						content()
								.string("{\"orders\":[{\"id\":0,\"name\":\"request #0\",\"orderDate\":\"2012-10-12\",\"orderStatus\":\"pending\"}]}"));
	}

	// TODO how to test multipart/form-data using fileUpload() and post()?
	// @Test
	// public void orderData() throws Exception
	// {
	//
	// }

	@Configuration
	public static class Config
	{
		@Bean
		public OrderStudyDataController orderStudyDataController()
		{
			return new OrderStudyDataController();
		}

		@Bean
		public OrderStudyDataService orderStudyDataService() throws DatabaseException
		{
			StudyDataRequest request0 = mock(StudyDataRequest.class);
			when(request0.getId()).thenReturn(0);
			when(request0.getRequestDate()).thenReturn(new Date(1350000000000l));
			when(request0.getRequestStatus()).thenReturn("pending");
			when(request0.getName()).thenReturn("request #0");
			StudyDataRequest request1 = mock(StudyDataRequest.class);
			when(request1.getId()).thenReturn(1);
			when(request1.getRequestDate()).thenReturn(new Date(1360000000000l));
			when(request1.getRequestStatus()).thenReturn("rejected");
			when(request1.getName()).thenReturn("request #1");
			OrderStudyDataService orderStudyDataService = mock(OrderStudyDataService.class);
			when(orderStudyDataService.getOrders("user0")).thenReturn(Arrays.asList(request0));
			when(orderStudyDataService.getOrders("user1")).thenReturn(Arrays.asList(request1));
			return orderStudyDataService;
		}

		@Bean
		public StudyManagerService studyManagerService()
		{
			return mock(StudyManagerService.class);
		}

		@Bean
		public Database database()
		{
			return mock(Database.class);
		}

		@Bean
		public MolgenisSettings molgenisSettings()
		{
			return mock(MolgenisSettings.class);
		}

		@Bean
		public JavaMailSender mailSender()
		{
			return mock(JavaMailSender.class);
		}

		@Bean
		public FileStore fileStore()
		{
			return mock(FileStore.class);
		}

		@Bean
		public ShoppingCart shoppingCart()
		{
			ShoppingCart shoppingCart = mock(ShoppingCart.class);
			when(shoppingCart.getCart()).thenReturn(Arrays.asList(Integer.valueOf(0), Integer.valueOf(1)));
			return shoppingCart;
		}

		@Bean
		public MolgenisUserService molgenisUserService()
		{
			return mock(MolgenisUserService.class);
		}
	}
}
