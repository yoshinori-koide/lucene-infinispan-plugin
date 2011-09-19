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

package com.liferay.portal.util;

import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;

/**
 * @author Brian Wing Shun Chan
 */
public class PropsValues {

	public static final int LUCENE_BUFFER_SIZE = GetterUtil.getInteger(PropsUtil.get(PropsKeys.LUCENE_BUFFER_SIZE));

	public static final int LUCENE_COMMIT_BATCH_SIZE = GetterUtil.getInteger(PropsUtil.get(PropsKeys.LUCENE_COMMIT_BATCH_SIZE));

	public static final int LUCENE_COMMIT_TIME_INTERVAL = GetterUtil.getInteger(PropsUtil.get(PropsKeys.LUCENE_COMMIT_TIME_INTERVAL));

	public static final String LUCENE_DIR = PropsUtil.get(PropsKeys.LUCENE_DIR);

	public static final String LUCENE_FILE_EXTRACTOR = PropsUtil.get(PropsKeys.LUCENE_FILE_EXTRACTOR);

	public static final String LUCENE_FILE_EXTRACTOR_REGEXP_STRIP = PropsUtil.get(PropsKeys.LUCENE_FILE_EXTRACTOR_REGEXP_STRIP);

	public static final int LUCENE_MERGE_FACTOR = GetterUtil.getInteger(PropsUtil.get(PropsKeys.LUCENE_MERGE_FACTOR));

	public static final int LUCENE_OPTIMIZE_INTERVAL = GetterUtil.getInteger(PropsUtil.get(PropsKeys.LUCENE_OPTIMIZE_INTERVAL));

	public static final boolean LUCENE_REPLICATE_WRITE = GetterUtil.getBoolean(PropsUtil.get(PropsKeys.LUCENE_REPLICATE_WRITE));

	public static final boolean LUCENE_STORE_JDBC_AUTO_CLEAN_UP_ENABLED = GetterUtil.getBoolean(PropsUtil.get(PropsKeys.LUCENE_STORE_JDBC_AUTO_CLEAN_UP_ENABLED));

	public static final int LUCENE_STORE_JDBC_AUTO_CLEAN_UP_INTERVAL = GetterUtil.getInteger(PropsUtil.get(PropsKeys.LUCENE_STORE_JDBC_AUTO_CLEAN_UP_INTERVAL));

	public static final String LUCENE_STORE_TYPE = PropsUtil.get(PropsKeys.LUCENE_STORE_TYPE);
	
	public static final int INDEX_ON_STARTUP_DELAY = GetterUtil.getInteger(PropsUtil.get(PropsKeys.INDEX_ON_STARTUP_DELAY));

}