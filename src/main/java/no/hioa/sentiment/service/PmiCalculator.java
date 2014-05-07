package no.hioa.sentiment.service;

import java.math.BigDecimal;
import java.util.List;

public interface PmiCalculator
{
	/**
	 * Calculate the probability that word1 occurs together with word2. Equation 7 in Turney (2003).
	 * 
	 * @param word1
	 * @param word2
	 * @return
	 */
	BigDecimal calculatePmi(String word1, String word2);

	/**
	 * Calculation the sentiment orientation PMI for word. Equation 8 in Turney (2003).
	 * 
	 * @param word
	 * @param pWords
	 *            list of positive semantic words
	 * @param nWords
	 *            list of negative semantic words
	 * @return
	 */
	BigDecimal calculateSoPmi(String word, List<String> pWords, List<String> nWords);
}
