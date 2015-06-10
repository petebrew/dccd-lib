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
package nl.knaw.dans.dccd.search;

import nl.knaw.dans.common.lang.search.bean.annotation.SearchBean;

@SearchBean(defaultIndex=DccdIndex.class, typeIdentifier="dccdProject")
public class DccdProjectSB extends DccdSB
{
	private static final long serialVersionUID = 4433107231579842498L;
  // only DATASTREAMID seems unneeded,
  // because now we have a Project and not a specific (TRiDaS)Object inside a project
  // When refactoring, we could make the DccdSB without the DATASTREAMID
  // and add it to the DccdObjectSB

}
