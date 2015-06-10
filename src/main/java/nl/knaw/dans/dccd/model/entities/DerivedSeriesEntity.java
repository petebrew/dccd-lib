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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import nl.knaw.dans.dccd.model.ProjectPermissionLevel;

import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasValues;

public class DerivedSeriesEntity extends AbstractEntity
{
	private static final long	serialVersionUID	= -630707665897017532L;
	private TridasDerivedSeries	tridasDerivedSeries	= null;
	private List<TridasValues>	valuesPruned		= null;

	public DerivedSeriesEntity()
	{
		// empty
	}

	public DerivedSeriesEntity(TridasDerivedSeries tridasDerivedSeries)
	{
		this.tridasDerivedSeries = tridasDerivedSeries;
		setTitle(tridasDerivedSeries.getTitle());
	}

	public ProjectPermissionLevel getPermissionLevel()
	{
		return ProjectPermissionLevel.SERIES;
	}

	@Override
	public String getUnitLabel()
	{
		return "DerivedSeriesEntity";
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class getTridasClass()
	{
		return TridasDerivedSeries.class;
	}
	
	@Override
	public void buildEntitySubTree()
	{
		// clear the list of subentities first
		List<Entity> subEntities = getDendroEntities();
		subEntities.clear();

		// use the tridas entity, to find sub-entity tridas data
		if (tridasDerivedSeries == null)
			return; // nothing to do

		// values
		Iterator<TridasValues> iVals = tridasDerivedSeries.getValues().iterator();
		while (iVals.hasNext())
		{
			TridasValues values = iVals.next();
			ValuesEntity valuesEntity = new ValuesEntity(values);
			subEntities.add(valuesEntity);
			valuesEntity.buildEntitySubTree();// go deeper
		}

		// Note: instead of depth first,
		// we could call the createTree of all sub-entities here }
	}

	@Override
	public Object getTridasAsObject()
	{
		return tridasDerivedSeries;
	}

	@Override
	public void setTridasObject(Object tridas)
	{
		tridasDerivedSeries = (TridasDerivedSeries) tridas;
	}

	public String getTridasTitle()
	{
		if (hasTridas()) 
			return tridasDerivedSeries.getTitle();
		else
			return null;
	}
	
	@Override
	public void connectTridasObjectTree()
	{
		if (tridasDerivedSeries == null)
			return; // nothing to do
		
		List<Entity> subEntities = getDendroEntities();
		for (Entity subEntity : subEntities)
		{
			// all subentities are values
			TridasValues valuesTridas = (TridasValues) subEntity.getTridasAsObject();
			tridasDerivedSeries.getValues().add(valuesTridas);
			subEntity.connectTridasObjectTree();
		}
	}

	@Override
	protected void pruneTridas()
	{
		TridasDerivedSeries objectToPrune = tridasDerivedSeries;

		if (objectToPrune != null)
		{
			valuesPruned = objectToPrune.getValues();
			List<TridasValues> empty = Collections.emptyList();
			objectToPrune.setValues(empty);
		}
	}

	@Override
	protected void unpruneTridas()
	{
		TridasDerivedSeries objectToUnprune = tridasDerivedSeries;

		if (objectToUnprune != null && 
			valuesPruned != null)
		{
			objectToUnprune.setValues(valuesPruned);
			valuesPruned = null;
		}
	}
	
	public List<String> getAssociatedFileNames()
	{
		List<String> resultList = new ArrayList<String>();
		// empty
		return resultList;
	}
}
