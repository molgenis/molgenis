package org.molgenis.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ShoppingCartTest
{
	private ShoppingCart shoppingCart;

	@BeforeMethod
	public void setUp()
	{
		shoppingCart = new ShoppingCart();
	}

	@Test
	public void addToCartInteger()
	{
		shoppingCart.addToCart(1);
		shoppingCart.addToCart(2);
		assertEquals(shoppingCart.getCart(), Arrays.<Integer> asList(Integer.valueOf(1), Integer.valueOf(2)));
	}

	@Test
	public void addToCartListInteger()
	{
		shoppingCart.addToCart(Arrays.<Integer> asList(Integer.valueOf(1), Integer.valueOf(2)));
		assertEquals(shoppingCart.getCart(), Arrays.<Integer> asList(Integer.valueOf(1), Integer.valueOf(2)));
	}

	@Test
	public void emptyAndAddToCartInteger()
	{
		shoppingCart.addToCart(0);
		shoppingCart.emptyAndAddToCart(1);
		assertEquals(shoppingCart.getCart(), Arrays.<Integer> asList(Integer.valueOf(1)));
	}

	@Test
	public void emptyAndAddToCartListInteger()
	{
		shoppingCart.addToCart(0);
		shoppingCart.emptyAndAddToCart(Arrays.<Integer> asList(Integer.valueOf(1), Integer.valueOf(2)));
		assertEquals(shoppingCart.getCart(), Arrays.<Integer> asList(Integer.valueOf(1), Integer.valueOf(2)));
	}

	@Test
	public void emptyAndAddToCartInteger_emptyCart()
	{
		shoppingCart.emptyAndAddToCart(1);
		assertEquals(shoppingCart.getCart(), Arrays.<Integer> asList(Integer.valueOf(1)));
	}

	@Test
	public void emptyAndAddToCartListInteger_emptyCart()
	{
		shoppingCart.emptyAndAddToCart(Arrays.<Integer> asList(Integer.valueOf(1), Integer.valueOf(2)));
		assertEquals(shoppingCart.getCart(), Arrays.<Integer> asList(Integer.valueOf(1), Integer.valueOf(2)));
	}

	@Test
	public void emptyCart()
	{
		shoppingCart.addToCart(0);
		shoppingCart.emptyCart();
		assertTrue(shoppingCart.getCart().isEmpty());
	}

	@Test
	public void emptyCart_emptyCart()
	{
		shoppingCart.emptyCart();
		assertTrue(shoppingCart.getCart().isEmpty());
	}

	@Test
	public void getCart()
	{
		shoppingCart.addToCart(1);
		shoppingCart.addToCart(2);
		assertEquals(shoppingCart.getCart(), Arrays.<Integer> asList(Integer.valueOf(1), Integer.valueOf(2)));
	}

	@Test
	public void getCart_emptyCart()
	{
		assertTrue(shoppingCart.getCart().isEmpty());
	}

	@Test
	public void removeFromCartInteger()
	{
		shoppingCart.addToCart(1);
		assertTrue(shoppingCart.removeFromCart(1));
		assertTrue(shoppingCart.getCart().isEmpty());
	}

	@Test
	public void removeFromCartInteger_notInCart()
	{
		shoppingCart.addToCart(1);
		assertFalse(shoppingCart.removeFromCart(0));
		assertEquals(shoppingCart.getCart(), Arrays.<Integer> asList(Integer.valueOf(1)));
	}

	@Test
	public void removeFromCartInteger_emptyCart()
	{
		assertFalse(shoppingCart.removeFromCart(1));
	}

	@Test
	public void removeFromCartListInteger()
	{
		shoppingCart.addToCart(Arrays.<Integer> asList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)));
		assertTrue(shoppingCart.removeFromCart(Arrays.<Integer> asList(Integer.valueOf(1), Integer.valueOf(2))));
		assertEquals(shoppingCart.getCart(), Arrays.<Integer> asList(Integer.valueOf(3)));
	}

	@Test
	public void removeFromCartListInteger_notInCart()
	{
		shoppingCart.addToCart(Arrays.<Integer> asList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)));
		assertFalse(shoppingCart.removeFromCart(Arrays.<Integer> asList(Integer.valueOf(-1), Integer.valueOf(0))));
		assertEquals(shoppingCart.getCart(),
				Arrays.<Integer> asList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)));
	}

	@Test
	public void removeFromCartListInteger_someInCart()
	{
		shoppingCart.addToCart(Arrays.<Integer> asList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)));
		assertTrue(shoppingCart.removeFromCart(Arrays.<Integer> asList(Integer.valueOf(0), Integer.valueOf(1))));
		assertEquals(shoppingCart.getCart(), Arrays.<Integer> asList(Integer.valueOf(2), Integer.valueOf(3)));
	}

	@Test
	public void removeFromCartListInteger_emptyCart()
	{
		assertFalse(shoppingCart.removeFromCart(Arrays.<Integer> asList(Integer.valueOf(1), Integer.valueOf(2))));
	}
}
