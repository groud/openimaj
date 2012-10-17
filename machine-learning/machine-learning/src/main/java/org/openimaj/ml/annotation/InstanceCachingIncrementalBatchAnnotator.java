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
package org.openimaj.ml.annotation;

import java.util.List;
import java.util.Set;

import org.openimaj.experiment.dataset.cache.GroupedListCache;
import org.openimaj.experiment.dataset.cache.InMemoryGroupedListCache;
import org.openimaj.feature.FeatureExtractor;

/**
 * Adaptor that allows a {@link BatchAnnotator} to behave like a
 * {@link IncrementalAnnotator} by caching instances and
 * then performing training only when {@link #annotate(Object)} is
 * called. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <OBJECT> Type of object
 * @param <ANNOTATION> Type of annotation
 * @param <EXTRACTOR> Type of object capable of extracting features from the object
 */
public class InstanceCachingIncrementalBatchAnnotator<
	OBJECT, 
	ANNOTATION,
	EXTRACTOR extends FeatureExtractor<?, OBJECT>> 
extends IncrementalAnnotator<OBJECT, ANNOTATION, EXTRACTOR> 
{
	BatchAnnotator<OBJECT, ANNOTATION, EXTRACTOR> batchAnnotator;
	GroupedListCache<ANNOTATION, OBJECT> objectCache;
	boolean isInvalid = true;
	
	/**
	 * Construct with an in-memory cache and the given batch annotator.
	 * @param batchAnnotator the batch annotator
	 */
	public InstanceCachingIncrementalBatchAnnotator(BatchAnnotator<OBJECT, ANNOTATION, EXTRACTOR> batchAnnotator) {
		super(batchAnnotator.extractor);
		this.batchAnnotator = batchAnnotator;
		this.objectCache = new InMemoryGroupedListCache<ANNOTATION, OBJECT>();
	}
	
	/**
	 * Construct with the given batch annotator and cache implementation.
	 * @param batchAnnotator the batch annotator
	 * @param cache the cache 
	 */
	public InstanceCachingIncrementalBatchAnnotator(BatchAnnotator<OBJECT, ANNOTATION, EXTRACTOR> batchAnnotator, GroupedListCache<ANNOTATION, OBJECT> cache) {
		super(batchAnnotator.extractor);
		this.batchAnnotator = batchAnnotator;
		this.objectCache = cache;
	}

	@Override
	public void train(Annotated<OBJECT, ANNOTATION> annotated) {
		objectCache.add(annotated.getAnnotations(), annotated.getObject());
		isInvalid = true;
	}

	@Override
	public void reset() {
		objectCache.reset();
		isInvalid = true;
	}

	@Override
	public Set<ANNOTATION> getAnnotations() {
		return objectCache.getDataset().getGroups();
	}

	@Override
	public List<ScoredAnnotation<ANNOTATION>> annotate(OBJECT object) {
		if (isInvalid) {
			batchAnnotator.train(objectCache.getDataset());
			isInvalid = false;
		}

		return batchAnnotator.annotate(object);
	}
}
