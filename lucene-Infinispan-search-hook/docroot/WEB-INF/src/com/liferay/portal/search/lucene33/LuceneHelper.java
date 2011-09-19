/**
 * Copyright (c) 2000-2011 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.search.lucene33;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

/**
 * @author Bruno Farache
 */
public interface LuceneHelper {

	public void addDocument(long companyId, Document document)
		throws IOException;

	public void addExactTerm(
		BooleanQuery booleanQuery, String field, String value);

	public void addRequiredTerm(
		BooleanQuery booleanQuery, String field, String value, boolean like);

	public void addTerm(
			BooleanQuery booleanQuery, String field, String value, boolean like)
		throws ParseException;

	public void delete(long companyId);

	public void deleteDocuments(long companyId, Term term) throws IOException;

	public Analyzer getAnalyzer();

	public String[] getQueryTerms(Query query);

	public IndexSearcher getSearcher(long companyId, boolean readOnly)
		throws IOException;

	public String getSnippet(
			Query query, String field, String s, int maxNumFragments,
			int fragmentLength, String fragmentSuffix, String preTag,
			String postTag)
		throws IOException;

	public Version getVersion();

	public void shutdown();

	public void updateDocument(long companyId, Term term, Document document)
		throws IOException;

}