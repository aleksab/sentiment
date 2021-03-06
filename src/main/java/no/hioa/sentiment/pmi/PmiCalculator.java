package no.hioa.sentiment.pmi;

import java.math.BigDecimal;
import java.util.List;

public interface PmiCalculator
{
	/**
	 * Find number of occurrence where two words are within a maximum distance
	 * of each other.
	 * 
	 * @param word1
	 * @param word2
	 * @param maxDistance
	 *            -1 means all possible distances
	 * @return
	 */
	public long findWordDistance(String word1, String word2, long maxDistance);

	/**
	 * Find all possible distances between two words. 
	 * 
	 * @param word1
	 * @param word2
	 * @return
	 */
	public WordDistance findAllWordDistances(String word1, String word2);

	/**
	 * Find how many times a word occurs with block length on either left or
	 * right side. If a word has a block with given length on both sides, it
	 * counts as two.
	 * 
	 * @param word
	 * @param textSpace
	 * @return
	 */
	public long findWordOccurenceWithBlock(String word, int blockLength);

	/**
	 * Find occurrence of words in collection. 
	 * 
	 * @param word
	 *            the word to find occurrence for.
	 * @return
	 */
	public long findWordOccurence(String word);

	/**
	 * Calculate the SO-PMI for word using distance of word from positive and
	 * negative words. Equation 10 in Turney (2003).
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

	/**
	 * Calculate PMI for a word against every seed word. Equation 9 in Turney
	 * (2003).
	 * 
	 * @param word
	 * @param seedWord
	 * @return
	 */
	public BigDecimal calculatePmi(String word, String seedWord, int maxDistance);
}
