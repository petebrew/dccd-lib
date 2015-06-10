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
import nl.knaw.dans.dccd.search.DccdSB;

import org.tridas.schema.TridasHeartwood;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSapwood;
import org.tridas.schema.TridasWoodCompleteness;

/**
 * @author paulboon
 */
public class RadiusEntity extends AbstractEntity
{
	private static final long				serialVersionUID		= -8747798588495461308L;
	private TridasRadius					tridasRadius			= null;
	private List<TridasMeasurementSeries>	measurementSeriesPruned	= null;

	public RadiusEntity()
	{
		// empty
	}

	public RadiusEntity(TridasRadius tridasRadius)
	{
		this.tridasRadius = tridasRadius;
		setTitle(tridasRadius.getTitle());
	}

	public ProjectPermissionLevel getPermissionLevel()
	{
		return ProjectPermissionLevel.RADIUS;
	}

	@Override
	public String getUnitLabel()
	{
		return "RadiusEntity";
	}

	@Override
	public DccdSB fillSearchBean(DccdSB searchBean)
	{
		if (hasTridas())
		{
			// tridas.radius.title
			List<String> tridasRadiusTitle = searchBean.getTridasRadiusTitle();
			if (tridasRadiusTitle == null)
				tridasRadiusTitle = new ArrayList<String>();
			if (tridasRadius.isSetTitle())
			{
				tridasRadiusTitle.add(tridasRadius.getTitle());
			}
			searchBean.setTridasRadiusTitle(tridasRadiusTitle);

			// tridas.radius.identifier
			List<String> tridasRadiusIdentifier = searchBean.getTridasRadiusIdentifier();
			if (tridasRadiusIdentifier == null)
				tridasRadiusIdentifier = new ArrayList<String>();
			if (tridasRadius.isSetIdentifier() && tridasRadius.getIdentifier().isSetValue())
			{
				// Note: ignore domain
				tridasRadiusIdentifier.add(tridasRadius.getIdentifier().getValue());
			}
			searchBean.setTridasRadiusIdentifier(tridasRadiusIdentifier);

			/* Woodcompleteness */

			// tridas.radius.woodCompleteness.pith
			// tridas.radius.woodCompleteness.heartwood
			// tridas.radius.woodCompleteness.heartwood.missingHeartwoodRingsToPith
			// tridas.radius.woodCompleteness.heartwood.missingHeartwoodRingsToPithFoundation
			// tridas.radius.woodCompleteness.sapwood
			// tridas.radius.woodCompleteness.sapwood.nrOfSapwoodRings
			// tridas.radius.woodCompleteness.sapwood.missingSapwoodRingsToBark
			// tridas.radius.woodCompleteness.sapwood.missingSapwoodRingsToBarkFoundation
			// tridas.radius.woodCompleteness.sapwood.lastRingUnderBark
			// tridas.radius.woodCompleteness.bark
			List<String> tridasRadiusWoodcompletenessBark = searchBean.getTridasRadiusWoodcompletenessBark();
			if (tridasRadiusWoodcompletenessBark == null)
				tridasRadiusWoodcompletenessBark = new ArrayList<String>();
			List<String> tridasRadiusWoodcompletenessHeartwood = searchBean.getTridasRadiusWoodcompletenessHeartwood();
			if (tridasRadiusWoodcompletenessHeartwood == null)
				tridasRadiusWoodcompletenessHeartwood = new ArrayList<String>();
			List<Integer> tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopith = searchBean
					.getTridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopith();
			if (tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopith == null)
				tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopith = new ArrayList<Integer>();
			List<String> tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopithfoundation = searchBean
					.getTridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopithfoundation();
			if (tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopithfoundation == null)
				tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopithfoundation = new ArrayList<String>();
			List<String> tridasRadiusWoodcompletenessPith = searchBean.getTridasRadiusWoodcompletenessPith();
			if (tridasRadiusWoodcompletenessPith == null)
				tridasRadiusWoodcompletenessPith = new ArrayList<String>();
			List<String> tridasRadiusWoodcompletenessSapwood = searchBean.getTridasRadiusWoodcompletenessSapwood();
			if (tridasRadiusWoodcompletenessSapwood == null)
				tridasRadiusWoodcompletenessSapwood = new ArrayList<String>();
			List<String> tridasRadiusWoodcompletenessSapwoodLastringunderbark = searchBean.getTridasRadiusWoodcompletenessSapwoodLastringunderbark();
			if (tridasRadiusWoodcompletenessSapwoodLastringunderbark == null)
				tridasRadiusWoodcompletenessSapwoodLastringunderbark = new ArrayList<String>();
			List<Integer> tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobark = searchBean
					.getTridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobark();
			if (tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobark == null)
				tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobark = new ArrayList<Integer>();
			List<String> tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobarkfoundation = searchBean
					.getTridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobarkfoundation();
			if (tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobarkfoundation == null)
				tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobarkfoundation = new ArrayList<String>();
			List<Integer> tridasRadiusWoodcompletenessSapwoodNrofsapwoodrings = searchBean.getTridasRadiusWoodcompletenessSapwoodNrofsapwoodrings();
			if (tridasRadiusWoodcompletenessSapwoodNrofsapwoodrings == null)
				tridasRadiusWoodcompletenessSapwoodNrofsapwoodrings = new ArrayList<Integer>();

			if (tridasRadius.isSetWoodCompleteness())
			{
				TridasWoodCompleteness woodCompleteness = tridasRadius.getWoodCompleteness();

				// bark
				if (woodCompleteness.isSetBark() && woodCompleteness.getBark().isSetPresence())
					tridasRadiusWoodcompletenessBark.add(woodCompleteness.getBark().getPresence().value());
				else
					tridasRadiusWoodcompletenessBark.add("");

				// pith
				if (woodCompleteness.isSetPith() && woodCompleteness.getPith().isSetPresence())
					tridasRadiusWoodcompletenessPith.add(woodCompleteness.getPith().getPresence().value());
				else
					tridasRadiusWoodcompletenessPith.add("");

				// Heartwood
				if (woodCompleteness.isSetHeartwood())
				{
					TridasHeartwood heartwood = woodCompleteness.getHeartwood();

					if (heartwood.isSetPresence())
						tridasRadiusWoodcompletenessHeartwood.add(heartwood.getPresence().value());
					else
						tridasRadiusWoodcompletenessHeartwood.add("");

					if (heartwood.isSetMissingHeartwoodRingsToPith())
						tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopith.add(heartwood.getMissingHeartwoodRingsToPith());
					else
						tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopith.add(0);

					if (heartwood.isSetMissingHeartwoodRingsToPithFoundation())
						tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopithfoundation.add(heartwood.getMissingHeartwoodRingsToPithFoundation());
					else
						tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopithfoundation.add("");
				}
				else
				{
					tridasRadiusWoodcompletenessHeartwood.add("");
					tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopith.add(0);
					tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopithfoundation.add("");
				}

				// Sapwood
				if (woodCompleteness.isSetSapwood())
				{
					TridasSapwood sapwood = woodCompleteness.getSapwood();

					if (sapwood.isSetPresence())
						tridasRadiusWoodcompletenessSapwood.add(sapwood.getPresence().value());
					else
						tridasRadiusWoodcompletenessSapwood.add("");

					if (sapwood.isSetLastRingUnderBark() && sapwood.getLastRingUnderBark().isSetPresence())
						tridasRadiusWoodcompletenessSapwoodLastringunderbark.add(sapwood.getLastRingUnderBark().getPresence().value());
					else
						tridasRadiusWoodcompletenessSapwoodLastringunderbark.add("");

					if (sapwood.isSetMissingSapwoodRingsToBark())
						tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobark.add(sapwood.getMissingSapwoodRingsToBark());
					else
						tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobark.add(0);

					if (sapwood.isSetMissingSapwoodRingsToBarkFoundation())
						tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobarkfoundation.add(sapwood.getMissingSapwoodRingsToBarkFoundation());
					else
						tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobarkfoundation.add("");

					if (sapwood.isSetNrOfSapwoodRings())
						tridasRadiusWoodcompletenessSapwoodNrofsapwoodrings.add(sapwood.getNrOfSapwoodRings());
					else
						tridasRadiusWoodcompletenessSapwoodNrofsapwoodrings.add(0);
				}
				else
				{
					tridasRadiusWoodcompletenessSapwood.add("");
					tridasRadiusWoodcompletenessSapwoodLastringunderbark.add("");
					tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobark.add(0);
					tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobarkfoundation.add("");
					tridasRadiusWoodcompletenessSapwoodNrofsapwoodrings.add(0);
				}
			}
			searchBean.setTridasRadiusWoodcompletenessBark(tridasRadiusWoodcompletenessBark);
			searchBean.setTridasRadiusWoodcompletenessHeartwood(tridasRadiusWoodcompletenessHeartwood);
			searchBean.setTridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopith(tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopith);
			searchBean
					.setTridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopithfoundation(tridasRadiusWoodcompletenessHeartwoodMissingheartwoodringstopithfoundation);
			searchBean.setTridasRadiusWoodcompletenessPith(tridasRadiusWoodcompletenessPith);
			searchBean.setTridasRadiusWoodcompletenessSapwood(tridasRadiusWoodcompletenessSapwood);
			searchBean.setTridasRadiusWoodcompletenessSapwoodLastringunderbark(tridasRadiusWoodcompletenessSapwoodLastringunderbark);
			searchBean.setTridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobark(tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobark);
			searchBean
					.setTridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobarkfoundation(tridasRadiusWoodcompletenessSapwoodMissingsapwoodringstobarkfoundation);
			searchBean.setTridasRadiusWoodcompletenessSapwoodNrofsapwoodrings(tridasRadiusWoodcompletenessSapwoodNrofsapwoodrings);
		}
		return searchBean;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class getTridasClass()
	{
		return TridasRadius.class;
	}

	@Override
	public void buildEntitySubTree()
	{
		// clear the list of subentities first
		List<Entity> subEntities = getDendroEntities();
		subEntities.clear();

		// use the tridas entity, to find sub-entity tridas data
		if (tridasRadius == null)
			return; // nothing to do

		// sub nodes
		Iterator<TridasMeasurementSeries> i = tridasRadius.getMeasurementSeries().iterator();
		while (i.hasNext())
		{
			TridasMeasurementSeries measurementSeries = i.next();
			MeasurementSeriesEntity seriesEntity = new MeasurementSeriesEntity(measurementSeries);
			subEntities.add(seriesEntity);
			seriesEntity.buildEntitySubTree();// go deeper
		}

		// Note: instead of depth first,
		// we could call the createTree of all sub-entities here
	}

	@Override
	public Object getTridasAsObject()
	{
		return tridasRadius;
	}

	@Override
	public void setTridasObject(Object tridas)
	{
		tridasRadius = (TridasRadius) tridas;
	}

	public String getTridasTitle()
	{
		if (hasTridas()) 
			return tridasRadius.getTitle();
		else
			return null;
	}
	
	@Override
	public void connectTridasObjectTree()
	{
		if (tridasRadius == null)
			return; // nothing to do
		
		List<Entity> subEntities = getDendroEntities();
		for (Entity subEntity : subEntities)
		{
			// all subentities are MeasurementSeries
			TridasMeasurementSeries seriesTridas = (TridasMeasurementSeries) subEntity.getTridasAsObject();
			tridasRadius.getMeasurementSeries().add(seriesTridas);
			subEntity.connectTridasObjectTree();
		}
	}

	@Override
	protected void pruneTridas()
	{
		TridasRadius objectToPrune = tridasRadius;

		if (objectToPrune != null && 
			measurementSeriesPruned == null)
		{
			measurementSeriesPruned = objectToPrune.getMeasurementSeries();
			List<TridasMeasurementSeries> empty = Collections.emptyList();
			objectToPrune.setMeasurementSeries(empty);
		}
	}

	@Override
	protected void unpruneTridas()
	{
		TridasRadius objectToUnprune = tridasRadius;

		if (objectToUnprune != null && 
			measurementSeriesPruned != null)
		{
			objectToUnprune.setMeasurementSeries(measurementSeriesPruned);
			measurementSeriesPruned = null;
		}
	}
	
	public List<String> getAssociatedFileNames()
	{
		List<String> resultList = new ArrayList<String>();
		// empty
		return resultList;
	}
}
