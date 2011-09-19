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

import com.liferay.portal.kernel.util.StringPool;

import java.io.IOException;

import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

/**
 * @author Brian Wing Shun Chan
 * @author Harry Mark
 * @author Bruno Farache
 */
public class LuceneHelperUtil {

	public static void addDate(Document doc, String field, Date value) {
		doc.add(LuceneFields.getDate(field, value));
	}

	public static void addDocument(long companyId, Document document)
		throws IOException {

		getLuceneHelper().addDocument(companyId, document);
	}

	public static void addExactTerm(
		BooleanQuery booleanQuery, String field, boolean value) {

		addExactTerm(booleanQuery, field, String.valueOf(value));
	}

	public static void addExactTerm(
		BooleanQuery booleanQuery, String field, double value) {

		addExactTerm(booleanQuery, field, String.valueOf(value));
	}

	public static void addExactTerm(
		BooleanQuery booleanQuery, String field, int value) {

		addExactTerm(booleanQuery, field, String.valueOf(value));
	}

	public static void addExactTerm(
		BooleanQuery booleanQuery, String field, long value) {

		addExactTerm(booleanQuery, field, String.valueOf(value));
	}

	public static void addExactTerm(
		BooleanQuery booleanQuery, String field, short value) {

		addExactTerm(booleanQuery, field, String.valueOf(value));
	}

	public static void addExactTerm(
		BooleanQuery booleanQuery, String field, String value) {

		getLuceneHelper().addExactTerm(booleanQuery, field, value);
	}

	public static void addRequiredTerm(
		BooleanQuery booleanQuery, String field, boolean value) {

		addRequiredTerm(booleanQuery, field, String.valueOf(value));
	}

	public static void addRequiredTerm(
		BooleanQuery booleanQuery, String field, double value) {

		addRequiredTerm(booleanQuery, field, String.valueOf(value));
	}

	public static void addRequiredTerm(
		BooleanQuery booleanQuery, String field, int value) {

		addRequiredTerm(booleanQuery, field, String.valueOf(value));
	}

	public static void addRequiredTerm(
		BooleanQuery booleanQuery, String field, long value) {

		addRequiredTerm(booleanQuery, field, String.valueOf(value));
	}

	public static void addRequiredTerm(
		BooleanQuery booleanQuery, String field, short value) {

		addRequiredTerm(booleanQuery, field, String.valueOf(value));
	}

	public static void addRequiredTerm(
		BooleanQuery booleanQuery, String field, String value) {

		addRequiredTerm(booleanQuery, field, value, false);
	}

	public static void addRequiredTerm(
		BooleanQuery booleanQuery, String field, String value, boolean like) {

		getLuceneHelper().addRequiredTerm(booleanQuery, field, value, like);
	}

	public static void addTerm(
			BooleanQuery booleanQuery, String field, long value)
		throws ParseException {

		addTerm(booleanQuery, field, String.valueOf(value));
	}

	public static void addTerm(
			BooleanQuery booleanQuery, String field, String value)
		throws ParseException {

		addTerm(booleanQuery, field, value, false);
	}

	public static void addTerm(
			BooleanQuery booleanQuery, String field, String value,
			boolean like)
		throws ParseException {

		getLuceneHelper().addTerm(booleanQuery, field, value, like);
	}

	public static void delete(long companyId) {
		getLuceneHelper().delete(companyId);
	}

	public static void deleteDocuments(long companyId, Term term)
		throws IOException {

		getLuceneHelper().deleteDocuments(companyId, term);
	}

	public static Analyzer getAnalyzer() {
		return getLuceneHelper().getAnalyzer();
	}

	public static LuceneHelper getLuceneHelper() {
		return _luceneHelper;
	}

	public static String[] getQueryTerms(Query query) {
		return getLuceneHelper().getQueryTerms(query);
	}

	public static IndexSearcher getSearcher(long companyId, boolean readOnly)
		throws IOException {

		return getLuceneHelper().getSearcher(companyId, readOnly);
	}

	public static String getSnippet(Query query, String field, String s)
		throws IOException {

		return getSnippet(
			query, field, s, 3, 80, "...", StringPool.BLANK, StringPool.BLANK);
	}

	public static String getSnippet(
			Query query, String field, String s, int maxNumFragments,
			int fragmentLength, String fragmentSuffix, String preTag,
			String postTag)
		throws IOException {

		return getLuceneHelper().getSnippet(
			query, field, s, maxNumFragments, fragmentLength, fragmentSuffix,
			preTag, postTag);
	}

	public static Version getVersion() {
		return getLuceneHelper().getVersion();
	}

	public static void updateDocument(
			long companyId, Term term, Document document)
		throws IOException {

		getLuceneHelper().updateDocument(companyId, term, document);
	}

	public static void shutdown() {
		getLuceneHelper().shutdown();
	}

	public void setLuceneHelper(LuceneHelper luceneHelper) {
		_luceneHelper = luceneHelper;
	}

	private static LuceneHelper _luceneHelper;

}