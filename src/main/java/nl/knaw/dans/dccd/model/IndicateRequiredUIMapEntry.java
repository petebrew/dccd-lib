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

/**
 * Indicate (show) that the entry is required by DCCD (but not necessarily by TRiDaS)
 */
public class IndicateRequiredUIMapEntry extends UIMapEntry
{
	private static final long	serialVersionUID	= -5417407203578231060L;

	public IndicateRequiredUIMapEntry(String name, String method)
	{
		super(name, method);
	}

	public IndicateRequiredUIMapEntry(String name, String method, String panel)
	{
		super(name, method, panel);
	}

	public IndicateRequiredUIMapEntry(String name, String method, String panel,
			boolean openAccess)
	{
		super(name, method, panel, openAccess);
	}

	public IndicateRequiredUIMapEntry(String name, String method, String panel, Multiplicity multiplicity)
	{
		super(name, method, panel, multiplicity);
	}

	public IndicateRequiredUIMapEntry(String name, String method, String panel, Multiplicity multiplicity,
			boolean openAccess)
	{
		super(name, method, panel, multiplicity, openAccess);
	}

	@Override
	public boolean isRequired()
	{
		return true;
	}
}
