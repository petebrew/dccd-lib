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
package nl.knaw.dans.dccd.repository.fedora;

import nl.knaw.dans.common.lang.RepositoryException;
import nl.knaw.dans.common.lang.repo.AbstractDmoFactory;
import nl.knaw.dans.common.lang.repo.DmoNamespace;
import nl.knaw.dans.dccd.model.Project;

public class DccdProjectFactory extends AbstractDmoFactory<Project> 
{

	@Override
	public DmoNamespace getNamespace() 
	{
		return Project.NAMESPACE;
	}

	@Override
	public Project newDmo() throws RepositoryException 
	{
		return createDmo(nextSid());
	}

	@Override
	public Project createDmo(String storeId) 
	{
		return new Project(storeId);
	}

}
