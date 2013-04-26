package org.molgenis.omx.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.util.GsonHttpMessageConverter;
import org.molgenis.util.HandleRequestDelegationException;
import org.molgenis.util.ShoppingCart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration
public class ShoppingCartControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private ShoppingCartController shoppingCartController;

	@Autowired
	private ShoppingCart shoppingCart;

	private MockMvc mockMvc;

	@BeforeMethod
	public void setUp() throws HandleRequestDelegationException, Exception
	{
		mockMvc = MockMvcBuilders.standaloneSetup(shoppingCartController)
				.setMessageConverters(new GsonHttpMessageConverter(), new FormHttpMessageConverter()).build();
	}

	@Test
	public void getCart() throws Exception
	{
		this.mockMvc
				.perform(get("/cart").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(
						content()
								.string("{\"features\":[{\"id\":0,\"name\":\"feature #0\",\"i18nDescription\":{\"en\":\"feature #0 description\"}},{\"id\":1,\"name\":\"feature #1\",\"i18nDescription\":{\"en\":\"feature #1 description\"}}]}"));
	}

	@Test
	public void addToCart() throws Exception
	{
		this.mockMvc.perform(
				post("/cart/add").content("{features:[{feature:3},{feature:4}]}")
						.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(
				status().isOk());
		verify(shoppingCart).addToCart(Arrays.asList(Integer.valueOf(3), Integer.valueOf(4)));
	}

	@Test
	public void addToCart_invalidBody() throws Exception
	{
		this.mockMvc.perform(
				post("/cart/add").content("[{feature:3},{feature:4}]").contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	@Test
	public void emptyCart() throws Exception
	{
		this.mockMvc.perform(post("/cart/empty")).andExpect(status().isOk());
		verify(shoppingCart).emptyCart();
	}

	@Test
	public void removeFromCart() throws Exception
	{
		this.mockMvc.perform(
				post("/cart/remove").content("{features:[{feature:3},{feature:4}]}")
						.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(
				status().isOk());
		verify(shoppingCart).removeFromCart(Arrays.asList(Integer.valueOf(3), Integer.valueOf(4)));
	}

	@Test
	public void removeFromCart_invalidBody() throws Exception
	{
		this.mockMvc.perform(
				post("/cart/remove").content("[{feature:3},{feature:4}]").contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	@Test
	public void emptyAndAddToCart() throws Exception
	{
		this.mockMvc.perform(
				post("/cart/replace").content("{features:[{feature:3},{feature:4}]}")
						.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(
				status().isOk());
		verify(shoppingCart).emptyAndAddToCart(Arrays.asList(Integer.valueOf(3), Integer.valueOf(4)));
	}

	@Test
	public void emptyAndAddToCart_invalidBody() throws Exception
	{
		this.mockMvc.perform(
				post("/cart/replace").content("[{feature:3},{feature:4}]").contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	@Configuration
	public static class Config
	{
		@Bean
		public ShoppingCartController shoppingCartController()
		{
			return new ShoppingCartController();
		}

		@Bean
		public ShoppingCart shoppingCart()
		{
			ShoppingCart shoppingCart = mock(ShoppingCart.class);
			when(shoppingCart.getCart()).thenReturn(Arrays.asList(Integer.valueOf(0), Integer.valueOf(1)));
			return shoppingCart;
		}

		@Bean
		public Database database() throws DatabaseException
		{
			ObservableFeature feature0 = mock(ObservableFeature.class);
			when(feature0.getId()).thenReturn(0);
			when(feature0.getName()).thenReturn("feature #0");
			when(feature0.getDescription()).thenReturn("feature #0 description");

			ObservableFeature feature1 = mock(ObservableFeature.class);
			when(feature1.getId()).thenReturn(1);
			when(feature1.getName()).thenReturn("feature #1");
			when(feature1.getDescription()).thenReturn("feature #1 description");

			Database database = mock(Database.class);
			when(database.findById(ObservableFeature.class, 0)).thenReturn(feature0);
			when(database.findById(ObservableFeature.class, 1)).thenReturn(feature1);
			return database;
		}
	}
}
