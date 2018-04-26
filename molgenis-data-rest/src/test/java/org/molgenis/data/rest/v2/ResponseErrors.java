package org.molgenis.data.rest.v2;

import java.util.ArrayList;
import java.util.List;

class ResponseErrors
{
	private List<Message> errors = new ArrayList<>();

	void setErrors(List<Message> errors)
	{
		this.errors = errors;
	}

	List<Message> getErrors()
	{
		return this.errors;
	}

	class Message
	{
		private String message;

		void setMessage(String message)
		{
			this.message = message;
		}

		String getMessage()
		{
			return this.message;
		}

	}
}