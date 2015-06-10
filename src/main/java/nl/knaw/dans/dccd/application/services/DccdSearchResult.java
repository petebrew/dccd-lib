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

public class DccdSearchResult {
	private String responseString = "";
	public String getResponseString() {
		return responseString;
	}
	public void setResponseString(String responseString) {
		this.responseString = responseString;
	}

	// identify the items found in the repository being searched
	private List<String> resultItemIds;
	// When paging is used, we need to know how many hits there are
	// this is possibly much larger than the number of items in the list!
	private int numFound = 0;
	public List<String> getResultItemIds() {
		return resultItemIds;
	}
	public void setResultItemIds(List<String> resultItemIds) {
		this.resultItemIds = resultItemIds;
	}
	public int getNumFound() {
		return numFound;
	}
	public void setNumFound(int numFound) {
		this.numFound = numFound;
	}

	// Note: also the query string, offset and limit used could be placed here
	// but there is no need for that information yet
}
