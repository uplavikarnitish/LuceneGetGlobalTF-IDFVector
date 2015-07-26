package tfIdfGlobalVector;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.index.DirectoryReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
/**
 * Created by nuplavikar on 7/11/15.
 */
public class TFIDFOnlySimilarity extends DefaultSimilarity {


    public static void main(String[] args) throws Exception {
        // write your code here
        if (args.length != 2) {
            throw new Exception("Usage Java " + GetTFIDFGlobalVector.class.getName() + "<index dir> <data dir>");
        }
        String indexDir = args[0];
        String fileName = args[1];
        mySearch(indexDir, fileName);
    }

    public static void mySearch(String indexDir, String fileName) throws IOException, ParseException {

        FileReader fr =  new FileReader(fileName);
        BufferedReader textReader = new BufferedReader(fr);
        String lineAsString, entireFileAsString="";
        while ( (lineAsString = textReader.readLine())!=null )
        {
            entireFileAsString += lineAsString;
        }
        System.out.println("File as below:\n");
        System.out.println(entireFileAsString);


        Directory dir = new SimpleFSDirectory(Paths.get(indexDir));
        //IndexSearcher is = new IndexSearcher( DirectoryReader.open(indexDir));  IMP
        IndexSearcher is = new IndexSearcher(DirectoryReader.open(dir));
        is.setSimilarity(new LuceneDefaultSimilarityModified());



        QueryParser parser = new QueryParser("contents", new StandardAnalyzer());
        Query query = parser.parse(entireFileAsString);
        System.out.println("query.getBoost() = "+query.getBoost());
        //Create the proper weight for this query
        //Weight weight = query.createWeight(is, true);
        //System.out.println("weight.getValueForNormalization: "+weight.getValueForNormalization());
        //System.out.println("Query Norm:"+is.getSimilarity().queryNorm(weight.getValueForNormalization()));
        long start = System.currentTimeMillis();
        TopDocs hits = is.search(query, 10);
        long end = System.currentTimeMillis();
        System.err.println("Found " + hits.totalHits + "; Document(s) (in " + (end - start) + " milliseconds) that matched query '" + "EntireFileString" + "' ;");

        for (int i = 0; i<hits.totalHits; i++)
        {
            ScoreDoc scoreDoc = hits.scoreDocs[i];
            Document doc = is.doc(scoreDoc.doc);
            System.out.println("<#"+String.format("%02d", (i+1))+"> "+doc.get("filename")+"<ID:"+scoreDoc.doc+">"+" score:"+scoreDoc.score);
        }
    }
}