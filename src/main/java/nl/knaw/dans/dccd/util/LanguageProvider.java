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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

//TODO: javadoc and possibly unittesting!

public class LanguageProvider {

	//  The official languages of the European Union,
	//  as stipulated in the latest amendment (as of 1 January 2007) of the Regulation No 1
	//  determining the languages to be used by the European Economic Community of 1958 are:
	//	(bg) Bulgarian	Bulgaria	2007
	//	(cs) Czech	Czech Republic and Slovakia	2004
	//	(da) Danish	Denmark and Germany1	1973
	//	(nl) Dutch	Netherlands and Belgium	1958
	//	(en) English	Ireland, Malta and United Kingdom	1958
	//	(et) Estonian	Estonia	2004
	//	(fi) Finnish	Finland	1995
	//	(fr) French	Belgium, France, Italy2 and Luxembourg	1958
	//	(de) German	Austria, Belgium, Denmark3, Germany, Italy4 and Luxembourg	1958
	//	(el) Greek	Cyprus, Greece and Italy5	1981
	//	(hu) Hungarian	Hungary, Austria6, Romania7 and Slovenia8	2004
	//	(ga) Irish	Ireland and United Kingdom9	2007
	//	(it) Italian	Italy and Slovenia10	1958
	//	(lv) Latvian	Latvia	2004
	//	(lt) Lithuanian	Lithuania	2004
	//	(ml) Maltese	Malta	2004
	//	(pl) Polish	Poland	2004
	//	(pt) Portuguese	Portugal	1986
	//	(ro) Romanian	Romania	2007
	//	(sk) Slovak	Slovakia and Czech Republic	2004
	//	(sl) Slovene	Slovenia, Austria11 and Italy12	2004
	//	(es) Spanish	Spain	1986
	//	(sv) Swedish	Finland and Sweden	1995
	//
	// TODO: implement function that returns those language locales; officialEULanguages
	public static List<Locale> getLocalesForAllOfficialEULanguages() {

		// just all locales, sorted by the display name
		//return Arrays.asList(Locale.getAvailableLocales());

		// not all variants, but sorted on the two-letter code...
		List<Locale> locales = new ArrayList<Locale>();

		String[] lang_arr = {
				"bg","cs","da","nl","en","et","fi","fr","de","el","hu","ga",
				"it","lv","lt","ml","pl","pt","ro","sk","sl","es","sv"
				};

		for (int i = 0; i < lang_arr.length; i++) {
			//System.out.println(lang_arr[i]);
			locales.add(new Locale(lang_arr[i]));
		}

		// sort on displayname
		sortLocales(locales);

		return locales;
	}


	public static List<Locale> getLocalesForAllLanguagesExcluding(final List<Locale> excludeList) {
		// not all variants, but sorted on the two-letter code...
		List<Locale> locales = new ArrayList<Locale>();
		String[] lang_arr = getLanguageCodes();
		for (int i = 0; i < lang_arr.length; i++) {
			//System.out.println(lang_arr[i]);

			Locale newlocale = new Locale(lang_arr[i]);

			// check if in excluding list
			boolean include = true;
			for (int e_index=0; e_index < excludeList.size(); e_index++) {
				Locale excludeLocale = excludeList.get(e_index);
				//if (excludeLocale.getLanguage().equals(lang_arr[i])) {
				if (excludeLocale.getLanguage().equals(newlocale.getLanguage() )) {
					//System.out.println("excluding " + excludeLocale.getLanguage() + " = " + lang_arr[i]);
					include = false;
					break; // needs to be excluded
				}
			}

			// only add if not excluded
			if (include) {
				locales.add(newlocale);
			}
		}

		// sort on displayname
		sortLocales(locales);

		return locales;
	}

	public static List<Locale> getLocalesForAllLanguages() {

		// just all locales, sorted by the display name
		//return Arrays.asList(Locale.getAvailableLocales());

		// not all variants, but sorted on the two-letter code...
		List<Locale> locales = new ArrayList<Locale>();
		String[] lang_arr = getLanguageCodes();
		for (int i = 0; i < lang_arr.length; i++) {
			//System.out.println(lang_arr[i]);
			locales.add(new Locale(lang_arr[i]));
		}

		// sort on displayname
		sortLocales(locales);

		return locales;
	}

	public static String[] getLanguageCodes() {
		return Locale.getISOLanguages();
	}

	// sort on displayname
	public static void sortLocales(List<Locale> locales) {
		// Locales are not comparable...
		Collections.sort(locales, new Comparator<Locale>() {
	        public int compare(Locale l1, Locale l2) {
	            return l1.getDisplayLanguage().compareTo(l2.getDisplayLanguage());
	        }
		});

	}
}
