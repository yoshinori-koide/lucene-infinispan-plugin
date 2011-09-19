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

import com.liferay.portal.kernel.search.QueryTerm;
import com.liferay.portal.kernel.search.TermQuery;

import org.apache.lucene.index.Term;

/**
 * @author Brian Wing Shun Chan
 */
public class TermQueryImpl implements TermQuery {

	public TermQueryImpl(String field, long value) {
		this(field, String.valueOf(value));
	}

	public TermQueryImpl(String field, String value) {
		_termQuery = new org.apache.lucene.search.TermQuery(
			new Term(field, value));
	}

	public QueryTerm getQueryTerm() {
		throw new UnsupportedOperationException();
	}

	public org.apache.lucene.search.TermQuery getTermQuery() {
		return _termQuery;
	}

	public String toString() {
		return _termQuery.toString();
	}

	private org.apache.lucene.search.TermQuery _termQuery;

}