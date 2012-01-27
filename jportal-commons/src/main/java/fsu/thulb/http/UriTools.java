package fsu.thulb.http;

public class UriTools {

    public static String removeLastPathSegment(String baseURI) {
        String[] splittedBaseURI = baseURI.split("/");
        String lastPathSegment = splittedBaseURI[splittedBaseURI.length -1];
        int lastPathSegmentIndex = baseURI.lastIndexOf(lastPathSegment);
        String baseURILastSegmentRemoved = baseURI.substring(0, lastPathSegmentIndex);
        return baseURILastSegmentRemoved;
    }

}
