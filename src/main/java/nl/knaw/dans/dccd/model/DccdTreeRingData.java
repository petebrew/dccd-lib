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
import java.util.ArrayList;
import java.util.List;

import nl.knaw.dans.dccd.model.entities.DerivedSeriesEntity;
import nl.knaw.dans.dccd.model.entities.Entity;
import nl.knaw.dans.dccd.model.entities.MeasurementSeriesEntity;

import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasValues;

/** contains information for storing and retrieving data in a non-tridas format
 *
 * Serializable is useful for Wicket,
 *
 * @author paulboon
 *
 */
public class DccdTreeRingData implements Serializable {
	private static final long serialVersionUID = -701547326276138833L;

	private TridasProject tridasProject = null;

	private String fileName = "";

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public TridasProject getTridasProject()
	{
		return tridasProject;
	}

	public void setTridasProject(TridasProject tridasProject)
	{
		this.tridasProject = tridasProject;
	}

	// Find TridasValues in the TridasProject
	// and not from a derived series but from a measurement series
	//
	// Note that this is based on the assumption 
	// that The TreeRing Datafiles are referenced from a single series(measurement or derived) 
	// and therefore contain the values of a single series after conversion with the TridasIOLibrary.
	//
	// The values we need to copy are then assumed to be the values of the first (and only) measurement series 
	//
	public List<TridasValues> getTridasValuesForMeasurementSeries()
	{
		List<TridasValues> foundValues = new ArrayList<TridasValues>();// empty

		// find the series
		if (tridasProject != null)
		{
			EntityTree entityTree = new EntityTree();
			entityTree.buildTree(tridasProject);
			List<Entity> entityList = entityTree.getEntities();
			for(Entity entity : entityList)
			{
				if (entity instanceof MeasurementSeriesEntity)
				{
					// Just take the first, and ignore the rest
					MeasurementSeriesEntity series = (MeasurementSeriesEntity)entity;
					// get the values
					TridasMeasurementSeries tridasSeries = (TridasMeasurementSeries)series.getTridasAsObject();
					foundValues = tridasSeries.getValues();
					break;
				}
			}
		}
		return foundValues;
	}
	
	/**
	 * For derived series, look into derived first, and when nothing found look into the measurement series
	 * 
	 * @return
	 */
	public List<TridasValues> getTridasValuesForDerivedSeries()
	{
		List<TridasValues> foundValues = new ArrayList<TridasValues>();// empty

		// find the series
		if (tridasProject != null)
		{
			EntityTree entityTree = new EntityTree();
			entityTree.buildTree(tridasProject);
			List<Entity> entityList = entityTree.getEntities();
			for(Entity entity : entityList)
			{
				if (entity instanceof DerivedSeriesEntity)
				{
					// Just take the first, and ignore the rest
					DerivedSeriesEntity series = (DerivedSeriesEntity)entity;
					// get the values
					TridasDerivedSeries tridasSeries = (TridasDerivedSeries)series.getTridasAsObject();
					foundValues = tridasSeries.getValues();
					break; // Found
				}
				
				// when nothing in derived, look into measurement series
				if (entity instanceof MeasurementSeriesEntity)
				{
					// Just take the first, and ignore the rest
					MeasurementSeriesEntity series = (MeasurementSeriesEntity)entity;
					// get the values
					TridasMeasurementSeries tridasSeries = (TridasMeasurementSeries)series.getTridasAsObject();
					foundValues = tridasSeries.getValues();
					break; //Found
				}
			}
		}
		return foundValues;
	}
}
