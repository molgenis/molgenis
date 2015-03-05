package org.molgenis.exceptions;

public class MissingValueException extends Exception
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8255558306664656524L;
	
	public MissingValueException(){
		
	}
	
	public MissingValueException(String msg){
		super(msg);
	}

}
