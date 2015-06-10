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

//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.InputStream;
import java.net.URL;

import nl.knaw.dans.common.lang.ResourceLocator;
import nl.knaw.dans.common.lang.mail.Attachement;
import nl.knaw.dans.common.lang.mail.CommonMailer;
import nl.knaw.dans.common.lang.mail.DansMailer;
import nl.knaw.dans.common.lang.util.StreamUtil;

import org.junit.Test;

public class TestMailerOnlineTest
{
	@Test
	public void testMailPdf() throws Exception
	{
        final CommonMailer mailer = (CommonMailer) DansMailer.getDefaultInstance();

        final String subject = "Simple test mail from unittest";
        final String filename = "Licence_en.pdf";
        final String MIME_TYPE_PDF = "application/pdf";
        Attachement att;

        /*        
		// load from a file without resource stuff
        File file = new File("/data/devhome/ORG-Licence_en.pdf");
		FileInputStream fin = new FileInputStream(file);
		byte[] bytes = new byte[(int)file.length()];
		fin.read(bytes);
		fin.close();
         */
        
        URL url = ResourceLocator.getURL(filename);
		byte[] bytes = StreamUtil.getBytes(url.openStream());
        
		/*
		// save into a file to see if the bytes are correct
		FileOutputStream fos = new FileOutputStream("/data/devhome/" + filename);
		fos.write(bytes);
		fos.close();
		 */
		
		att = new Attachement("Licence_en.pdf", MIME_TYPE_PDF, bytes);				
        Attachement[] attachments = { att };
        mailer.sendSimpleMail(subject, "test content", attachments, "paul.boon@dans.knaw.nl");
	}

}
