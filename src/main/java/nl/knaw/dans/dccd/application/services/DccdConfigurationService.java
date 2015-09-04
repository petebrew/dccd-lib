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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class DccdConfigurationService implements ConfigurationService 
{
	private static Logger logger = Logger.getLogger(DccdConfigurationService.class);
	private static Properties defaultSettings = null;
	private static Properties settings = null;
	private static String defaultFileLocation = "/opt/dccd-home/";// note: a UNIX path
	private static final String filename = "dccd.properties";
	private static final String HOME_NAME = "dccd.home";

	private static final String maintenance_filename = "maintenance.properties";
	private static final String maintenance_message_key = "message";
	private static Properties defaultMaintenance = null;
	
	public DccdConfigurationService() 
	{
		super();
		
		// Fedora 
		defaultSettings = new Properties();
		defaultSettings.put("fedora.protocol","http");
		defaultSettings.put("fedora.host","localhost");
		defaultSettings.put("fedora.port","80");
		defaultSettings.put("fedora.context","fedora");
		defaultSettings.put("fedora.user","fedoraAdmin");
		// NO defaults for passwords
		// defaultSettings.put("fedora.password","###Fill-In-fedoraAdmin-Password###");

		// Solr
		defaultSettings.put("solr.url","http://localhost:8983/solr");
		//? {
		defaultSettings.put("solr.protocol","http");
		defaultSettings.put("solr.host","localhost");
		defaultSettings.put("solr.port","80");
		// } ?
		defaultSettings.put("solr.context","solr-example/select");// select!
		defaultSettings.put("solr.context.update","solr-example/update");
		
		// LDAP
		defaultSettings.put("ldap.url","ldap://localhost:10389");
		defaultSettings.put("ldap.securityPrincipal","uid=admin,ou=system"); // apacheDS
		// NO defaults for passwords 
		// defaultSettings.put("ldap.securityCredentials","###Fill-In-ldap-Password###");
		
		// Mail
		defaultSettings.put("mail.smtp.host","localhost");
		defaultSettings.put("mail.fromAddress","###Fill-In-Email###");
		
		// Maintenance info, separate properties
		defaultMaintenance = new Properties();
		defaultMaintenance.put(maintenance_message_key, ""); // empty message, assume nothing to inform about!
	}

	// singleton pattern with lazy construction
	private static DccdConfigurationService service = null;
	public static DccdConfigurationService getService() 
	{
		if (service == null) 
		{
			service = new DccdConfigurationService();
		}
		return service;
	}

	// TODO
	// use the current process running user, to select the properties file
	// "dev.properties" for the developer and tomcat.properties for 
	// the deployment on test or production server
	// EOF has such code...!
	public Properties getSettings() 
	{
		// construct one , use many
		if(settings == null) 
		{
			settings = new Properties(defaultSettings);

			/* TODO: get file location from the environment */
			// get the location for the properties file
			String home = System.getProperty(HOME_NAME);
			
			logger.info("Home: " + HOME_NAME + "=" + home);

			String fileLocation = defaultFileLocation;
			if(home != null)
			{
				fileLocation = home + "/";
			}
			
			// try reading from the file, if it fails just use the defaults
			FileInputStream in = null;
			try 
			{
				in = new FileInputStream(fileLocation + filename);
				settings.load(in);
			} 
			catch (FileNotFoundException e) 
			{
				logger.warn("Settings file: '"+fileLocation + filename+"' could not be found, trying to use default settings, but missing credentials will likely cause deployment to fail.");
			} 
			catch (IOException e) 
			{
				logger.warn("There was an IOException reing the settings file: '"+fileLocation + filename+"'.  Trying to use default settings, but missing credentials will likely cause deployment to fail.");
			}
			finally
			{
				if (in != null)
					try
					{
						in.close();
					}
					catch (IOException e)
					{
						logger.warn("Could not close input steam");
					}
			}
			
			// show what has been loaded
			//settings.list(System.out);
		}
		
		
		// note: instead of returning the settings, we return a copy
		// Callers can change this copy but not the original private member!
		return new Properties(settings);
	}
	
	/**
	 * Read the maintenance information from a separate file instead of the configuration file
	 * Note: when calling this on every page request slows the server down, 
	 * it could be reimplemented to only load the file on a new session.
	 * 
	 * TODO Only START and END dates in the properties file would allow for the 
	 * Wicket GUI code to construct a message which is localized (time) and language especially!
	 * 
	 *  @return the message string
	 */
	public String getMaintenanceMessage()
	{
		Properties maintenance = new Properties(defaultMaintenance);
		
		String fileLocation = defaultFileLocation;
		FileInputStream in = null;
		try 
		{
			in = new FileInputStream(fileLocation + maintenance_filename);
			maintenance.load(in);
		}
		catch (FileNotFoundException e) 
		{
			logger.warn("Could not read maintenance info: "+ maintenance_filename + ", using defaults");
		} 
		catch (IOException e) 
		{
			logger.warn("Could not read maintenance info: "+ maintenance_filename + ", using defaults");
		}
		finally
		{
			if (in != null)
				try
				{
					in.close();
				}
				catch (IOException e)
				{
					logger.warn("Could not close input steam");
				}
		}
		
		return maintenance.getProperty("message").trim();
	}
}
