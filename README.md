# JLink
A Joint Entity Recognition and Linking Tool for Technical Domains using Undirected Probabilistic Graphical Models with BiGram (former BIRE)



Quick start:

1. Clone BIRE repository from: https://github.com/ag-sc
2. Clone this project.
3. Import projects into your IDE-workspace (preferably eclipse).
3. Update config file "config/BC5_test_dataset.properties" and create folder structure in workspace project.
5. Download files from:
	5.1 CDR_Corpus http://www.biocreative.org/resources/corpora/biocreative-v-cdr-corpus/
	5.2 CTD_diseases "CTD_diseases.tsv.gz"	 (tsv-version) http://ctdbase.org/downloads/
	5.3 CTD_chemicals "CTD_chemicals.tsv.gz" (tsv-version) http://ctdbase.org/downloads/
	5.4 OMIM(Not in use but maybe interesting) https://omim.org/downloads/
	5.5 Stanford-postagger (english-bidirectional-distsim.tagger) https://github.com/richardwilly98/test-stanford-tagger/tree/master/models
	
	

HINT:
If you want to test on test data. Merge Training and Development Files from CDR_Corpus into one file. Use this as training data. 


If you want to apply the systems model to completely new data merge all three files (train, develop, test) into one big file and use this for training. 


