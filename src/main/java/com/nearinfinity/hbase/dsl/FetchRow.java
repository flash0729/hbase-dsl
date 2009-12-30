package com.nearinfinity.hbase.dsl;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Map;
import java.util.NavigableSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * 
 * @author Aaron McCurry
 * 
 * @param <I>
 */
public class FetchRow<I> {

	private static final Log LOG = LogFactory.getLog(FetchRow.class);
	private byte[] currentFamily;
	private Get get = new Get();
	private byte[] tableName;
	private HBase<? extends QueryOps<I>, I> hBase;
	private Result result;

	FetchRow(HBase<? extends QueryOps<I>, I> hBase, String tableName) {
		this.hBase = hBase;
		this.tableName = Bytes.toBytes(tableName);
	}

	public Row<I> row(I id) {
		get = newGet(id);
		fetch();
		if (result.getRow() == null) {
			return null;
		}
		return new ResultRow<I>(hBase, result);
	}

	public FetchRow<I> select() {
		return this;
	}

	public FetchRow<I> family(String family) {
		currentFamily = Bytes.toBytes(family);
		get.addFamily(currentFamily);
		return this;
	}

	public FetchRow<I> col(String name) {
		get.addColumn(currentFamily, Bytes.toBytes(name));
		return this;
	}

	private void fetch() {
		if (result == null) {
			LOG.debug("Fetching row with id [" + Bytes.toString(get.getRow()) + "]");
			result = hBase.getResult(tableName, get);
		}
	}

	private Get newGet(I id) {
		Get newGet = new Get(hBase.toBytes(id));
		Map<byte[], NavigableSet<byte[]>> familyMap = get.getFamilyMap();
		for (byte[] family : familyMap.keySet()) {
			NavigableSet<byte[]> qualifiers = familyMap.get(family);
			if (qualifiers == null) {
				newGet.addFamily(family);
			} else {
				for (byte[] qualifier : qualifiers) {
					newGet.addColumn(family, qualifier);
				}
			}
		}
		return newGet;
	}

}
