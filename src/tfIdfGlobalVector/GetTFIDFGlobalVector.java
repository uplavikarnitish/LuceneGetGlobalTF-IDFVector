package tfIdfGlobalVector;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;

import java.awt.dnd.InvalidDnDOperationException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import static java.lang.Math.*;

public class GetTFIDFGlobalVector {

    public static void main(String[] args) throws IOException {
	// write your code here
        String indexDir = args[0];
        String filename = args[1];
        String contentsFieldName = "contents";
        String fileNamesFieldName = "filename";
        String fileName;
        LeafReader indexLeafReader = null;
        float freq = 0, termWt = 0, docMagnitude = 0;
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

        TreeMap<String, TreeMap<String, Float>> docTFIDFVectorTreeMap = new TreeMap<String, TreeMap<String, Float>>();
        TreeMap<String, Float> docMagnitudeTreeMap = new TreeMap<String, Float>();
        System.out.println("Total number of indexed documents found = "+n);
        //#Get the global terms
        Terms globalTerms = indexLeafReader.terms(contentsFieldName);
        long globalTermsSz = globalTerms.size();

        //Fill in all the terms as key into a TreeMap with the corresponding value as a idf
        TreeMap<String, Float> globalTermIDFTreeMap = new TreeMap<String, Float>();
        TermsEnum iGlobalTerm = globalTerms.iterator(null);
        BytesRef bytesRef;
        while ( (bytesRef = iGlobalTerm.next())!=null )
        {
            float IDF = (float) (log((((float) n / (indexLeafReader.docFreq(new Term(contentsFieldName, bytesRef)) + 1))))+1);
            globalTermIDFTreeMap.put(bytesRef.utf8ToString(), IDF);
            System.out.println(bytesRef.utf8ToString()+"=="+IDF);
        }
        //IDF of entire collection dictionary now stored as a map in termIDFTreeMap

        System.out.println("Total number of unique terms found in the index = "+globalTermsSz);
        //System.out.println("Size of global termIDFTreeMap = "+globalTermIDFTreeMap.size());
        //printSize(globalTermIDFTreeMap, "globalTermIDFTreeMap");
        //printTerms(indexLeafReader.terms(contentsFieldName));  //Printing the total number of terms within the index

        for ( int i = 0; i < n; i++  )
        {
            Document doc = indexLeafReader.document(i);
            IndexableField indexableField = doc.getField(fileNamesFieldName);
            fileName = indexableField.stringValue();
            System.out.println("#"+(i+1)+">"+fileNamesFieldName+": "+fileName);
            Fields fields = indexLeafReader.getTermVectors(i);
            //Iterator<String> docFieldNameIterator =  fields.iterator();
            Terms locDocTerms = fields.terms(contentsFieldName);

            System.out.println("\t\tTotal number of unique terms in file:" + doc.get("filename") + " = " + locDocTerms.size());

            //Create a treeMap to hold the document's tf-idf vector
                //First create a vector(TreeMap) equal to the global dictionary size dimensions
                //Creation can be done initially by copying the globalTermIDF as it is and then multiplying it with TF
            TreeMap<String, Float> docTFIDFTermVector = new TreeMap<String, Float>(globalTermIDFTreeMap);
            docMagnitude = 0;
            //System.out.println("Size of docTFIDFTermVector = "+docTFIDFTermVector.size());
            //printSize(docTFIDFTermVector, "docTFIDFTermVector");

            //Looping over all the global space dimensions
            //TODO: remove this line: TermsEnum termsEnum = locDocTerms.iterator(null);
            Set<String> globalTermsSet = globalTermIDFTreeMap.keySet();
            BytesRef termBytesRef;
            int count = 1;

            Iterator<String> termStrIt = globalTermsSet.iterator();
            while (termStrIt.hasNext())
            {


                PostingsEnum postingsEnum;
                String curTerm = termStrIt.next();
                Term term = new Term(contentsFieldName, curTerm);
                postingsEnum = indexLeafReader.postings(term);
                //String termInDocs = "";
                //String termInDocsPostingEnumEntry = "";

                int postingEntry = postingsEnum.nextDoc();
                int postingLstLngth = 0;
                boolean isTermInDoc = false;
                freq = 0;
                while( postingEntry != PostingsEnum.NO_MORE_DOCS )
                {
                    //termInDocs = termInDocs + postingsEnum.docID() + "; ";
                    //termInDocsPostingEnumEntry  = termInDocsPostingEnumEntry + postingEntry + "; ";
                    if (postingsEnum.docID() == i )
                    {

                        //System.out.println("\n\n"+count+"> term = '"+termBytesRef.utf8ToString()+"' :: freq = "+postingsEnum.freq());
                        freq = postingsEnum.freq();
                        count=count+1;
                        isTermInDoc = true;

                    }
                    postingEntry = postingsEnum.nextDoc();
                    postingLstLngth++;
                }
                termWt = freq*globalTermIDFTreeMap.get(curTerm);
                docTFIDFTermVector.put(curTerm, termWt);
                docMagnitude = docMagnitude + (termWt*termWt);



                //System.out.println("\n\t\tIndexLeafReader.docFreq = "+indexLeafReader.docFreq(term)+";");
                //System.out.println("\t\tLucene Int. Documents idx. containing term = "+ termInDocs+"\t\tPostingsEnumLength = "+postingLstLngth);
                //TODO Remove this line!! //System.out.println("\tTerm Frequency(termInDocsPostingEnumEntry) = "+ termInDocsPostingEnumEntry);
            }
            //System.out.println(docTFIDFTermVector);
            System.out.println("Doc #"+(i+1)+" Document magnitude = "+docMagnitude+"\n");

            //Put each document's term vector and magnitude in two different TreeMaps
            docTFIDFVectorTreeMap.put(fileName, docTFIDFTermVector);
            docMagnitude = (float)Math.sqrt(docMagnitude);
            docMagnitudeTreeMap.put(fileName, docMagnitude);
        }


        printDocTFIDFVectorTreeMapAndDocMagnitudeTreeMap(docTFIDFVectorTreeMap, docMagnitudeTreeMap);

    }

    private static void printSize(Object a, String msg)
    {
        System.out.println("DEBUG!!--->> "+"size = "+ObjectSizeFetcher.sizeof(a)+msg);

    }

    public static void printDocTFIDFVectorTreeMapAndDocMagnitudeTreeMap(TreeMap<String, TreeMap<String, Float>> docTFIDFVector, TreeMap<String, Float> docMagnitude)
    {
        Set<String> fileNames= docTFIDFVector.keySet();
        String fileName;
        if( fileNames.equals(docMagnitude.keySet()) == false )
        {
            InvalidDnDOperationException invalidDnDOperationException = new InvalidDnDOperationException("File name integrity wrong!!");
            invalidDnDOperationException.printStackTrace();
            System.exit(1);
        }
        Iterator<String> it = fileNames.iterator();
        int count = 0;
        while( it.hasNext() ) {
            fileName = it.next();
            float magnitude = docMagnitude.get(fileName);
            System.out.println("#"+(++count)+" fileName:"+fileName+" Magnitude:"+magnitude+" terms=weight:"+docTFIDFVector.get(fileName));
        }
    }

}
