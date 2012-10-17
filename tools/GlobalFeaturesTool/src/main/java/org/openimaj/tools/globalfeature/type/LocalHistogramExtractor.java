/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.tools.globalfeature.type;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.openimaj.feature.FeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.pixel.statistics.BlockHistogramModel;
import org.openimaj.image.pixel.statistics.MaskingBlockHistogramModel;
import org.openimaj.tools.globalfeature.GlobalFeatureExtractor;

/**
 * Local (block-based) pixel histograms
 * @see BlockHistogramModel
 */
public class LocalHistogramExtractor extends GlobalFeatureExtractor {
	@Option(name="--color-space", aliases="-c", usage="Specify colorspace model", required=true)
	ColourSpace converter;
	
	@Option(name="--blocks-x", aliases="-bx", usage="Specify number of blocks in x-direction", required=true)
	int blocks_x;
	
	@Option(name="--blocks-y", aliases="-by", usage="Specify number of blocks in y-direction", required=true)
	int blocks_y;
	
	@Argument(required=true, usage="Number of bins per dimension")
	List<Integer> bins = new ArrayList<Integer>();
	
	@Override
	public FeatureVector extract(MBFImage image, FImage mask) {
		MBFImage converted = converter.convert(image);
		
		if (converted.numBands() != bins.size()) {
			throw new RuntimeException("Incorrect number of dimensions - recieved " + bins.size() +", expected " + converted.numBands() +".");
		}
		
		int [] ibins = new int[bins.size()];
		for (int i=0; i<bins.size(); i++)
			ibins[i] = bins.get(i);
		
		BlockHistogramModel hm = null;
		if (mask == null)
			hm = new BlockHistogramModel(blocks_x, blocks_y, ibins);
		else
			hm = new MaskingBlockHistogramModel(mask, blocks_x, blocks_y, ibins);
		
		hm.estimateModel(converted);
		return hm.toSingleHistogram();
	}
}
