package no.hioa.sentiment.pmi;

import java.util.List;

public interface SeedWord
{
	/**
	 * Get all positive seed words.
	 * 
	 * @return
	 */
	public List<String> getPositiveSeedWords();
	
	/**
	 * Get all negative seed words.
	 * 
	 * @return
	 */
	public List<String> getNegativeSeedWords();	
}
