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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import org.tridas.spatial.GMLPointSRSHandler;

public class TestGMLPoint
{
	private static net.opengis.gml.schema.ObjectFactory	gmlFactory = new net.opengis.gml.schema.ObjectFactory();
	
	final static double FIRST = 1.0;
	final static double SECOND = 20.0; // must be different
	final static double EPSILON = 1.0e-8; // almost exact
	
	@Test
	public void testWGS84AxisOrder() throws Exception
	{
		// The order of the coordinates is a source of confusion and discussion, 
		// it is not specified by the GML, but by the Coordinate Reference System in the srsName
		// Officially WGS84 should be lat/long and have an "urn:ogc:def:crs:EPSG:6.6:4326"
		// But most users expect long/lat if "WGS84" or "EPSG:4326" is used and only lat/long 
		// if the 'official' urn is used.
		//
		// TRiDABASE now uses "WGS 84" long/lat
		// and TRiCYCLE uses "urn:ogc:def:crs:EPSG:6.6:4326" with lat/long
		
		// long/lat
		testLongLat("WGS 84");
		testLongLat("WGS84");
		testLongLat("EPSG:4326");
		
		// DOES NOT WORK IN MAVEN BUILD, but is works when tested in eclipse !!!!!!!!!!!!!!!!!!!!
		// lat/long		
		testLatLong("urn:ogc:def:crs:EPSG:6.6:4326");
	}
	
	private void testLongLat(String srsName)
	{
		net.opengis.gml.schema.PointType point = createPoint(srsName);

		GMLPointSRSHandler handler = new GMLPointSRSHandler(point);
		Double latitude = handler.getWGS84LatCoord();
		Double longitude = handler.getWGS84LongCoord();
		assertEquals(longitude, FIRST, EPSILON); 
		assertEquals(latitude, SECOND, EPSILON); 
	}
	
	private void testLatLong(String srsName)
	{
		net.opengis.gml.schema.PointType point = createPoint(srsName);

		GMLPointSRSHandler handler = new GMLPointSRSHandler(point);
		Double latitude = handler.getWGS84LatCoord();
		Double longitude = handler.getWGS84LongCoord();
		assertEquals(longitude, SECOND, EPSILON); 
		assertEquals(latitude, FIRST, EPSILON); 
	}
	
	net.opengis.gml.schema.PointType createPoint(String srsName)
	{
		net.opengis.gml.schema.PointType point = gmlFactory.createPointType();
		net.opengis.gml.schema.Pos pos = gmlFactory.createPos();
		List<Double> values = pos.getValues();
		values.add(FIRST); 
		values.add(SECOND);
		point.setPos(pos);

		point.setSrsName(srsName);
		return point;
	}
}
