

/*****************************************************************************
 *                                                                           *
 *  Copyright 2014 Rice University                                           *
 *                                                                           *
 *  Licensed under the Apache License, Version 2.0 (the "License");          *
 *  you may not use this file except in compliance with the License.         *
 *  You may obtain a copy of the License at                                  *
 *                                                                           *
 *      http://www.apache.org/licenses/LICENSE-2.0                           *
 *                                                                           *
 *  Unless required by applicable law or agreed to in writing, software      *
 *  distributed under the License is distributed on an "AS IS" BASIS,        *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 *  See the License for the specific language governing permissions and      *
 *  limitations under the License.                                           *
 *                                                                           *
 *****************************************************************************/


package simsql.runtime;

import org.apache.hadoop.io.compress.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import java.io.*;

/*
 * A central point where we manage all the compression.
 *
 * @author Luis.
 */
public class RecordCompression {

  // our codec.
  private static CompressionCodec codec = null;
  private static Compressor compressor = null;
  private static Decompressor decompressor = null;

  // we load the codec at startup.
  static {

    // get a factory
    CompressionCodecFactory c = new CompressionCodecFactory(new Configuration());

    // try to get snappy -- we might not have it in the native libraries.
    try {
      codec = c.getCodecByName("snappy");
      compressor = codec.createCompressor();
      decompressor = codec.createDecompressor();
    } catch (Throwable e) {
      System.out.println("Snappy library not available -- using default compression.");
      codec = c.getCodecByName("default");
      compressor = codec.createCompressor();
      decompressor = codec.createDecompressor();
    }   
  }

  public static DataInputStream getInputStream(InputStream inStream) {
    try {
      return new DataInputStream(codec.createInputStream(inStream));
    } catch (Exception e) {
      
      return new DataInputStream(inStream);
    }
  }

  public static DataOutputStream getOutputStream(OutputStream outStream) {
    try {
      return new DataOutputStream(codec.createOutputStream(outStream));
    } catch (Exception e) {

      return new DataOutputStream(outStream);
    }
  }

  public static String getCodecClass() {
    return codec.getClass().getName();
  }
}
