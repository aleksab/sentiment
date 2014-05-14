package no.hioa.sentiment.service;

import java.math.BigDecimal;
import java.util.List;

public interface PmiCalculator
{
	/**
	 * Find number of occurrence where two words are within a maximum distance of each other.
	 * 
	 * @param word1
	 * @param word2
	 * @param maxDistance
	 *            -1 means all possible distances
	 * @return
	 */
	public long findWordDistance(String word1, String word2, long maxDistance);

	/**
	 * Find occurrence of a word in content.
	 * 
	 * @param word
	 * @return
	 */
	public long findWordOccurence(String word);

	/**
	 * Calculate the SO-PMI for word using distance of word from positive and negative words. Equation 10 in Turney (2003).
	 * 
	 * @param word
	 *            word to calculate sentiment orientation for
	 * @param pWords
	 *            list of positive seed words
	 * @param nWords
	 *            list of negative seed words
	 * @param maxDistance
	 *            max distance from word to seed words
	 * @return
	 */
	public BigDecimal calculateSoPmi(String word, List<String> pWords, List<String> nWords, int maxDistance);
}
