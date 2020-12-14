

import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.math.BigDecimal;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.nio.charset.StandardCharsets;

import java.math.RoundingMode;
import javax.xml.parsers.*;
import java.util.Date;
import java.sql.Timestamp;

@SuppressWarnings("javadoc")
public class DblpCalcDistance {
    HashMap<Integer,String> authorHash = new HashMap<Integer,String>();
    HashMap<Integer,String> titlesHash = new HashMap<Integer,String>();

    Integer countAuthors = 0;
    // freqPatterns : list of List of Authors Ids
    ArrayList<ArrayList<Integer>> freqPatterns = new ArrayList<ArrayList<Integer>>();
    ArrayList<HashSet<Integer>> transactions = new ArrayList<HashSet<Integer>>();
    ArrayList<HashSet<Integer>> freqPatternsByTrans = new ArrayList<HashSet<Integer>>();
    ArrayList<ArrayList<BigDecimal>> JaccardDistanceMatrix = new ArrayList<ArrayList<BigDecimal>>();
    ArrayList<ArrayList<Integer>> clusters = new ArrayList<ArrayList<Integer>>();

    /*
        This method takes as an input file the outputfp.txt that was created by thFle
        Close Frequent Pattern jar library. Loads the content in an ArrayList of Integer ArrayList
        named freqPatterns.
        Every line of the list represents a list of one or more authors Ids,.
     */
    public void loadFreqPattToArray() {
        //
        try (FileReader f = new FileReader("outputfp.txt")) {
            StringBuffer sb = new StringBuffer();
            while (f.ready()) {
                char c = (char) f.read();
                if (c == '\n') {
                    String line = sb.toString();
                    String[] arrayLine = line.split(" #SUP: ");
                    //System.out.println("line: " + line);
                    if (Integer.parseInt(arrayLine[1]) >= 10 ){
                        String[] listAuthors = arrayLine[0].split(" ");
                        ArrayList<Integer> listAuthorInt = new ArrayList<Integer>();
                        for (int i=0; i< listAuthors.length; i++){
                            listAuthorInt.add(Integer.parseInt(listAuthors[i]));
                        }
                        freqPatterns.add(listAuthorInt);
                        //System.out.println(arrayLine[0]);
                        //System.out.println(listAuthorInt.toString());
                    }
                    //result.add(sb.toString());
                    sb = new StringBuffer();
                } else {
                    sb.append(c);
                }
            }

        } catch (Exception e)
        {
            System.err.println("Problems reading file");
        }
        //return result;
    }


    /*
        loadTransactionstoArray: It loads an ArrayList with transactions
        A transaction is a list of Author IDs
     */
    public void loadTransactionstoArray() {
        //
        try (FileReader f = new FileReader("inputfp_100K.txt")) {
            StringBuffer sb = new StringBuffer();
            while (f.ready()) {
                char c = (char) f.read();
                if (c == '\n') {
                    String line = sb.toString();
                    String[] arrayLine = line.split(" ");
                    HashSet<Integer> listAuthorInt = new HashSet<Integer>();
                    for (int i=0; i< arrayLine.length; i++){
                        listAuthorInt.add(Integer.parseInt(arrayLine[i]));
                    }
                    transactions.add(listAuthorInt);
                    //System.out.println(listAuthorInt.toString());

                    //result.add(sb.toString());
                    sb = new StringBuffer();
                } else {
                    sb.append(c);
                }
            }

        } catch (Exception e)
        {
            System.err.println("Problems reading file");
        }
        //return result;
    }

    /*
        uploadTitlesArray: Uploads an HashMap with publication Titles and their IDs
     */
    public void uploadTitlesArray(){
        //"titles.txt"
        try (FileReader f = new FileReader("titles.txt")) {
            StringBuffer sb = new StringBuffer();
            while (f.ready()) {
                char c = (char) f.read();
                if (c == '\n') {
                    String line = sb.toString();
                    String[] arrayLine = line.split("\t");
                    titlesHash.put(Integer.parseInt(arrayLine[0]),arrayLine[1]);
                    sb = new StringBuffer();
                } else {
                    sb.append(c);
                }
            }

        } catch (Exception e)
        {
            System.err.println("Problems reading file");
        }
    }


    /*
        uploadAuthorsArray: Uploads a HasmMap with authors and their IDs
     */
    public void uploadAuthorsArray(){
        //authors.txt
        try (FileReader f = new FileReader("authors.txt")) {
            StringBuffer sb = new StringBuffer();
            while (f.ready()) {
                char c = (char) f.read();
                if (c == '\n') {
                    String line = sb.toString();
                    String[] arrayLine = line.split("\t");
                    authorHash.put(Integer.parseInt(arrayLine[0]),arrayLine[1]);
                    sb = new StringBuffer();
                } else {
                    sb.append(c);
                }
            }

        } catch (Exception e)
        {
            System.err.println("Problems reading file");
        }
    }

    /*
        isPatternInTransaction: Receives a frequent pattern (ptrn) which is al ist of AuthorIDs  and a single transaction represented
        by a hasmap of Author Ids. It checks if the Whole list of authors in the pattern exist in the transaction. Returns true/false
     */
    public Boolean isPatternInTransaction(ArrayList<Integer> ptrn, HashSet<Integer> transSet){
        Boolean found = true;
        //System.out.println("ptrn:" + ptrn.toString());
        //System.out.println("transSet:" + transSet.toString());
            for (Integer authorId : ptrn ){

                if (!(transSet.contains(authorId))){
                    found = false;
                }
                if (!found) {
                    break;
                }
            }
        return found;
    }


    /*
        setReqPatternsByTrans: Creates a list of frequent patterns and for each of them it stores the list of Transactions IDs wheer
        the frequent pattern appears, this helps later to calculate Jaccard Distances
     */
    public void setReqPatternsByTrans(){
        System.out.println("Total Of Frequent Patterns: ");
        System.out.println(freqPatterns.size());
        System.out.println("Total of transactions: ");
        System.out.println(transactions.size());
        System.out.println("Transactions Loading .....");
        for(int fp=0;fp<freqPatterns.size();fp++){
            HashSet<Integer> transactionsRow= new HashSet<Integer>();
            for (int t=0; t<transactions.size();t++){
                if (isPatternInTransaction(freqPatterns.get(fp),transactions.get(t))){
                    transactionsRow.add(t);
                }
             }
            //System.out.println(transactionsRow.toString());
            freqPatternsByTrans.add(transactionsRow);
        }
    }

    /*
        calculateJaccardDistance: The Jaccard Distances is a matrix of Number of Frequent Patterns X Number of Frequent Patterns.
        It stores the distance between each Frequent Patterns  with all of the others.
        Jaccard Distance = 1 - (Total transactions where BOTH patterns appear / Total transactions where EITHER patterns appear)

     */
    public void calculateJaccardDistance() {
        Date date = new Date();
        System.out.println(new Timestamp(date.getTime()));
        for (int r=0; r<freqPatterns.size(); r++){
             ArrayList<BigDecimal> rowMatrix = new ArrayList<BigDecimal>();
             for (int c=0; c<freqPatterns.size(); c++){
                if (r==c){
                    rowMatrix.add(BigDecimal.valueOf(0.0));
                } else{
                    // loop through transactions
                    HashSet<Integer> transactionsFrqPtrnUnion = new  HashSet<Integer>(freqPatternsByTrans.get(r));
                    HashSet<Integer> transactionsFrqPtrnIntersect = new  HashSet<Integer>(freqPatternsByTrans.get(r));
                    HashSet<Integer> transactionsFrqPtrn2 = freqPatternsByTrans.get(c);
                    transactionsFrqPtrnIntersect.retainAll(transactionsFrqPtrn2);
                    transactionsFrqPtrnUnion.addAll(transactionsFrqPtrn2);
                    BigDecimal intersectPtn = BigDecimal.valueOf(transactionsFrqPtrnIntersect.size());
                    BigDecimal unionPtn =BigDecimal.valueOf(transactionsFrqPtrnUnion.size());
                    BigDecimal subt = BigDecimal.valueOf(1.000000000);
                    BigDecimal div = intersectPtn.divide(unionPtn,7, RoundingMode.HALF_UP);
                    BigDecimal distance = subt.subtract(div);
                    rowMatrix.add( distance);
                }
            }
            JaccardDistanceMatrix.add(rowMatrix);
             if (r % 100 == 0){
                  date = new Date();
                 System.out.println("One more hundred: " + r);
                 System.out.println(new Timestamp(date.getTime()));
             }
        }
        System.out.println(new Timestamp(date.getTime()));
    }

    /*
        makeClusters: This is the ONE-PASS Microclustering algorithm as explained in the Paper
     */
    public void makeClusters(){
        // the clusters will be defined by a list of integers
        // where the integer will be the Id of the Frequent Pattern
        for (int fp=0; fp<freqPatterns.size(); fp++){
            // if first round, no clusters...
            if (clusters.size() == 0){
                ArrayList<Integer> newItem = new ArrayList<Integer>();
                newItem.add(fp);
                clusters.add(newItem);
                System.out.println("Cluster Size=0 , Added Freq Pattern " + fp + " to cluster : 0"  );
            }else{

                // This section creates a maxDistanceByCluster list with the Max distance between
                // the current frequent patttern being process and the farthest frequent patter of
                // each cluster
                ArrayList<BigDecimal> maxDistanceByCluster = new ArrayList<BigDecimal>();
                // loop through clusters
                for (ArrayList<Integer> cluster: clusters){
                    // loop through list of frequent pattern ids in cluster
                    // and gets the frequent pattern in the cluster witht he biggest distance
                    // to the current frequent pattern being processed
                    BigDecimal maxDistance = BigDecimal.valueOf(0.0);
                    for (Integer clustFP : cluster ){
                        BigDecimal distFPClustFP = (JaccardDistanceMatrix.get(fp)).get(clustFP);
                        if  (distFPClustFP.compareTo(maxDistance)>0){
                            maxDistance = distFPClustFP;
                        }
                    }
                    maxDistanceByCluster.add(maxDistance);
                }

                //  Having the list of the fartherst freq patter of each cluster,
                // now it chooses the smalles of them, the one with the smaller distance
                BigDecimal shortestDistance=maxDistanceByCluster.get(0);
                int assignToCluster = 0;
                for (int i=0;i<maxDistanceByCluster.size();i++){
                    //System.out.println("Shortest: " + shortestDistance);
                    //System.out.println("maxDistanceByCluster (i) : "+maxDistanceByCluster.get(i));
                    if (maxDistanceByCluster.get(i).compareTo(shortestDistance) < 0 ){
                        shortestDistance = maxDistanceByCluster.get(i);
                        assignToCluster =i;
                    }
                }

                // It uses a threshold of .95 to choose what patters to relate as Context indiators
                if (shortestDistance.compareTo(BigDecimal.valueOf(0.95)) < 0 ){
                    ArrayList<Integer> listOfFQInCluster = clusters.get(assignToCluster);
                    listOfFQInCluster.add(fp);
                    clusters.set(assignToCluster,listOfFQInCluster);
                }else{
                    ArrayList<Integer> newItem = new ArrayList<Integer>();
                    newItem.add(fp);
                    clusters.add(newItem);
                    //System.out.println("Added Freq Pattern " + fp + " to cluster : " + (clusters.size()-1));
                }
            }
        }
    }

    /*
        convertToAuthorNames: helper function that transforms a list of author ids into
        a String with author names
     */
    public String convertToAuthorNames(ArrayList<Integer> listOfAuthors){
        String output="";
        output = listOfAuthors.stream().map(i -> authorHash.get(i)).collect(Collectors.toList()).toString();
        return output;
    }

    /*
        getSemanticallySimilarPatterns: get the list of related frequent patters (the rest of the ones in the same
        cluster) and create a streing of the author names for them, to be included as Semantically Similar Patterns
     */
    public String getSemanticallySimilarPatterns(ArrayList<Integer>  cluster, Integer currentFrqPattern){
        String output ="\n";
        HashMap<BigDecimal,Integer> listOfClosestPatterns =  new HashMap<BigDecimal,Integer>();
        ArrayList<Integer> allSimSemanticPtrns = new ArrayList<Integer>();
        BigDecimal shortestDistance = new BigDecimal(1.0);
        Integer SemtSimPattern = -1;
        for (Integer clusterFrqPattern: cluster){
            if (clusterFrqPattern != currentFrqPattern){
                listOfClosestPatterns.put(JaccardDistanceMatrix.get(clusterFrqPattern).get(currentFrqPattern),
                        clusterFrqPattern);
                allSimSemanticPtrns.add(clusterFrqPattern);
            }
        }
        ArrayList<BigDecimal> listOfDistances =  new ArrayList<BigDecimal> (listOfClosestPatterns.keySet());
        Collections.sort(listOfDistances);
        ArrayList<Integer> finalSemPatternList =new  ArrayList<Integer> ();
        for (int i=0; i<listOfDistances.size();i++){
            finalSemPatternList.add(listOfClosestPatterns.get(listOfDistances.get(i)));
            if (i==3) {
                break;
            }
        }

        for (Integer ptr:allSimSemanticPtrns){
            output =  convertToAuthorNames( freqPatterns.get(ptr)) +"\n";
        }
        return output;
    }

    /*
        getTransactionTitlesForFrqPattern: helper function to print 3 titles by where the freq pattern of authors
        appear
     */
    public String getTransactionTitlesForFrqPattern(ArrayList<Integer> listOfTrans){
        List newList = listOfTrans.subList(0,3);
        String output="";
        output = newList.stream().map(i -> titlesHash.get(i)+"\n").collect(Collectors.toList()).toString();
        return output;

    }

    /*
        Loops through clusters and freq patterns of each cluster and produces the output to the
        pattern_annotations.txt file
     */
    public void createOutputAnnotations(){

        FileOutputStream fos;
        try {
            fos= new FileOutputStream("pattern_annotations.txt");
        } catch (FileNotFoundException e) {
            System.err.println("Could not open Output file:" + e.getMessage());
            return;
        }
        try {
            OutputStreamWriter osw;
            osw =  new OutputStreamWriter(fos, StandardCharsets.ISO_8859_1) ;
            for (ArrayList<Integer> listOfFrqPatterns : clusters){
                for (Integer frequentPattern : listOfFrqPatterns){
                    // Frequent Pattern is a list of Auhor Ids
                    osw.write("Pattern : " + convertToAuthorNames(freqPatterns.get(frequentPattern)) +"\n");
                    HashSet<Integer> setOfTrans =  freqPatternsByTrans.get(frequentPattern);
                    osw.write("Transactions: " + getTransactionTitlesForFrqPattern( new ArrayList<Integer>(setOfTrans)) +"\n");
                    osw.write("Semantically Similar Patterns: " + getSemanticallySimilarPatterns(listOfFrqPatterns,frequentPattern) +"\n");
                    osw.write("********************************************************************************"+"\n");
                    osw.write("\n");
                }
            }
            osw.close();
        } catch (IOException e) {
            System.err.println("Could not Close Output file:" + e.getMessage());
            return;
        }
    }

    public static void main(String[] args) {

        DblpCalcDistance currentdblp = new DblpCalcDistance();

        System.out.println("Upload Titles Array");
        currentdblp.uploadTitlesArray();
        System.out.println("Upload Authors Array");
        currentdblp.uploadAuthorsArray();
        System.out.println("Load Frequent Patterns To Array");
        currentdblp.loadFreqPattToArray();
        System.out.println("Load Transactions to Array");
        currentdblp.loadTransactionstoArray();
        System.out.println("Set List of Frequent Patterns with their transaction ids");
        currentdblp.setReqPatternsByTrans();
        System.out.println("Calculate Jaccard Distance");
        currentdblp.calculateJaccardDistance();
        System.out.println("Run One Pass micro cluster algorithm ");
        currentdblp.makeClusters();
        System.out.println("Create Output Annotations");
        currentdblp.createOutputAnnotations();
        System.out.println("DONE");
    }

}

