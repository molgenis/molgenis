package org.molgenis.dataexplorer.event;

import org.springframework.context.ApplicationEvent;

public abstract class DataExplorerRegisterEvent extends ApplicationEvent
{
	private static final long serialVersionUID = 1L;

	public static enum Type
	{
		REGISTER, DEREGISTER
	}

	private final Type type;
	private final String id;

	public DataExplorerRegisterEvent(Type type, String id, Object source)
	{
		super(source);
		if (type == null) throw new IllegalArgumentException("type cannot be null");
		if (id == null) throw new IllegalArgumentException("id cannot be null");
		this.type = type;
		this.id = id;
	}

	public Type getType()
	{
		return type;
	}

	public String getId()
	{
		return id;
	}

	@Override
	public String toString()
	{
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(type.toString());
		stringBuilder.append(' ');
		stringBuilder.append(this.getClass().getSimpleName());
		stringBuilder.append('[').append(id).append(']');
		return stringBuilder.toString();
	}
}
