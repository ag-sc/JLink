package sampler;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import corpus.DataReader;
import corpus.DatasetConfig;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import main.setting.EDataset;
import tokenization.Tokenizer;

public class PosTagger {

	private static MaxentTagger tagger;

	public PosTagger() {
		try {
			tagger = new MaxentTagger(DatasetConfig.getPosTaggerFile());
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	public boolean wordIsNONAdjective(String word) {

		if (word.endsWith("ic") || word.endsWith("ical"))
			return false;

		List<HasWord> sentence = MaxentTagger.tokenizeText(new StringReader(word)).get(0);

		TaggedWord taggedWord = tagger.tagSentence(sentence).get(0);
		// if (taggedWord.tag().equals("NN") || taggedWord.tag().equals("NNS")
		// || taggedWord.tag().equals("NNP")
		// || taggedWord.tag().equals("NNPS"))

		if (taggedWord.tag().equals("JJ"))
			return false;

		return true;
	}

	private void tagCorpus() throws IOException {
		Map<String, String> documents = DataReader.loadTexts(EDataset.DEVELOP);

		for (Entry<String, String> string : documents.entrySet()) {
			System.out.println(string.getKey());
			final String doc = Tokenizer.getTokenizedForm(string.getValue());

			List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(doc));
			for (List<HasWord> sentence : sentences) {
				List<TaggedWord> tSentence = tagger.tagSentence(sentence);
				System.out.println(Sentence.listToString(tSentence, false));
			}
		}
	}

}
