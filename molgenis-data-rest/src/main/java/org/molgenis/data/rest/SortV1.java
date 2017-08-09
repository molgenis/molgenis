/*
 * Copyright 2008-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.molgenis.data.rest;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;

/**
 * Copy of org.springframework.data.domain.Sort for backwards compatibility
 * <p>
 * Sort option for queries. You have to provide at least a list of properties to sort for that must not include
 * {@literal null} or empty strings. The direction defaults to {@link SortV1#DEFAULT_DIRECTION}.
 *
 * @author Oliver Gierke
 * @deprecated use {@link org.molgenis.data.Sort} instead.
 */
public class SortV1 implements Iterable<SortV1.OrderV1>, Serializable
{

	private static final long serialVersionUID = 5737186511678863905L;
	public static final DirectionV1 DEFAULT_DIRECTION = DirectionV1.ASC;

	private final List<OrderV1> orders;

	/**
	 * Creates a new {@link SortV1} instance using the given {@link OrderV1}s.
	 *
	 * @param orders must not be {@literal null}.
	 */
	public SortV1(OrderV1... orders)
	{
		this(Arrays.asList(orders));
	}

	/**
	 * Creates a new {@link SortV1} instance.
	 *
	 * @param orders must not be {@literal null} or contain {@literal null}.
	 */
	public SortV1(List<OrderV1> orders)
	{

		if (null == orders || orders.isEmpty())
		{
			throw new IllegalArgumentException("You have to provide at least one sort property to sort by!");
		}

		this.orders = orders;
	}

	/**
	 * Creates a new {@link SortV1} instance. Order defaults to {@link DirectionV1#ASC}.
	 *
	 * @param properties must not be {@literal null} or contain {@literal null} or empty strings
	 */
	public SortV1(String... properties)
	{
		this(DEFAULT_DIRECTION, properties);
	}

	/**
	 * Creates a new {@link SortV1} instance.
	 *
	 * @param direction  defaults to {@link SortV1#DEFAULT_DIRECTION} (for {@literal null} cases, too)
	 * @param properties must not be {@literal null} or contain {@literal null} or empty strings
	 */
	public SortV1(DirectionV1 direction, String... properties)
	{
		this(direction, properties == null ? new ArrayList<>() : Arrays.asList(properties));
	}

	/**
	 * Creates a new {@link SortV1} instance.
	 *
	 * @param direction
	 * @param properties
	 */
	public SortV1(DirectionV1 direction, List<String> properties)
	{

		if (properties == null || properties.isEmpty())
		{
			throw new IllegalArgumentException("You have to provide at least one property to sort by!");
		}

		this.orders = new ArrayList<>(properties.size());

		for (String property : properties)
		{
			this.orders.add(new OrderV1(direction, property));
		}
	}

	/**
	 * Returns a new {@link SortV1} consisting of the {@link OrderV1}s of the current {@link SortV1} combined with the
	 * given ones.
	 *
	 * @param sort can be {@literal null}.
	 * @return
	 */
	public SortV1 and(SortV1 sort)
	{

		if (sort == null)
		{
			return this;
		}

		ArrayList<OrderV1> these = new ArrayList<>(this.orders);

		for (OrderV1 order : sort)
		{
			these.add(order);
		}

		return new SortV1(these);
	}

	/**
	 * Returns the order registered for the given property.
	 *
	 * @param property
	 * @return
	 */
	public OrderV1 getOrderFor(String property)
	{

		for (OrderV1 order : this)
		{
			if (order.getProperty().equals(property))
			{
				return order;
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<OrderV1> iterator()
	{
		return this.orders.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{

		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof SortV1))
		{
			return false;
		}

		SortV1 that = (SortV1) obj;

		return this.orders.equals(that.orders);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{

		int result = 17;
		result = 31 * result + orders.hashCode();
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return StringUtils.join(orders, ',');
	}

	/**
	 * Enumeration for sort directions.
	 *
	 * @author Oliver Gierke
	 */
	public enum DirectionV1
	{

		ASC, DESC;

		/**
		 * Returns the {@link DirectionV1} enum for the given {@link String} value.
		 *
		 * @param value
		 * @return
		 */
		public static DirectionV1 fromString(String value)
		{

			try
			{
				return DirectionV1.valueOf(value.toUpperCase(Locale.US));
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException(String.format(
						"Invalid value '%s' for orders given! Has to be either 'desc' or 'asc' (case insensitive).",
						value), e);
			}
		}
	}

	/**
	 * PropertyPath implements the pairing of an {@link DirectionV1} and a property. It is used to provide input for
	 * {@link SortV1}
	 *
	 * @author Oliver Gierke
	 */
	public static class OrderV1 implements Serializable
	{

		private static final long serialVersionUID = 1522511010900108987L;

		private final DirectionV1 direction;
		private final String property;

		/**
		 * Creates a new {@link OrderV1} instance. if order is {@literal null} then order defaults to
		 * {@link SortV1#DEFAULT_DIRECTION}
		 *
		 * @param direction can be {@literal null}, will default to {@link SortV1#DEFAULT_DIRECTION}
		 * @param property  must not be {@literal null} or empty.
		 */
		public OrderV1(DirectionV1 direction, String property)
		{

			if (!StringUtils.isNotEmpty(property))
			{
				throw new IllegalArgumentException("Property must not null or empty!");
			}

			this.direction = direction == null ? DEFAULT_DIRECTION : direction;
			this.property = property;
		}

		/**
		 * Creates a new {@link OrderV1} instance. Takes a single property. Direction defaults to
		 * {@link SortV1#DEFAULT_DIRECTION}.
		 *
		 * @param property must not be {@literal null} or empty.
		 */
		public OrderV1(String property)
		{
			this(DEFAULT_DIRECTION, property);
		}

		/**
		 * @deprecated use {@link SortV1#SortV1(DirectionV1, List)} instead.
		 */
		@Deprecated
		public static List<OrderV1> create(DirectionV1 direction, Iterable<String> properties)
		{

			List<OrderV1> orders = new ArrayList<>();
			for (String property : properties)
			{
				orders.add(new OrderV1(direction, property));
			}
			return orders;
		}

		/**
		 * Returns the order the property shall be sorted for.
		 *
		 * @return
		 */
		public DirectionV1 getDirection()
		{
			return direction;
		}

		/**
		 * Returns the property to order for.
		 *
		 * @return
		 */
		public String getProperty()
		{
			return property;
		}

		/**
		 * Returns whether sorting for this property shall be ascending.
		 *
		 * @return
		 */
		public boolean isAscending()
		{
			return this.direction.equals(DirectionV1.ASC);
		}

		/**
		 * Returns a new {@link OrderV1} with the given {@link OrderV1}.
		 *
		 * @param order
		 * @return
		 */
		public OrderV1 with(DirectionV1 order)
		{
			return new OrderV1(order, this.property);
		}

		/**
		 * Returns a new {@link SortV1} instance for the given properties.
		 *
		 * @param properties
		 * @return
		 */
		public SortV1 withProperties(String... properties)
		{
			return new SortV1(this.direction, properties);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{

			int result = 17;

			result = 31 * result + direction.hashCode();
			result = 31 * result + property.hashCode();

			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj)
		{

			if (this == obj)
			{
				return true;
			}

			if (!(obj instanceof OrderV1))
			{
				return false;
			}

			OrderV1 that = (OrderV1) obj;

			return this.direction.equals(that.direction) && this.property.equals(that.property);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return String.format("%s: %s", property, direction);
		}
	}
}
