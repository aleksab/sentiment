package no.hioa.sentiment.service;

import java.util.List;

public interface SeedWord
{
	/**
	 * Get all possible seed words that we have.
	 * 
	 * @return
	 */
	public List<String> getAllSeedWords();

	/**
	 * Get seed words which seems to be unamvigious. This means a score of very positive or very negative.
	 * 
	 * @return
	 */
	public List<String> getUnAmbigiousSeedWords();

	/**
	 * Get predefined seed words.
	 * 
	 * @return
	 */
	public List<String> getStaticSeedWords();
}
