package processing.priorityqueue;

import processing.ImageScore;




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


public class HitQueue extends PriorityQueue<ImageScore> {

  private boolean prePopulate;
  private final boolean IS_DISTANCE_MEASURE;
  

  /**
   * Creates a new instance with <code>size</code> elements. If
   * <code>prePopulate</code> is set to true, the queue will pre-populate itself
   * with sentinel objects and set its {@link #size()} to <code>size</code>. In
   * that case, you should not rely on {@link #size()} to get the number of
   * actual elements that were added to the queue, but keep track yourself.<br>
   * <b>NOTE:</b> in case <code>prePopulate</code> is true, you should pop
   * elements from the queue using the following code example:
   * 
   * <pre>
   * PriorityQueue pq = new HitQueue(10, true); // pre-populate.
   * ScoreDoc top = pq.top();
   * 
   * // Add/Update one element.
   * top.score = 1.0f;
   * top.doc = 0;
   * top = (ScoreDoc) pq.updateTop();
   * int totalHits = 1;
   * 
   * // Now pop only the elements that were *truly* inserted.
   * // First, pop all the sentinel elements (there are pq.size() - totalHits).
   * for (int i = pq.size() - totalHits; i &gt; 0; i--) pq.pop();
   * 
   * // Now pop the truly added elements.
   * ScoreDoc[] results = new ScoreDoc[totalHits];
   * for (int i = totalHits - 1; i &gt;= 0; i--) {
   *   results[i] = (ScoreDoc) pq.pop();
   * }
   * </pre>
   * 
   * <p><b>NOTE</b>: This class pre-allocate a full array of
   * length <code>size</code>.
   * 
   * @param size
   *          the requested size of this queue.
   * @param prePopulate
   *          specifies whether to pre-populate the queue with sentinel values.
   * @see #getSentinelObject()
   */
  HitQueue(int size, boolean prePopulate, boolean _is_distance_measure) {
    this.prePopulate = prePopulate;
    this.IS_DISTANCE_MEASURE = _is_distance_measure;
    initialize(size);
  }

  // Returns null if prePopulate is false.
  @Override
  protected ImageScore getSentinelObject() {
    // Always set the doc Id to MAX_VALUE so that it won't be favored by
    // lessThan. This generally should not happen since if score is not NEG_INF,
    // TopScoreDocCollector will always add the object to the queue.
	  if (IS_DISTANCE_MEASURE) 		//distancia
		  return !prePopulate ? null : new ImageScore(Integer.MAX_VALUE, Float.POSITIVE_INFINITY);
	  else
		  return !prePopulate ? null : new ImageScore(Integer.MAX_VALUE, Float.NEGATIVE_INFINITY);
  }
  
  @Override
  protected final boolean lessThan(ImageScore hitA, ImageScore hitB) {
	//Esse if era usado no lucene pra poder ordenar por documento quando o score era igual
    //if (hitA.score == hitB.score)
    //  return hitA.doc > hitB.doc; 
    //else
	  if (IS_DISTANCE_MEASURE) //distancia
		  return hitA.score > hitB.score;
	  else 	//similaridade
		  return hitA.score < hitB.score;
  }
  
  public ImageScore[] getResults(int TOP_K) {
		// In case pq was populated with sentinel values, there might be less
      // results than pq.size(). Therefore return all results until either
      // pq.size() or totalHits.
      //totalHits é igual a top_k pq a gente tem certeza que sempre vai ter top_K resultados no heap, ou seja não tem como sobrer objeto sentinel
      int totalHits = TOP_K; 
      int size = totalHits < this.size() ? totalHits : this.size();
      
      // We know that start < pqsize, so just fix howMany. 
      int howMany = Math.min(size, TOP_K);
      ImageScore[] results = new ImageScore[howMany];
      for (int i = howMany - 1; i >= 0; i--) {
          results[i] = this.pop();
      }
      return results;
	}
}
