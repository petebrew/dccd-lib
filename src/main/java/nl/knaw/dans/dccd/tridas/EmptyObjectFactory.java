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
package nl.knaw.dans.dccd.tridas;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.log4j.Logger;
import org.tridas.schema.ComplexPresenceAbsence;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.Date;
import org.tridas.schema.DateTime;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasDatingType;
import org.tridas.schema.NormalTridasLocationType;
import org.tridas.schema.NormalTridasMeasuringMethod;
import org.tridas.schema.NormalTridasShape;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.ObjectFactory;
import org.tridas.schema.PresenceAbsence;
import org.tridas.schema.SeriesLink;
import org.tridas.schema.SeriesLinksWithPreferred;
import org.tridas.schema.TridasAddress;
import org.tridas.schema.TridasBark;
import org.tridas.schema.TridasBedrock;
import org.tridas.schema.TridasCoverage;
import org.tridas.schema.TridasDating;
import org.tridas.schema.TridasDatingReference;
import org.tridas.schema.TridasDimensions;
import org.tridas.schema.TridasFile;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasHeartwood;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasInterpretationUnsolved;
import org.tridas.schema.TridasLaboratory;
import org.tridas.schema.TridasLastRingUnderBark;
import org.tridas.schema.TridasLocation;
import org.tridas.schema.TridasLocationGeometry;
import org.tridas.schema.TridasPith;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasResearch;
import org.tridas.schema.TridasSapwood;
import org.tridas.schema.TridasShape;
import org.tridas.schema.TridasSlope;
import org.tridas.schema.TridasSoil;
import org.tridas.schema.TridasStatFoundation;
import org.tridas.schema.TridasTridas;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasWoodCompleteness;
import org.tridas.schema.Year;

/**
 * Creates empty objects which are part of TRiDaS
 * Only if parts of the object are required they are added empty as well?
 * 
 * @author dev
 *
 */
public class EmptyObjectFactory
{
	private static Logger logger = Logger.getLogger(EmptyObjectFactory.class);
	private static ObjectFactory	tridasFactory = new ObjectFactory();	
	private static net.opengis.gml.schema.ObjectFactory	gmlFactory = new net.opengis.gml.schema.ObjectFactory();
	
	public static Object create(final Class<?> objectClass)
	{
		Object object = null;
		
		if (objectClass == String.class)
		{
			object = new String("");
		}
		else if (objectClass == TridasTridas.class)
		{
			TridasTridas newObject = tridasFactory.createTridasTridas();
			List<TridasProject> projects = new ArrayList<TridasProject>();
			newObject.setProjects(projects); // empty list
			
			object = newObject;
		}
		else if (objectClass == TridasGenericField.class)
		{
			TridasGenericField newObject = tridasFactory.createTridasGenericField();
			object = newObject;
		}
		else if (objectClass == ControlledVoc.class)
		{
			ControlledVoc newObject = tridasFactory.createControlledVoc();
			object = newObject;
		}
		else if (objectClass == TridasResearch.class)
		{
			TridasResearch newObject = tridasFactory.createTridasResearch();
			object = newObject;
			
			TridasIdentifier tridasIdentifier = createEmptyTridasIdentifier();
			newObject.setIdentifier(tridasIdentifier);
			newObject.setDescription("");
		}
		else if (objectClass == TridasFile.class)
		{
			TridasFile newObject = tridasFactory.createTridasFile();
			newObject.setHref("");
			object = newObject;
		}
		else if (objectClass == TridasLaboratory.class)
		{
			TridasLaboratory newObject = tridasFactory.createTridasLaboratory();
			object = newObject;
			
			//TridasIdentifier tridasIdentifier = createEmptyTridasIdentifier();
			//newObject.setIdentifier(tridasIdentifier);
			
			TridasLaboratory.Name name = new TridasLaboratory.Name();
			//name.setValue("");
			//name.setAcronym("");
			newObject.setName(name);
			
			TridasAddress address = createEmptyTridasAddress();
			newObject.setAddress(address);
		}
		else if (objectClass == TridasStatFoundation.class)
		{
			TridasStatFoundation newObject = tridasFactory.createTridasStatFoundation();
			
			// Required
			newObject.setStatValue(new BigDecimal(0));
			newObject.setUsedSoftware("");
			
			ControlledVoc cvoc = tridasFactory.createControlledVoc();
			newObject.setType(cvoc );

			// Optional
			//newObject.setSignificanceLevel(0);

			object = newObject;
		}
		else if (objectClass == SeriesLink.class)
		{
			object = createSeriesLink();
		}
		else if (objectClass == SeriesLink.XLink.class)
		{
			object = tridasFactory.createSeriesLinkXLink();
		}
		else if (objectClass == SeriesLink.IdRef.class)
		{
			object = tridasFactory.createSeriesLinkIdRef();
		}
		else if (objectClass == TridasIdentifier.class)
		{
			TridasIdentifier newObject =  createEmptyTridasIdentifier();
			
			// domain is required!
			newObject.setDomain("");
			
			object = newObject;
		}
		else if (objectClass == TridasAddress.class)
		{
			object = createEmptyTridasAddress();
		}
		// NEW for Optional Panels
		else if (objectClass == Date.class)
		{
			Date newObject = tridasFactory.createDate();
			// we create an XMLGregorianCalendar set at today, 0:00 AM
	        java.util.GregorianCalendar today =
	                new java.util.GregorianCalendar();
	        javax.xml.datatype.DatatypeFactory factory;
			try
			{
				factory = javax.xml.datatype.DatatypeFactory.newInstance();
		        javax.xml.datatype.XMLGregorianCalendar calendar =
	                factory.newXMLGregorianCalendar(
	                today.get(java.util.GregorianCalendar.YEAR),
	                today.get(java.util.GregorianCalendar.MONTH) + 1,
	                today.get(java.util.GregorianCalendar.DAY_OF_MONTH),
	                0, 0, 0, 0, 0);

		        newObject.setValue(calendar);
			}
			catch (DatatypeConfigurationException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//newObject.setCertainty(Certainty.UNKNOWN);
			
			object = newObject;
		}
		else if (objectClass == DateTime.class)
		{
			DateTime newObject = tridasFactory.createDateTime();
			
			// we create an XMLGregorianCalendar set at today, 0:00 AM
	        java.util.GregorianCalendar today =
	                new java.util.GregorianCalendar();
	        javax.xml.datatype.DatatypeFactory factory;
			try
			{
				factory = javax.xml.datatype.DatatypeFactory.newInstance();
		        javax.xml.datatype.XMLGregorianCalendar calendar =
	                factory.newXMLGregorianCalendar(
	                today.get(java.util.GregorianCalendar.YEAR), //year
	                today.get(java.util.GregorianCalendar.MONTH) + 1, //month
	                today.get(java.util.GregorianCalendar.DAY_OF_MONTH), //day
	                today.get(java.util.GregorianCalendar.HOUR_OF_DAY), //hour
	                today.get(java.util.GregorianCalendar.MINUTE), //minute
	                today.get(java.util.GregorianCalendar.SECOND), //second
	                today.get(java.util.GregorianCalendar.MILLISECOND), //millisecond
	                today.get(java.util.GregorianCalendar.ZONE_OFFSET)/(1000*60));//timezone ; convert from milliseconds to minutes

		        newObject.setValue(calendar);
			}
			catch (DatatypeConfigurationException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
						
			//newObject.setCertainty(Certainty.UNKNOWN);
			
			object = newObject;
		}		
		else if (objectClass == TridasCoverage.class)
		{
			TridasCoverage newObject =  tridasFactory.createTridasCoverage();
			
			// required
			newObject.setCoverageTemporal("");
			newObject.setCoverageTemporalFoundation("");
			
			object = newObject;
		}	
		else if (objectClass == TridasLocation.class)
		{
			TridasLocation newObject =  tridasFactory.createTridasLocation();
			
			// required
			//TridasLocationGeometry geom =  tridasFactory.createTridasLocationGeometry();
			//net.opengis.gml.schema.PointType pt = gmlFactory.createPointType();
			//geom.setPoint(pt); // point is the simplest 
			//newObject.setLocationGeometry(geom);
			
			object = newObject;
		}
		else if (objectClass == TridasLocationGeometry.class)
		{
			TridasLocationGeometry newObject =  tridasFactory.createTridasLocationGeometry();
			net.opengis.gml.schema.PointType pt = gmlFactory.createPointType();
			newObject.setPoint(pt); // point is the simplest 
			
			object = newObject;
		}
		else if (objectClass == TridasAddress.class)
		{
			TridasAddress newObject =  tridasFactory.createTridasAddress();			
			object = newObject;
		}
		else if (objectClass == NormalTridasLocationType.class)
		{
			NormalTridasLocationType newObject =  NormalTridasLocationType.GROWTH_LOCATION;//tridasFactory.createLocationType(null);			
			object = newObject;
		}
		else if (objectClass == SeriesLinksWithPreferred.class)
		{
			SeriesLinksWithPreferred newObject =  tridasFactory.createSeriesLinksWithPreferred();	
			
			// add a prefered, its the only thing we can have here, 
			// so without it it seems pointless to create
			newObject.setPreferredSeries(createSeriesLink());
			
			object = newObject;
		}
		else if (objectClass == TridasShape.class)
		{
			TridasShape newObject =  tridasFactory.createTridasShape();			
			object = newObject;
		}
		else if (objectClass == NormalTridasShape.class)
		{
			NormalTridasShape newObject =  NormalTridasShape.UNKNOWN;	
			object = newObject;
		}
		else if (objectClass == TridasDimensions.class)
		{
			TridasDimensions newObject =  tridasFactory.createTridasDimensions();	
			
			// must have Unit
			TridasUnit unit = tridasFactory.createTridasUnit();
			newObject.setUnit(unit);
			
			// must have Height
			newObject.setHeight(new BigDecimal(0));
			
			// diameter or (width + depth)???
			//newObject.setDiameter(new BigDecimal(0));
			
			object = newObject;
		}
		else if (objectClass == NormalTridasUnit.class)
		{
			NormalTridasUnit newObject =  NormalTridasUnit.MILLIMETRES;	
			object = newObject;
		}
		else if (objectClass == TridasSlope.class)
		{
			TridasSlope newObject =  tridasFactory.createTridasSlope();	
			object = newObject;
		}
		else if (objectClass == TridasSoil.class)
		{
			TridasSoil newObject =  tridasFactory.createTridasSoil();	
			object = newObject;
		}
		else if (objectClass == TridasBedrock.class)
		{
			TridasBedrock newObject =  tridasFactory.createTridasBedrock();	
			object = newObject;
		}
		else if (objectClass == TridasWoodCompleteness.class)
		{
			TridasWoodCompleteness newObject =  tridasFactory.createTridasWoodCompleteness();	
			
			// Required are: 
			// pith
			TridasPith pith = tridasFactory.createTridasPith();
			pith.setPresence(ComplexPresenceAbsence.UNKNOWN);
			newObject.setPith(pith);
			
			// heartwood
			TridasHeartwood heartwood = tridasFactory.createTridasHeartwood();
			heartwood.setPresence(ComplexPresenceAbsence.UNKNOWN);
			newObject.setHeartwood(heartwood);
			
			// sapwood
			TridasSapwood sapwood = tridasFactory.createTridasSapwood();
			sapwood.setPresence(ComplexPresenceAbsence.UNKNOWN);
			newObject.setSapwood(sapwood);
			
			// bark
			TridasBark bark = tridasFactory.createTridasBark();
			bark.setPresence(PresenceAbsence.ABSENT);
			newObject.setBark(bark);
			
			object = newObject;
		}
		else if (objectClass == NormalTridasMeasuringMethod.class)
		{
			NormalTridasMeasuringMethod newObject = NormalTridasMeasuringMethod.MEASURING_PLATFORM;	
			object = newObject;
		}
		else if (objectClass == TridasInterpretation.class)
		{
			TridasInterpretation newObject =  tridasFactory.createTridasInterpretation();	
			
			object = newObject;
		}
		else if (objectClass == TridasInterpretationUnsolved.class)
		{
			TridasInterpretationUnsolved newObject =  tridasFactory.createTridasInterpretationUnsolved();	
			
			object = newObject;
		}
		else if (objectClass == Year.class)
		{
			Year newObject =  tridasFactory.createYear();			
			
			// suffix is required
			newObject.setSuffix(DatingSuffix.AD);
			
			object = newObject;
		}
		else if (objectClass == TridasDating.class)
		{
			TridasDating newObject =  tridasFactory.createTridasDating();
			newObject.setType(NormalTridasDatingType.ABSOLUTE);
			object = newObject;
		}
		else if (objectClass == TridasDatingReference.class)
		{
			TridasDatingReference newObject =  tridasFactory.createTridasDatingReference();
			SeriesLink seriesLink = createEmptySeriesLink();
			newObject.setLinkSeries(seriesLink );
			object = newObject;
		}
		else if (objectClass == TridasLastRingUnderBark.class)
		{
			TridasLastRingUnderBark newObject =  tridasFactory.createTridasLastRingUnderBark();
			newObject.setPresence(PresenceAbsence.ABSENT);
			object = newObject;
		}
		else
		{
			logger.error("Could not create empty object of class: " + objectClass.getSimpleName());
		}
		
		return object;
	}
	
	// TODO more refactoring for creating empty parts
	// but no private member for the empty string, because that would be overkill
	
	private static SeriesLink createEmptySeriesLink()
	{
		SeriesLink link = tridasFactory.createSeriesLink();
		
		// initialize
		link.setIdentifier(createEmptyTridasIdentifier());
		link.setIdRef(null);//? no ref
		link.setXLink(null);//? no href
		
		return link;
	}
	
	private static TridasIdentifier createEmptyTridasIdentifier()
	{
		TridasIdentifier tridasIdentifier = tridasFactory.createTridasIdentifier();

		// initialize
		//tridasIdentifier.setDomain("");
		//tridasIdentifier.setValue("");

		return tridasIdentifier;
	}
	
	private static TridasAddress createEmptyTridasAddress()
	{
		TridasAddress address = tridasFactory.createTridasAddress();

		// initialize
		//address.setAddressLine1("");
		//address.setAddressLine2("");
		//address.setCityOrTown("");
		//address.setCountry("");
		//address.setPostalCode("");
		//address.setStateProvinceRegion("");
		return address;
	}	
	
	private static SeriesLink createSeriesLink()
	{
		SeriesLink newObject = tridasFactory.createSeriesLink();
		
		//newObject.setIdentifier(createEmptyTridasIdentifier());
		//newObject.setIdRef(tridasFactory.createSeriesLinkIdRef()); //? no ref
		//newObject.setXLink(tridasFactory.createSeriesLinkXLink()); //? no href
		
		return newObject;
	}

}
