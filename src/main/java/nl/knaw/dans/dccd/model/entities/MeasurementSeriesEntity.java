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

import javax.xml.datatype.XMLGregorianCalendar;

import nl.knaw.dans.dccd.model.ProjectPermissionLevel;
import nl.knaw.dans.dccd.search.DccdSB;
import nl.knaw.dans.dccd.tridas.TridasYearConvertor;

import org.joda.time.DateTime;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasMeasuringMethod;
import org.tridas.schema.TridasStatFoundation;
import org.tridas.schema.TridasValues;
import org.tridas.schema.Year;

public class MeasurementSeriesEntity extends AbstractEntity
{
	// private static Logger logger = Logger.getLogger(MeasurementSeriesEntity.class);

	private static final long		serialVersionUID		= -3893997234665008778L;
	private TridasMeasurementSeries	tridasMeasurementSeries	= null;
	private List<TridasValues>		valuesPruned			= null;

	public MeasurementSeriesEntity()
	{
		// empty
	}

	public MeasurementSeriesEntity(TridasMeasurementSeries tridasMeasurementSeries)
	{
		this.tridasMeasurementSeries = tridasMeasurementSeries;
		setTitle(tridasMeasurementSeries.getTitle());
	}

	public ProjectPermissionLevel getPermissionLevel()
	{
		return ProjectPermissionLevel.SERIES;
	}

	@Override
	public String getUnitLabel()
	{
		return "MeasurementSeriesEntity";
	}

	@Override
	public DccdSB fillSearchBean(DccdSB searchBean)
	{
		if (hasTridas())
		{
			// tridas.measurementSeries.title
			List<String> tridasMeasurementseriesTitle = searchBean.getTridasMeasurementseriesTitle();
			if (tridasMeasurementseriesTitle == null)
				tridasMeasurementseriesTitle = new ArrayList<String>();
			if (tridasMeasurementSeries.isSetTitle())
			{
				tridasMeasurementseriesTitle.add(tridasMeasurementSeries.getTitle());
			}
			searchBean.setTridasMeasurementseriesTitle(tridasMeasurementseriesTitle);

			// tridas.measurementSeries.identifier
			List<String> tridasMeasurementseriesIdentifier = searchBean.getTridasMeasurementseriesIdentifier();
			if (tridasMeasurementseriesIdentifier == null)
				tridasMeasurementseriesIdentifier = new ArrayList<String>();
			if (tridasMeasurementSeries.isSetIdentifier() && tridasMeasurementSeries.getIdentifier().isSetValue())
			{
				// Note: ignore domain
				tridasMeasurementseriesIdentifier.add(tridasMeasurementSeries.getIdentifier().getValue());
			}
			// BUG! searchBean.setTridasRadiusIdentifier(tridasMeasurementseriesIdentifier);
			searchBean.setTridasMeasurementseriesIdentifier(tridasMeasurementseriesIdentifier);

			// tridas.measurementSeries.analyst
			List<String> tridasMeasurementseriesAnalyst = searchBean.getTridasMeasurementseriesAnalyst();
			if (tridasMeasurementseriesAnalyst == null)
				tridasMeasurementseriesAnalyst = new ArrayList<String>();
			if (tridasMeasurementSeries.isSetAnalyst())
			{
				tridasMeasurementseriesAnalyst.add(tridasMeasurementSeries.getAnalyst());
			}
			searchBean.setTridasMeasurementseriesAnalyst(tridasMeasurementseriesAnalyst);

			// tridas.measurementSeries.dendrochronologist
			List<String> tridasMeasurementseriesDendrochronologist = searchBean.getTridasMeasurementseriesDendrochronologist();
			if (tridasMeasurementseriesDendrochronologist == null)
				tridasMeasurementseriesDendrochronologist = new ArrayList<String>();
			if (tridasMeasurementSeries.isSetDendrochronologist())
			{
				tridasMeasurementseriesDendrochronologist.add(tridasMeasurementSeries.getDendrochronologist());
			}
			searchBean.setTridasMeasurementseriesDendrochronologist(tridasMeasurementseriesDendrochronologist);

			// tridas.measurementSeries.measuringDate
			if (tridasMeasurementSeries.isSetMeasuringDate() && tridasMeasurementSeries.getMeasuringDate().isSetValue())
			{
				XMLGregorianCalendar xmlDate = tridasMeasurementSeries.getMeasuringDate().getValue();
				DateTime dateTime = new DateTime(xmlDate.toGregorianCalendar());

				// add to the list
				List<DateTime> measuringDate = searchBean.getTridasMeasurementseriesMeasuringdate();
				if (measuringDate == null)
					measuringDate = new ArrayList<DateTime>();
				measuringDate.add(dateTime);
				searchBean.setTridasMeasurementseriesMeasuringdate(measuringDate);
			}

			// tridas.measurementSeries.measuringMethod
			// tridas.measurementSeries.measuringMethod.normal
			List<String> tridasMeasurementseriesMeasuringmethod = searchBean.getTridasMeasurementseriesMeasuringmethod();
			if (tridasMeasurementseriesMeasuringmethod == null)
				tridasMeasurementseriesMeasuringmethod = new ArrayList<String>();
			List<String> tridasMeasurementseriesMeasuringmethodNormal = searchBean.getTridasMeasurementseriesMeasuringmethodNormal();
			if (tridasMeasurementseriesMeasuringmethodNormal == null)
				tridasMeasurementseriesMeasuringmethodNormal = new ArrayList<String>();
			if (tridasMeasurementSeries.isSetMeasuringMethod())
			{
				TridasMeasuringMethod method = tridasMeasurementSeries.getMeasuringMethod();

				if (method.isSetValue())
					tridasMeasurementseriesMeasuringmethod.add(method.getValue());
				else
					tridasMeasurementseriesMeasuringmethod.add("");

				if (method.isSetNormal())
					tridasMeasurementseriesMeasuringmethodNormal.add(method.getNormal());
				else
					tridasMeasurementseriesMeasuringmethodNormal.add("");
			}
			searchBean.setTridasMeasurementseriesMeasuringmethod(tridasMeasurementseriesMeasuringmethod);
			searchBean.setTridasMeasurementseriesMeasuringmethodNormal(tridasMeasurementseriesMeasuringmethodNormal);

			// tridas.measurementSeries.interpretationUnsolved
			List<String> tridasMeasurementseriesInterpretationunsolved = searchBean.getTridasMeasurementseriesInterpretationunsolved();
			if (tridasMeasurementseriesInterpretationunsolved == null)
				tridasMeasurementseriesInterpretationunsolved = new ArrayList<String>();
			if (tridasMeasurementSeries.isSetInterpretationUnsolved())
			{
				// tridasMeasurementseriesInterpretationunsolved.add(tridasMeasurementSeries.getInterpretationUnsolved());
				// Note: in TRiDaSv1.2.1 it changed from String to a Class with no information
				// Therefore I add an empty string here
				tridasMeasurementseriesInterpretationunsolved.add("");
			}
			searchBean.setTridasMeasurementseriesInterpretationunsolved(tridasMeasurementseriesInterpretationunsolved);

			if (tridasMeasurementSeries.isSetInterpretation())
			{
				TridasInterpretation interpretation = tridasMeasurementSeries.getInterpretation();

				// tridas.measurementSeries.interpretation.provenance
				List<String> tridasMeasurementseriesInterpretationProvenance = searchBean.getTridasMeasurementseriesInterpretationProvenance();
				if (tridasMeasurementseriesInterpretationProvenance == null)
					tridasMeasurementseriesInterpretationProvenance = new ArrayList<String>();
				if (interpretation.isSetProvenance())
					tridasMeasurementseriesInterpretationProvenance.add(interpretation.getProvenance());
				else
					tridasMeasurementseriesInterpretationProvenance.add("");
				searchBean.setTridasMeasurementseriesInterpretationProvenance(tridasMeasurementseriesInterpretationProvenance);

				// Note: removed in TRiDaSv1.2.1
				/*
				 * //tridas.measurementSeries.interpretation.usedSoftware List<String> tridasMeasurementseriesInterpretationUsedsoftware =
				 * searchBean.getTridasMeasurementseriesInterpretationUsedsoftware(); if (tridasMeasurementseriesInterpretationUsedsoftware == null)
				 * tridasMeasurementseriesInterpretationUsedsoftware = new ArrayList<String>(); if (interpretation.isSetUsedSoftware())
				 * tridasMeasurementseriesInterpretationUsedsoftware.add(interpretation.getUsedSoftware()); else
				 * tridasMeasurementseriesInterpretationUsedsoftware.add("");
				 * searchBean.setTridasMeasurementseriesInterpretationUsedsoftware(tridasMeasurementseriesInterpretationUsedsoftware);
				 */

				// tridas.measurementSeries.interpretation.deathYear
				if (interpretation.isSetDeathYear() && interpretation.getDeathYear().isSetValue())
				{
					Year year = tridasMeasurementSeries.getInterpretation().getDeathYear();
					
					// only index non-relative years
					if (!year.isSetSuffix() || year.getSuffix() != DatingSuffix.RELATIVE) 
					{
						Integer yearInteger = TridasYearConvertor.tridasYearToInteger(year);
	
						// add to the list
						List<Integer> interpretationDeathYear = searchBean.getTridasMeasurementseriesInterpretationDeathyear();
						if (interpretationDeathYear == null)
							interpretationDeathYear = new ArrayList<Integer>();
						interpretationDeathYear.add(yearInteger);
						searchBean.setTridasMeasurementseriesInterpretationDeathyear(interpretationDeathYear);
					}
				}

				// tridas.measurementSeries.interpretation.firstYear
				if (interpretation.isSetFirstYear() && interpretation.getFirstYear().isSetValue())
				{
					Year year = tridasMeasurementSeries.getInterpretation().getFirstYear();
					// only index non-relative years
					if (!year.isSetSuffix() || year.getSuffix() != DatingSuffix.RELATIVE) 
					{
						Integer yearInteger = TridasYearConvertor.tridasYearToInteger(year);
	
						// add to the list
						List<Integer> interpretationFirstYear = searchBean.getTridasMeasurementseriesInterpretationFirstyear();
						if (interpretationFirstYear == null)
							interpretationFirstYear = new ArrayList<Integer>();
						interpretationFirstYear.add(yearInteger);
						searchBean.setTridasMeasurementseriesInterpretationFirstyear(interpretationFirstYear);
					}
				}

				// tridas.measurementSeries.interpretation.lastYear
				if (interpretation.isSetLastYear() && interpretation.getLastYear().isSetValue())
				{
					Year year = tridasMeasurementSeries.getInterpretation().getLastYear();
					// only index non-relative years
					if (!year.isSetSuffix() || year.getSuffix() != DatingSuffix.RELATIVE) 
					{
						Integer yearInteger = TridasYearConvertor.tridasYearToInteger(year);
	
						// add to the list
						List<Integer> interpretationLastYear = searchBean.getTridasMeasurementseriesInterpretationLastyear();
						if (interpretationLastYear == null)
							interpretationLastYear = new ArrayList<Integer>();
						interpretationLastYear.add(yearInteger);
						searchBean.setTridasMeasurementseriesInterpretationLastyear(interpretationLastYear);
					}
				}
				
				// tridas.measurementSeries.interpretation.pithYear
				// was tridas.measurementSeries.interpretation.sproutYear
				if (interpretation.isSetPithYear() && interpretation.getPithYear().isSetValue())
				{
					Year year = tridasMeasurementSeries.getInterpretation().getPithYear();
					// only index non-relative years
					if (!year.isSetSuffix() || year.getSuffix() != DatingSuffix.RELATIVE) 
					{
						Integer yearInteger = TridasYearConvertor.tridasYearToInteger(year);
	
						// add to the list
						List<Integer> interpretationPithYear = searchBean.getTridasMeasurementseriesInterpretationPithyear();
						if (interpretationPithYear == null)
							interpretationPithYear = new ArrayList<Integer>();
						interpretationPithYear.add(yearInteger);
						searchBean.setTridasMeasurementseriesInterpretationPithyear(interpretationPithYear);
					}
				}

				// tridas.measurementSeries.interpretation.statFoundation.statValue
				// <field name="tridas.measurementSeries.interpretation.statFoundation.usedSoftware" type="text" indexed="true" stored="true" multiValued="true"
				// />
				// <field name="tridas.measurementSeries.interpretation.statFoundation.type" type="text" indexed="true" stored="true" multiValued="true" />
				// <field name="tridas.measurementSeries.interpretation.statFoundation.type.normal" type="text" indexed="true" stored="true" multiValued="true"
				// />
				// <!-- xs:double -->
				// <field name="tridas.measurementSeries.interpretation.statFoundation.significanceLevel" type="double" indexed="true" stored="true"
				// multiValued="true" />
				if (interpretation.isSetStatFoundations())
				{
					List<TridasStatFoundation> statFoundations = tridasMeasurementSeries.getInterpretation().getStatFoundations();

					List<Double> interpretationStatFoundationStatValue = searchBean.getTridasMeasurementseriesInterpretationStatfoundationStatvalue();
					if (interpretationStatFoundationStatValue == null)
						interpretationStatFoundationStatValue = new ArrayList<Double>();
					List<String> tridasMeasurementseriesInterpretationStatfoundationUsedsoftware = searchBean
							.getTridasMeasurementseriesInterpretationStatfoundationUsedsoftware();
					if (tridasMeasurementseriesInterpretationStatfoundationUsedsoftware == null)
						tridasMeasurementseriesInterpretationStatfoundationUsedsoftware = new ArrayList<String>();
					List<String> tridasMeasurementseriesInterpretationStatfoundationType = searchBean
							.getTridasMeasurementseriesInterpretationStatfoundationType();
					if (tridasMeasurementseriesInterpretationStatfoundationType == null)
						tridasMeasurementseriesInterpretationStatfoundationType = new ArrayList<String>();
					List<String> tridasMeasurementseriesInterpretationStatfoundationTypeNormal = searchBean
							.getTridasMeasurementseriesInterpretationStatfoundationTypeNormal();
					if (tridasMeasurementseriesInterpretationStatfoundationTypeNormal == null)
						tridasMeasurementseriesInterpretationStatfoundationTypeNormal = new ArrayList<String>();
					List<Double> tridasMeasurementseriesInterpretationStatfoundationSignificancelevel = searchBean
							.getTridasMeasurementseriesInterpretationStatfoundationSignificancelevel();
					if (tridasMeasurementseriesInterpretationStatfoundationSignificancelevel == null)
						tridasMeasurementseriesInterpretationStatfoundationSignificancelevel = new ArrayList<Double>();

					for (TridasStatFoundation statFoundation : statFoundations)
					{
						if (statFoundation.isSetStatValue())
						{
							Double statValue = statFoundation.getStatValue().doubleValue();
							// Note: could (in theory) have rounding errors
							interpretationStatFoundationStatValue.add(statValue);
						}

						if (statFoundation.isSetUsedSoftware())
							tridasMeasurementseriesInterpretationStatfoundationUsedsoftware.add(statFoundation.getUsedSoftware());

						if (statFoundation.isSetType())
						{
							ControlledVoc type = statFoundation.getType();

							if (type.isSetValue())
								tridasMeasurementseriesInterpretationStatfoundationType.add(type.getValue());

							if (type.isSetNormal())
								tridasMeasurementseriesInterpretationStatfoundationTypeNormal.add(type.getNormal());
						}
					}
					searchBean.setTridasMeasurementseriesInterpretationStatfoundationStatvalue(interpretationStatFoundationStatValue);
					searchBean
							.setTridasMeasurementseriesInterpretationStatfoundationUsedsoftware(tridasMeasurementseriesInterpretationStatfoundationUsedsoftware);
					searchBean.setTridasMeasurementseriesInterpretationStatfoundationType(tridasMeasurementseriesInterpretationStatfoundationType);
					searchBean.setTridasMeasurementseriesInterpretationStatfoundationTypeNormal(tridasMeasurementseriesInterpretationStatfoundationTypeNormal);
					searchBean
							.setTridasMeasurementseriesInterpretationStatfoundationSignificancelevel(tridasMeasurementseriesInterpretationStatfoundationSignificancelevel);
				}

			}

		}

		return searchBean;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class getTridasClass()
	{
		return TridasMeasurementSeries.class;
	}

	@Override
	public void buildEntitySubTree()
	{
		// clear the list of subentities first
		List<Entity> subEntities = getDendroEntities();
		subEntities.clear();

		// use the tridas entity, to find sub-entity tridas data
		if (tridasMeasurementSeries == null)
			return; // nothing to do

		// values
		Iterator<TridasValues> iVals = tridasMeasurementSeries.getValues().iterator();
		while (iVals.hasNext())
		{
			TridasValues values = iVals.next();
			ValuesEntity valuesEntity = new ValuesEntity(values);
			subEntities.add(valuesEntity);
			valuesEntity.buildEntitySubTree();// go deeper
		}

		// Note: instead of depth first,
		// we could call the createTree of all sub-entities here
	}

	@Override
	public Object getTridasAsObject()
	{
		return tridasMeasurementSeries;
	}

	@Override
	public void setTridasObject(Object tridas)
	{
		tridasMeasurementSeries = (TridasMeasurementSeries) tridas;
	}

	public String getTridasTitle()
	{
		if (hasTridas()) 
			return tridasMeasurementSeries.getTitle();
		else
			return null;
	}
	
	@Override
	public void connectTridasObjectTree()
	{
		if (tridasMeasurementSeries == null)
			return; // nothing to do
		
		List<Entity> subEntities = getDendroEntities();
		for (Entity subEntity : subEntities)
		{
			// all subentities are values
			TridasValues valuesTridas = (TridasValues) subEntity.getTridasAsObject();
			tridasMeasurementSeries.getValues().add(valuesTridas);
			subEntity.connectTridasObjectTree();
		}
	}

	@Override
	protected void pruneTridas()
	{
		TridasMeasurementSeries objectToPrune = tridasMeasurementSeries;

		if (objectToPrune != null && 
			valuesPruned == null)
		{
			valuesPruned = objectToPrune.getValues();
			List<TridasValues> empty = Collections.emptyList();
			objectToPrune.setValues(empty);
		}
	}

	@Override
	protected void unpruneTridas()
	{
		TridasMeasurementSeries objectToUnprune = tridasMeasurementSeries;

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
