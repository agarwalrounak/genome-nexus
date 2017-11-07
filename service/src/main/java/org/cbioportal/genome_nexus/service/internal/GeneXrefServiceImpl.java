/*
 * Copyright (c) 2017 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal Genome Nexus.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.cbioportal.genome_nexus.service.internal;

import org.cbioportal.genome_nexus.model.GeneXref;
import org.cbioportal.genome_nexus.service.GeneXrefService;

import java.util.*;

import org.cbioportal.genome_nexus.service.exception.EnsemblWebServiceException;
import org.cbioportal.genome_nexus.service.exception.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author ochoaa
 */
@Service
public class GeneXrefServiceImpl implements GeneXrefService {

    private String geneXrefsURL;
    @Value("${genexrefs.url}")
    public void setGeneXrefsURL(String geneXrefsURL) { this.geneXrefsURL = geneXrefsURL; }

    private final ExternalResourceTransformer externalResourceTransformer;

    @Autowired
    public GeneXrefServiceImpl(ExternalResourceTransformer externalResourceTransformer)
    {
        this.externalResourceTransformer = externalResourceTransformer;
    }

    @Override
    public List<GeneXref> getGeneXrefs(String accession) throws EnsemblWebServiceException
    {
        List<GeneXref> geneXrefs;

        try {
            String xrefJSON = getGeneXrefsJSON(accession);
            geneXrefs = this.externalResourceTransformer.transform(xrefJSON, GeneXref.class);
        }
        catch (JsonMappingException e) {
            throw new EnsemblWebServiceException(e.getMessage());
        }
        catch (HttpClientErrorException e)
        {
            throw new EnsemblWebServiceException(e.getResponseBodyAsString(), e.getStatusCode());
        }
        catch (ResourceAccessException e)
        {
            throw new EnsemblWebServiceException(e.getMessage());
        }

        return geneXrefs;
    }

    private String getGeneXrefsJSON(String accession) {
        String uri = geneXrefsURL.replace("ACCESSION", accession);
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(uri, String.class);
    }

}
