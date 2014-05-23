package no.hioa.sentiment.score;

import java.util.List;

import no.hioa.sentiment.service.Corpus;

public interface SentimentScore
{
	public List<Score> getSentimentScore(Corpus corpus, List<SentimentList> sentimentList);
}
