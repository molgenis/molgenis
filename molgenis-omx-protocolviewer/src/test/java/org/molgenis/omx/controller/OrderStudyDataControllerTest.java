package org.molgenis.omx.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Arrays;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.security.Login;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.service.OrderStudyDataService;
import org.molgenis.util.GsonHttpMessageConverter;
import org.molgenis.util.HandleRequestDelegationException;
import org.molgenis.util.ShoppingCart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.mail.javamail.JavaMailSender;
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

	@BeforeMethod
	public void setUp() throws HandleRequestDelegationException, Exception
	{
		mockMvc = MockMvcBuilders.standaloneSetup(orderStudyDataController)
				.setMessageConverters(new GsonHttpMessageConverter(), new FormHttpMessageConverter()).build();
	}

	@Test
	public void getOrderDataForm() throws Exception
	{
		this.mockMvc.perform(get("/plugin/order")).andExpect(status().isOk()).andExpect(view().name("orderdata-modal"));
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
		public OrderStudyDataService orderStudyDataService()
		{
			OrderStudyDataService orderStudyDataService = mock(OrderStudyDataService.class);
			return orderStudyDataService;
		}

		@Bean
		public Database database()
		{
			return mock(Database.class);
		}

		@Bean
		public Login login()
		{
			return mock(Login.class);
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
		public ShoppingCart shoppingCart()
		{
			ShoppingCart shoppingCart = mock(ShoppingCart.class);
			when(shoppingCart.getCart()).thenReturn(Arrays.asList(Integer.valueOf(0), Integer.valueOf(1)));
			return shoppingCart;
		}
	}
}
