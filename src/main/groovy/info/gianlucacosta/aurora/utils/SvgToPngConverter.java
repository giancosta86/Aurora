/*§
  ===========================================================================
  Aurora
  ===========================================================================
  Copyright (C) 2015-2016 Gianluca Costa
  ===========================================================================
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  ===========================================================================
*/

package info.gianlucacosta.aurora.utils;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class SvgToPngConverter {
    public static void convert(File inputFile, File outputFile, float outputSize) {
        PNGTranscoder transcoder = new PNGTranscoder();

        transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, outputSize);
        transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, outputSize);

        try {
            String sourceURL = inputFile.toURI().toString();
            TranscoderInput transcoderInput = new TranscoderInput(sourceURL);

            try (OutputStream outputStream = new FileOutputStream(outputFile)) {
                TranscoderOutput transcoderOutput = new TranscoderOutput(outputStream);
                transcoder.transcode(transcoderInput, transcoderOutput);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    private SvgToPngConverter(){}
}
