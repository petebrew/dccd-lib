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

import java.util.ArrayList;
import java.util.List;

import nl.knaw.dans.dccd.model.entities.DerivedSeriesEntity;
import nl.knaw.dans.dccd.model.entities.MeasurementSeriesEntity;

import org.apache.log4j.Logger;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasMeasurementSeries;

// Detect (find) references to external tree ring data (value) files
// which needed to be uploaded separately
public class ProjectTreeRingDataFileDetector
{
	private static Logger logger = Logger.getLogger(ProjectTreeRingDataFileDetector.class);
	
	public static final boolean isTreeRingDataFileIndicator(String formatString)
	{
		boolean isIndicator = false;
		
		// After a file has been uploaded we have a different indicator (Project.DATAFILE_INDICATOR_UPLOADED). 
		// we don't need to upload them again because the conversion has been done 
		// and the values are in the TRiDaS xml.
		if(Project.DATAFILE_INDICATOR.equalsIgnoreCase(formatString))
		{
			isIndicator = true;
		}
		
		return isIndicator;

		// Note: we could use different strings for each format and check if 
		// it starts with Project.DATAFILE_INDICATOR = "dccd.treeringdatafile"
	}

	/**
	 * get the list of filenames referenced by the project as measurement data (in external files)
	 *
	 * @return The list of names
	 */
	public static List<String> getProjectTreeRingDataFileNames(Project project)
	{
		List<String> resultList = new ArrayList<String>();

		List<MeasurementSeriesEntity> measurementList = project.getMeasurementSeriesEntities();
		// only series with external file ref's
		for(MeasurementSeriesEntity measurementSeries : measurementList)
		{
			// check the genericFields
			TridasMeasurementSeries tridas = (TridasMeasurementSeries)measurementSeries.getTridasAsObject();

			if (tridas.isSetGenericFields())
			{
				// Ok we have potential candidates
				List<TridasGenericField> fields = tridas.getGenericFields();
				for(TridasGenericField field : fields)
				{
					if (field.isSetValue() && field.isSetName() &&
						isTreeRingDataFileIndicator(field.getName()))
					{
						resultList.add(field.getValue());
						break; // only one, the first in this case!
					}
				}
			}
		}

		// also derived series
		// TODO refactor into two functions
		List<DerivedSeriesEntity> derivedList = project.getDerivedSeriesEntities();
		// only series with external file ref's
		for(DerivedSeriesEntity derivedtSeries : derivedList)
		{
			// check the genericFields
			TridasDerivedSeries tridas = (TridasDerivedSeries)derivedtSeries.getTridasAsObject();

			if (tridas.isSetGenericFields())
			{
				// Ok we have potential candidates
				List<TridasGenericField> fields = tridas.getGenericFields();
				for(TridasGenericField field : fields)
				{
					if (field.isSetValue() && field.isSetName() &&
						isTreeRingDataFileIndicator(field.getName()))
					{
						resultList.add(field.getValue());
						break; // only one, the first in this case!
					}
				}
			}
		}
		
		return resultList;
	}
}
