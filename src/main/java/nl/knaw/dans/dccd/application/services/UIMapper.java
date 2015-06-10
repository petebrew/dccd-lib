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

import java.util.ArrayList;
import java.util.List;

import nl.knaw.dans.dccd.model.IndicateRequiredUIMapEntry;
import nl.knaw.dans.dccd.model.UIMapEntry;
import nl.knaw.dans.dccd.model.UIMapEntry.Multiplicity;

import org.apache.log4j.Logger;

/** Contains mapping info for the Panels getting data from Tridas classes
 * Mapping information is kept in the map entries.
 * The order of the entries in the map is used when building the UI
 *
 * note: When testing we should check the method names of the entries!
 *  also if names need to be trimmed; it should not.
 *
 * TODO: unit testing
 *
 * @author paulboon
 *
 */
public class UIMapper
{
	private static Logger logger = Logger.getLogger(UIMapper.class);

	// Note: Quick implementation...
	// should not need to recreate this information
	//
	// Could implement init that does the creation and holds the array's
	// Either read the mapping information from an xml file or maybe use a map 'literal':
	// private static final Map ProjetMap = new HashMap() {{
	//   put("TITLE", new DendroUIMapEntry("Title",      "getTitle"));
	//   put("ID",    new DendroUIMapEntry("Identifier", "getIdentifier"));
	// }};
	//
	// Note2:
	// Could have Entity's and EntityInfo for describing the possible tree structure:
	// Projects have Objects have Elements have Samples ... each level corresponds to an entity

	/** Part that all tridas entities have
	 * add to each map before adding specific entries.
	 * Each entity level with name *** has a get***Entries
	 *
	 * note: instead of a function;
	 * place in generalEntityMap an add that first to all others, instead of using inheritance
	 */
	private List<UIMapEntry> getCommonEntries()
	{
		List<UIMapEntry> entries = new ArrayList<UIMapEntry>();

		//entries.add(new IndicateRequiredUIMapEntry("Title", 					"Title", 				 "TextPanel"));
		entries.add(new IndicateRequiredUIMapEntry("Title", 					"Title", 				 "TextAreaPanel"));
		entries.add(new UIMapEntry("Identifier", 				"Identifier", 			 "IdentifierPanel", Multiplicity.OPTIONAL));
		entries.add(new UIMapEntry("Created timestamp", 		"CreatedTimestamp", 	 "DateTimePanel", Multiplicity.OPTIONAL)); 
		entries.add(new UIMapEntry("Last modified timestamp", "LastModifiedTimestamp", "DateTimePanel", Multiplicity.OPTIONAL));
		entries.add(new UIMapEntry("Comments", 				"Comments", 			 "TextAreaPanel"));

		return entries;
	}

	/** get mapping info for the TridasProject class
	 * Used by getUIMapEntries
	 *
	 * @return List of The list of UIMapEntry's
	 */
	private List<UIMapEntry> getProjectEntries()
	{
		List<UIMapEntry> entries = new ArrayList<UIMapEntry>();
		// Part that all tridas entities have
		//entries.add(new IndicateRequiredUIMapEntry("Title", 					"Title", 				 "TextPanel", true));
		entries.add(new IndicateRequiredUIMapEntry("Title", 					"Title", 				 "TextAreaPanel", true));
		
		
		// Identifier is required by DCCD and not by TRiDaS
		entries.add(new IndicateRequiredUIMapEntry("Identifier", 				"Identifier", 			 "IdentifierPanel", Multiplicity.OPTIONAL, true));
		entries.add(new UIMapEntry("Created timestamp", 		"CreatedTimestamp", 	 "DateTimePanel", Multiplicity.OPTIONAL)); 
		entries.add(new UIMapEntry("Last modified timestamp", 	"LastModifiedTimestamp", "DateTimePanel", Multiplicity.OPTIONAL));
		entries.add(new UIMapEntry("Comments", 					"Comments", 			 "TextAreaPanel"));

		// Project specific part
		//entries.add(new IndicateRequiredUIMapEntry("Type", 			"Types", 		"ControlledVocabularyPanel", Multiplicity.MULTIPLE, true)); 
		// use ProjectTypepanel instead of cvoc		
		entries.add(new IndicateRequiredUIMapEntry("Type", 			"Types", 		"ProjectTypePanel", Multiplicity.MULTIPLE, true)); 

		entries.add(new UIMapEntry("Description", 	"Description", 	"TextAreaPanel"));//, true)); NOT open acces!
		entries.add(new IndicateRequiredUIMapEntry("Investigator", 	"Investigator", "TextPanel", true));
		entries.add(new IndicateRequiredUIMapEntry("Period", 		"Period", 		"TextPanel"));
		entries.add(new UIMapEntry("Commissioner", 	"Commissioner", "TextAreaPanel"));
		entries.add(new IndicateRequiredUIMapEntry("Category", 		"Category", 	"CategoryPanel", true)); 
		entries.add(new UIMapEntry("Reference", 	"References", 	"TextPanel", Multiplicity.MULTIPLE)); 
		entries.add(new IndicateRequiredUIMapEntry("Laboratory", 	"Laboratories", "LaboratoryPanel", Multiplicity.MULTIPLE, true));
		//entries.add(new UIMapEntry("File", 			"Files", 		"FilePanel", Multiplicity.MULTIPLE));
		// disable editing on files
		entries.add(new UIMapEntry("File", 			"Files", 		"FileRepeaterPanel"));
		
		entries.add(new UIMapEntry("Request date", 	"RequestDate", 	"DatePanel", Multiplicity.OPTIONAL, true)); 		
		entries.add(new UIMapEntry("Research", 		"Researches", 	"ResearchPanel", Multiplicity.MULTIPLE));
		entries.add(new UIMapEntry("GenericField", 	"GenericFields", 	"GenericFieldPanel", Multiplicity.MULTIPLE));
		
		return entries;
	}

	/** get mapping info for the TridasObject class
	 * Used by getUIMapEntries
	 *
	 * @return List of The list of UIMapEntry's
	 */
	private List<UIMapEntry> getObjectEntries()
	{
		List<UIMapEntry> entries = new ArrayList<UIMapEntry>();
		// Part that all tridas entities have
		//entries.add(new IndicateRequiredUIMapEntry("Title", 					"Title", 				 "TextPanel", true));
		entries.add(new IndicateRequiredUIMapEntry("Title", 					"Title", 				 "TextAreaPanel", true));
		entries.add(new UIMapEntry("Identifier", 				"Identifier", 			 "IdentifierPanel", Multiplicity.OPTIONAL));
		entries.add(new UIMapEntry("Created timestamp", 		"CreatedTimestamp", 	 "DateTimePanel", Multiplicity.OPTIONAL)); 
		entries.add(new UIMapEntry("Last modified timestamp", 	"LastModifiedTimestamp", "DateTimePanel", Multiplicity.OPTIONAL));
		entries.add(new UIMapEntry("Comments", 					"Comments", 			 "TextAreaPanel"));

		// object specific part
		//entries.add(new RequiredUIMapEntry("Type", 			"Type", 		"ControlledVocabularyPanel", true)); 
		entries.add(new IndicateRequiredUIMapEntry("Type", 			"Type", 		"ObjectTypePanel", true)); 

		entries.add(new UIMapEntry("Description", 	"Description", 	"TextAreaPanel"));
		entries.add(new UIMapEntry("Creator", 		"Creator", 		"TextPanel"));
		entries.add(new UIMapEntry("Owner", 		"Owner", 		"TextPanel"));
		//entries.add(new UIMapEntry("File", 			"Files", 		"FilePanel", Multiplicity.MULTIPLE));
		// disable editing on files
		entries.add(new UIMapEntry("File", 			"Files", 		"FileRepeaterPanel"));
		entries.add(new UIMapEntry("Coverage temporal", 		"Coverage", 	"CoveragePanel", Multiplicity.OPTIONAL)); 
		entries.add(new UIMapEntry("Location", 		"Location", 	"LocationPanel", Multiplicity.OPTIONAL));
		entries.add(new UIMapEntry("Linkseries", 	"LinkSeries", 	"SeriesLinksWithPreferredPanel", Multiplicity.OPTIONAL));
		entries.add(new UIMapEntry("GenericField", 	"GenericFields", "GenericFieldPanel", Multiplicity.MULTIPLE));

		return entries;
	}

	/** get mapping info for the TridasElement class
	 * Used by getUIMapEntries
	 *
	 * @return List of The list of UIMapEntry's
	 */
	private List<UIMapEntry> getElementEntries()
	{
		List<UIMapEntry> entries = new ArrayList<UIMapEntry>();
		// Part that all tridas entities have
		entries.addAll(getCommonEntries());

		// element specific part
		//entries.add(new UIMapEntry("Type", 			"Type", 		"ControlledVocabularyPanel")); 
		entries.add(new UIMapEntry("Type", 			"Type", 		"ObjectTypePanel", Multiplicity.OPTIONAL, true)); 

		entries.add(new UIMapEntry("Description", 	"Description", 	"TextAreaPanel"));
		entries.add(new UIMapEntry("Authenticity", 	"Authenticity", "TextAreaPanel"));
		entries.add(new UIMapEntry("Processing", 	"Processing", 	"TextAreaPanel"));
		entries.add(new UIMapEntry("Marks", 		"Marks", 		"TextAreaPanel"));
		//entries.add(new UIMapEntry("File", 			"Files", 		"FilePanel", Multiplicity.MULTIPLE));
		// disable editing on files
		entries.add(new UIMapEntry("File", 			"Files", 		"FileRepeaterPanel"));
		entries.add(new UIMapEntry("Location", 		"Location", 	"LocationPanel", Multiplicity.OPTIONAL)); 
		entries.add(new UIMapEntry("Linkseries", 	"LinkSeries", "SeriesLinksWithPreferredPanel", Multiplicity.OPTIONAL));
		//entries.add(new RequiredUIMapEntry("Taxon", 		"Taxon", 		"ControlledVocabularyPanel")); 
		entries.add(new IndicateRequiredUIMapEntry("Taxon", 		"Taxon", 		"TaxonPanel", Multiplicity.OPTIONAL)); 
	
		entries.add(new UIMapEntry("Shape", 		"Shape",		"ShapePanel", Multiplicity.OPTIONAL));
		entries.add(new UIMapEntry("Dimensions", 	"Dimensions", 	"DimensionsPanel", Multiplicity.OPTIONAL));
		entries.add(new UIMapEntry("Altitude", 		"Altitude" , 	"DoublePanel")); 
		entries.add(new UIMapEntry("Slope", 		"Slope", 		"SlopePanel", Multiplicity.OPTIONAL)); 
		entries.add(new UIMapEntry("Soil", 			"Soil", 		"SoilPanel", Multiplicity.OPTIONAL)); 
		entries.add(new UIMapEntry("Bedrock", 		"Bedrock", 		"BedrockPanel", Multiplicity.OPTIONAL)); 
		entries.add(new UIMapEntry("GenericField", 	"GenericFields", "GenericFieldPanel", Multiplicity.MULTIPLE));

		return entries;
	}

	/** get mapping info for the TridasSample class
	 * Used by getUIMapEntries
	 *
	 * @return List of The list of UIMapEntry's
	 */
	private List<UIMapEntry> getSampleEntries()
	{
		List<UIMapEntry> entries = new ArrayList<UIMapEntry>();
		// Part that all tridas entities have
		entries.addAll(getCommonEntries());

		// sample specific part
		entries.add(new UIMapEntry("Description", 		"Description", 		"TextAreaPanel"));
		entries.add(new UIMapEntry("Position",			"Position", 		"TextAreaPanel"));
		entries.add(new UIMapEntry("State",				"State", 			"TextAreaPanel"));
		//entries.add(new IndicateRequiredUIMapEntry("Type", 				"Type", 			"ControlledVocabularyPanel")); 
		entries.add(new IndicateRequiredUIMapEntry("Type", 				"Type", 			"SampleTypePanel")); 
		
		//entries.add(new UIMapEntry("File", 			"Files", 		"FilePanel", Multiplicity.MULTIPLE));
		// disable editing on files
		entries.add(new UIMapEntry("File", 			"Files", 		"FileRepeaterPanel"));
		entries.add(new UIMapEntry("Sampling date", 	"SamplingDate", 	"DatePanel", Multiplicity.OPTIONAL)); 
		entries.add(new UIMapEntry("Knots", 			"Knots", 			"BooleanPanel")); 
		entries.add(new UIMapEntry("GenericField", 		"GenericFields", 	"GenericFieldPanel", Multiplicity.MULTIPLE));
//		entries.add(new UIMapEntry("RadiusPlaceholder", "RadiusPlaceholder", "RadiusPlaceholderPanel"));
		// Note: RadiusPlaceholder is optional, but only allowed if there are no Radius entities under this Sample
		// Since we cannot add or remove the Radius, we can also not add or remove the RadiusPlaceholder
		// Only uploading another TRiDaS file would help
		
		return entries;
	}

	/** get mapping info for the TridasRadius class
	 * Used by getUIMapEntries
	 *
	 * @return List of The list of UIMapEntry's
	 */
	private List<UIMapEntry> getRadiusEntries()
	{
		List<UIMapEntry> entries = new ArrayList<UIMapEntry>();
		// Part that all tridas entities have
		entries.addAll(getCommonEntries());

		// radius specific part
		entries.add(new UIMapEntry("Azimuth", 			"Azimuth", 			"DecimalPanel")); 
		entries.add(new UIMapEntry("Wood Completeness", "WoodCompleteness", "WoodCompletenessPanel", Multiplicity.OPTIONAL)); 
		entries.add(new UIMapEntry("GenericField", 		"GenericFields", 	"GenericFieldPanel", Multiplicity.MULTIPLE));

		return entries;
	}


	/** get mapping info for the getMeasurementSeriesEntries class
	 * Used by getUIMapEntries
	 * Note the common base class with TridasMeasurementSeries
	 *
	 * @return List of The list of UIMapEntry's
	 */
	private List<UIMapEntry> getMeasurementSeriesEntries()
	{
		List<UIMapEntry> entries = new ArrayList<UIMapEntry>();
		// Part that all tridas entities have
		entries.addAll(getCommonEntries());

		// MeasurementSeries specific part
		entries.add(new UIMapEntry("Measuring date", 		"MeasuringDate", 			"DatePanel", Multiplicity.OPTIONAL)); 
		entries.add(new UIMapEntry("Wood Completeness", 	"WoodCompleteness", 		"WoodCompletenessPanel", Multiplicity.OPTIONAL)); 
		entries.add(new UIMapEntry("Analyst", 				"Analyst", 					"TextPanel"));
		entries.add(new UIMapEntry("Dendrochronologist", 	"Dendrochronologist", 		"TextPanel"));
		entries.add(new IndicateRequiredUIMapEntry("Measuring method", 		"MeasuringMethod", 			"MeasuringMethodPanel")); 
		//entries.add(new UIMapEntry("Interpretation", 		"Interpretation", 			"InterpretationPanel", Multiplicity.OPTIONAL)); 
		// JAXB has made this a separate (String) attribute "interpretationUnsolved"
		// It is not a String, but has a toString, so only view is used here!
		//entries.add(new UIMapEntry("Interpretation unsolved", "interpretationUnsolved", 	"InterpretationUnsolvedPanel"));
		// NEW InterpretationOptionalPanel
		entries.add(new UIMapEntry("Interpretation", 		"Interpretation", 			"InterpretationOptionalPanel")); 

		entries.add(new UIMapEntry("GenericField", 	"GenericFields", 	"GenericFieldPanel", Multiplicity.MULTIPLE));

		
		// No values on the panel!

		return entries;
	}

	/** get mapping info for the TridasDerivedSeries class
	 * Used by getUIMapEntries
	 * Note the common base class with TridasMeasurementSeries
	 *
	 * @return List of The list of UIMapEntry's
	 */
	private List<UIMapEntry> getDerivedSeriesEntries()
	{
		List<UIMapEntry> entries = new ArrayList<UIMapEntry>();
		// Part that all tridas entities have
		entries.addAll(getCommonEntries());

		// DerivedSeries specific part
		entries.add(new UIMapEntry("Derivation date", 		"DerivationDate", 			"DatePanel", Multiplicity.OPTIONAL)); 
		entries.add(new IndicateRequiredUIMapEntry("Type", 				"Type", 			"DerivedSeriesTypePanel")); 

		entries.add(new IndicateRequiredUIMapEntry("Linkseries", "LinkSeries", "LinkSeriesPanel"));
		entries.add(new UIMapEntry("Objective", 				"Objective", 				"TextAreaPanel"));
		entries.add(new UIMapEntry("Standardizing method", 		"StandardizingMethod", 		"TextAreaPanel"));
		entries.add(new UIMapEntry("Author", 					"Author", 					"TextAreaPanel"));
		entries.add(new UIMapEntry("Version", 					"Version", 					"TextAreaPanel"));
		entries.add(new UIMapEntry("Location", 					"Location", 				"LocationPanel", Multiplicity.OPTIONAL)); 
		
		// NEW InterpretationOptionalPanel
		entries.add(new UIMapEntry("Interpretation", 		"Interpretation", 			"InterpretationOptionalPanel")); 
		
		entries.add(new UIMapEntry("GenericField", 	"GenericFields", 	"GenericFieldPanel", Multiplicity.MULTIPLE));
		
		// No values on the panel!

		return entries;
	}

	/** Get the UIMapEntry's for an entity
	 * Instead of using the entity class, the entities tridas class is used
	 * Note: instead of a 'if-else' we could use a Map as well for the method and name
	 * using the class name as a key. But there are only six levels (Entities)
	 *
	 * @param Tclass The tridas class corresponding to the entity
	 * @return The list of UIMapEntry's
	 */
	@SuppressWarnings("unchecked")
	public List<UIMapEntry> getUIMapEntries(Class Tclass)
	{
		//System.out.println("getEntries called with: "+ Tclass.getName());

		// TODO compare classes and not strings with package name

		if (Tclass.getName().contentEquals("org.tridas.schema.TridasProject"))
		{
			return getProjectEntries();
		}
		else if (Tclass.getName().contentEquals(
				"org.tridas.schema.TridasObject") ||
				Tclass.getName().contentEquals(
				"org.tridas.util.TridasObjectEx"))
		{
			return getObjectEntries();
		}
		else if (Tclass.getName().contentEquals(
				"org.tridas.schema.TridasElement"))
		{
			return getElementEntries();
		}
		else if (Tclass.getName().contentEquals(
				"org.tridas.schema.TridasSample"))
		{
			return getSampleEntries();
		}
		else if (Tclass.getName().contentEquals(
				"org.tridas.schema.TridasRadius"))
		{
			return getRadiusEntries();
		}
		else if (Tclass.getName().contentEquals(
				"org.tridas.schema.TridasMeasurementSeries"))
		{
			return getMeasurementSeriesEntries();
		}
		else if (Tclass.getName().contentEquals(
				"org.tridas.schema.TridasDerivedSeries"))
		{
			return getDerivedSeriesEntries();
		}
		else
		{
			// no UI mapping available
			logger.warn("Entries for class: " + Tclass.getName()
					+ " not found, returning empty list");
			return new ArrayList<UIMapEntry>(); // empty list!
		}
	}

	/** Get the string for an entity (short name or label) that can be used on screen
	 * Instead of using the entity class, the entities tridas class is used
	 *
	 * @param Tclass The tridas class corresponding to the entity
	 * @return The string
	 */
	@SuppressWarnings("unchecked")
	public static String getEntityLabelString(Class Tclass)
	{
		// TODO compare classes, because its saver then strings
		return getEntityLabelString(Tclass.getName());
	}

	public static String getEntityLabelString(String className)
	{
		if (className.contentEquals("org.tridas.schema.TridasProject"))
		{
			return "Project";
		}
		else if (className.contentEquals(
				"org.tridas.schema.TridasObject") ||
				className.contentEquals(
				"org.tridas.util.TridasObjectEx"))
		{
			return "Object";
		}
		else if (className.contentEquals(
				"org.tridas.schema.TridasElement"))
		{
			return "Element";
		}
		else if (className.contentEquals(
				"org.tridas.schema.TridasSample"))
		{
			return "Sample";
		}
		else if (className.contentEquals(
				"org.tridas.schema.TridasRadius"))
		{
			return "Radius";
		}
		else if (className.contentEquals(
				"org.tridas.schema.TridasMeasurementSeries"))
		{
			return "Measurement series";
		}
		else if (className.contentEquals(
				"org.tridas.schema.TridasDerivedSeries"))
		{
			return "Derived series";
		}
		else
		{
			// no UI mapping (Name to use on screen) available
			logger.warn("UI Name for class: " + className
					+ " not found, returning empty string");
			return ""; // empty string!, should warn
		}
	}

}
