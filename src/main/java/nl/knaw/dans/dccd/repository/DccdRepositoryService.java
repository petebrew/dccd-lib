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
package nl.knaw.dans.dccd.repository;

import java.util.Collection;

import nl.knaw.dans.dccd.model.Project;
import nl.knaw.dans.dccd.model.entities.Entity;

/**
 * Handles request to the repository
 *
 * @author paulboon
 *
 */
public interface DccdRepositoryService {

	//TODO: LB20090923: create DendroProjectSearchResult (see DendroProject.java for more info)

	/** Get projects from repository
	 * note: only id and title, no other data
	 */
	public Collection< Project > getDendroProjects() throws DccdRepositoryException;

	 //TODO: LB20090923: I would expect this method to return a project id

	/** ingest a whole project
    *
    * @param project The project to be ingested or stored
    */
   public void ingest(Project project) throws DccdRepositoryException;

 //TODO: LB20090923: I would expect this method to return a project

   /** Get the complete project data from the repository
	 * places the retrieved data into the given project
	 * note: only use if you need all information, otherwise use
	 * retrieveEntityTree and retrieveEntity
	 *
	 * @param project The project to be retrieved, it's id must be set
	 */
    public void retrieve(Project project) throws DccdRepositoryException;

  //TODO: LB20090923: I would expect this method to return an EntityTree
  // let the application layer put the entityTree in the project.
  // may project parameter be a projectId?
    /** retrieve the entity tree for the given project
     *
     * @param project The project, it's id must be set
     */
    public void retrieveEntityTree(Project project) throws DccdRepositoryException;

//TODO: LB20090923: I would expect this method to return an Entity
//  may entity parameter be a entityId?

    /** retrieve a given entity from an object
     *
     * @param sid The object id
     * @param entity The entity to retrieve, it's id must be set
     */
    public void retrieveEntity(String sid, Entity entity) throws DccdRepositoryException;
}
