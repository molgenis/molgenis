package org.molgenis.exceptions;

public class TooManyQueriesException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -747526791049239149L;
	public TooManyQueriesException(){
		
	}
	public TooManyQueriesException(String msg){
		super(msg);
	}
}
