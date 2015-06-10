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
package nl.knaw.dans.dccd.application.services;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;

import nl.knaw.dans.common.lang.ResourceLocator;

public class DccdApplicationVersionService implements ApplicationVersionService
{
	private static Logger logger = Logger.getLogger(DccdApplicationVersionService.class);
	final static String TIMESTAMP_PROPERTY_KEY = "timestamp";
	final static String TIMESTAMP_PROPERTY_FILE = "version.properties";
	final static String TIMESTAMP_PROPERTY_DEFAULT = "";
	
	private final static Properties version = new Properties();
	
	// singleton pattern with lazy construction
	private static ApplicationVersionService service = null;
	public static ApplicationVersionService getService() 
	{
		if (service == null) 
		{
			service = new DccdApplicationVersionService();
		}
		return service;
	}

	public DccdApplicationVersionService() 
	{
		super();

		// defaults
		version.setProperty(TIMESTAMP_PROPERTY_KEY, TIMESTAMP_PROPERTY_DEFAULT); 

		// load from properties file
		URL url = ResourceLocator.getURL(TIMESTAMP_PROPERTY_FILE);
		if (url == null)
		{
			logger.error("Could not load resource from: " + TIMESTAMP_PROPERTY_FILE);
		}
		else
		{
			try
			{
				version.load(url.openStream());
			}
			catch (IOException e)
			{
				logger.error("Could not load resource from: " + TIMESTAMP_PROPERTY_FILE);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see nl.knaw.dans.dccd.application.services.ApplicationVersionService#getTimestamp()
	 */
	public String getTimestamp()
	{
		return version.getProperty(TIMESTAMP_PROPERTY_KEY);
	}
}
