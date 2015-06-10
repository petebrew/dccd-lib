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
*******************************************************************************/
YAHOO.namespace("taxon");
function initTaxonPanel() {
		// Instantiate a Panel from markup
			YAHOO.taxon.Panel = 
				new YAHOO.widget.Panel("taxonPanelYUI", 
									   { width:"400px", 
									     visible:false, 
									     constraintoviewport:true,
									     draggable:false,
									     modal:true 
									   } 
									  );
			YAHOO.taxon.Panel.render();			
}
//YAHOO.util.Event.addListener(window, "load", initTaxonPanel);
YAHOO.util.Event.onDOMReady(function() {
	initTaxonPanel();
});
