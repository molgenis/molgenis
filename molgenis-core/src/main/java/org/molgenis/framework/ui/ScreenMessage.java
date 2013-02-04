/**
 * File: invengine.tdg.TDG <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
 * Changelog:
 * <ul>
 * <li>2005-11-30; 1.0.0; RA Scheltema; Creation.
 * </ul>
 */

package org.molgenis.framework.ui;

import java.io.Serializable;

import org.molgenis.util.Entity;

// imports

/**
 * This class is a combining place for the information, which is generated when
 * executing a query or update on a database. For each action on the database
 * this type of message is returned.
 * 
 * @author Richard Scheltema
 * @version 1.0.0
 */
public class ScreenMessage implements Serializable
{
	private static final long serialVersionUID = 1L;

	// constructor
	/**
	 * Standard constructor, where all the information about the message can be
	 * set.
	 * 
	 * @param message
	 *            The string representation of the message.
	 * @param entity
	 *            Pointer to the entity used in the database transaction.
	 * @param success
	 *            Indicates whether the transaction was succesfull.
	 */
	public ScreenMessage(String message, Entity entity, boolean success)
	{
		this.entity = entity;
		this.message = message;
		this.success = success;
	}

	public ScreenMessage(String message, boolean success)
	{
		this.message = message;
		this.success = success;
	}

	// access
	/**
	 * Returns the string representation of this message.
	 * 
	 * @return The message.
	 */
	public String getText()
	{
		if (message == null) return "Unknown error";
		return message;
	}

	public String test()
	{
		return "test";
	}

	/**
	 * Returns the pointer to the entity used in the database transaction.
	 * 
	 * @return The entity.
	 */
	public Entity getEntity()
	{
		return entity;
	}

	/**
	 * Returns whether the database transaction was executed succesfully.
	 * 
	 * @return Success of failure.
	 */
	public boolean isSuccess()
	{
		return success ? true : false;
	}

	/**
	 * Returns a string-representation of this message.
	 * 
	 * @return The string representation of this message.
	 */
	@Override
	public String toString()
	{
		return getText() + ":" + (isSuccess() ? "success" : "failed");
	}

	public String getHtml()
	{
		/*
		 * <#if screen.message.isSuccess()><span
		 * style="color: green">${screen.message.message}</span><p/>
		 * <#else><span
		 * style="color: red">${screen.message.message}</span><p/></#if>
		 */
		if (getText().length() > 0) return "<span style=\"color: " + (isSuccess() ? "green" : "red") + "\">"
				+ getText() + "</span>";
		else
			return "<span><!-- no message--></span>";

	}

	// member variables
	/** The string representation of this message */
	private String message;

	/** The entity that was used in the database and generated this message */
	private Entity entity;

	/** Whether the operation on the database was succesfull */
	private boolean success;
}
