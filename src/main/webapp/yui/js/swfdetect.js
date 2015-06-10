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
/*
Copyright (c) 2010, Yahoo! Inc. All rights reserved.
Code licensed under the BSD License:
http://developer.yahoo.com/yui/license.html
version: 2.8.1
*/
/**
 * Utility for Flash version detection
 * @namespace YAHOO.util
 * @module swfdetect
 */
YAHOO.namespace("util");

/**
 * Flafh detection utility.
 * @class SWFDetect
 * @static
 */
(function () {
	
var version = 0;
var uA = YAHOO.env.ua;
var sF = "ShockwaveFlash";

 	if (uA.gecko || uA.webkit || uA.opera) {
		   if ((mF = navigator.mimeTypes['application/x-shockwave-flash'])) {
		      if ((eP = mF.enabledPlugin)) {
				 var vS = [];
		         vS = eP.description.replace(/\s[rd]/g, '.').replace(/[A-Za-z\s]+/g, '').split('.');
		        version = vS[0] + '.';
				switch((vS[2].toString()).length)
				{
					case 1:
					version += "00";
					break;
					case 2: 
					version += "0";
					break;
				}
		 		version +=  vS[2];
				version = parseFloat(version);
		      }
		   }
		}
		else if(uA.ie) {
		    try
		    {
		        var ax6 = new ActiveXObject(sF + "." + sF + ".6");
		        ax6.AllowScriptAccess = "always";
		    }
		    catch(e)
		    {
		        if(ax6 != null)
		        {
		            version = 6.0;
		        }
		    }
		    if (version == 0) {
		    try
		    {
		        var ax  = new ActiveXObject(sF + "." + sF);
		       	var vS = [];
		        vS = ax.GetVariable("$version").replace(/[A-Za-z\s]+/g, '').split(',');
		        version = vS[0] + '.';
				switch((vS[2].toString()).length)
				{
					case 1:
					version += "00";
					break;
					case 2: 
					version += "0";
					break;
				}
		 		version +=  vS[2];
				version = parseFloat(version);
				
		    } catch (e) {}
		    }
		}
		
		uA.flash = version;
		
YAHOO.util.SWFDetect = {		
		getFlashVersion : function () {
			return version;
		},
		
		isFlashVersionAtLeast : function (ver) {
			return version >= ver;
		}	
	};
})();
YAHOO.register("swfdetect", YAHOO.util.SWFDetect, {version: "2.8.1", build: "19"});
