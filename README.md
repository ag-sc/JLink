# JLink
A Joint Entity Recognition and Linking Tool for Technical Domains using Undirected Probabilistic Graphical Models with BiGram (former BIRE)


<b> NOTE: The documentation is far from being complete. If you have any issues feel free to write me an email. I will try to add more documentation by time. </b>


<b>Quick start:</b>

1. Clone BIRE simplified-api branch from: https://github.com/ag-sc
2. Clone this project.
3. Import projects into your IDE-workspace (preferably eclipse).
4. Build maven. (If necessary)
5. Update config file "config/BC5_test_dataset.properties" and create folder structure in workspace project.

6. Download files from:

	6.1 CDR_Corpus http://www.biocreative.org/resources/corpora/biocreative-v-cdr-corpus/
	
	6.2 CTD_diseases "CTD_diseases.tsv.gz"	 (tsv-version) http://ctdbase.org/downloads/
	
	6.3 CTD_chemicals "CTD_chemicals.tsv.gz" (tsv-version) http://ctdbase.org/downloads/
	
	6.4 Stanford-postagger (english-bidirectional-distsim.tagger) https://nlp.stanford.edu/software/tagger.shtml
	
7. Copy requiered files according to your config-file. 
	Training, Dev, and Test data can be found in the downloaded zip-folder in CDR_Data/CDR.Corpus.vxxxxxxx/*PubTator.txt. 
8. Since a lot of things are loaded to memory, we need approx. 8g to run smooth. In Eclipse add -Xmx8g to VM Arguments. 

9. Run the Main.java 

<b>HINT:</b>

If you want to test only on test data, merge training and development files from CDR_Corpus into one file. Use this as training data corpus. 

If you want to apply a learned model to completely new data, merge all three files (train, develop, test) into one big file and use this for training. 



