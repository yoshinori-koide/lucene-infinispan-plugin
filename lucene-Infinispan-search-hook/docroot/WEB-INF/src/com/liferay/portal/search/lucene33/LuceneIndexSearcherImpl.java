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

import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentImpl;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.HitsImpl;
import com.liferay.portal.kernel.search.IndexSearcher;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.kernel.util.Validator;

import java.io.IOException;

import java.util.List;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;

/**
 * @author yoshinori koide
 */
public class LuceneIndexSearcherImpl implements IndexSearcher {

	public Hits search(
			long companyId, Query query, Sort[] sorts, int start, int end)
		throws SearchException {

		if (_log.isDebugEnabled()) {
			_log.debug("Query " + query);
		}

		Hits hits = null;

		org.apache.lucene.search.IndexSearcher searcher = null;
		org.apache.lucene.search.Sort luceneSort = null;

		try {
			searcher = LuceneHelperUtil.getSearcher(companyId, true);

			if (sorts != null) {
				searcher.setDefaultFieldSortScoring(true, true);

				SortField[] sortFields = new SortField[sorts.length];

				for (int i = 0; i < sorts.length; i++) {
					Sort sort = sorts[i];

					sortFields[i] = new SortField(
						sort.getFieldName(), sort.getType(), sort.isReverse());
				}

				luceneSort = new org.apache.lucene.search.Sort(sortFields);
			}

			long startTime = System.currentTimeMillis();

			org.apache.lucene.search.TopDocs luceneHits;
			
			if(luceneSort!=null) {
				luceneHits = searcher.search(QueryTranslator.translate(query), Integer.MAX_VALUE, luceneSort);
			}
			else {
				luceneHits = searcher.search(QueryTranslator.translate(query), Integer.MAX_VALUE);
			}

			long endTime = System.currentTimeMillis();

			float searchTime = (float)(endTime - startTime) / Time.SECOND;

			hits = subset(searcher, luceneHits, query, startTime, searchTime, start, end);
		}
		catch (BooleanQuery.TooManyClauses tmc) {
			int maxClauseCount = BooleanQuery.getMaxClauseCount();

			BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);

			try {
				long startTime = System.currentTimeMillis();

				org.apache.lucene.search.TopDocs luceneHits = searcher.search(
					QueryTranslator.translate(query), Integer.MAX_VALUE);

				long endTime = System.currentTimeMillis();

				float searchTime = (float)(endTime - startTime) / Time.SECOND;

				hits = subset(
						searcher, luceneHits, query, startTime, searchTime, start, end);
			}
			catch (Exception e) {
				throw new SearchException(e);
			}
			finally {
				BooleanQuery.setMaxClauseCount(maxClauseCount);
			}
		}
		catch (ParseException pe) {
			_log.error("Query: " + query, pe);

			return new HitsImpl();
		}
		catch (Exception e) {
			throw new SearchException(e);
		}
		finally {
			try {
				if (searcher != null) {
					searcher.close();
				}
			}
			catch (IOException ioe) {
				throw new SearchException(ioe);
			}
		}

		if (_log.isDebugEnabled()) {
			_log.debug(
				"Search found " + hits.getLength() + " results in " +
					hits.getSearchTime() + "ms");
		}

		return hits;
	}

	protected DocumentImpl getDocument(
		org.apache.lucene.document.Document oldDoc) {

		DocumentImpl newDoc = new DocumentImpl();

		List<org.apache.lucene.document.Fieldable> oldFields = oldDoc.getFields();

		for (org.apache.lucene.document.Fieldable oldField : oldFields) {
			String[] values = oldDoc.getValues(oldField.name());

			if ((values != null) && (values.length > 1)) {
				Field newField = new Field(
					oldField.name(), values, oldField.isTokenized());

				newDoc.add(newField);
			}
			else {
				Field newField = new Field(
					oldField.name(), oldField.stringValue(),
					oldField.isTokenized());

				newDoc.add(newField);
			}
		}

		return newDoc;
	}

	protected String[] getQueryTerms(Query query) {
		String[] queryTerms = new String[0];

		try {
			queryTerms = LuceneHelperUtil.getQueryTerms(
				QueryTranslator.translate(query));
		}
		catch (ParseException pe) {
			_log.error("Query: " + query, pe);
		}

		return queryTerms;
	}

	protected String getSnippet(
			org.apache.lucene.document.Document doc, Query query, String field)
		throws IOException {

		String[] values = doc.getValues(field);

		String snippet = null;

		if (Validator.isNull(values)) {
			return snippet;
		}

		String s = StringUtil.merge(values);

		try {
			snippet = LuceneHelperUtil.getSnippet(
				QueryTranslator.translate(query), field, s);
		}
		catch (ParseException pe) {
			_log.error("Query: " + query, pe);
		}

		return snippet;
	}

	protected Hits subset(
			org.apache.lucene.search.IndexSearcher searcher,
			TopDocs luceneHits, Query query, long startTime,
			float searchTime, int start, int end)
		throws IOException {

		int length = luceneHits.totalHits;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS)) {
			start = 0;
			end = length;
		}

		String[] queryTerms = getQueryTerms(query);

		Hits subset = new HitsImpl();

		if ((start > - 1) && (start <= end)) {
			if (end > length) {
				end = length;
			}

			int subsetTotal = end - start;

			Document[] subsetDocs = new DocumentImpl[subsetTotal];
			String[] subsetSnippets = new String[subsetTotal];
			float[] subsetScores = new float[subsetTotal];

			int j = 0;

			for (int i = start; i < end; i++, j++) {
				org.apache.lucene.document.Document doc = searcher.doc(luceneHits.scoreDocs[i].doc);

				subsetDocs[j] = getDocument(doc);
				subsetSnippets[j] = getSnippet(doc, query, Field.CONTENT);
				subsetScores[j] = luceneHits.scoreDocs[i].score;
			}

			subset.setStart(startTime);
			subset.setSearchTime(searchTime);
			subset.setQueryTerms(queryTerms);
			subset.setDocs(subsetDocs);
			subset.setLength(length);
			subset.setSnippets(subsetSnippets);
			subset.setScores(subsetScores);
		}

		return subset;
	}

	private static Log _log = LogFactoryUtil.getLog(
		LuceneIndexSearcherImpl.class);

}