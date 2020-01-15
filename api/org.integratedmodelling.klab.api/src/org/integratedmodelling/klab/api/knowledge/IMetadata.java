/*
 * This file is part of k.LAB.
 * 
 * k.LAB is free software: you can redistribute it and/or modify
 * it under the terms of the Affero GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * A copy of the GNU Affero General Public License is distributed in the root
 * directory of the k.LAB distribution (LICENSE.txt). If this cannot be found 
 * see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (C) 2007-2018 integratedmodelling.org and any authors mentioned
 * in author tags. All rights reserved.
 */
package org.integratedmodelling.klab.api.knowledge;

import org.integratedmodelling.kim.api.IParameters;

// TODO: Auto-generated Javadoc
/**
 * The Interface IMetadata.
 *
 * @author ferdinando.villa
 * @version $Id: $Id
 */
public interface IMetadata extends IParameters<String> {

	/** The dc name. */
	public static String DC_NAME = "dc:name";

	/** The dc label. */
	public static String DC_LABEL = "dc:label";

	/** The dc comment. */
	public static String DC_COMMENT = "dc:comment";

	/** The dc definition. */
	public static String DC_DEFINITION = "dc:definition";

	/** The dc seealso. */
	public static String DC_SEEALSO = "dc:name";

	/**
	 * DCMI point http://dublincore.org/documents/dcmi-point/ ISO 3166
	 * http://www.din.de/gremien/nas/nabd/iso3166ma/codlstp1/index.html DCMI box
	 * http://dublincore.org/documents/dcmi-box/ TGN
	 * http://shiva.pub.getty.edu/tgn_browser/
	 */
	public static String DC_COVERAGE_SPATIAL = "dc:coverage-spatial";

	/**
	 * DCMI period http://dublincore.org/documents/dcmi-period/ W3C-DTF
	 * http://www.w3.org/TR/NOTE-datetime
	 */
	public static String DC_COVERAGE_TEMPORAL = "dc:coverage-temporal";

	/** free text. */
	public static String DC_DESCRIPTION = "dc:description";

	/** free text. */
	public static String DC_DESCRIPTION_TABLEOFCONTENTS = "dc:tableofcontents";

	/** free text. */
	public static String DC_DESCRIPTION_ABSTRACT = "dc:abstract";

	/**
	 * DCMI type vocabulary http://dublincore.org/documents/dcmi-type-vocabulary/
	 */
	public static String DC_TYPE = "dc:type";

	/**
	 * The dc relation.
	 *
	 * @deprecated use subclasses
	 */
	@Deprecated
	public static String DC_RELATION = "dc:relation";

	/**
	 * URI http://www.ietf.org/rfc/rfc2396.txt
	 */
	public static String DC_RELATION_ISVERSIONOF = "dc:isversionof";

	/** URI. */
	public static String DC_RELATION_HASVERSION = "dc:hasversion";

	/** URI. */
	public static String DC_RELATION_ISREPLACEDBY = "dc:isreplacedby";

	/** URI. */
	public static String DC_RELATION_REPLACES = "dc:replaces";

	/** URI. */
	public static String DC_RELATION_ISREQUIREDBY = "dc:isrequiredby";

	/** URI. */
	public static String DC_RELATION_REQUIRES = "dc:requires";

	/** URI. */
	public static String DC_RELATION_ISPARTOF = "dc:ispartof";

	/** URI. */
	public static String DC_RELATION_HASPART = "dc:haspart";

	/** URI. */
	public static String DC_RELATION_ISREFERENCEDBY = "dc:isreferencedby";

	/** URI. */
	public static String DC_RELATION_REFERENCES = "dc:references";

	/** URI. */
	public static String DC_RELATION_ISFORMATOF = "dc:isformatof";

	/** URI. */
	public static String DC_RELATION_HASFORMAT = "dc:hasformat";

	/** URI. */
	public static String DC_SOURCE = "dc:source";

	/**
	 * Vocabularies:
	 * 
	 * LCSH Library of Congress Subject Headings MeSH
	 * http://www.nlm.nih.gov/mesh/meshhome.html DDC
	 * http://www.oclc.org/dewey/index.htm LCC
	 * http://lcweb.loc.gov/catdir/cpso/lcco/lcco.html UDC http://www.udcc.org/
	 */
	public static String DC_SUBJECT = "dc:subject";

	/** The dc title. */
	public static String DC_TITLE = "dc:title";

	/** The dc title alternative. */
	public static String DC_TITLE_ALTERNATIVE = "dc:title-alternative";

	/** The dc contributor. */
	public static String DC_CONTRIBUTOR = "dc:contributor";

	/** The dc url. */
	// TODO is this in DC?
	public static String DC_URL = "dc:url";

	/** The dc originator. */
	// TODO is this in DC?
	public static String DC_ORIGINATOR = "dc:originator";

	/** The dc creator. */
	public static String DC_CREATOR = "dc:creator";

	/** The dc publisher. */
	public static String DC_PUBLISHER = "dc:publisher";

	/** The dc rights. */
	public static String DC_RIGHTS = "dc:rights";

	/**
	 * DCMI period http://dublincore.org/documents/dcmi-period/ W3C-DTF
	 * http://www.w3.org/TR/NOTE-datetime
	 */
	public static String DC_DATE_CREATED = "dc:date-created";

	/** DCMI period W3C-DTF. */
	public static String DC_DATE_VALID = "dc:date-valid";

	/** DCMI period W3C-DTF. */
	public static String DC_DATE_AVAILABLE = "dc:date-available";

	/** DCMI period W3C-DTF. */
	public static String DC_DATE_ISSUED = "dc:date-issued";

	/** DCMI period W3C-DTF. */
	public static String DC_MODIFIED = "dc:modified";

	/** The dc format extent. */
	public static String DC_FORMAT_EXTENT = "dc:format-extent";

	/**
	 * http://www.isi.edu/in-notes/iana/assignments/media-types/media-types
	 */
	public static String DC_FORMAT_MEDIUM = "dc:format-medium";

	/** The dc identifier. */
	public static String DC_IDENTIFIER = "dc:identifier";

	public static String IM_KEYWORDS = "im:keywords";

	public static String IM_KEY = "im:key";

	public static String IM_THEMATIC_AREA = "im:thematic-area";

	public static String IM_GEOGRAPHIC_AREA = "im:geographic-area";

	/**
	 * ISO639-2 http://www.w3.org/TR/NOTE-datetime RFC1766
	 * http://www.ietf.org/rfc/rfc1766.txt
	 */
	public static final String DC_LANGUAGE = "dc:language";

	/** The Constant IM_NAME. */
	public static final String IM_NAME = "im:name";

	/** The Constant IM_MIN_SPATIAL_SCALE. */
	public static final String IM_MIN_SPATIAL_SCALE = "im:min-spatial-scale";

	/** The Constant IM_MAX_SPATIAL_SCALE. */
	public static final String IM_MAX_SPATIAL_SCALE = "im:max-spatial-scale";

	/** The Constant IM_MIN_TEMPORAL_SCALE. */
	public static final String IM_MIN_TEMPORAL_SCALE = "im:min-temporal-scale";

	/** The Constant IM_MAX_TEMPORAL_SCALE. */
	public static final String IM_MAX_TEMPORAL_SCALE = "im:max-temporal-scale";

	/**
	 * 
	 */
	public static final String IM_NOTES = "im:notes";

	/**
	 * Tags concepts that annotate physical states that don't need units because of
	 * adopting rescaling traits.
	 */
	public static final String IM_IS_RESCALED = "im:is-rescaled";

	/**
	 * Permissions in k.LAB are either "*" for public and/or a list of
	 * comma-separated groups (uppercase) and/or usernames (lowercase). An empty
	 * permission string means "owner only" (and possibly admin, left to
	 * implementations). Prefixing either with a ! denies the permission for the
	 * user or group (supposedly to narrow a previous more general one: e.g.
	 * *,!BADGUYS).
	 */
	public static final String IM_PERMISSIONS = "im:permissions";

	// publication data to send along with publish requests
	public static final String IM_SUGGESTED_RESOURCE_ID = "im:suggested-resource-id";
	public static final String IM_SUGGESTED_NAMESPACE_ID = "im:suggested-namespace-id";
	public static final String IM_SUGGESTED_CATALOG_ID = "im:suggested-catalog-id";
	
	/**
	 * Tags those extensive observables that are actually intensive because the
	 * observation is of an inherent countable.
	 */
	public static final String IM_RESCALES_INHERENT = "im:rescales-inherent";

	/**
	 * KLAB-specific, for visualization and display
	 */
	public static final String KLAB_LINE_COLOR = "klab:linecolor";

	/** The Constant KLAB_FILL_COLOR. */
	public static final String KLAB_FILL_COLOR = "klab:fillcolor";

	/** The Constant KLAB_OPACITY. */
	public static final String KLAB_OPACITY = "klab:opacity";

	/**
	 * Get a variable irrespective of case.
	 * 
	 * @param attr
	 * @return
	 */
	public Object getCaseInsensitive(String attr);

}
