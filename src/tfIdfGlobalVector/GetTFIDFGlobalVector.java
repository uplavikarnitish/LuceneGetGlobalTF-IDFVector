package tfIdfGlobalVector;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import static java.lang.Math.*;

public class GetTFIDFGlobalVector {

    public static void main(String[] args) throws IOException {
	// write your code here
        String indexDir = args[0];
        String filename = args[1];
        String contentsFieldName = "contents";
        String fileNamesFieldName = "filename";
        LeafReader indexLeafReader = null;
        try {
            IndexReader indexReader = DirectoryReader.open(new SimpleFSDirectory(Paths.get(indexDir)));
            List<LeafReaderContext> leafReaderContextList = indexReader.leaves();
            if (leafReaderContextList.isEmpty()) {
                System.err.println("leafReaderContextList is empty!! ERROR!!");
                System.exit(2);
            }
            indexLeafReader = leafReaderContextList.iterator().next().reader();
            if (indexLeafReader == null)
            {
                Exception e = new Exception("indexLeafReader == null!!");
                e.printStackTrace();
                System.exit(3);
            }


        } catch (IndexNotFoundException e)
        {
            System.out.println("No files found in the index specified in directory = "+indexDir);
            System.exit(1);
        }

        int n =  indexLeafReader.numDocs();
        System.out.println("Total number of indexed documents found = "+n);
        //#Get the global terms
        Terms globalTerms = indexLeafReader.terms(contentsFieldName);
        long globalTermsSz = globalTerms.size();

        //Fill in all the terms as key into a TreeMap with the corresponding value as a idf
        TreeMap<String, Float> termIDFTreeMap = new TreeMap<String, Float>();
        TermsEnum iGlobalTerm = globalTerms.iterator(null);
        BytesRef bytesRef;
        while ( (bytesRef = iGlobalTerm.next())!=null )
        {
            float IDF = (float) log((1 + ((float) n / (indexLeafReader.docFreq(new Term(contentsFieldName, bytesRef)) + 1))));
            termIDFTreeMap.put(bytesRef.utf8ToString(), IDF);
            System.out.println(bytesRef.utf8ToString()+"=="+IDF);
        }
        //IDF of entire collection dictionary now stored as a map in termIDFTreeMap

        System.out.println("Total number of unique terms found in the index = "+globalTermsSz);
        //System.out.println("Size of global termIDFTreeMap = "+termIDFTreeMap.size());
        //printTerms(indexLeafReader.terms(contentsFieldName));  //Printing the total number of terms within the index

        for ( int i = 0; i < n; i++  )
        {
            Document doc = indexLeafReader.document(i);
            IndexableField indexableField = doc.getField(fileNamesFieldName);
            System.out.println("#"+(i+1)+">"+fileNamesFieldName+": "+indexableField.stringValue());
            Fields fields = indexLeafReader.getTermVectors(i);
            //Iterator<String> docFieldNameIterator =  fields.iterator();
            Terms locDocTerms = fields.terms(contentsFieldName);

            System.out.println("\t\tTotal number of unique terms in file:" + doc.get("filename") + " = " + locDocTerms.size());

            //Create a treeMap to hold the document's tf-idf vector


            TermsEnum termsEnum = locDocTerms.iterator(null);

            BytesRef termBytesRef;
            int count = 1;

            while ((termBytesRef = termsEnum.next()) != null)
            {


                PostingsEnum postingsEnum;
                Term term = new Term(contentsFieldName, termBytesRef);
                postingsEnum = indexLeafReader.postings(term);
                String termInDocs = "";
                String termInDocsPostingEnumEntry = "";

                int postingEntry = postingsEnum.nextDoc();
                int postingLstLngth = 0;
                while( postingEntry != PostingsEnum.NO_MORE_DOCS )
                {
                    termInDocs = termInDocs + postingsEnum.docID() + "; ";
                    //termInDocsPostingEnumEntry  = termInDocsPostingEnumEntry + postingEntry + "; ";
                    if (postingsEnum.docID() == i )
                    {

                        //System.out.println("\n\n"+count+"> term = '"+termBytesRef.utf8ToString()+"' :: freq = "+postingsEnum.freq());
                        count=count+1;

                    }
                    postingEntry = postingsEnum.nextDoc();
                    postingLstLngth++;
                }
                //System.out.println("\n\t\tIndexLeafReader.docFreq = "+indexLeafReader.docFreq(term)+";");
                //System.out.println("\t\tLucene Int. Documents idx. containing term = "+ termInDocs+"\t\tPostingsEnumLength = "+postingLstLngth);
                //TODO Remove this line!! //System.out.println("\tTerm Frequency(termInDocsPostingEnumEntry) = "+ termInDocsPostingEnumEntry);

            }*/
        }


    }
}
