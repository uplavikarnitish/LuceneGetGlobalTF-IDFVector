package tfIdfGlobalVector;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

class PrintAllFilesTermVectors {

    public static void main(String [] args) throws IOException {
        String indexDir = args[0];
        String filename = args[1];
        String contentsFieldName = "contents";
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
        System.out.println("Total number of unique terms found in the index = "+indexLeafReader.terms(contentsFieldName).size());
        printTerms(indexLeafReader.terms(contentsFieldName));  //Printing the total number of terms within the index

        for ( int i = 0; i < n; i++  )
        {
            Document doc = indexLeafReader.document(i);
            Fields fields = indexLeafReader.getTermVectors(i);
            //Iterator<String> docFieldNameIterator =  fields.iterator();
            Terms terms = fields.terms(contentsFieldName);

            System.out.println("\t\tTotal number of unique terms in file:"+doc.get("filename")+" = "+terms.size());


            TermsEnum termsEnum = terms.iterator(null);

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

            }
        }

    }

    public static int printTerms(Terms terms) throws IOException
    {
        int count = 0;
        if((terms!=null) && (terms.size()>0))
        {
            BytesRef termBytesRef = null;
            TermsEnum termsEnum;
            termsEnum = terms.iterator(null);
            System.out.println("Total no. of terms in the terms object, obtained from index, passed = "+terms.size());
            while(( termBytesRef = termsEnum.next())!= null)
            {
                System.out.println("term = "+termBytesRef.utf8ToString()+"("+termBytesRef.length+")");
                count++;
            }
        }
        System.out.println("Total terms printed from index = "+count);
        return count;
    }
}
