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

/** Configuration settings for DCCD can be retieved using this service
 * Implementations will load them from the system or use defaults
 * This enables DCCD to have information 'external',
 * and the same 'war' can be deployed on different systems
 * Each system having their own settings that are not overwritten by deploying the app.
 *
 * @author paulboon
 *
 */
public interface ConfigurationService {
	public Properties getSettings();
	
	// TODO make separate Service for maintenance info
	public String getMaintenanceMessage();
}
