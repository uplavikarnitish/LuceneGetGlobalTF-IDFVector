package tfIdfGlobalVector;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.similarities.DefaultSimilarity;

/**
 * Created by nuplavikar on 7/12/15.
 */
public class LuceneDefaultSimilarityModified extends DefaultSimilarity {
    @Override
    public float coord(int overlap, int maxOverlap)
    {
        return (float)1;
    }

    @Override
    public float lengthNorm(FieldInvertState state)
    {
        return (float)1;
    }

    @Override
    public float sloppyFreq(int distance)
    {
        return 1;
    }


}
