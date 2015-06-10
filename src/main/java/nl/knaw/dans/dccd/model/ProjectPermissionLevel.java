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

// a level for each entity
// the order of the level is important
// Values level means that all other levels are also allowed,
// for each level the one above it (in code) are also allowed
// example: object permits project but not element
public enum ProjectPermissionLevel
{
	// Note: keep order as it is!
	MINIMAL, // minimal, only open access information can be shown
	PROJECT,
	OBJECT,
	ELEMENT,
	SAMPLE,
	RADIUS,
	SERIES,
	VALUES; // maximal

	public static ProjectPermissionLevel minimum() { return MINIMAL; }
	public static ProjectPermissionLevel maximum() { return VALUES; }

	// if the given level is lower or equal it is permitted by this level
	// for example OBJECT is permitted by ELEMENT
	public boolean isPermittedBy(ProjectPermissionLevel level)
	{
		// Note: uses the ordinal
		return this.compareTo(level) <= 0;
	}
}
