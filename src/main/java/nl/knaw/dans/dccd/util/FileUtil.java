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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

public class FileUtil {

	/**
	 * pboon: Faster would be using FileChannel, but that has other problems.
	 * When copying needs to be optimized, this is a place to look!
	 */
	public static void copyFile(String srcFilename, String dtsFilename) throws FileNotFoundException, IOException
	{
		// maybe check if src != dst?
        InputStream in = null;
        OutputStream out = null;

	    try {
	        File f1 = new File(srcFilename);
	        File f2 = new File(dtsFilename);
	        in = new FileInputStream(f1);
	        out = new FileOutputStream(f2);//Overwrite the file.

	        byte[] buf = new byte[16*1024];// what is the optimal size?
	        int len;
	        while ((len = in.read(buf)) > 0){
	          out.write(buf, 0, len);
	        }
	        //System.out.println("File copied.");
	      } catch (FileNotFoundException e){
	        //System.out.println(e.getMessage());
	    	  throw e;//new IOException("File not found");
	      } catch (IOException e){
	        //System.out.println(e.getMessage());
	    	  throw e;//new IOException("Could not copy files");
	      } finally {
	    	  if (in != null) in.close();
	    	  if (out != null) out.close();
	      }
	}

	public static File createTempDirectory(File basePath, String prefix) throws IOException
	{
		if (!basePath.isDirectory())
			throw new IOException("Basepath directory does not exist");

		Integer i = 1; // infinite loop protection
		Random r = new Random();
		File destPath;
		do
		{
			destPath = new File(
			        basePath.getAbsolutePath() +
			        File.separatorChar +
					prefix + "_"+ Math.abs(r.nextInt()) );
			i++;
		}
		while((destPath.isFile() || destPath.isDirectory()) &&  i < 1024);
		if (i >= 1024)
			throw new IOException("Unable to detect unique path");

		if (!destPath.mkdir())
			throw new IOException("Unable to create temp directory");

		return destPath;
	}

	public static boolean deleteDirectory(File path)
	{
		if (!path.exists())
			return false;
		if( path.isDirectory())
		{
			File[] files = path.listFiles();
			for(int i=0; i<files.length; i++)
			{
				if(files[i].isDirectory())
					deleteDirectory(files[i]);
				else
					files[i].delete();
			}
		}
	    return path.delete();
	}

	/**
	 * Returns just filename of a full path, e.g. returns smily.gif for /images/smily.gif
	 * @param path The full path to the filename
	 */
	public static String getBasicFilename(String path)
	{
		// get uploaded filename without path
		Integer bIdx = path.lastIndexOf("/");
		if (bIdx < 0)
			bIdx = path.lastIndexOf("\\");
		if (bIdx >= 0 && bIdx != path.length())
			return path.substring(bIdx+1);
		return path;
	}

	// TODO ask Henk vd B if we can put this into the commons, maybe force to UTF-8?
	public static String getSaveFilename(String name)
	{
		String saveName = name; 
		// Replace characters that are known to give problems for filenames 
		// not only on Unix, but also on Windows

		// replace slashes
		saveName = saveName.replaceAll("[/\\\\]", " ");
		// and other chars as well
		saveName = saveName.replaceAll("[\\'\\*\\\"\\?\\|:><]", " ");
		
		saveName = StringUtil.cleanWhitespace(saveName);
		// replace the dots, those can give problems at the start of a filename
		saveName = saveName.replaceAll("\\A[\\.\\s]+", "");

		// replace center when to long 
		final int MAX_SZ = 128; // Note: smaller than 3 makes no sense!
		int MAX_SZ_DIV2 = (MAX_SZ-1)/2; // make sure we have an extra char at the central position 
		if (saveName.length() > MAX_SZ)
		{
			String start = saveName.substring(0, MAX_SZ_DIV2);
			String end = saveName.substring(saveName.length() - MAX_SZ_DIV2, saveName.length());
			saveName = start + "~" + end;
		}
		
		return saveName; 
	}
	
	/*
	public static byte[] getBytesFromFile(File file) throws IOException 
	{
        InputStream is = new FileInputStream(file);
    
        // Get the size of the file
        long length = file.length();
    
        if (length > Integer.MAX_VALUE) 
        {
            // File is too large
        	throw new IOException("File to large " + file.getName());
        }
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) 
        {
            offset += numRead;
        }
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length) 
        {
            throw new IOException("Could not completely read file " + file.getName());
        }
    
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }	
    */
}
