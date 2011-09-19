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

import com.liferay.portal.kernel.io.unsync.UnsyncStringReader;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.lucene33.IndexAccessor;
import com.liferay.portal.search.lucene.infinispan.IndexAccessorImpl;
//import com.liferay.portal.util.PropsUtil;
import com.liferay.util.lucene.KeywordsUtil;

import java.io.IOException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.QueryTermExtractor;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.WeightedTerm;
import org.apache.lucene.util.Version;

/**
 * @author Brian Wing Shun Chan
 * @author Harry Mark
 * @author Bruno Farache
 */
public class LuceneHelperImpl implements LuceneHelper {

	public void addDocument(long companyId, Document document)
		throws IOException {

		IndexAccessor indexAccessor = _getIndexAccessor(companyId);

		indexAccessor.addDocument(document);
	}

	public void addExactTerm(
		BooleanQuery booleanQuery, String field, String value) {

		//text = KeywordsUtil.escape(value);

		Query query = new TermQuery(new Term(field, value));

		booleanQuery.add(query, BooleanClause.Occur.SHOULD);
	}

	public void addRequiredTerm(
		BooleanQuery booleanQuery, String field, String value, boolean like) {

		if (like) {
			value = StringUtil.replace(
				value, StringPool.PERCENT, StringPool.STAR);

			value = value.toLowerCase();

			WildcardQuery wildcardQuery = new WildcardQuery(
				new Term(field, value));

			booleanQuery.add(wildcardQuery, BooleanClause.Occur.MUST);
		}
		else {
			//text = KeywordsUtil.escape(value);

			Term term = new Term(field, value);
			TermQuery termQuery = new TermQuery(term);

			booleanQuery.add(termQuery, BooleanClause.Occur.MUST);
		}
	}

	public void addTerm(
			BooleanQuery booleanQuery, String field, String value, boolean like)
		throws ParseException {

		if (Validator.isNull(value)) {
			return;
		}

		if (like) {
			value = StringUtil.replace(
				value, StringPool.PERCENT, StringPool.BLANK);

			value = value.toLowerCase();

			Term term = new Term(
				field, StringPool.STAR.concat(value).concat(StringPool.STAR));

			WildcardQuery wildcardQuery = new WildcardQuery(term);

			booleanQuery.add(wildcardQuery, BooleanClause.Occur.SHOULD);
		}
		else {
			QueryParser queryParser = new QueryParser(
				_version, field, getAnalyzer());

			try {
				Query query = queryParser.parse(value);

				booleanQuery.add(query, BooleanClause.Occur.SHOULD);
			}
			catch (ParseException pe) {
				if (_log.isDebugEnabled()) {
					_log.debug(
						"ParseException thrown, reverting to literal search",
						pe);
				}

				value = KeywordsUtil.escape(value);

				Query query = queryParser.parse(value);

				booleanQuery.add(query, BooleanClause.Occur.SHOULD);
			}
		}
	}

	public void delete(long companyId) {
		IndexAccessor indexAccessor = _getIndexAccessor(companyId);

		indexAccessor.delete();
	}

	public void deleteDocuments(long companyId, Term term) throws IOException {
		IndexAccessor indexAccessor = _getIndexAccessor(companyId);

		indexAccessor.deleteDocuments(term);
	}

	public Analyzer getAnalyzer() {
		try {
			return (Analyzer)getAnalyzerClass().newInstance();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public Class<Analyzer> getAnalyzerClass() {
		
		if(_analyzerClass == null) {
			String analyzerName = PropsUtil.get(PropsKeys.LUCENE_ANALYZER);
	
			if (Validator.isNotNull(analyzerName)) {
				try {
					_analyzerClass = Class.forName(analyzerName);
				}
				catch (Exception e) {
					_log.error(e);
				}
			}
		}		
		return (Class<Analyzer>)_analyzerClass;
	}

	public String[] getQueryTerms(Query query) {
		String[] fieldNames = new String[] {
			Field.CONTENT, Field.DESCRIPTION, Field.PROPERTIES, Field.TITLE,
			Field.USER_NAME
		};

		WeightedTerm[] weightedTerms = null;

		for (String fieldName : fieldNames) {
			weightedTerms = QueryTermExtractor.getTerms(
				query, false, fieldName);

			if (weightedTerms.length > 0) {
				break;
			}
		}

		Set<String> queryTerms = new HashSet<String>();

		for (WeightedTerm weightedTerm : weightedTerms) {
			queryTerms.add(weightedTerm.getTerm());
		}

		return queryTerms.toArray(new String[queryTerms.size()]);
	}

	public IndexSearcher getSearcher(long companyId, boolean readOnly)
		throws IOException {

		IndexAccessor indexAccessor = _getIndexAccessor(companyId);

		return new IndexSearcher(indexAccessor.getLuceneDir(), readOnly);
	}

	public String getSnippet(
			Query query, String field, String s, int maxNumFragments,
			int fragmentLength, String fragmentSuffix, String preTag,
			String postTag)
		throws IOException {

		SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter(
			preTag, postTag);

		QueryScorer queryScorer = new QueryScorer(query, field);

		Highlighter highlighter = new Highlighter(
			simpleHTMLFormatter, queryScorer);

		highlighter.setTextFragmenter(new SimpleFragmenter(fragmentLength));

		TokenStream tokenStream = getAnalyzer().tokenStream(
			field, new UnsyncStringReader(s));

		try {
			String snippet = highlighter.getBestFragments(
				tokenStream, s, maxNumFragments, fragmentSuffix);

			if (Validator.isNotNull(snippet) &&
				!StringUtil.endsWith(snippet, fragmentSuffix)) {

				snippet = snippet + fragmentSuffix;
			}

			return snippet;
		}
		catch (InvalidTokenOffsetsException itoe) {
			throw new IOException(itoe.getMessage());
		}
	}

	public Version getVersion() {
		return _version;
	}

	public void updateDocument(long companyId, Term term, Document document)
		throws IOException {

		IndexAccessor indexAccessor = _getIndexAccessor(companyId);

		indexAccessor.updateDocument(term, document);
	}

	public void shutdown() {
		for (IndexAccessor indexAccessor : _indexAccessorMap.values()) {
			indexAccessor.close();
		}
	}

	private IndexAccessor _getIndexAccessor(long companyId) {
		IndexAccessor indexAccessor = _indexAccessorMap.get(companyId);

		if (indexAccessor == null) {
			synchronized (this) {
				indexAccessor = _indexAccessorMap.get(companyId);

				if (indexAccessor == null) {
					indexAccessor = new IndexAccessorImpl(companyId);

					_indexAccessorMap.put(companyId, indexAccessor);
				}
			}
		}

		return indexAccessor;
	}

	private static Log _log = LogFactoryUtil.getLog(LuceneHelperImpl.class);

	private Class<?> _analyzerClass = WhitespaceAnalyzer.class;
	private Map<Long, IndexAccessor> _indexAccessorMap =
		new ConcurrentHashMap<Long, IndexAccessor>();
	private Version _version = Version.LUCENE_33;

}