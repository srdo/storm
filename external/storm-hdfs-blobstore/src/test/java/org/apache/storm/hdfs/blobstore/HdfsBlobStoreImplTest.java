
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.storm.hdfs.blobstore;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.storm.blobstore.BlobStoreFile;
import org.apache.storm.generated.SettableBlobMeta;
import org.apache.storm.hdfs.testing.MiniDFSClusterRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.*;

import org.junit.Rule;

public class HdfsBlobStoreImplTest {

    @Rule
    public final MiniDFSClusterRule DFS_CLUSTER_RULE = new MiniDFSClusterRule();

    private static final Logger LOG = LoggerFactory.getLogger(HdfsBlobStoreImplTest.class);

    // key dir needs to be number 0 to number of buckets, choose one so we know where to look
    private static String KEYDIR = "0";
    private Path blobDir = new Path("/storm/blobstore1");
    private Path fullKeyDir = new Path(blobDir, KEYDIR);
    private String BLOBSTORE_DATA = "data";

    public class TestHdfsBlobStoreImpl extends HdfsBlobStoreImpl implements AutoCloseable {

        public TestHdfsBlobStoreImpl(Path path, Map<String, Object> conf) throws IOException {
            super(path, conf);
        }

        public TestHdfsBlobStoreImpl(Path path, Map<String, Object> conf,
            Configuration hconf) throws IOException {
            super(path, conf, hconf);
        }

        @Override
        protected Path getKeyDir(String key) {
            return new Path(new Path(blobDir, KEYDIR), key);
        }

        @Override
        public void close() throws Exception {
            this.shutdown();
        }
    }

    @Test
    public void testMultiple() throws Exception {
        String testString = "testingblob";
        String validKey = "validkeyBasic";

        //Will be closed automatically when shutting down the DFS cluster
        FileSystem fs = DFS_CLUSTER_RULE.getDfscluster().getFileSystem();
        Map<String, Object> conf = new HashMap<>();

        try (TestHdfsBlobStoreImpl hbs = new TestHdfsBlobStoreImpl(blobDir, conf, DFS_CLUSTER_RULE.getHadoopConf())) {
            // should have created blobDir
            assertTrue("BlobStore dir wasn't created", fs.exists(blobDir));
            assertEquals("BlobStore dir was created with wrong permissions",
                HdfsBlobStoreImpl.BLOBSTORE_DIR_PERMISSION, fs.getFileStatus(blobDir).getPermission());

            // test exist with non-existent key
            assertFalse("file exists but shouldn't", hbs.exists("bogus"));

            // test write
            BlobStoreFile pfile = hbs.write(validKey, false);
            // Adding metadata to avoid null pointer exception
            SettableBlobMeta meta = new SettableBlobMeta();
            meta.set_replication_factor(1);
            pfile.setMetadata(meta);
            try (OutputStream ios = pfile.getOutputStream()) {
                ios.write(testString.getBytes(StandardCharsets.UTF_8));
            }

            // test commit creates properly
            assertTrue("BlobStore key dir wasn't created", fs.exists(fullKeyDir));
            pfile.commit();
            Path dataFile = new Path(new Path(fullKeyDir, validKey), BLOBSTORE_DATA);
            assertTrue("blob data not committed", fs.exists(dataFile));
            assertEquals("BlobStore dir was created with wrong permissions",
                HdfsBlobStoreFile.BLOBSTORE_FILE_PERMISSION, fs.getFileStatus(dataFile).getPermission());
            assertTrue("key doesn't exist but should", hbs.exists(validKey));

            // test read
            BlobStoreFile readpFile = hbs.read(validKey);
            try (InputStream inStream = readpFile.getInputStream()) {
                String readString = IOUtils.toString(inStream, StandardCharsets.UTF_8);
                assertEquals("string read from blob doesn't match", testString, readString);
            }

            // test listkeys
            Iterator<String> keys = hbs.listKeys();
            assertTrue("blob has one key", keys.hasNext());
            assertEquals("one key in blobstore", validKey, keys.next());

            // delete
            hbs.deleteKey(validKey);
            assertFalse("key not deleted", fs.exists(dataFile));
            assertFalse("key not deleted", hbs.exists(validKey));

            // Now do multiple
            String testString2 = "testingblob2";
            String validKey2 = "validkey2";

            // test write
            pfile = hbs.write(validKey, false);
            pfile.setMetadata(meta);
            try (OutputStream ios = pfile.getOutputStream()) {
                ios.write(testString.getBytes(StandardCharsets.UTF_8));
            }

            // test commit creates properly
            assertTrue("BlobStore key dir wasn't created", fs.exists(fullKeyDir));
            pfile.commit();
            assertTrue("blob data not committed", fs.exists(dataFile));
            assertEquals("BlobStore dir was created with wrong permissions",
                HdfsBlobStoreFile.BLOBSTORE_FILE_PERMISSION, fs.getFileStatus(dataFile).getPermission());
            assertTrue("key doesn't exist but should", hbs.exists(validKey));

            // test write again
            pfile = hbs.write(validKey2, false);
            pfile.setMetadata(meta);
            try (OutputStream ios2 = pfile.getOutputStream()) {
                ios2.write(testString2.getBytes(StandardCharsets.UTF_8));
            }

            // test commit second creates properly
            pfile.commit();
            Path dataFile2 = new Path(new Path(fullKeyDir, validKey2), BLOBSTORE_DATA);
            assertTrue("blob data not committed", fs.exists(dataFile2));
            assertEquals("BlobStore dir was created with wrong permissions",
                HdfsBlobStoreFile.BLOBSTORE_FILE_PERMISSION, fs.getFileStatus(dataFile2).getPermission());
            assertTrue("key doesn't exist but should", hbs.exists(validKey2));

            // test listkeys
            keys = hbs.listKeys();
            int total = 0;
            boolean key1Found = false;
            boolean key2Found = false;
            while (keys.hasNext()) {
                total++;
                String key = keys.next();
                if (key.equals(validKey)) {
                    key1Found = true;
                } else if (key.equals(validKey2)) {
                    key2Found = true;
                } else {
                    fail("Found key that wasn't expected: " + key);
                }
            }
            assertEquals("number of keys is wrong", 2, total);
            assertTrue("blobstore missing key1", key1Found);
            assertTrue("blobstore missing key2", key2Found);

            // test read
            readpFile = hbs.read(validKey);
            try (InputStream inStream = readpFile.getInputStream()) {
                String readString = IOUtils.toString(inStream, StandardCharsets.UTF_8);
                assertEquals("string read from blob doesn't match", testString, readString);
            }

            // test read
            readpFile = hbs.read(validKey2);
            try (InputStream inStream = readpFile.getInputStream()) {
                String readString = IOUtils.toString(inStream, StandardCharsets.UTF_8);
                assertEquals("string read from blob doesn't match", testString2, readString);
            }

            hbs.deleteKey(validKey);
            assertFalse("key not deleted", hbs.exists(validKey));
            hbs.deleteKey(validKey2);
            assertFalse("key not deleted", hbs.exists(validKey2));
        }
    }

    @Test
    public void testGetFileLength() throws Exception {
        Map<String, Object> conf = new HashMap<>();
        String validKey = "validkeyBasic";
        String testString = "testingblob";
        try (TestHdfsBlobStoreImpl hbs = new TestHdfsBlobStoreImpl(blobDir, conf, DFS_CLUSTER_RULE.getHadoopConf())) {
            BlobStoreFile pfile = hbs.write(validKey, false);
            // Adding metadata to avoid null pointer exception
            SettableBlobMeta meta = new SettableBlobMeta();
            meta.set_replication_factor(1);
            pfile.setMetadata(meta);
            try (OutputStream ios = pfile.getOutputStream()) {
                ios.write(testString.getBytes(StandardCharsets.UTF_8));
            }
            assertEquals(testString.getBytes(StandardCharsets.UTF_8).length, pfile.getFileLength());
        }
    }
}
