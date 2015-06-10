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

import java.util.Collection;

import nl.knaw.dans.dccd.model.Project;
import nl.knaw.dans.dccd.model.entities.Entity;

/** Data services interface
 *
 * @author paulboon
 *
 */
public interface DataService {

	/** Get projects (info) from the repository
	 *
	 * @return The list of projects
	 * @throws DataServiceException
	 */
	public Collection< Project > getProjects() throws DataServiceException;

	/** Store project in repository
	 *
	 * @param project The project to store
	 * @throws DataServiceException
	 */
	public void storeProject(Project project) throws DataServiceException;

	/** Retrieve all data for the given project
	 *
	 * @param project
	 * @throws DataServiceException
	 */
	public Project getProject(String projectId) throws DataServiceException;

	/** retrieve the tree for the given project
	 *
	 * @param project
	 * @throws DataServiceException
	 */
	public void getProjectEntityTree(Project project) throws DataServiceException;

	/**  retrieve an entity from the repository
	 *
	 * @param id
	 * @param entity
	 * @throws DataServiceException
	 */
	public void retrieveEntity(String id, Entity entity) throws DataServiceException;

	/**
	 *
	 * @return
	 * @throws DataServiceException
	 */
	Project createProject() throws DataServiceException;

}
