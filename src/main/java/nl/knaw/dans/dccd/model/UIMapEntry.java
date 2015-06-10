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
package nl.knaw.dans.dccd.model;

import java.io.Serializable;

/**
 * Mapping information is contained in the map entries: - Name, used for
 * displaying the data as in title: "Title" - Method, for getting or setting the
 * value using invoke, getTitle() when the string is *** both get*** and set***
 * must exist.
 *
 * made serializable for Wicket
 *
 * @see nl.knaw.dans.dccd.application.services.UIMapper TODO: unit testing
 *
 */
public class UIMapEntry implements Serializable
{
	private static final long serialVersionUID = 5863719899376653711L;
	private String name; // short descriptive 'label' for display in UI
	private String method; // method for getting value
	private String panel; // name of the panel to use for the UI
	private boolean openAccess = false; // indicates if it has open access information
	private Multiplicity multiplicity = Multiplicity.SINGLE;// if a panel is to be repeated
	private boolean required = false;

	public enum Multiplicity {
		SINGLE, 
		MULTIPLE,
		OPTIONAL
	};
	
	public UIMapEntry(String name, String method)
	{
		setName(name);
		setMethod(method);
		setPanel("");
	}

	public UIMapEntry(String name, String method, String panel)
	{
		setName(name);
		setMethod(method);
		setPanel(panel);
	}

	public UIMapEntry(String name, String method, String panel,
			boolean openAccess)
	{
		setName(name);
		setMethod(method);
		setPanel(panel);
		this.openAccess = openAccess;
	}

	public UIMapEntry(String name, String method, Multiplicity multiplicity)
	{
		setName(name);
		setMethod(method);
		setPanel("");
		this.multiplicity = multiplicity;
	}
	
	public UIMapEntry(String name, String method, String panel, Multiplicity multiplicity)
	{
		setName(name);
		setMethod(method);
		setPanel(panel);
		this.multiplicity = multiplicity;
	}

	public UIMapEntry(String name, String method, String panel, Multiplicity multiplicity,
			boolean openAccess)
	{
		setName(name);
		setMethod(method);
		setPanel(panel);
		this.multiplicity = multiplicity;
		this.openAccess = openAccess;
	}
	
	public boolean isRequired()
	{
		return required;
	}

	// Maybe use a derived RequiredUIMapEntry instead?
	//public UIMapEntry setRequired(boolean required)
	//{
	//	this.required = required;
	//	return this;
	//}
	
	public Multiplicity getMultiplicity()
	{
		return multiplicity;
	}
	
	public void setMultiplicity(Multiplicity multiplicity)
	{
		this.multiplicity = multiplicity;
	}

	public boolean isOpenAccess()
	{
		return openAccess;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getMethod()
	{
		return method;
	}

	public void setMethod(String method)
	{
		this.method = method;
	}

	public String getPanel()
	{
		return panel;
	}

	public void setPanel(String panel)
	{
		this.panel = panel;
	}

}
