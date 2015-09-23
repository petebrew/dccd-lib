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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.Writer;
import java.util.List;

import nl.knaw.dans.dccd.application.services.DccdDataService;
import nl.knaw.dans.dccd.application.services.DccdProjectValidationService;
import nl.knaw.dans.dccd.application.services.DccdUserService;
import nl.knaw.dans.dccd.application.services.TreeRingDataFileService;
import nl.knaw.dans.dccd.application.services.TreeRingDataFileServiceException;
import nl.knaw.dans.dccd.application.services.UserServiceException;
import nl.knaw.dans.dccd.application.services.ValidationErrorMessage;
import nl.knaw.dans.dccd.model.DccdTreeRingData;
import nl.knaw.dans.dccd.model.DccdUser;
import nl.knaw.dans.dccd.model.Project;
import nl.knaw.dans.dccd.repository.xml.XMLFilesRepositoryService;

import org.apache.log4j.Logger;
import org.junit.Test;

public class passThroughOnlineTest
{
	private static Logger logger = Logger.getLogger(passThroughOnlineTest.class);

	private DccdUser user = null;
	
	// class for filtering xml files from folder listing
	class XMLFilter implements FilenameFilter {
	    public boolean accept(File dir, String name) {
	    	// assume xml files have (lowercase) extension xml
	        return (name.endsWith(".xml"));
	    }
	}
	
	/**
	 * use generated test tridas data for dccd only testing 
	 */
	@Test
	public void testDccdData() throws Exception 
	{
		passThroughFolder(new File("TestData/Input"), new File("TestData/Output"));
	}

	private void passThroughFolder(File inputFolder, File outputFolder) throws Exception
	{
		// assume all xml files are Tridas
		File[] files = inputFolder.listFiles(new XMLFilter());
		if (files == null) 
		{
			logger.info("Could not get files from folder: " + inputFolder.getAbsolutePath());
		}

		String report = "";
		
		for (File inputFile : files)
		{
			logger.debug("TESTING file: " + inputFile.getName());
			
			// Use the minimal and maximal xml files
			// standalone tridas, no external files...
			//
			report += "\nFile: " + inputFile.getName()+ ":\n" + passThroughFile(inputFile, outputFolder);
		}
		
		// write report
		File reportFile = new File(outputFolder.getAbsolutePath() + File.separator +  "report.txt");
		Writer output = new BufferedWriter(new FileWriter(reportFile));
		output.write(report);
		output.close();
	}
	
	/**
	 * test if we can use TRiCYCLE generated files
	 */
	@Test
	public void testTRiCYCLEOutput() throws Exception
	{
		passThroughFolder(new File("TestData/TRiCYCLE-Output-From-Legacy"), 
							new File("TestData/DCCD-Output-From-TRiCYCLE"));
	}
	
	/*
	 * 
	 */
	private DccdUser getUser()
	{
		// get the user that is uploaded
		// fake a user, but this one must exist in LDAP!!!
		// We could try to Mock the DccdUserService...
		if (user == null)
		{
			try
			{
				user = DccdUserService.getService().getUserById("normaltestuser");
			}
			catch (UserServiceException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		return user;
	}

	// 1) read xml (like upload) and convert to Project
	// 2) store in Repository (also try DCCD validation)
	// 3) convert to xml (like download)
	// 4) delete (cleanup)
	private String passThroughFile(File inputFile, File outputFolder) throws Exception 
	{
		String report = "";
		String userId = getUser().getId();

		// 1) read xml (like upload) and convert to Project
		//logger.info("Reading file: " + file.getAbsolutePath());
		FileInputStream fis = new FileInputStream(inputFile.getAbsolutePath());
		
		// parse TRiDaS file
		logger.info("Parse input...");
		Project inputProject = XMLFilesRepositoryService.createDendroProjectFromTridasXML(fis, userId);
		logger.info("Done");

		// NOTE: following should also be in business layer!!!
		// store the user(ID) that uploaded it
		inputProject.setOwnerId(userId);
		// store the (original) xml filename
		inputProject.setFileName(inputFile.getName());
		inputProject.entityTree.buildTree(inputProject.getTridas());

		// TEST and force to Dutch, maybe this could be 'read' from the TRiDaS?
		//inputProject.setTridasLanguage(new Locale("nl"));
		
		// DCCD validate for archiving
		report += "DCCD archiving validation result (empty when OK):\n";
		List<ValidationErrorMessage> validateMsgs = DccdProjectValidationService.getService().validate(inputProject);
		for (ValidationErrorMessage msg : validateMsgs) {
			String validationMessagesText =
			"" + msg.getMessage() + 
			": " + msg.getFieldNameInUIStyle() + " (" + msg.getClassName() + ")\n";
			report += validationMessagesText;
		}
			

		// 2) store in Repository (also try DCCD validation)
		// Store the project (as Draft!)
		logger.info("Store in repository...");
		DccdDataService.getService().storeProject(inputProject);
		logger.info("Done");
		
		try
		{
			logger.info("Retrieve from repository...");
			// 3) retrieve and convert to xml (like download)
			Project outputProject = DccdDataService.getService().getProject(inputProject.getStoreId());
			logger.info("Done");
		
			logger.info("Save in tridas file...");
			String basePath = outputFolder.getAbsolutePath();
			File outputFile = new File(basePath + File.separator + 
					inputFile.getName());//XMLFilesRepositoryService.constructTridasFilename(outputProject));
			XMLFilesRepositoryService.saveToTridasXML(outputFile, outputProject);
			logger.info("Done");
		}
		finally
		{
			logger.info("Remove from repository...");
			// cleanup!
			// 4) delete project (remove from repo = test cleanup)
			DccdDataService.getService().deleteProject(inputProject, getUser());
			logger.info("Done");
		}
		return report;
	}
	
	// TreeRingData export test
	// NOT online but would only want to test it when DendroFileIO changes and when passthrough testing is done!
	@Test
	public void testTreeRingDataOutput() throws Exception
	{
		File inputFolder = new File("TestData/Input");
		File outputFolder = new File("TestData/Output/values");
		// assume all xml files are Tridas
		File[] files = inputFolder.listFiles(new XMLFilter());
		if (files == null) 
		{
			logger.info("Could not get files from folder: " + inputFolder.getAbsolutePath());
		}

		//String report = "";
		
		for (File inputFile : files)
		{
			// Filter, so we only get what we want to use for this test
			//if(inputFile.getName().compareTo("dccd-webapp-testdata-maximal-v_1_tridas.xml") != 0) 
			//	continue;
			
			logger.debug("TESTING file: " + inputFile.getName());
			
			// Use the minimal and maximal xml files
			// standalone tridas, no external files...
			//
			//report += "\nFile: " + inputFile.getName()+ ":\n" + passThroughFile(inputFile, outputFolder);
			
			// 1) read xml (like upload) and convert to Project
			//logger.info("Reading file: " + file.getAbsolutePath());
			FileInputStream fis = new FileInputStream(inputFile.getAbsolutePath());			
			// parse TRiDaS file
			logger.info("Parse input...");
			Project inputProject = XMLFilesRepositoryService.createDendroProjectFromTridasXML(fis, getUser().getId());
			logger.info("Done");
			// NOTE: following should also be in business layer!!!
			// store the (original) xml filename
			inputProject.setFileName(inputFile.getName());
			inputProject.entityTree.buildTree(inputProject.getTridas());
			
			//String format = "Heidelberg";
			List<String> formatList = TreeRingDataFileService.getWritingFormats();
			for (String format : formatList)
			{	
				// Note: Could skip problematic formats, and continue testing
				//if(format.compareTo("CSV") == 0 
				//		|| format.compareTo("FHX2") == 0) 
				//	continue;

				// export
				// construct the data to save
				DccdTreeRingData data = new DccdTreeRingData();
				data.setTridasProject(inputProject.getTridas());
				logger.debug("Start converting to format: " + format);
				try {
					List<String> filenames = TreeRingDataFileService.save(data, outputFolder, format);
					// Note: 
					// when filenames is empty, maybe show a message when there was nothing saved, 
					// because conversion not possible?
					logger.debug("Saved " + filenames.size() + " files to: [" + outputFolder.getPath() + "] with format: " + format);
				}
				catch (TreeRingDataFileServiceException e)
				{
					// check what it is
					logger.warn("EXCEPTION: "+ e.getMessage());
				}
			}
		}
		
	}
	
}
