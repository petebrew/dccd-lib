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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import nl.knaw.dans.common.lang.ClassUtil;
import nl.knaw.dans.common.lang.search.SearchHit;
import nl.knaw.dans.common.lang.search.SearchResult;
import nl.knaw.dans.dccd.model.Project;
import nl.knaw.dans.dccd.model.entities.DerivedSeriesEntity;
import nl.knaw.dans.dccd.model.entities.ElementEntity;
import nl.knaw.dans.dccd.model.entities.Entity;
import nl.knaw.dans.dccd.model.entities.MeasurementSeriesEntity;
import nl.knaw.dans.dccd.model.entities.ObjectEntity;
import nl.knaw.dans.dccd.model.entities.ProjectEntity;
import nl.knaw.dans.dccd.model.entities.RadiusEntity;
import nl.knaw.dans.dccd.model.entities.SampleEntity;
import nl.knaw.dans.dccd.repository.xml.TridasValidationException;
import nl.knaw.dans.dccd.repository.xml.XMLFilesRepositoryService;
import nl.knaw.dans.dccd.search.DccdProjectSB;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.SeriesLink;
import org.tridas.schema.SeriesLinks;
import org.tridas.schema.SeriesLinksWithPreferred;
//import org.tridas.schema.TridasCategory;
import org.tridas.schema.TridasDatingReference;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasDimensions;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasStatFoundation;
import org.tridas.schema.TridasValues;
import org.tridas.util.TridasObjectEx;

/**
 * Validation plays a role in the workflow of DCCD 
 * - When a TRiDaS file is being uploaded it must be valid to the schema, but it is not explicitely validated. 
 * - The Draft project can be edited and required pars can be removed, so the draft can become non-valid to the schema. 
 *   It is be stored in the repository as XML-fragments. 
 *   Whenever an edited entity is saved validation is done with the extra requirements 
 *   imposed by the DCCD (ControledVocabulary etc.)
 *   But parts that cannot be edited must still be valid and are not validated again. 
 *   Validation results are show, in order for the user to see what needs to be done to make it valid. 
 * - When a Draft is to be archived the whoel project is validated (for DCCD requiremenst) and 
 *   just to be sure, we do an explicit schema validation. 
 *
 */
public class DccdProjectValidationService
{
	private static Logger logger = Logger.getLogger(DccdProjectValidationService.class);
	private static String MISSING_REQUIRED_FIELD_MSG = "Missing required field";
	private static String MISSING_TRIDAS_CVOC_MSG = "Missing TRiDaS controlled vocabulary";
	private static String MISSING_DCCD_CVOC_MSG = "Missing DCCD controlled vocabulary";
	private static String INVALID_DCCD_CVOC_MSG = "Invalid DCCD controlled vocabulary term for";
	private static String INVALID_LINK_MSG = "Invalid Link, needs one and only one of IdRef, XLink or Identifier for";
	private static String INVALID_TRIDAS_MSG = "Not conform the TRiDaS Standard";
	
	// singleton pattern with lazy construction
	private static DccdProjectValidationService service = null;
	public static DccdProjectValidationService getService() 
	{
		if (service == null) 
		{
			service = new DccdProjectValidationService();
		}
		return service;
	}

	/**
	 * Validate one entity (and not it's sub entities)
	 * 
	 * @param entity
	 * @return
	 */
	public List<ValidationErrorMessage> validate(Entity entity, String languageCode)
	{
		List<ValidationErrorMessage> entityErrorMessages = new ArrayList<ValidationErrorMessage>();
		
		// start as being valid
		entity.setValidForArchiving(true);	
		
		// TRiDaS validation, test for required fields
		// Note; always test for required tridas fields, JAXB doesn't check it on upload
		if (entity.hasTridas())
		{
			// required fields must be available
			List<ValidationErrorMessage> validateForRequiredTridasFields = validateForRequiredTridasFields(entity.getTridasAsObject());
			entityErrorMessages.addAll(validateForRequiredTridasFields);

			if (!validateForRequiredTridasFields.isEmpty()) 
			{
				entity.setValidForArchiving(false);
			}
		}
		
		// Entity specific validation
		// Non-TRiDaS validation, extra requirements by DCCD
		// Note: maybe delegate this to the entities instead of a switch
		if (entity instanceof ProjectEntity)
		{
			entityErrorMessages.addAll(validateProjectEntity((ProjectEntity)entity, languageCode));
		}
		else if (entity instanceof ObjectEntity)
		{
			entityErrorMessages.addAll(validateObjectEntity((ObjectEntity)entity, languageCode));
		}
		else if (entity instanceof ElementEntity)
		{
			entityErrorMessages.addAll(validateElementEntity((ElementEntity)entity, languageCode));
		}
		else if (entity instanceof SampleEntity)
		{
			entityErrorMessages.addAll(validateSampleEntity((SampleEntity)entity, languageCode));
		}
		else if (entity instanceof RadiusEntity)
		{
			entityErrorMessages.addAll(validateRadiusEntity((RadiusEntity)entity, languageCode));
		}
		else if (entity instanceof MeasurementSeriesEntity)
		{
			entityErrorMessages.addAll(validateMeasurementSeriesEntity((MeasurementSeriesEntity)entity, languageCode));
		}
		else if (entity instanceof DerivedSeriesEntity)
		{
			entityErrorMessages.addAll(validateDerivedSeriesEntity((DerivedSeriesEntity)entity, languageCode));
		}
		// Other entities have nothing specific to be validated...
		
		// set the entity name on all its error messages
		for(ValidationErrorMessage entityErrorMessage : entityErrorMessages)
		{
			entityErrorMessage.setEntityId(entity.getId());
		}
		
		return entityErrorMessages;
	}
	
	public List<ValidationErrorMessage> validateAgainstTridasSchema(Project project)
	{
		//boolean isValid = true;
		final List<ValidationErrorMessage> errorMessages = new ArrayList<ValidationErrorMessage>(); 
		
		if(project != null && project.hasTridas() )
		{			
			logger.debug("Validating project against TRiDaS schema: "+ project.getTitle());
			try 
			{
				XMLFilesRepositoryService.validateAgainstTridasSchema(project);
			}
			catch(TridasValidationException e)
			{
				//isValid = false;
				
				String errorMsg = INVALID_TRIDAS_MSG;
				// - Not conform the TRiDaS Standard  (also when not valid XML)
				//
				// use cause when available
				Throwable cause = e.getCause();
				if (cause != null && cause.getMessage() != null) 
				{
					errorMsg +=	", Cause: " + cause.getMessage();
				}
				else
				{
					errorMsg += ": " + e.getMessage();
				}
				
				//System.out.println(errorMsg);
				
				// converting it to a ValidationErrorMessage (only one possible)
				// ? could just put it at the highest (ProjectEntity) level
				// but use empty strings for class and field instead
				errorMessages.add(new ValidationErrorMessage(errorMsg, "", ""));
			}
		}
		
		//System.out.println("valid = " + isValid);
		return errorMessages;
	}
	
	/**
	 * validate(Project project)
	 * should return a list of validationErrors, empty if there are no errors
	 * 
	 * @param project
	 * @return
	 */
	public List<ValidationErrorMessage> validate(Project project)
	{
		final List<ValidationErrorMessage> errorMessages = new ArrayList<ValidationErrorMessage>(); 
		
		if(project != null && project.hasTridas() )
		{			
			logger.debug("Validating project: "+ project.getTitle());
			
			String languageCode = project.getTridasLanguage().getLanguage();
			
			List<Entity> entities = project.entityTree.getEntities();
			
			logger.debug("Number of entities to validate: " + entities.size() );
			for (Entity entity : entities)
			{
				//logger.debug("Entity " + entity.getLabel());
				// do the validation, and add errors when found
				errorMessages.addAll(validate(entity, languageCode));
			}
			logger.debug("Done validating project: "+ project.getTitle());
		}
		
		return errorMessages;
	}

	// Controlled vocabularies
	//
	//  TRiDaS controlled vocabulary; normalTridas attribute (enumerations)
	// - element.dimensions.unit 
	// - element.shape
	// - measurementSeries.measuringMethod
	// DCCD lists; multiple languages possible, but only one-to-one mapping
	// - project.category  
	// - project.type
	// - sample.type
	// - derivedSeries.type 
	// DCCD lists; Complex mapping between languages
	// - object.type & element.type
	// Taxonomy
	// - element.taxon

	/**
	 * Validation specific for the Project entity
	 * Note: maybe we should have the entity validate itself?
	 * @param projectEntity
	 * @param project
	 * 
	 * @return
	 */
	private List<ValidationErrorMessage> validateProjectEntity(ProjectEntity projectEntity, String languageCode)
	{
		final List<ValidationErrorMessage> errorMessages = new ArrayList<ValidationErrorMessage>(); 
		logger.debug("validation specific for ProjectEntity");
		
		// Check fields that are required by tridas
		if (projectEntity.hasTridas())
		{
			TridasProject tridasProject = (TridasProject)projectEntity.getTridasAsObject();

			// Identifier must be set and unique within system, extra DCCD requirement
			if (tridasProject.isSetIdentifier())
			{
				// check if correct Tridas, but don't add message for a missing required
				List<ValidationErrorMessage> identifierFieldMessages = validateForRequiredTridasFields(tridasProject.getIdentifier());
				if (identifierFieldMessages.isEmpty())
				{
					// it has the required fields, but now check if it is unique
					List<ValidationErrorMessage> identifierUniqueMessages = validateProjectIdentfierUniqueness(tridasProject.getIdentifier());
					if(!identifierUniqueMessages.isEmpty())
					{
						errorMessages.addAll(identifierUniqueMessages);
						projectEntity.setValidForArchiving(false);
					}
				}
			}
			else
			{
				errorMessages.add( new ValidationErrorMessage(MISSING_REQUIRED_FIELD_MSG, tridasProject.getClass().getName(), "identifier"));
				projectEntity.setValidForArchiving(false);
			}
			
			// DCCD vocabularies
			// - project.category  
			if (tridasProject.isSetCategory())
			{
				ControlledVoc category = tridasProject.getCategory();
				// term or normalId?
				if (category.isSetValue())
				{
					// should be in the DCCD vocabulary
					String term = category.getValue(); 
					if (!DccdVocabularyService.getService().hasTerm("project.category", term, languageCode))
					{
						// illegal term!
						errorMessages.add( new ValidationErrorMessage(INVALID_DCCD_CVOC_MSG, tridasProject.getClass().getName(), "category"));
						projectEntity.setValidForArchiving(false);
					}
				}
				else
				{
					// its missing!
					errorMessages.add( new ValidationErrorMessage(MISSING_DCCD_CVOC_MSG, tridasProject.getClass().getName(), "category"));
					projectEntity.setValidForArchiving(false);
				}
			}
			
			// - project.type
			if (tridasProject.isSetTypes())
			{
				List<ControlledVoc> types = tridasProject.getTypes();
				for(int index=0; index < types.size(); index++)
				{
					ControlledVoc type = types.get(index);
					String fieldName = "type" + "[" + index + "]";
					
					// term or normalId?
					if (type.isSetValue())
					{
						// should be in the DCCD vocabulary
						String term = type.getValue(); 
						
						if (!DccdVocabularyService.getService().hasTerm("project.type", term, languageCode))
						{
							// illegal term!
							errorMessages.add( new ValidationErrorMessage(INVALID_DCCD_CVOC_MSG, 
									tridasProject.getClass().getName(), fieldName));
							projectEntity.setValidForArchiving(false);
						}
					}
					else
					{
						// its missing!
						errorMessages.add( new ValidationErrorMessage(MISSING_DCCD_CVOC_MSG, 
								tridasProject.getClass().getName(), fieldName));
						projectEntity.setValidForArchiving(false);
					}
				}
			}
		}
		
		return errorMessages;
	}
	
	private List<ValidationErrorMessage> validateObjectEntity(ObjectEntity objecttEntity, String languageCode)
	{
		final List<ValidationErrorMessage> errorMessages = new ArrayList<ValidationErrorMessage>(); 
		logger.debug("validation specific for ObjectEntity");

		// Check fields that are required by DCCD
		if (objecttEntity.hasTridas())
		{
			TridasObject tridasObject = (TridasObject)objecttEntity.getTridasAsObject();
			
			// - object.type
			if (tridasObject.isSetType())
			{
				// term or normalId?
				if (tridasObject.getType().isSetValue())
				{
					// should be in the DCCD vocabulary
					String term = tridasObject.getType().getValue(); 
					if (!DccdVocabularyService.getService().hasTerm("object.type", term, languageCode))
					{
						// illegal term!
						errorMessages.add( new ValidationErrorMessage(INVALID_DCCD_CVOC_MSG, 
								tridasObject.getClass().getName(), "type"));
						objecttEntity.setValidForArchiving(false);
					}
				}
				else
				{
					// its missing!
					errorMessages.add( new ValidationErrorMessage(MISSING_DCCD_CVOC_MSG, 
							tridasObject.getClass().getName(), "type"));
					objecttEntity.setValidForArchiving(false);
				}
			}
	
			// Check linkseries 
			if (tridasObject.isSetLinkSeries())
			{
				SeriesLinksWithPreferred seriesLinksWithPreferred = tridasObject.getLinkSeries();
				if (seriesLinksWithPreferred.isSetPreferredSeries())
				{
					SeriesLink preferredSeries = seriesLinksWithPreferred.getPreferredSeries();
					
					List<ValidationErrorMessage> validateForSeriesLink = 
						validateSeriesLink(tridasObject, preferredSeries);
					if (!validateForSeriesLink.isEmpty()) 
						objecttEntity.setValidForArchiving(false);
					
					errorMessages.addAll(validateForSeriesLink);
				}
			}
		}
		// what if it has no tridas, what message is generated then?
		
		return errorMessages;
	}

	private List<ValidationErrorMessage> validateElementEntity(ElementEntity elementEntity, String languageCode)
	{
		final List<ValidationErrorMessage> errorMessages = new ArrayList<ValidationErrorMessage>(); 
		logger.debug("validation specific for ElementEntity");

		// Check fields that are required by DCCD
		if (elementEntity.hasTridas())
		{
			TridasElement tridasElement = (TridasElement)elementEntity.getTridasAsObject();
			
			// element.shape
			// if it has a shape the normalTridas must be set
			if (tridasElement.isSetShape() && 
				!tridasElement.getShape().isSetNormalTridas())
			{
				// not valid!
				errorMessages.add( new ValidationErrorMessage(MISSING_TRIDAS_CVOC_MSG, 
						tridasElement.getClass().getName(), "shape"));
				elementEntity.setValidForArchiving(false);
			}
			
			// element.dimensions.unit
			// if it has a dimensions the unit normalTridas must be set
			if (tridasElement.isSetDimensions())
			{
				TridasDimensions dimensions = tridasElement.getDimensions();
				
				if(	dimensions.isSetUnit() && 
					!dimensions.getUnit().isSetNormalTridas())
				{
					// not valid!
					errorMessages.add( new ValidationErrorMessage(MISSING_TRIDAS_CVOC_MSG, 
							tridasElement.getClass().getName(), "dimensions.unit"));
					elementEntity.setValidForArchiving(false);
				}
			}
			
			
			// element.type
			if (tridasElement.isSetType())
			{
				// term or normalId?
				if (tridasElement.getType().isSetValue())
				{
					// should be in the DCCD vocabulary
					String term = tridasElement.getType().getValue(); 
					if (!DccdVocabularyService.getService().hasTerm("element.type", term, languageCode))
					{
						// illegal term!
						errorMessages.add( new ValidationErrorMessage(INVALID_DCCD_CVOC_MSG, 
								tridasElement.getClass().getName(), "type"));
						elementEntity.setValidForArchiving(false);
					}
				}
				else
				{
					// its missing!
					errorMessages.add( new ValidationErrorMessage(MISSING_DCCD_CVOC_MSG, 
							tridasElement.getClass().getName(), "type"));
					elementEntity.setValidForArchiving(false);
				}
			}
			
			// element.taxon
			if (tridasElement.isSetTaxon())
			{
				// term or normalId?
				if (tridasElement.getTaxon().isSetValue())
				{
					// should be in the DCCD vocabulary
					String term = tridasElement.getTaxon().getValue(); 
					if (!DccdVocabularyService.getService().hasTerm("element.taxon", term, languageCode))
					{
						// illegal term!
						errorMessages.add( new ValidationErrorMessage(INVALID_DCCD_CVOC_MSG, 
								tridasElement.getClass().getName(), "taxon"));
						elementEntity.setValidForArchiving(false);
					}
				}
				else
				{
					// its missing!
					errorMessages.add( new ValidationErrorMessage(MISSING_DCCD_CVOC_MSG, 
							tridasElement.getClass().getName(), "taxon"));
					elementEntity.setValidForArchiving(false);
				}
			}
			
			// Check linkseries 
			if (tridasElement.isSetLinkSeries())
			{
				SeriesLinksWithPreferred seriesLinksWithPreferred = tridasElement.getLinkSeries();
				if (seriesLinksWithPreferred.isSetPreferredSeries())
				{
					SeriesLink preferredSeries = seriesLinksWithPreferred.getPreferredSeries();
					
					List<ValidationErrorMessage> validateForSeriesLink = 
						validateSeriesLink(tridasElement, preferredSeries);
					if (!validateForSeriesLink.isEmpty()) 
						elementEntity.setValidForArchiving(false);
					
					errorMessages.addAll(validateForSeriesLink);
				}
			}
		}
		// what if it has no tridas, what message is generated then?
		
		return errorMessages;
	}
	

	private List<ValidationErrorMessage> validateSampleEntity(SampleEntity sampleEntity, String languageCode)
	{
		final List<ValidationErrorMessage> errorMessages = new ArrayList<ValidationErrorMessage>(); 
		logger.debug("validation specific for SampleEntity");

		// Check fields that are required by DCCD
		if (sampleEntity.hasTridas())
		{
			TridasSample tridasSample = (TridasSample)sampleEntity.getTridasAsObject();
			
			// TODO refactor into validateType function?
			// sample.type
			if (tridasSample.isSetType())
			{
				// term or normalId?
				if (tridasSample.getType().isSetValue())
				{
					// should be non-empty
					if (tridasSample.getType().getValue().trim().isEmpty())
					{
						logger.debug("Missing required \'" + tridasSample.getClass().getName() + "\' was empty or whitespace string");
						errorMessages.add( new ValidationErrorMessage(MISSING_REQUIRED_FIELD_MSG, tridasSample.getClass().getName(), "type"));									
						sampleEntity.setValidForArchiving(false);
					}

					// should be in the DCCD vocabulary
					//String term = tridasSample.getType().getValue(); 
					//if (!DccdVocabularyService.getService().hasTerm("sample.type", term, languageCode))
					//{
					//	// illegal term!
					//	errorMessages.add( new ValidationErrorMessage(INVALID_DCCD_CVOC_MSG, 
					//			tridasSample.getClass().getName(), "type"));
					//	sampleEntity.setValidForArchiving(false);
					//}
				}
				else
				{
					// its missing!
					errorMessages.add( new ValidationErrorMessage(MISSING_DCCD_CVOC_MSG, 
							tridasSample.getClass().getName(), "type"));
					sampleEntity.setValidForArchiving(false);
				}
			}
		}
		// what if it has no tridas, what message is generated then?
		
		return errorMessages;
	}

	private List<ValidationErrorMessage> validateRadiusEntity(RadiusEntity radiusEntity, String languageCode)
	{
		final List<ValidationErrorMessage> errorMessages = new ArrayList<ValidationErrorMessage>(); 
		logger.debug("validation specific for RadiusEntity");

		// Check fields that are required by DCCD
		if (radiusEntity.hasTridas())
		{
			TridasRadius tridasRadius = (TridasRadius)radiusEntity.getTridasAsObject();
			
		}
		// what if it has no tridas, what message is generated then?
		
		return errorMessages;
	}
	
	private List<ValidationErrorMessage> validateMeasurementSeriesEntity(MeasurementSeriesEntity seriesEntity, String languageCode)
	{
		final List<ValidationErrorMessage> errorMessages = new ArrayList<ValidationErrorMessage>(); 
		//logger.debug("validation specific for MeasurementSeriesEntity");

		// Check fields that are required by DCCD
		if (seriesEntity.hasTridas())
		{
			TridasMeasurementSeries tridasSeries = (TridasMeasurementSeries)seriesEntity.getTridasAsObject();
			
			// if it has a measuringMethod the normalTridas must be set
			if (tridasSeries.isSetMeasuringMethod() && 
				!tridasSeries.getMeasuringMethod().isSetNormalTridas())
			{
				// not valid!
				errorMessages.add( new ValidationErrorMessage(MISSING_TRIDAS_CVOC_MSG, 
						tridasSeries.getClass().getName(), "measuringMethod"));
				seriesEntity.setValidForArchiving(false);
			}
			
			// check interpretation.statFoundation
			if (tridasSeries.isSetInterpretation())
			{
				TridasInterpretation interpretation = tridasSeries.getInterpretation();
				if (interpretation.isSetStatFoundations())
				{
					List<TridasStatFoundation> statFoundations = interpretation.getStatFoundations();
					for(int i=0; i < statFoundations.size(); i++)
					{
						TridasStatFoundation statFoundation = statFoundations.get(i);
						// should be non-empty
						if (!statFoundation.isSetType() || 
							!statFoundation.getType().isSetValue() || 
							statFoundation.getType().getValue().trim().isEmpty())
						{
							logger.debug("Missing required \'" + statFoundation.getType().getClass().getName() + "\' was empty or whitespace string");
							errorMessages.add( new ValidationErrorMessage(MISSING_REQUIRED_FIELD_MSG, 
									tridasSeries.getClass().getName(), 
									"interpretation.statFoundations" + "[" + i + "]" + "." + "type"));									
							seriesEntity.setValidForArchiving(false);
						}
					}
				}
			}			
			// linkSeries
			if (tridasSeries.isSetInterpretation()){
				TridasInterpretation interpretation = tridasSeries.getInterpretation();
				if (interpretation.isSetDatingReference())
				{
					TridasDatingReference datingReference = interpretation.getDatingReference();
					if (datingReference.isSetLinkSeries()) // should be there
					{
						SeriesLink seriesLink = datingReference.getLinkSeries();
						List<ValidationErrorMessage> validateForSeriesLink = 
							validateSeriesLink(tridasSeries, seriesLink);
						if (!validateForSeriesLink.isEmpty()) 
							seriesEntity.setValidForArchiving(false);
						
						errorMessages.addAll(validateForSeriesLink);						
					}
				}
			}
		}
		// what if it has no tridas, what message is generated then?
		
		return errorMessages;
	}
			
	private List<ValidationErrorMessage> validateDerivedSeriesEntity(DerivedSeriesEntity seriesEntity, String languageCode)
	{
		final List<ValidationErrorMessage> errorMessages = new ArrayList<ValidationErrorMessage>(); 
		logger.debug("validation specific for MeasurementSeriesEntity");
		
		// Check fields that are required by DCCD
		if (seriesEntity.hasTridas())
		{
			TridasDerivedSeries tridasSeries = (TridasDerivedSeries)seriesEntity.getTridasAsObject();
			
			// type
			if (tridasSeries.isSetType())
			{
				// term or normalId?
				if (tridasSeries.getType().isSetValue())
				{
					// should be non-empty
					if (tridasSeries.getType().getValue().trim().isEmpty())
					{
						logger.debug("Missing required \'" + tridasSeries.getClass().getName() + "\' was empty or whitespace string");
						errorMessages.add( new ValidationErrorMessage(MISSING_REQUIRED_FIELD_MSG, tridasSeries.getClass().getName(), "type"));									
						seriesEntity.setValidForArchiving(false);
					}
					
					// should be in the DCCD vocabulary
					//String term = tridasSeries.getType().getValue(); 
					//if (!DccdVocabularyService.getService().hasTerm("derivedseries.type", term, languageCode))
					//{
					//	// illegal term!
					//	errorMessages.add( new ValidationErrorMessage(INVALID_DCCD_CVOC_MSG, 
					//			tridasSeries.getClass().getName(), "type"));
					//	seriesEntity.setValidForArchiving(false);
					//}
				}
				else
				{
					// its missing!
					errorMessages.add( new ValidationErrorMessage(MISSING_DCCD_CVOC_MSG, 
							tridasSeries.getClass().getName(), "type"));
					seriesEntity.setValidForArchiving(false);
				}
			}
			
			// check interpretation.statFoundation
			if (tridasSeries.isSetInterpretation())
			{
				TridasInterpretation interpretation = tridasSeries.getInterpretation();
				if (interpretation.isSetStatFoundations())
				{
					List<TridasStatFoundation> statFoundations = interpretation.getStatFoundations();
					for(int i=0; i < statFoundations.size(); i++)
					{
						TridasStatFoundation statFoundation = statFoundations.get(i);
						// should be non-empty
						if (!statFoundation.isSetType() || 
							!statFoundation.getType().isSetValue() || 
							statFoundation.getType().getValue().trim().isEmpty())
						{
							logger.debug("Missing required \'" + statFoundation.getType().getClass().getName() + "\' was empty or whitespace string");
							errorMessages.add( new ValidationErrorMessage(MISSING_REQUIRED_FIELD_MSG, 
									tridasSeries.getClass().getName(), 
									"interpretation.statFoundations" + "[" + i + "]" + "." + "type"));									
							seriesEntity.setValidForArchiving(false);
						}
					}
				}
			}
			
			// Check linkseries 
			if (tridasSeries.isSetLinkSeries())
			{
				SeriesLinks seriesLinks = tridasSeries.getLinkSeries();
				if (seriesLinks.isSetSeries())
				{
					List<SeriesLink> series = seriesLinks.getSeries();
					for(SeriesLink seriesLink : series)
					{
						errorMessages.addAll(validateSeriesLink(tridasSeries, seriesLink));
					}
					
					if (!errorMessages.isEmpty()) 
						seriesEntity.setValidForArchiving(false);
				}
			}
		}
		// what if it has no tridas, what message is generated then?
		
		return errorMessages;
	}
			
	/**
	 * Check it there is not an archived project with the same identifier
	 * 
	 * @param tridasIdentifier
	 * @return
	 */
	private List<ValidationErrorMessage> validateProjectIdentfierUniqueness(TridasIdentifier tridasIdentifier)
	{		
		List<ValidationErrorMessage> errorMessages = new ArrayList<ValidationErrorMessage>(); 
		
		logger.debug("Checking if Project Identifier is unique");
		
		try
		{
			SearchResult<DccdProjectSB> result = DccdSearchService.getService().findProjectArchivedWithSameTridasIdentifier(tridasIdentifier);
			if (result.getTotalHits() > 0)
			{
				// Not unique!
				logger.debug("Project has not a unique identifier!");
				
				SearchHit<DccdProjectSB> searchHit = result.getHits().get(0);
				// construct message
				String archivedProjectTitle = "";
				if (searchHit != null) 
				{
					archivedProjectTitle = searchHit.getData().getTridasProjectTitle();
				}
				String invalidIdMsg = "The project has the same ID as the existing, archived project '" + 
					archivedProjectTitle + "\'";
				errorMessages.add( new ValidationErrorMessage(invalidIdMsg, TridasProject.class.getName(), "identifier"));				
			}
		}
		catch (SearchServiceException e)
		{
			logger.error("Failed to validate project id", e);
			//getSession().error("Failed to validate project id"); // use resource?
			//throw new RestartResponseException(ErrorPage.class);
			
			// TODO throw some exception!
		}
		
		return errorMessages;
	}

	/**
	 * find the required fields and then checks if set 
	 * using the JAXB annotations 
	 * 
	 * The JAXB object fields are of the correct type, so no extra validation needed
	 * and also the structure cannot be wrong
	 * 
	 * @param tridas
	 * @return
	 */
	private List<ValidationErrorMessage> validateForRequiredTridasFields(Object tridas)
	{
		final List<ValidationErrorMessage> errorMessages = new ArrayList<ValidationErrorMessage>(); 

		//logger.debug("Validating required fields for object of class: " + tridas.getClass().getSimpleName());

		// NOTE an object has a lot of fields; all from Object down to the specific class...
		//
		// find all properties with required = true:
		//@XmlElement(name = "type", required = true)
		// and then call the isSet method
		Class tridasClass = tridas.getClass();
		Collection<Field> values = ClassUtil.getAllFields(tridasClass).values();
		for(java.lang.reflect.Field classField : values)
		{
			boolean isRequired = false;
			
			// determine the field is required
			isRequired = isFieldRequired(classField);			
			
			String fieldName = classField.getName();
			String getMethodName = "get"+ StringUtils.capitalize(fieldName);
			String isSetMethodName = "isSet"+ StringUtils.capitalize(fieldName);

			Boolean isSet = false;
			Object fieldObject = null;
			// retrieve the methods and call them
			try
			{
				Method isSetMethod = null;
				Method getMethod = null;
				isSetMethod = tridasClass.getMethod(isSetMethodName);
				getMethod = tridasClass.getMethod(getMethodName);
				// JAXB, so we assume isSet returns boolean
				isSet = (Boolean)isSetMethod.invoke(tridas);
				fieldObject = getMethod.invoke(tridas);
			}
			catch (NoSuchMethodException e)
			{
				continue; //skip this field, probably no JAXB TRiDaS generated class 
			}
			catch (SecurityException e)
			{
				continue; //skip this field 
			}
			catch (IllegalArgumentException e)
			{
				continue; //skip this field
			}
			catch (IllegalAccessException e)
			{
				continue; //skip this field
			}
			catch (InvocationTargetException e)
			{
				continue; //skip this field
			}
			
			// Required fields only
			if (isRequired)
			{
				if (!isSet)
				{
					// required field missing
					logger.debug("Missing required " + fieldName);
					errorMessages.add( new ValidationErrorMessage(MISSING_REQUIRED_FIELD_MSG, tridasClass.getName(), fieldName));
				}
				else
				{
					//logger.debug("Required field \'" + fieldName + "\' was set");
					// assume if it is set, the object return should not be null
					//
					// Handle empty fields (set but empty)
					// do extra check for strings that might be empty...
					if (fieldObject instanceof String)
					{
						if ( ((String)fieldObject).trim().isEmpty())
						{
							logger.debug("Missing required \'" + fieldName + "\' was empty or whitespace string");
							errorMessages.add( new ValidationErrorMessage(MISSING_REQUIRED_FIELD_MSG, tridasClass.getName(), fieldName));									
						}
					}
				}
			}
			
			// All fields set, required or not
			// Validating sub Objects 
			if (isSet)
			{
				if (fieldObject instanceof List)
				{
					// Handle List objects
					List listField = (List)fieldObject; // we can cast
					errorMessages.addAll(validateListFieldItemsForRequiredTridasFields(listField, tridasClass, fieldName));
				}
				else
				{
					// not a List, just a normal field
					// validate the sub fields (recurse into tree)					
					errorMessages.addAll(validateSubFieldForRequiredTridasFields(fieldObject, tridasClass, fieldName));
				}
			}			
		}		
		
		return errorMessages;
	}
	
	/**
	 * Determine if annotation is present and the field is required
	 *
	 * Note: This function is called often and optimizing it was needed
	 * 
	 * @param classField
	 * @return
	 */
	static private boolean isFieldRequired(final java.lang.reflect.Field classField)
	{
		if (classField.isAnnotationPresent(XmlElement.class))
		{
			//logger.debug("Inspecting annotations for " + XmlElement.class.getSimpleName());
			return(classField.getAnnotation(XmlElement.class).required());
		}
		else if (classField.isAnnotationPresent(XmlAttribute.class))
		{
			// assume it can not have both Element and Attribute annotations 
			//logger.debug("Inspecting annotations for " + XmlAttribute.class.getSimpleName());
			return(classField.getAnnotation(XmlAttribute.class).required());
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Validation for all items in the list
	 *  
	 * @param listField
	 * @param tridasClass
	 * @param fieldName
	 * @return
	 */
	private List<ValidationErrorMessage> validateListFieldItemsForRequiredTridasFields(List listField, Class tridasClass, String fieldName)
	{
		List<ValidationErrorMessage> errorMessages = new ArrayList<ValidationErrorMessage>();

		//logger.debug("Field is a List \'" + fieldName + "\'");				    
		
		// Validation for all elements
		for(int i=0; i < listField.size(); i++)
		{
			Object listFieldItem = listField.get(i);
			
			// skip subEntities, entity level Tridas object that are in a list!
			if (isTridasEntityLevelObject(listFieldItem))	
				break; // maybe have a check on the list itself

			// validate it
			List<ValidationErrorMessage> itemErrorMessages = validateForRequiredTridasFields(listFieldItem);
			// class name and filed name must be changed
			for(ValidationErrorMessage itemErrorMessage : itemErrorMessages)
			{
				// use the class name of this level
				itemErrorMessage.setClassName(tridasClass.getName());
				// prepend this fieldname
				itemErrorMessage.setFieldName(fieldName + "[" + i + "]" + "." + itemErrorMessage.getFieldName());
			}
			errorMessages.addAll(itemErrorMessages);
		}
		
		return errorMessages;
	}
	
	/**
	 * Each entity has a Tridas object that corresponds with the entity (level) in the TRiDaS hierarchie
	 * This member tests if the given object is one of those Tridas objects
	 * 
	 * @param object
	 * @return
	 */
	private boolean isTridasEntityLevelObject(Object object)
	{
		if (object instanceof TridasProject ||
			object instanceof TridasObject || object instanceof TridasObjectEx || 
			object instanceof TridasElement || 
			object instanceof TridasRadius || 
			object instanceof TridasSample ||
			object instanceof TridasMeasurementSeries ||
			object instanceof TridasDerivedSeries ||
			object instanceof TridasValues
			// TridasValue is omitted here!
		)
		{
			return true; 
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * 	validate the field (recurse into tree)					
	 * 
	 * @param fieldObject
	 * @param tridasClass
	 * @param fieldName
	 * @return
	 */
	private List<ValidationErrorMessage> validateSubFieldForRequiredTridasFields(Object fieldObject, Class tridasClass, String fieldName)
	{
		List<ValidationErrorMessage> fieldErrorMessages = validateForRequiredTridasFields(fieldObject);

		//logger.debug("Recursing into field \'" + fieldName + "\'");
		
		// class name and field name must be changed
		for(ValidationErrorMessage entityErrorMessage : fieldErrorMessages)
		{
			// use the class name of this level
			entityErrorMessage.setClassName(tridasClass.getName());
			// prepend this fieldname
			entityErrorMessage.setFieldName(fieldName + "." + entityErrorMessage.getFieldName());
		}
		
		return fieldErrorMessages;
	}

	private List<ValidationErrorMessage> validateSeriesLink(Object object, SeriesLink seriesLink)
	{		
		List<ValidationErrorMessage> errorMessages = new ArrayList<ValidationErrorMessage>(); 
		
		// Note; could check for non-whitespace, but we don't check if the ID in IdRef excists
		int linkCounter = 0; 
		if(seriesLink.isSetIdentifier()) linkCounter++;
		if(seriesLink.isSetIdRef()) linkCounter++;
		if(seriesLink.isSetXLink()) linkCounter++;
		if(linkCounter != 1)
		{
			// error, needs one and only one of IdRef, XLink or Identifier
			errorMessages.add( new ValidationErrorMessage(INVALID_LINK_MSG, 
					object.getClass().getName(), "linkseries"));						
		}
		return errorMessages;
	}
}
