/*******************************************************************************
 * Copyright 2015 DANS - Data Archiving and Networked Services
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package nl.knaw.dans.dccd.application.services;

import java.io.Serializable;

public class ValidationErrorMessage implements Serializable 
{
	private static final long	serialVersionUID	= -4727764166481970253L;

	private String message = "";
	private String className = "";// class name (name of the Tridas class)
	private String fieldName = "";
	private String entityId = "";

	ValidationErrorMessage(String message, String className, String fieldName)
	{
		this.message = message;
		this.className = className;
		this.fieldName = fieldName;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getClassName()
	{
		return className;
	}

	public void setClassName(String className)
	{
		this.className = className;
	}
	
	public String getFieldName()
	{
		return fieldName;
	}

	public void setFieldName(String fieldName)
	{
		this.fieldName = fieldName;
	}
	
	public String getEntityId()
	{
		return entityId;
	}

	public void setEntityId(String entityId)
	{
		this.entityId = entityId;
	}
	
	// Message needs to be improved for the UI, so we convert it:
	public String getFieldNameInUIStyle()
	{
		return convertFromJavaCodeStyleToUI(fieldName);
	}
	
	// Convert fieldname from java code style to UI style
	// "person[3].address.city" -> "person #3 address city"
	private String convertFromJavaCodeStyleToUI(String fieldNameJCStyle)
	{	
		final String fieldNameUISeperator = " > ";
		
		// split on '.'
		String [] splitNames = fieldNameJCStyle.split("\\.");
		// find indexes
		for(int i = 0; i < splitNames.length; i++)
		{
			String name = splitNames[i];
			// if the last character is ']' we have an index
			if (name.endsWith("]"))
			{
				// convert [<n>] to #<n+1>
				int indexStart = name.indexOf('[');
				String namePart = name.substring(0, indexStart);
				// only the part in between the '[' and ']'
				String indexPart = name.substring(indexStart+1, name.length()-1);
				// parse index
				int index = Integer.parseInt(indexPart);
				index++;
				splitNames[i] = namePart + " #" + index; 
			}
		}
		
		// combine with spaces, maybe use stringBuilder...
		String fieldNameUIStyle = splitNames[0]; // fieldName cannot be empty!
		for(int i = 1; i < splitNames.length; i++)
		{
			fieldNameUIStyle += fieldNameUISeperator + splitNames[i];
		}
		
		return fieldNameUIStyle;
	}
}
