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

import org.tridas.schema.ControlledVoc;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasFile;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasSlope;
import org.tridas.schema.TridasSoil;

public class ElementEntity extends AbstractEntity
{
	private static final long	serialVersionUID	= -507226161706482578L;
	private TridasElement		tridasElement		= null;
	private List<TridasSample>	samplesPruned		= null;

	public ElementEntity()
	{
		// empty
	}

	public ElementEntity(TridasElement tridasElement)
	{
		this.tridasElement = tridasElement;
		setTitle(tridasElement.getTitle());
	}

	@Override
	public String getUnitLabel()
	{
		return "ElementEntity";
	}

	public ProjectPermissionLevel getPermissionLevel()
	{
		return ProjectPermissionLevel.ELEMENT;
	}

	@Override
	public DccdSB fillSearchBean(DccdSB searchBean)
	{
		if (hasTridas())
		{
			// tridas.element.title
			List<String> tridasElementTitle = searchBean.getTridasElementTitle();
			if (tridasElementTitle == null)
				tridasElementTitle = new ArrayList<String>();
			if (tridasElement.isSetTitle())
			{
				tridasElementTitle.add(tridasElement.getTitle());
			}
			searchBean.setTridasElementTitle(tridasElementTitle);

			// tridas.element.identifier
			List<String> tridasElementIdentifier = searchBean.getTridasElementIdentifier();
			if (tridasElementIdentifier == null)
				tridasElementIdentifier = new ArrayList<String>();
			if (tridasElement.isSetIdentifier() && tridasElement.getIdentifier().isSetValue())
			{
				// Note: ignore domain
				tridasElementIdentifier.add(tridasElement.getIdentifier().getValue());
			}
			// BUG! searchBean.setTridasObjectIdentifier(tridasElementIdentifier);
			searchBean.setTridasElementIdentifier(tridasElementIdentifier);

			// tridas.element.type
			// tridas.element.type.normal
			List<String> tridasElementType = searchBean.getTridasElementType();
			if (tridasElementType == null)
				tridasElementType = new ArrayList<String>();
			List<String> tridasElementTypeNormal = searchBean.getTridasElementTypeNormal();
			if (tridasElementTypeNormal == null)
				tridasElementTypeNormal = new ArrayList<String>();
			if (tridasElement.isSetType())
			{
				ControlledVoc type = tridasElement.getType();

				if (type.isSetValue())
					tridasElementType.add(type.getValue());
				else
					tridasElementType.add("");

				if (type.isSetNormal())
					tridasElementTypeNormal.add(type.getNormal());
				else
					tridasElementTypeNormal.add("");
			}
			searchBean.setTridasElementType(tridasElementType);
			searchBean.setTridasElementTypeNormal(tridasElementTypeNormal);

			// tridas.element.description
			List<String> tridasElementDescription = searchBean.getTridasElementDescription();
			if (tridasElementDescription == null)
				tridasElementDescription = new ArrayList<String>();
			if (tridasElement.isSetDescription())
			{
				tridasElementDescription.add(tridasElement.getDescription());
			}
			searchBean.setTridasElementDescription(tridasElementDescription);

			// tridas.element.taxon
			List<String> tridasElementTaxon = searchBean.getTridasElementTaxon();
			if (tridasElementTaxon == null)
				tridasElementTaxon = new ArrayList<String>();
			if (tridasElement.isSetTaxon())
			{
				tridasElementTaxon.add(tridasElement.getTaxon().getValue());
			}
			searchBean.setTridasElementTaxon(tridasElementTaxon);

			// tridas.element.shape
			List<String> tridasElementShape = searchBean.getTridasElementShape();
			if (tridasElementShape == null)
				tridasElementShape = new ArrayList<String>();
			if (tridasElement.isSetShape() && tridasElement.getShape().isSetValue())
			{
				tridasElementShape.add(tridasElement.getShape().getValue());
			}
			searchBean.setTridasElementShape(tridasElementShape);

			// tridas.element.altitude
			List<Double> tridasElementAltitude = searchBean.getTridasElementAltitude();
			if (tridasElementAltitude == null)
				tridasElementAltitude = new ArrayList<Double>();
			if (tridasElement.isSetAltitude())
			{
				tridasElementAltitude.add(tridasElement.getAltitude());
			}
			searchBean.setTridasElementAltitude(tridasElementAltitude);

			// tridas.element.slope.angle
			// tridas.element.slope.azimuth
			List<Integer> tridasElementSlopeAngle = searchBean.getTridasElementSlopeAngle();
			if (tridasElementSlopeAngle == null)
				tridasElementSlopeAngle = new ArrayList<Integer>();
			List<Integer> tridasElementSlopeAzimuth = searchBean.getTridasElementSlopeAzimuth();
			if (tridasElementSlopeAzimuth == null)
				tridasElementSlopeAzimuth = new ArrayList<Integer>();
			if (tridasElement.isSetSlope())
			{
				TridasSlope slope = tridasElement.getSlope();
				if (slope.isSetAngle())
					tridasElementSlopeAngle.add(slope.getAngle().intValue());
				if (slope.isSetAzimuth())
					tridasElementSlopeAzimuth.add(slope.getAzimuth().intValue());
			}
			searchBean.setTridasElementSlopeAngle(tridasElementSlopeAngle);
			searchBean.setTridasElementSlopeAzimuth(tridasElementSlopeAzimuth);

			// tridas.element.soil.description
			// tridas.element.soil.depth
			List<String> tridasElementSoilDescription = searchBean.getTridasElementSoilDescription();
			if (tridasElementSoilDescription == null)
				tridasElementSoilDescription = new ArrayList<String>();
			List<Double> tridasElementSoilDepth = searchBean.getTridasElementSoilDepth();
			if (tridasElementSoilDepth == null)
				tridasElementSoilDepth = new ArrayList<Double>();
			if (tridasElement.isSetSoil())
			{
				TridasSoil soil = tridasElement.getSoil();
				if (soil.isSetDescription())
					tridasElementSoilDescription.add(soil.getDescription());
				if (soil.isSetDepth())
					tridasElementSoilDepth.add(soil.getDepth());
			}
			searchBean.setTridasElementSoilDescription(tridasElementSoilDescription);
			searchBean.setTridasElementSoilDepth(tridasElementSoilDepth);

			// tridas.element.bedrock.description
			List<String> tridasElementBedrockDescription = searchBean.getTridasElementBedrockDescription();
			if (tridasElementBedrockDescription == null)
				tridasElementBedrockDescription = new ArrayList<String>();
			if (tridasElement.isSetBedrock() && tridasElement.getBedrock().isSetDescription())
			{
				tridasElementBedrockDescription.add(tridasElement.getBedrock().getDescription());
			}
			searchBean.setTridasElementBedrockDescription(tridasElementBedrockDescription);

		}
		return searchBean;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class getTridasClass()
	{
		return TridasElement.class;
	}

	@Override
	public void buildEntitySubTree()
	{
		// clear the list of subentities first
		List<Entity> subEntities = getDendroEntities();
		subEntities.clear();

		// use the tridas entity, to find sub-entity tridas data
		if (tridasElement == null)
			return; // nothing to do

		// samples
		Iterator<TridasSample> iSamp = tridasElement.getSamples().iterator();
		while (iSamp.hasNext())
		{
			TridasSample sample = iSamp.next();
			SampleEntity sampleEntity = new SampleEntity(sample);
			subEntities.add(sampleEntity);
			sampleEntity.buildEntitySubTree();// go deeper
		}

		// Note: instead of depth first,
		// we could call the createTree of all sub-entities here
	}

	@Override
	public Object getTridasAsObject()
	{
		return tridasElement;
	}

	@Override
	public void setTridasObject(Object tridas)
	{
		tridasElement = (TridasElement) tridas;
	}

	public String getTridasTitle()
	{
		if (hasTridas()) 
			return tridasElement.getTitle();
		else
			return null;
	}
	
	@Override
	public void connectTridasObjectTree()
	{
		if (tridasElement == null)
			return; // nothing to do
		
		List<Entity> subEntities = getDendroEntities();
		for (Entity subEntity : subEntities)
		{
			// all subentities are samples
			TridasSample sampleTridas = (TridasSample) subEntity.getTridasAsObject();
			tridasElement.getSamples().add(sampleTridas);
			subEntity.connectTridasObjectTree();
		}
	}

	@Override
	protected void pruneTridas()
	{
		TridasElement objectToPrune = tridasElement;

		if (objectToPrune != null &&
			samplesPruned == null)
		{
			samplesPruned = objectToPrune.getSamples();
			List<TridasSample> empty = Collections.emptyList();
			objectToPrune.setSamples(empty);
		}
	}

	@Override
	protected void unpruneTridas()
	{
		TridasElement objectToUnprune = tridasElement;

		if (objectToUnprune != null && 
			samplesPruned != null)
		{
			objectToUnprune.setSamples(samplesPruned);
			samplesPruned = null;
		}
	}
	
	public List<String> getAssociatedFileNames()
	{
		List<String> resultList = new ArrayList<String>();
		
		if (hasTridas())
		{
			TridasElement tridas = (TridasElement)getTridasAsObject();
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
