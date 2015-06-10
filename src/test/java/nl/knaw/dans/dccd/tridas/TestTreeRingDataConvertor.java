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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//import org.tridas.treeringdatafileio.TreeRingData;

import junit.framework.TestCase;

//TODO remove; not used, Cornell university Dendro library takes care of that now

public class TestTreeRingDataConvertor extends TestCase {

	public void testConvertionToTridasValue() {
/*
		// create dummy data
		TreeRingData trData = new TreeRingData();
		// we are not testing this call, so we have to assume it works
		//
		// fill with random values
		List<Integer> dummyValues = createRandomIntArray(50, 250, 1, 998);
		for (int i=0; i< dummyValues.size(); i++){
			trData.getData().add(dummyValues.get(i));
			//System.out.println("val: "+ dummyValues.get(i));
		}

		// convert
		TridasValues tridasValues = TreeRingDataConvertor.convert(trData);

		// check result
		checkValues(trData, tridasValues);
		// assume ring widths
		//tridasValues.
		// assume 0.01 mm units
*/
	}

/*
	// compares the data/measurement values and fails if the numbers are not the same
	// assume same order
	private void checkValues(TreeRingData trData, TridasValues tridasValues) {
		assertNotNull(trData);
		assertNotNull(tridasValues);

		// check result, assume same order
		List<TridasValue> valueList = tridasValues.getValues();
		assertNotNull(valueList);
		assertNotNull(trData.getData());

		// must have same number of elements
		assertEquals(trData.getData().size(), valueList.size());
		for (int i=0; i< valueList.size(); i++){
			String valStr = valueList.get(i).getValue();
			assertEquals(trData.getData().get(i), Integer.parseInt(valStr));
		}
	}
*/
	
	private List<Integer> createRandomIntArray(int minSize, int maxSize, int minValue, int maxValue) {
		if (maxSize < minSize) throw new IllegalArgumentException("max. size cannot be smaler than min. size");
		if (maxSize < 0 || minSize < 0) throw new IllegalArgumentException("size cannot be negative");
		if (maxValue < minValue) throw new IllegalArgumentException("max. value cannot be smaler than min. value");

		List<Integer> list = new ArrayList<Integer>();
		Random r = new Random();
		int n = r.nextInt(maxSize-minSize) + minSize;
		for (int i = 0; i < n; i++) {
			Integer val = new Integer(minValue + r.nextInt(maxValue-minValue));
			list.add(val);
		}

		return list;
	}

}
