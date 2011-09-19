package com.liferay.portal.search.lucene.infinispan;
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


import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.infinispan.Cache;
import org.infinispan.lucene.InfinispanDirectory;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.SearchEngineUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.search.lucene33.IndexAccessor;
import com.liferay.portal.search.lucene33.LuceneHelperUtil;

import com.liferay.portal.util.PropsValues;

/**
 * @author yoshinori koide
 */
public class IndexAccessorImpl implements IndexAccessor {

	public IndexAccessorImpl(long companyId) {
		_companyId = companyId;

		_initInfinispan();
		_checkLuceneDir();
		_initIndexWriter();
		_initCommitScheduler();
	}

	public void addDocument(Document document) throws IOException {
		if (SearchEngineUtil.isIndexReadOnly()) {
			return;
		}

		_write(null, document);
	}

	public void close() {
		try {
			_getWriter().close();
		}
		catch(Exception e) {
			_log.error(
				"Closing Lucene writer failed for " + _companyId, e);
		}
	}

	public void delete() {
		if (SearchEngineUtil.isIndexReadOnly()) {
			return;
		}

		close();

		_deleteDirectory();

		_initIndexWriter();
	}

	public void deleteDocuments(Term term) throws IOException {
		if (SearchEngineUtil.isIndexReadOnly()) {
			return;
		}

		try {
			_getWriter().deleteDocuments(term);

			_batchCount++;
		}
		finally {
			_commit();
		}
	}

	public long getCompanyId() {
		return _companyId;
	}

	public Directory getLuceneDir() {
		if (_log.isDebugEnabled()) {
			_log.debug("Lucene store type infinispan");
		}

		return _getLuceneInfinispan();
	}

	public void updateDocument(Term term, Document document)
		throws IOException {

		if (SearchEngineUtil.isIndexReadOnly()) {
			return;
		}

		_write(term, document);
	}

	private void _checkLuceneDir() {
		if (SearchEngineUtil.isIndexReadOnly()) {
			return;
		}

		try {
			Directory directory = getLuceneDir();

			if (IndexWriter.isLocked(directory)) {
				IndexWriter.unlock(directory);
			}
		}
		catch (Exception e) {
			_log.error("Check Lucene directory failed for " + _companyId, e);
		}
	}

	private void _commit() throws IOException {
		if ((PropsValues.LUCENE_COMMIT_BATCH_SIZE == 0) ||
			(PropsValues.LUCENE_COMMIT_BATCH_SIZE <= _batchCount)) {

			_doCommit();
		}
	}


	private void _deleteDirectory() {

		String path = _getPath();
		
		try {
			Directory directory = _Directories.get(path);
			
			directory.close();
		}
		catch (Exception e) {
			if (_log.isWarnEnabled()) {
				_log.warn("Could not close directory " + path);
			}
		}
	}

	private void _doCommit() throws IOException {
		if (_getWriter() != null) {
			_getWriter().commit();
		}

		_batchCount = 0;
	}

	private Directory _getDirectory(String path) throws IOException {
		
		Directory directory = _Directories.get(path);
		
		if(directory==null) {
			directory = new InfinispanDirectory(_cache, path);
			_Directories.put(path, directory);
		}
		
		return directory;
	}

	private Directory _getLuceneInfinispan() {
		Directory directory = null;

		String path = _getPath();

		try {
			directory = _getDirectory(path);
		}
		catch (IOException ioe1) {
			if (directory != null) {
				try {
					directory.close();
				}
				catch (Exception e) {
				}
			}
		}

		return directory;
	}

	private String _getPath() {
		return PropsValues.LUCENE_DIR.concat(String.valueOf(_companyId)).concat(
			StringPool.SLASH);
	}

	private void _initCommitScheduler() {
		if ((PropsValues.LUCENE_COMMIT_BATCH_SIZE == 0) ||
				(PropsValues.LUCENE_COMMIT_BATCH_SIZE <= _batchCount)) {

			return;
		}

		ScheduledExecutorService scheduledExecutorService =
			Executors.newSingleThreadScheduledExecutor();

		Runnable runnable = new Runnable() {

			public void run() {
				try {
					_doCommit();
				}
				catch (IOException ioe) {
					_log.error("Could not run scheduled commit", ioe);
				}
			}

		};

		scheduledExecutorService.scheduleWithFixedDelay(
			runnable, 0, PropsValues.LUCENE_COMMIT_TIME_INTERVAL,
			TimeUnit.MILLISECONDS);
	}

	private void _initIndexWriter() {
		try {
			IndexWriterConfig config = new IndexWriterConfig(
					Version.LUCENE_33, LuceneHelperUtil.getAnalyzer());
			
			config.setRAMBufferSizeMB(PropsValues.LUCENE_BUFFER_SIZE);

			_indexWriter = new IndexWriter(getLuceneDir(), config);
		}
		catch (Exception e) {
			_log.error(
				"Initializing Lucene writer failed for " + _companyId, e);
		}
	}



	private void _write(Term term, Document document) throws IOException {
		try {
			if (term != null) {
				_getWriter().updateDocument(term, document);
			}
			else {
				_getWriter().addDocument(document);
			}

			_optimizeCount++;

			if ((PropsValues.LUCENE_OPTIMIZE_INTERVAL == 0) ||
				(_optimizeCount >= PropsValues.LUCENE_OPTIMIZE_INTERVAL)) {

				_getWriter().optimize();

				_optimizeCount = 0;
			}

			_batchCount++;
		}
		finally {
			_commit();
		}
	}
	
	private void _initInfinispan() {
		EmbeddedCacheManager manager = null;
		try {
			manager = new DefaultCacheManager(_INFINISPAN_XML);
			
			_cache = manager.getCache();

		} catch (IOException e) {
			_log.error(e);
		}
	}
	
	private IndexWriter _getWriter() {
		if(_indexWriter==null) {
			_initIndexWriter();
		}
		return _indexWriter;
	}

	private static Log _log = LogFactoryUtil.getLog(IndexAccessorImpl.class);
	
	private static final String _INFINISPAN_XML = "/META-INF/infinispan.xml";

	private int _batchCount;
	private long _companyId;
	private IndexWriter _indexWriter;
	private Cache _cache;
	private Map<String, Directory> _Directories =
		new ConcurrentHashMap<String, Directory>();
	private int _optimizeCount;
}