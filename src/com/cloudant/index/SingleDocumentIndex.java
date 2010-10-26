package com.cloudant.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.json.JSONArray;

/**
 * parses a single document that is output in proper format for cloudant reverse index map reduce view.
 * 
 * Analyzer a = new StandardAnalyzer();
 * SingleDocumentIndex index = new SingleDocumentIndex();
 * index.addField("text", "Cloudant Rocks", analyzer, 1.0f);
 * JSONArray jsonout = index.jsonMap();
 */
public class SingleDocumentIndex {

  /**
   * Constructs an empty instance.
   */
  public SingleDocumentIndex() {

  }
  
   
  /**
   * Convenience method; Tokenizes the given field text and adds the resulting
   * terms to the index; Equivalent to adding an indexed non-keyword Lucene
   * 
   * @param fieldName
   *            a name to be associated with the text
   * @param text
   *            the text to tokenize and index.
   * @param analyzer
   *            the analyzer to use for tokenization
   * @param boost
   * 	boost for this field           
   */
  public void addField(String fieldName, String text, Analyzer analyzer, float boost) throws IllegalArgumentException {

  }
  /**
   * Convenience method; Tokenizes the given field text and adds the resulting
   * terms to the index; Equivalent to adding an indexed non-keyword Lucene.  Boost set to 1.0.
   * @param fieldName
   *            a name to be associated with the text
   * @param text
   *            the text to tokenize and index.
   * @param analyzer
   *            the analyzer to use for tokenization   
   */
  public void addField(String fieldName, String text, Analyzer analyzer) throws IllegalArgumentException {

  }
  
 
  /**
   * Equivalent to <code>addField(fieldName, stream, 1.0f)</code>.
   * 
   * @param fieldName
   *            a name to be associated with the text
   * @param stream
   *            the token stream to retrieve tokens from
   */
  public void addField(String fieldName, TokenStream stream) throws IllegalArgumentException {
    addField(fieldName, stream, 1.0f);
  }

  /**
   * Iterates over the given token stream and adds the resulting terms to the index;
     * 
   * @param fieldName
   *            a name to be associated with the text
   * @param stream
   *            the token stream to retrieve tokens from.
   * @param boost
   *            the boost factor for hits for this field
   * @see org.apache.lucene.document.Field#setBoost(float)
   */
	public void addField(String fieldName, TokenStream stream, float boost) {
  	}
	/**
	 * Adds an an unanalyzed object.  The object can be an valid IndexType.
	*/
  	public void addField(String fieldName, Object o, float boost) throws IllegalArgumentException {
 	}
  
   
   	public JSONArray jsonMap() {
   		JSONArray jout = new JSONArray();
   		return jout;
  	}
  
 
}
