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
package nl.knaw.dans.dccd.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.knaw.dans.dccd.model.entities.Entity;

// Finds all references to associated files which need to be uploaded separately
public class ProjectAssociatedFileDetector
{
	public static  List<String> getProjectAssociatedFileNames(Project project)
	{
		List<String> resultList = new ArrayList<String>();
		
		// get all entities!
		List<Entity> entities = project.entityTree.getEntities();
		for(Entity entity : entities)
		{
			resultList.addAll(entity.getAssociatedFileNames());
		}
		
		// TODO use StringUtil.getUniqueStrings(resultList);
		
		// Make sure that each name is unique
		// using a HashSet is a known solution 
		Set set = new HashSet(resultList);
		String[] array = (String[])(set.toArray(new String[set.size()]));
		
		//List<String> list = Arrays.asList(array);
		//resultList = list;
		// Note Arrays.asList returns a List that does not support remove, it's a crippled List!
		// remove throws a UnsupportedOperationException
		resultList.clear();
		for(int i = 0; i < array.length; i++)
		{
			resultList.add(array[i]);
		}
		
		//logger.debug("Found Associated Files: " + resultList.size());
		return resultList;
	}
}
