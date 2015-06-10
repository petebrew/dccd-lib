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
package nl.knaw.dans.dccd.common.lang.geo;

import java.io.Serializable;

/**
 * Geographical coordinate pair; Longitude, Latitude
 * Coordinate system WGS84 assumed: 
 * Coordinates are Latitude and Longitude as signed decimal degrees 
 * with negative longitude in the western hemisphere and 
 * negative latitude in the southern hemisphere.
 * The normal range for longitude is -180 West to 0 (greenwich) to 180 East and 
 * the normal range of latitude is -90 Southpole to 0 equator to 90 Northpole
 * WGS84 is EPSG:4326 or in GML: Point srsName="urn:ogc:def:crs:EPSG:6.6:4326"
 * 
 * Note: there is no check for these ranges!
 * Out of the bounds could be the result of erratic swapping Lon and Lat 
 * or forgetting to do a cyclic reduction after caculations.
 */
public class LonLat implements Serializable
{
	private static final long	serialVersionUID	= 7776409180074886268L;
	private double lon = 0;
	private double lat = 0;
	
	public LonLat(double lon, double lat)
	{
		this.lon = lon;
		this.lat = lat;
	}

	public double getLon()
	{
		return lon;
	}

	public void setLon(double lon)
	{
		this.lon = lon;
	}

	public double getLat()
	{
		return lat;
	}

	public void setLat(double lat)
	{
		this.lat = lat;
	}
	
	// Note: 
	// to indicate the 'normal' ranges
	// getMax() could return LonLat(180,90)
	// and getMin() could return LonLat(-180,-90)
}
