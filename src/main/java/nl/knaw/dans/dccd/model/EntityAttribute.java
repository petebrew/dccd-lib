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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

/** A (TRiDaS) entity property or attribute
 * Holds a reference to an object in the TRiDaS data tree (JAXB generated classses)
 * And also mapping UI information for this data
 * Needed for building web panels
 *
 * TODO: unit testing
 *
 * @author paulboon
 *
 */
public class EntityAttribute implements Serializable {
	private static final long serialVersionUID = -4665491283153225755L;
	private static Logger logger = Logger.getLogger(EntityAttribute.class);

	//TODO: LB20090923: object needs to be non-transient, otherwise
	// it may not be cached by Wicket leaving values empty after they
	// are retrieved from the datapanels
	private transient Object object; // Hmm, maybe make it Serializable instead of Object?
	private UIMapEntry entry;

	public EntityAttribute(Object object, UIMapEntry entry) {
		super();
		this.object = object;
		this.entry = entry;
	}

	public EntityAttribute(Object object, String methodname) {
		super();
		this.object = object;
		this.entry = new UIMapEntry(methodname, methodname);
	}
	
	public Object getObject() {
		return object;
	}

	public UIMapEntry getEntry() {
		return entry;
	}

	public Object getEntryObject() {
		// default prefix
		return getEntryObject("get");
	}

	public Object getEntryObject(String getter_prefix) {
		// invoke given (entry) method on object and get the 'mapped' object
		Object obj = getObject();
		if (object == null) {
			logger.info("getEntryObject has no object, returning null");
			return null;
		}

		Method m;
		// construct method name
		String name = getEntry().getMethod();
		String firstChar = name.substring(0, 1);
		name = getter_prefix + firstChar.toUpperCase() + name.substring(1);
		try {
			m = obj.getClass().getMethod(name);
			return m.invoke(obj);
		// Note: following exceptions are result of programming errors
		// and should be handled as runtime exceptions
		} catch (SecurityException e) {
			logger.error("Could not get object by invoking method: " + name, e);
			throw( new InternalErrorException(e));
		} catch (NoSuchMethodException e) {
			logger.error("Could not get object by invoking method: " + name, e);
			throw( new InternalErrorException(e));
		} catch (IllegalArgumentException e) {
			logger.error("Could not get object by invoking method: " + name, e);
			throw( new InternalErrorException(e));
		} catch (IllegalAccessException e) {
			logger.error("Could not get object by invoking method: " + name, e);
			throw( new InternalErrorException(e));
		} catch (InvocationTargetException e) {
			logger.error("Could not get object by invoking method: " + name, e);
			throw( new InternalErrorException(e));
		}

		//return null;// if we get here we are in trouble!
	}
	
	public void setEntryObject(Object entryObject)
	{
		Object obj = getObject();
		if (object == null) {
			logger.info("setEntryObject has no object, returning null");
			return;
		}

		Method m;
		// construct method name
		String name = getEntry().getMethod();
		String firstChar = name.substring(0, 1);
		name = "set" + firstChar.toUpperCase() + name.substring(1);
		try {
			Class partypes[] = new Class[1];
			partypes[0] = entryObject.getClass();
			m = obj.getClass().getMethod(name, partypes);
			m.invoke(obj, entryObject);
			
		// Note: following exceptions are result of programming errors
		// and should be handled as runtime exceptions
		} catch (SecurityException e) {
			logger.error("Could not set object by invoking method: " + name, e);
			throw( new InternalErrorException(e));
		} catch (NoSuchMethodException e) {
			logger.error("Could not set object by invoking method: " + name, e);
			throw( new InternalErrorException(e));
		} catch (IllegalArgumentException e) {
			logger.error("Could not set object by invoking method: " + name, e);
			throw( new InternalErrorException(e));
		} catch (IllegalAccessException e) {
			logger.error("Could not set object by invoking method: " + name, e);
			throw( new InternalErrorException(e));
		} catch (InvocationTargetException e) {
			logger.error("Could not set object by invoking method: " + name, e);
			throw( new InternalErrorException(e));
		}
		
	}
	
	public void deleteEntryObject()
	{
		Object obj = getObject();
		if (object == null) {
			logger.info("setEntryObject has no object, returning null");
			return;
		}

		// call "set" method with a null 
		// we need to know the class of the object we want to set to null
		Object entryObject = getEntryObject();
		if (entryObject == null)
		{
			logger.error("Could not delete entry object because there is none");
			return;
		}
		
		Method m;
		// construct method name
		String name = getEntry().getMethod();
		String firstChar = name.substring(0, 1);
		name = "set" + firstChar.toUpperCase() + name.substring(1);
		try {
			Class partypes[] = new Class[1];
			partypes[0] = entryObject.getClass();
			m = obj.getClass().getMethod(name, partypes);
			m.invoke(obj, (Object)null);
			
		// Note: following exceptions are result of programming errors
		// and should be handled as runtime exceptions
		} catch (SecurityException e) {
			logger.error("Could not set object by invoking method: " + name, e);
			throw( new InternalErrorException(e));
		} catch (NoSuchMethodException e) {
			logger.error("Could not set object by invoking method: " + name, e);
			throw( new InternalErrorException(e));
		} catch (IllegalArgumentException e) {
			logger.error("Could not set object by invoking method: " + name, e);
			throw( new InternalErrorException(e));
		} catch (IllegalAccessException e) {
			logger.error("Could not set object by invoking method: " + name, e);
			throw( new InternalErrorException(e));
		} catch (InvocationTargetException e) {
			logger.error("Could not set object by invoking method: " + name, e);
			throw( new InternalErrorException(e));
		}
		
	}
	
}
