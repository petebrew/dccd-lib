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
package nl.knaw.dans.dccd.repository.xml;

/** Thrown when a supposedly TRiDaS xml file could not be loaded
 *
 * Note:
 * When more detail is needed we could use exceptions derived from this general one,
 * which are thrown when a certain type of problem is at hand.
 * Callers of the throwing method can either catch the 'general' exception or
 * the more specific ones.
 *
 * @author paulboon
 *
 */
public class TridasLoadException extends Exception {
	private static final long serialVersionUID = 7341688209304114982L;

	public TridasLoadException() {
	}

	public TridasLoadException(String message) {
		super(message);
	}

	public TridasLoadException(Throwable cause) {
		super(cause);
	}

	public TridasLoadException(String message, Throwable cause) {
		super(message, cause);
	}

}
