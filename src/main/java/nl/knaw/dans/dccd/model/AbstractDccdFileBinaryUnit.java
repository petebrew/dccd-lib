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

import nl.knaw.dans.common.lang.repo.AbstractBinaryUnit;

public abstract class AbstractDccdFileBinaryUnit extends AbstractBinaryUnit
{
	private static final long	serialVersionUID	= -871423753896520667L;

	private String unitId = "";
	
    // for holding the name of the 'original' file
    // if we download the data we need to have it
    private String fileName = null;
    
	public String getFileName()
	{
		// Note could get it from the File, if it's not null
		return fileName;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	abstract String getUnitIdPrefix();
	
	@Override
	public String getUnitId()
	{
		return unitId;
	}
	
	public void setUnitId(String unitId)
	{
		this.unitId = unitId;
	}	
}
