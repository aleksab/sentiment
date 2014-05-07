package no.hioa.sentiment.service;

import java.math.BigDecimal;

public interface PmiCalculator
{
	/**
	 * Calculate the probability that word1 occurs together with word2 with a certain distance. Equation 9 in Turney (2003).
	 * 
	 * @param word1
	 * @param word2
	 * @param limit
	 *            Maximum number of words between word1 and word2. -1 indicate infinite (no limit)
	 * @return
	 */
	BigDecimal calculatePmi(String word1, String word2, int limit);
}
