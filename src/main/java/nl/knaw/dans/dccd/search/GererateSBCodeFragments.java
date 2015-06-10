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
package nl.knaw.dans.dccd.search;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Generate Java Code fragments (text) from xml (Solr schema field elements)
 *
 * NOTE:
 * multiValued="true" is used here to generate List<String>, but in the real Solr
 * schema.xml for the AdvancedSearch those fields COULD be set to multiValued="false"
 *
 * @author paulboon
 *
 */
public class GererateSBCodeFragments
{
	public static void main(String[] args) throws Exception
	{
		final String xmlFileName = "/Users/paulboon/Documents/Development/dccd/trunk/Workspace/DCCD/src/main/java/nl/knaw/dans/dccd/search/fields.xml";
		InputStream is;
		try
		{
			is = new FileInputStream(xmlFileName);
		}
		catch (IOException e)
		{
			System.out.println("Could not open xml: " + xmlFileName);
			throw e;
		}

		// load the xml file
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse (is);//new File("fields.xml"));

		NodeList fieldNodes = doc.getElementsByTagName("field");

		System.out.println("/* Start - generated code */");

		printFieldNamesAsComment(fieldNodes);
		System.out.println("");

		for (int i = 0; i < fieldNodes.getLength(); i++)
		{
			Node fieldNode = fieldNodes.item(i);
			Element field = (Element)fieldNode;
			String fieldName = field.getAttribute("name");
			System.out.println("// " + fieldName);

			String nameConstant = fieldName.toUpperCase().replace('.', '_')+"_NAME";
			String varName = "";
			String[] splitName = fieldName.split("\\.");
			// camelCasing
			varName += splitName[0];
			for(int n = 1; n < splitName.length; n++)
			{
				String namePart = splitName[n];
				varName += namePart.substring(0, 1).toUpperCase() + namePart.substring(1).toLowerCase();
			}

			String baseType = getbaseType(field);

			if (field.getAttribute("multiValued").equals("true"))
				printDeclarationForListVar(baseType, fieldName, nameConstant, varName);
			else
				printDeclarationForVar(baseType, fieldName, nameConstant, varName);
		}

		System.out.println("/* End - generated code */");
	}

	private static void printFieldNamesAsComment(NodeList fieldNodes)
	{
		for (int i = 0; i < fieldNodes.getLength(); i++)
		{
			Node fieldNode = fieldNodes.item(i);
			Element field = (Element)fieldNode;
			String fieldName = field.getAttribute("name");
			System.out.println("// " + fieldName);
		}
	}

	private static String getbaseType(final Element field)
	{
		String baseType = "=== Unknown ===";

		if (field.getAttribute("type").equals("text"))
		{
			baseType = "String";
		}
		else if (field.getAttribute("type").equals("date_utc"))
		{
			baseType = "DateTime";
		}
		else if (field.getAttribute("type").equals("date_utc"))
		{
			baseType = "DateTime";
		}
		else if (field.getAttribute("type").equals("int"))
		{
			baseType = "Integer";
		}
		else if (field.getAttribute("type").equals("double"))
		{
			baseType = "Double";
		}

		return baseType;
	}

	// multiValued="false"
	private static void printDeclarationForVar(String baseType, String fieldName, String nameConstant, String varName)
	{
		//declaration
		//
		//public final static String X_Y_NAME = "x.y";
		//@SearchField(name=X_Y_NAME)
		//private String xY;
		System.out.println("public final static String " + nameConstant+ " = \"" + fieldName +  "\";");
		System.out.println("@SearchField(name=" + nameConstant + ")");
		System.out.println("private " + baseType + " " + varName + ";");
		System.out.println("");

		// method name variable part
		String varPart = varName.substring(0, 1).toUpperCase() + varName.substring(1);

		// getter
		//public String getX() {
		//	return x;
		//}
		System.out.println("public " + baseType + " get" + varPart + "() {");
		System.out.println("    return " + varName + ";");
		System.out.println("}");
		System.out.println("");

		// setter
		//public void setX(String x) {
		//	this.x = x;
		//}
		System.out.println("public void set" + varPart +"(" + baseType + " " + varName + ") {");
		System.out.println("	this." + varName + " = " + varName + ";");
		System.out.println("}");
		System.out.println("");

		// has
		//public boolean hasX() {
		//	return (x != null && x.length() > 0);
		//}
		System.out.println("public boolean has" + varPart + "() {");
		if (baseType.equals("String"))
		{
			System.out.println("	return (" + varName + " != null && " + varName + ".length() > 0);");
		}
		else
		{
			System.out.println("	return (" + varName + " != null);");
		}
		System.out.println("}");
		System.out.println("");
	}

	// multiValued="true"
	private static void printDeclarationForListVar(String baseType, String fieldName, String nameConstant, String varName)
	{
		//declaration
		//
		//public final static String X_Y_NAME = "x.y";
		//@SearchField(name=X_Y_NAME)
		//private String xY;
		System.out.println("public final static String " + nameConstant+ " = \"" + fieldName +  "\";");
		System.out.println("@SearchField(name=" + nameConstant + ")");
		System.out.println("private List<" + baseType + "> " + varName + ";");
		System.out.println("");

		// method name variable part
		String varPart = varName.substring(0, 1).toUpperCase() + varName.substring(1);

		// getter
		//public List<String> getX() {
		//	return x;
		//}
		System.out.println("public List<" + baseType + "> get" + varPart + "() {");
		System.out.println("    return " + varName + ";");
		System.out.println("}");
		System.out.println("");

		// setter
		//public void setX(List<String> x) {
		//	this.x = x;
		//}
		System.out.println("public void set" + varPart +"(List<" + baseType + "> " + varName + ") {");
		System.out.println("	this." + varName + " = " + varName + ";");
		System.out.println("}");
		System.out.println("");

		// has
		//public boolean hasX() {
		//	return (x != null && x.size() > 0);
		//}
		System.out.println("public boolean has" + varPart + "() {");
		System.out.println("	return (" + varName + " != null && " + varName + ".size() > 0);");
		System.out.println("}");
		System.out.println("");
	}

}
