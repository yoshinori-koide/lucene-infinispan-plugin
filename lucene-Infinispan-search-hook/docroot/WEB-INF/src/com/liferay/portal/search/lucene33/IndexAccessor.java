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

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;

/**
 * @author Bruno Farache
 */
public interface IndexAccessor {

	public void addDocument(Document document) throws IOException;

	public void close();

	public void delete() ;

	public void deleteDocuments(Term term) throws IOException;

	public long getCompanyId();

	public Directory getLuceneDir();

	public void updateDocument(Term term, Document document) throws IOException;

}