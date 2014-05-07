package no.hioa.sentiment.service;

public interface AroundSearch
{
	/**
	 * Performs an around search between two words. This means it will return the average distance between the words in the corpus for the given
	 * range. This search is symmetric so the order of word1 and word2 does not matter.
	 * 
	 * @param word1
	 * @param word2
	 * @param startRange
	 *            must be smaller or equal to endRange
	 * @param endRange
	 */
	public void search(String word1, String word2, int startRange, int endRange);
}
