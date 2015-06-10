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
package nl.knaw.dans.dccd.util;

public class ClassUtil
{
	// returns the class (without the package if any)
	public static String getClassName(Class c)
	{
		String FQClassName = c.getName();
		int firstChar;
		firstChar = FQClassName.lastIndexOf('.') + 1;
		if (firstChar > 0)
		{
			FQClassName = FQClassName.substring(firstChar);
		}
		return FQClassName;
	}

	// returns the package without the classname, empty string if
	// there is no package
	public static String getPackageName(Class c)
	{
		String fullyQualifiedName = c.getName();
		int lastDot = fullyQualifiedName.lastIndexOf('.');
		if (lastDot == -1)
		{
			return "";
		}
		return fullyQualifiedName.substring(0, lastDot);
	}

}
