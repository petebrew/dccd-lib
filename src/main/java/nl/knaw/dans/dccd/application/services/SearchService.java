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

import java.util.List;

import org.tridas.schema.TridasIdentifier;

import nl.knaw.dans.common.lang.search.SearchRequest;
import nl.knaw.dans.common.lang.search.SearchResult;
import nl.knaw.dans.dccd.model.Project;
import nl.knaw.dans.dccd.search.DccdProjectSB;
import nl.knaw.dans.dccd.search.DccdSB;

/**
 * search service interface
 *
 * @author paulboon
 *
 */
public interface SearchService
{
	/**
	 *
	 * @param project
	 * @throws SearchServiceException
	 */
	public void updateSearchIndex(Project project) throws SearchServiceException;

	/**
	 *
	 * @param project
	 * @throws SearchServiceException
	 */
	public void deleteSearchIndex(Project project) throws SearchServiceException;

	/**
	 *
	 * @param request
	 * @return
	 * @throws SearchServiceException
	 */
	public SearchResult<? extends DccdSB> doSearch(SearchRequest request)
		throws SearchServiceException;

	public SearchResult<DccdProjectSB> findProjectArchivedWithSameTridasIdentifier(Project project) 
		throws SearchServiceException;
	
	public SearchResult<DccdProjectSB> findProjectArchivedWithSameTridasIdentifier(TridasIdentifier tridasIdentifier) 
		throws SearchServiceException;

	/** @deprecated
	 *
	 * @param sid
	 * @throws SearchServiceException
	 */
	public void updateSearchIndex(String sid) throws SearchServiceException;

	/** @deprecated
	 * Search the repository
	 * Use offset and limit for paging the results
	 *
	 * @param query The query to search for
	 * @param offset Zero base index offset
	 * @param limit maximum number of resulting items to return
	 * @return
	 * @throws SearchServiceException
	 */
	public List<String> simpleSearch(String query, int offset, int limit) throws SearchServiceException;

	/** @deprecated
	 * Using defaults for offset and limit
	 */
	public List< String > simpleSearch(String query) throws SearchServiceException;

	/**@deprecated
	 * now return an object, the other search(es) should be removed eventually
	 * @param query
	 * @param offset
	 * @param limit
	 * @return
	 * @throws SearchServiceException
	 */
	public DccdSearchResult search(String query, int offset, int limit) throws SearchServiceException;
}
