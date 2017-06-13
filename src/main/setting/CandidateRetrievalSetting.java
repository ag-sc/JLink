package main.setting;

import java.util.LinkedHashMap;
import java.util.Map;

import candidateretrieval.ICandidateRetrieval;
import candidateretrieval.jaccard.JaccardRetrieval;
import candidateretrieval.levenshtein.LevenshteinRetrieval;
import candidateretrieval.lucene.LuceneRetrieval;

public class CandidateRetrievalSetting {

	public final Map<Class<? extends ICandidateRetrieval>, Boolean> setting;

	public CandidateRetrievalSetting(boolean includeLuceneCandidateRetrieval, boolean includeJaccardCandidateRetrieval,
			boolean includeLevenshteinCandidateRetrieval) {

		setting = new LinkedHashMap<>();

		setting.put(LuceneRetrieval.class, includeLuceneCandidateRetrieval);
		setting.put(JaccardRetrieval.class, includeJaccardCandidateRetrieval);
		setting.put(LevenshteinRetrieval.class, includeLevenshteinCandidateRetrieval);

	}

	@Override
	public String toString() {
		return "CandidateRetrievalSetting [candidateRetrievalSetting=" + setting + "]";
	}

}
