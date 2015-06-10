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

import org.joda.time.DateTime;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.TridasFile;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;

/**
 * @author paulboon
 */
public class SampleEntity extends AbstractEntity
{
	private static final long	serialVersionUID	= -7608981465899768501L;
	private TridasSample		tridasSample		= null;
	List<TridasRadius>			radiusesPruned		= null;

	public SampleEntity()
	{
		// empty
	}

	public SampleEntity(TridasSample tridasSample)
	{
		this.tridasSample = tridasSample;
		setTitle(tridasSample.getTitle());
	}

	public ProjectPermissionLevel getPermissionLevel()
	{
		return ProjectPermissionLevel.SAMPLE;
	}

	@Override
	public String getUnitLabel()
	{
		return "SampleEntity";
	}

	@Override
	public DccdSB fillSearchBean(DccdSB searchBean)
	{
		if (hasTridas())
		{
			// tridas.sample.title
			List<String> tridasSampleTitle = searchBean.getTridasSampleTitle();
			if (tridasSampleTitle == null)
				tridasSampleTitle = new ArrayList<String>();
			if (tridasSample.isSetTitle())
			{
				tridasSampleTitle.add(tridasSample.getTitle());
			}
			searchBean.setTridasSampleTitle(tridasSampleTitle);

			// tridas.sample.identifier
			List<String> tridasSampleIdentifier = searchBean.getTridasSampleIdentifier();
			if (tridasSampleIdentifier == null)
				tridasSampleIdentifier = new ArrayList<String>();
			if (tridasSample.isSetIdentifier() && tridasSample.getIdentifier().isSetValue())
			{
				// Note: ignore domain
				tridasSampleIdentifier.add(tridasSample.getIdentifier().getValue());
			}
			searchBean.setTridasSampleIdentifier(tridasSampleIdentifier);

			// tridas.sample.type
			// tridas.sample.type.normal
			List<String> tridasSampleType = searchBean.getTridasSampleType();
			if (tridasSampleType == null)
				tridasSampleType = new ArrayList<String>();
			List<String> tridasSampleTypeNormal = searchBean.getTridasSampleTypeNormal();
			if (tridasSampleTypeNormal == null)
				tridasSampleTypeNormal = new ArrayList<String>();
			if (tridasSample.isSetType())
			{
				ControlledVoc type = tridasSample.getType();

				if (type.isSetValue())
					tridasSampleType.add(type.getValue());
				else
					tridasSampleType.add("");

				if (type.isSetNormal())
					tridasSampleTypeNormal.add(type.getNormal());
				else
					tridasSampleTypeNormal.add("");
			}
			searchBean.setTridasSampleType(tridasSampleType);
			searchBean.setTridasSampleTypeNormal(tridasSampleTypeNormal);

			// tridas.sample.samplingDate
			List<DateTime> tridasSampleSamplingdate = searchBean.getTridasSampleSamplingdate();
			if (tridasSampleSamplingdate == null)
				tridasSampleSamplingdate = new ArrayList<DateTime>();
			if (tridasSample.isSetSamplingDate())
			{
				XMLGregorianCalendar xmlDate = tridasSample.getSamplingDate().getValue();
				DateTime dateTime = new DateTime(xmlDate.toGregorianCalendar());

				tridasSampleSamplingdate.add(dateTime);
			}
			searchBean.setTridasSampleSamplingdate(tridasSampleSamplingdate);

		}
		return searchBean;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class getTridasClass()
	{
		return TridasSample.class;
	}

	@Override
	public void buildEntitySubTree()
	{
		// clear the list of subentities first
		List<Entity> subEntities = getDendroEntities();
		subEntities.clear();

		// use the tridas entity, to find sub-entity tridas data
		if (tridasSample == null)
			return; // nothing to do

		// radius
		Iterator<TridasRadius> i = tridasSample.getRadiuses().iterator();
		while (i.hasNext())
		{
			TridasRadius radius = i.next();
			RadiusEntity radiusEntity = new RadiusEntity(radius);
			subEntities.add(radiusEntity);
			radiusEntity.buildEntitySubTree();// go deeper
		}

		// Note: instead of depth first,
		// we could call the createTree of all sub-entities here
	}

	@Override
	public Object getTridasAsObject()
	{
		return tridasSample;
	}

	@Override
	public void setTridasObject(Object tridas)
	{
		tridasSample = (TridasSample) tridas;
	}

	public String getTridasTitle()
	{
		if (hasTridas()) 
			return tridasSample.getTitle();
		else
			return null;
	}
	
	@Override
	public void connectTridasObjectTree()
	{
		if (tridasSample == null)
			return; // nothing to do
		
		List<Entity> subEntities = getDendroEntities();
		for (Entity subEntity : subEntities)
		{
			// all subentities are radius
			TridasRadius radiusTridas = (TridasRadius) subEntity.getTridasAsObject();
			tridasSample.getRadiuses().add(radiusTridas);
			subEntity.connectTridasObjectTree();
		}
	}

	@Override
	protected void pruneTridas()
	{
		TridasSample objectToPrune = tridasSample;

		if (objectToPrune != null && 
			radiusesPruned == null)
		{
			radiusesPruned = objectToPrune.getRadiuses();
			List<TridasRadius> empty = Collections.emptyList();
			objectToPrune.setRadiuses(empty);
		}
	}

	@Override
	protected void unpruneTridas()
	{
		TridasSample objectToUnprune = tridasSample;

		if (objectToUnprune != null && 
			radiusesPruned != null)
		{
			objectToUnprune.setRadiuses(radiusesPruned);
			radiusesPruned = null;
		}
	}

	public List<String> getAssociatedFileNames()
	{
		List<String> resultList = new ArrayList<String>();
		
		if (hasTridas())
		{
			TridasSample tridas = (TridasSample)getTridasAsObject();
			if (tridas.isSetFiles()) 
			{
				List<TridasFile> files = tridas.getFiles();
				resultList.addAll(getFileNames(files));
			}
		}
		
		//logger.debug("Found Associated Files: " + resultList.size());
		
		return resultList;		
	}		
}
