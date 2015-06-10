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
package nl.knaw.dans.dccd.model.entities;

import java.util.List;

import nl.knaw.dans.common.lang.repo.MetadataUnit;
import nl.knaw.dans.dccd.model.ProjectPermissionLevel;
import nl.knaw.dans.dccd.search.DccdSB;

/**
 * The DCCD information (metadata) which is also stored in TRiDaS xml files, but split into (entity)levels. Supports facilities needed for storage and retrieval
 * of its data The Entities are compound objects (hierarchy) and form a tree of DendroEntity objects corresponding to the tridas
 * 
 * @see class EntityTree
 * @author paulboon
 */
public interface Entity extends MetadataUnit
{

	/**
	 * Add info for this entity alone, and don't recurse
	 * 
	 * @param searchBean
	 * @return
	 */
	DccdSB fillSearchBean(DccdSB searchBean);

	/**
	 * @param title
	 *        the title to set
	 */
	void setTitle(String title);

	/**
	 * @return the title
	 */
	String getTitle();

	/**
	 * @param id
	 *        the id to set
	 */
	void setId(String id);

	/**
	 * @return the id
	 */
	String getId();

	/**
	 * @return the label a descriptive string, more like a type never empty or null
	 */
	String getLabel();

	/**
	 * @return the List of sub-entities (direct descendants) An empty list if there are no sub entities, but never null
	 */
	List<Entity> getDendroEntities();

	/**
	 * Recursive function to get all sub entities in a tree
	 * 
	 * @see getEntities Note: Probably more efficient if we pass a 'stack like' container to avoid the recursive creation and appending of arraylists
	 * @return The list of entities
	 */
	public List<Entity> getSubTreeAsList();

	/**
	 * Builds the tree of entities which corresponds to the (tree) structure of the tridas data Firts creates all sub-entities, which should then create
	 * sub-entities as well
	 */
	void buildEntitySubTree();

	/**
	 * Note: maybe use the word Data instead of Tridas for public member names?
	 * 
	 * @return The tridas object being wrapped
	 */
	Object getTridasAsObject();

	/**
	 * @param tridas
	 *        The tridas object to set
	 */
	void setTridasObject(Object tridas);

	boolean hasTridas();

	/**
	 * Use the tree structure of the entities to connect the (wrapped)tridas members accordingly This is needed if the Tridas has been pruned, for intance when
	 * retrieved from a repository as unconnected fragments
	 */
	void connectTridasObjectTree();

	/**
	 * @return The class of the tridas data member
	 */
	@SuppressWarnings("unchecked")
	Class getTridasClass();

	/**
	 * @return the title of the Tridas object, null if there is none
	 */
	String getTridasTitle();
	
	/**
	 * Get the XML representation of this entity's tridas data
	 * 
	 * @return The string with the xml
	 */
	String getXMLString();

	/**
	 * Produce multi-line textual tree representation of the entities (not tridas) Usefull for debugging
	 * 
	 * @param indent
	 *        Indenting string; whitespace to prefix each line
	 * @return Lines
	 */
	List<String> toTreeString(String indent);

	/**
	 * Determine if this entity is permitted by the given permissionlevel
	 * 
	 * @param level
	 * @return
	 */
	boolean isPermittedBy(ProjectPermissionLevel level);

	public ProjectPermissionLevel getPermissionLevel();

	// A 'Validation' service will be used to determine this value and set it
	public boolean isValidForArchiving();
	public void setValidForArchiving(boolean valid);
	
	// return the names of the associated files for this entity (and not subentities)
	public List<String> getAssociatedFileNames();
}
