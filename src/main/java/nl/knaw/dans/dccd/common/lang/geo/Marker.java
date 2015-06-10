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

public class Marker implements Serializable
{
	private static final long	serialVersionUID	= 7398027605177570560L;

	private LonLat lonLat;
	private String info;
	
	public Marker(LonLat lonLat, String info)
	{
		this.lonLat = lonLat;
		this.info = info;
	}
	
	/*
	public Marker(LonLat lonLat)
	{
		this(lonLat, "");
	}
	*/
	
	public Marker(double lon, double lat, String info)
	{
		this(new LonLat(lon,lat), info);
	}

	public String getInfo()
	{
		return info;
	}

	public void setInfo(String info)
	{
		this.info = info;
	}

	public LonLat getLonLat()
	{
		return lonLat;
	}

	public void setLonLat(LonLat lonLat)
	{
		this.lonLat = lonLat;
	}
	
	public double getLon()
	{
		return lonLat.getLon();
	}

	public double getLat()
	{
		return lonLat.getLat();
	}
}
