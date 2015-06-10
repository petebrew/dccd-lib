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
package nl.knaw.dans.dccd.model.entities;

import java.util.ArrayList;
import java.util.List;

import nl.knaw.dans.dccd.model.ProjectPermissionLevel;

import org.tridas.schema.TridasValues;

/**
 * Note that values are a bit different, because the are at the end of the hierarchy and not a TridasEntity?
 * 
 * @author paulboon
 */
public class ValuesEntity extends AbstractEntity
{
	private static final long	serialVersionUID	= -5474928479154006698L;
	private TridasValues		tridasValues		= null;

	public ValuesEntity()
	{
		// empty
	}

	public ValuesEntity(TridasValues tridasValues)
	{
		this.tridasValues = tridasValues;
		setTitle(""); // empty sting
	}

	public ProjectPermissionLevel getPermissionLevel()
	{
		return ProjectPermissionLevel.VALUES;
	}

	@Override
	public String getUnitLabel()
	{
		return "ValuesEntity";
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class getTridasClass()
	{
		return TridasValues.class;
	}

	@Override
	public void buildEntitySubTree()
	{
		// leave node
	}

	@Override
	public Object getTridasAsObject()
	{
		return tridasValues;
	}

	@Override
	public void setTridasObject(Object tridas)
	{
		tridasValues = (TridasValues) tridas;
	}

	public String getTridasTitle()
	{
		return null;
	}
	
	@Override
	public void connectTridasObjectTree()
	{
		// empty, nothing to connect
	}

	@Override
	protected void pruneTridas()//Object o)
	{
		// nothing to prune
	}

	@Override
	protected void unpruneTridas()//Object o)
	{
		// nothing to unprune
	}
	
	public List<String> getAssociatedFileNames()
	{
		List<String> resultList = new ArrayList<String>();
		// empty
		return resultList;
	}
}
