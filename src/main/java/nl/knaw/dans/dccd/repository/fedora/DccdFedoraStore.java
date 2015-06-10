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
package nl.knaw.dans.dccd.repository.fedora;

import java.net.MalformedURLException;
import java.net.URL;

import nl.knaw.dans.common.fedora.Fedora;
import nl.knaw.dans.common.fedora.store.FedoraDmoStore;
import nl.knaw.dans.common.lang.repo.AbstractDmoFactory;
import nl.knaw.dans.common.lang.search.SearchEngine;
import nl.knaw.dans.dccd.repository.DccdRepoSearchListener;

public class DccdFedoraStore extends FedoraDmoStore
{
	private static final long serialVersionUID = 1288905408847378535L;
	private static DccdFedoraStore INSTANCE;

    public static DccdFedoraStore getInstance()
    {
    	return INSTANCE;
    }

    public DccdFedoraStore(String name, final Fedora fedora)
    {
        this(name, fedora, null, null);
    }

    public DccdFedoraStore(String name, final Fedora fedora, final SearchEngine dccdSearchEngine,
    		final SearchEngine easySearchEngine)
    {
//        super(name, fedora, new DccdDmoContext());
        super(name, fedora);

        INSTANCE = this;

        AbstractDmoFactory.register(new DccdProjectFactory());
        addConverter(new DccdProjectConverter());

        if (dccdSearchEngine != null)
        {
            addEventListener(new DccdRepoSearchListener(dccdSearchEngine, easySearchEngine));
        }
    }

    // Only DCCD, TODO enable search updating on Easy as well
    public DccdFedoraStore(String name, final Fedora fedora, final SearchEngine dccdSearchEngine)
    {
//        super(name, fedora, new DccdDmoContext());
        super(name, fedora);

        INSTANCE = this;

        AbstractDmoFactory.register(new DccdProjectFactory());
        addConverter(new DccdProjectConverter());

        if (dccdSearchEngine != null)
        {
            addEventListener(new DccdRepoSearchListener(dccdSearchEngine, null)); //!!!
        }
    }
    
    public URL getFileURL(String sid, String unitId) 
    {
        URL url = null;
        String spec = getFedora().getBaseURL() + "/get/" + sid + "/" + unitId;

        try
		{
			url = new URL(spec);
		}
		catch (MalformedURLException e)
		{
			// should always be a valid url, (not existing however...)
			//e.printStackTrace();
			throw new RuntimeException(e);
		}
        
        return url;
    }    
}
