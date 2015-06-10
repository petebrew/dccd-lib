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

import nl.knaw.dans.dccd.common.lang.geo.LonLat;

import static org.junit.Assert.*;

import org.junit.Test;

public class LonLatTest
{
	@Test
	public void construction()
	{
		double lon = 1;
		double lat = 2;
		LonLat pos = new LonLat(lon,lat);
		
		// does it give back what I have put into it?
		// use zero tolerance (epsilon=0), we must have an exact match
		assertEquals(lon, pos.getLon(), 0);
		assertEquals(lat, pos.getLat(), 0);
		
		// Note: large numbers or negative numbers are not tested, 
		// there is no such thing as a wrong lon or lat in this implementation!
		// Also no check for NaN (Double.isNaN), if you put it in it will store it, no warnings
	}
}
