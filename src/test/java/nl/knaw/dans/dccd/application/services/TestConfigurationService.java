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

import java.util.Properties;

import junit.framework.TestCase;

public class TestConfigurationService extends TestCase {

	/**
	 * Note: testing in a unit test is difficult,
	 * the following scenarios can be tried but not easily automated here:
	 *  - move the properties file and the defaults should be used instead
	 *
	 */
	public void testConfigurationService() {
		ConfigurationService service = DccdConfigurationService.getService();
		Properties settings = service.getSettings();

		// just show the settings?
		//settings.list(System.out);

		// test if we can change the setting for other callers after us
		// which should not be allowed!
		// there should be at least one property,
		// and the first one is what we use
		String key = (String)settings.propertyNames().nextElement();
		String val = settings.getProperty(key);
		//System.out.println("Original Key: "+ key + " val : " + val);
		// change it
		settings.put(key, val + "@");
		key = (String)settings.propertyNames().nextElement();
		val = settings.getProperty(key);
		//System.out.println("Changed Key: "+ key + " val : " + val);

		// retrieve for the second time
		Properties settings2 = service.getSettings();
		String key2 = (String)settings2.propertyNames().nextElement();
		String val2 = settings2.getProperty(key2);
		//System.out.println("Original Key: "+ key2 + " val : " + val2);

		assertNotSame(val, val2);

		String home = System.getenv("DCCD_HOME");
		//System.out.println(home);

		/* Just see if we have an environment setting for DCCD_HOME
		Map map = System.getenv();
		Set keys = map.keySet();
		Iterator iterator = keys.iterator();
		System.out.println("Variable Name \t Variable Values");
		while (iterator.hasNext()){
			key = (String) iterator.next();
			String value = (String) map.get(key);
			System.out.println(key + "     " + value);
		}
		*/
	}

}
