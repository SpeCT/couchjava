/**
 * 
 */
package com.cloudant.couchdbjavaserver;

import org.apache.lucene.analysis.Analyzer;

/**
 * @author hardtke
 * SearchView extends the general JavaView class for implementing Java Language Views in Cloudant
 * SearchView requires that a Lucene Analyzer is specified.
 *
 */
public interface SearchView extends JavaView {
	public Analyzer getAnalyzer();
	public void setAnalyzer(Analyzer analyzer);
}
