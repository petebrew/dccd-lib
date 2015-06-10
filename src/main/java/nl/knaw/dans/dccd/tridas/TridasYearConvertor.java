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
package nl.knaw.dans.dccd.tridas;

import org.tridas.schema.DatingSuffix;
import org.tridas.schema.Year;

public class TridasYearConvertor
{
	// used for filling the search bean
	//
	// How to handle years; we can make BC negative which makes comparing years possible
	// timespans are not correctly calculated however due to the year zero omission.
	// How to handle BP = Before Present? (we could do 1950 - "years BP")
	// Another solution is to make it a string?
	// Or could we make it into DateTimes
	// the minimum for years in 'joda' DateTime is -292,000,000
	// if there are no trees before that...
	// "Earth's oldest known tree ...the 385 million-year-old tree"
	//
	// The choice is to use Astronomical years numbering: 
	// BP, means years before 1950
	// and with BC we make it negative but starting at 0 for 1 BC,
	// 2 BC becomes -1.
	// Also the minimal integer value on Java is -2147483648, more then 2 billion years ago!
	public static Integer tridasYearToInteger(Year tridasYear)
	{
		Integer yearInteger = tridasYear.getValue().intValue();
		// Note: could (in theory) have rounding errors

		// make sure we start positive
		yearInteger = Math.abs(yearInteger);

		if (tridasYear.isSetSuffix())
		{
			if (tridasYear.getSuffix() == DatingSuffix.BC)
			{
				// make BC negative
				yearInteger = 1-yearInteger; // Note the 1 for using the year zero!
			}
			else if (tridasYear.getSuffix() == DatingSuffix.BP)
			{
				yearInteger = 1950-yearInteger;
			}
		}

		return yearInteger;
	}
}
