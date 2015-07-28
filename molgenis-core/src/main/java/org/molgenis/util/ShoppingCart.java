package org.molgenis.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShoppingCart implements Serializable
{
	private static final long serialVersionUID = 1L;

	private List<Integer> cartItems;

	public void addToCart(Integer id)
	{
		if (id == null) throw new IllegalArgumentException("id is null");
		if (cartItems == null) cartItems = new ArrayList<Integer>();
		cartItems.add(id);
	}

	public void addToCart(List<Integer> ids)
	{
		if (ids == null) throw new IllegalArgumentException("ids is null");
		if (cartItems == null) cartItems = new ArrayList<Integer>();
		cartItems.addAll(ids);
	}

	public boolean removeFromCart(Integer id)
	{
		return cartItems != null ? cartItems.remove(id) : false;
	}

	public boolean removeFromCart(List<Integer> ids)
	{
		return cartItems != null ? cartItems.removeAll(ids) : false;
	}

	public void emptyCart()
	{
		if (cartItems != null) cartItems.clear();
	}

	public void emptyAndAddToCart(Integer id)
	{
		emptyCart();
		addToCart(id);
	}

	public void emptyAndAddToCart(List<Integer> ids)
	{
		emptyCart();
		addToCart(ids);
	}

	public List<Integer> getCart()
	{
		return Collections.unmodifiableList(cartItems != null ? cartItems : Collections.<Integer> emptyList());
	}
}
