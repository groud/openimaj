/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
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
package org.openimaj.tools.twitter.modes.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.arabidopsis.ahocorasick.AhoCorasick;
import org.kohsuke.args4j.Option;
import org.openimaj.twitter.USMFStatus;

/**
 * The grep functionality. Should only be used as a post filter most of the time
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class GrepFilter extends TwitterPreprocessingFilter {
	
	@Option(name="--string-match", aliases="-sm", required=false, usage="Match these strings exactly, uses aho-corasick", metaVar="STRING", multiValued=true)
	List<String> stringMatch = new ArrayList<String>();
	AhoCorasick<String> searcher = null;
	
	@Option(name="--regexes", aliases="-r", required=false, usage="Match these regexes. Uses java pattern", metaVar="STRING", multiValued=true)
	List<String> regexStrings = new ArrayList<String>();
	List<Pattern> regex = new ArrayList<Pattern>();
	
	@Override
	public boolean filter(USMFStatus twitterStatus) {
		String text = twitterStatus.text;
		boolean match = searcher.search(text.getBytes()).hasNext();
		if(match) return match;
		// now do the slower regexes if there are any
		for (Pattern reg : this.regex) {
			match = reg.matcher(text).find();
			if(match) return match;
		}
		
		return match; // must be false
	}
	
	@Override
	public void validate() {
		searcher = new AhoCorasick<String>();
		for (String match : this.stringMatch) {
			searcher.add(match.getBytes(), match);	
		}
		searcher.prepare();
		
		for (String pat : this.regexStrings) {
			regex.add(Pattern.compile(pat));
		}
	}
}
