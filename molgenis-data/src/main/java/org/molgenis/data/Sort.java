package org.molgenis.data;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Sort implements Iterable<Sort.Order>
{
	private final List<Order> orders;

	public Sort()
	{
		this(new ArrayList<>());
	}

	public Sort(String attr)
	{
		this(attr, Direction.ASC);
	}

	public Sort(String attr, Direction direction)
	{
		this(Collections.singletonList(new Sort.Order(attr, direction)));
	}

	public Sort(List<Sort.Order> orders)
	{
		this.orders = orders != null ? new ArrayList<>(orders) : new ArrayList<>();
	}

	public Sort(Sort sort)
	{
		orders = new ArrayList<>(sort.orders.size());
		for (Sort.Order order : sort)
		{
			orders.add(new Order(order.getAttr(), order.getDirection()));
		}
	}

	@Override
	public Iterator<Order> iterator()
	{
		return orders.iterator();
	}

	public Sort on(String attr)
	{
		return on(attr, Direction.ASC);
	}

	public Sort on(String attr, Direction direction)
	{
		orders.add(new Order(attr, direction));
		return this;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((orders == null) ? 0 : orders.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Sort other = (Sort) obj;
		if (orders == null)
		{
			if (other.orders != null) return false;
		}
		else if (!orders.equals(other.orders)) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "Sort [orders=" + orders + "]";
	}

	public static Sort parse(String orderByStr)
	{
		Sort sort = new Sort();
		for (String sortClauseStr : StringUtils.split(orderByStr, ';'))
		{
			String[] tokens = StringUtils.split(sortClauseStr, ',');
			if (tokens.length == 1)
			{
				sort.on(tokens[0]);
			}
			else
			{
				sort.on(tokens[0], Direction.valueOf(tokens[1]));
			}
		}
		return sort;
	}

	public String toSortString()
	{
		return orders.stream()
					 .map(order -> order.getAttr() + ',' + order.getDirection().toString())
					 .collect(Collectors.joining(";"));
	}

	public static class Order
	{
		private final String attr;
		private final Direction direction;

		public Order(String attr)
		{
			this(attr, Direction.ASC);
		}

		public Order(String attr, Direction direction)
		{
			this.attr = attr;
			this.direction = direction;
		}

		public String getAttr()
		{
			return attr;
		}

		public Direction getDirection()
		{
			return direction;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((attr == null) ? 0 : attr.hashCode());
			result = prime * result + ((direction == null) ? 0 : direction.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Order other = (Order) obj;
			if (attr == null)
			{
				if (other.attr != null) return false;
			}
			else if (!attr.equals(other.attr)) return false;
			return direction == other.direction;
		}

		@Override
		public String toString()
		{
			return "Order [attr=" + attr + ", direction=" + direction + "]";
		}
	}

	public enum Direction
	{
		ASC, DESC
	}

	public boolean hasField(String attributeName)
	{
		for (Order order : this.orders)
		{
			if (order.getAttr().equals(attributeName))
			{
				return true;
			}
		}
		return false;
	}
}
