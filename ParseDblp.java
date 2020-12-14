
import org.dblp.mmdb.*;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.*;


@SuppressWarnings("javadoc")
public class ParseDblp {
    HashMap<String,Integer> authorHash = new HashMap<String,Integer>();
    Integer countAuthors = 0;
    ArrayList<String> titlesList =  new ArrayList<String>();

    /*
        addToAuthorHash: Creates hash map authorHash with unique authors and assignd each of the a numeric ID
     */
    public  Integer addToAuthorHash(String author){
        if (authorHash.containsKey(author)){
            return authorHash.get(author);
        }else{
          countAuthors++;
          authorHash.put(author,countAuthors);
          return countAuthors;
        }
    }

    /*
        writeAuthorsToFile: Creates authors.txt with the unique list  of numeric IDs and authors (tab separated)
     */
    public void writeAuthorsToFile(){
        FileOutputStream fos;
        try {
            fos= new FileOutputStream("authors.txt");
        } catch (FileNotFoundException e) {
            System.err.println("Could not open Output file:" + e.getMessage());
            return;
        }
        try {
            OutputStreamWriter osw;
            osw =  new OutputStreamWriter(fos, StandardCharsets.ISO_8859_1) ;
            for (Map.Entry mapElement : authorHash.entrySet()){
                osw.write(mapElement.getValue()  + "\t" + (String)mapElement.getKey() + "\n");

            }
            osw.close();
        } catch (IOException e) {
            System.err.println("Could not Close Output file:" + e.getMessage());
            return;
        }
    }

    /*
        writeTitlesToFile: Creates the titles.txt with titles and their unique ID (tab separated)
     */
    public void writeTitlesToFile(){
        FileOutputStream fos;
        try {
            fos= new FileOutputStream("titles.txt");
        } catch (FileNotFoundException e) {
            System.err.println("Could not open Output file:" + e.getMessage());
            return;
        }
        try {
            OutputStreamWriter osw;
            osw =  new OutputStreamWriter(fos, StandardCharsets.ISO_8859_1) ;
            for (String s : titlesList){
                osw.write(s);

            }
            osw.close();
        } catch (IOException e) {
            System.err.println("Could not Close Output file:" + e.getMessage());
            return;
        }
    }

    /*
        Main processing of the xml file
     */
    public static void main(String[] args) {

        ParseDblp currentdblp = new ParseDblp();

        System.setProperty("entityExpansionLimit", "10000000");

        String dblpXmlFilename =  "dblp-2020-11-01.xml"; // args[0];
        String dblpDtdFilename =  "dblp.dtd" ; // args[1];

        // Use the library mmdb-2019-04-29.jar to parse the xml and load the
        // dblp object of the type RecordDb
        RecordDbInterface dblp;
        try {
            dblp = new RecordDb(dblpXmlFilename, dblpDtdFilename, false);
        }
        catch (final IOException ex) {
            System.err.println("cannot read dblp XML: " + ex.getMessage());
            return;
        }
        catch (final SAXException ex) {
            System.err.println("cannot parse XML: " + ex.getMessage());
            return;
        }
        //System.out.format("MMDB ready: %d publs, %d pers\n\n", dblp.numberOfPublications(), dblp.numberOfPersons());

        java.util.Collection<Publication> allPublications = dblp.getPublications();
        java.util.Collection<BookTitle> allBookTitles = 	dblp.getBookTitles();

        //System.out.println("Starting Loop ");
        int count = 0;
        // Loops through all publications
        for(Publication pub : allPublications){
            try {
                String currentXml = pub.getXml();
                currentXml =   "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + currentXml;
                //System.out.println("XML Publication:" + currentXml);

                // Only Choose the ariticles from 2019 and 2020, to reduce the number
                if ( currentXml.indexOf("2019") != -1 || currentXml.indexOf("2020") != -1) {

                    ArrayList<Integer> transaction = new   ArrayList<Integer>();

                    DocumentBuilderFactory dbf;
                    DocumentBuilder db ;
                    Document doc ;
                    boolean skip = false;
                    dbf = DocumentBuilderFactory.newInstance();
                    db = dbf.newDocumentBuilder();
                    doc = db.parse(new StringBufferInputStream(currentXml));

                    // Extract title and list of authors from xml
                    NodeList list = doc.getElementsByTagName("title");
                    NodeList listAuthor = doc.getElementsByTagName("author");

                    // Only process when there is a title AND and author, otherwise, skip
                    if(list != null && list.getLength() > 0 && listAuthor != null && listAuthor.getLength() > 0) {
                        Element elementTitle = (Element) list.item(0);
                        String titleTextVal = count + "\t" + elementTitle.getFirstChild().getNodeValue() + "\n";
                        currentdblp.titlesList.add(titleTextVal);
                        // Every author foumnd is added to a hash map (to make it unique) and
                        // it is assgined an ID
                        for (int i = 0; i < listAuthor.getLength(); i++) {
                            Element element = (Element) listAuthor.item(i);
                            String textVal = element.getFirstChild().getNodeValue();
                            //System.out.println("Author " +   Integer.toString(i) + " : " +  textVal);
                            transaction.add(currentdblp.addToAuthorHash(textVal));
                        }
                        Collections.sort(transaction);
                        String transactionStr = transaction.toString();
                        transactionStr = transactionStr.replaceAll(",", "");
                        transactionStr = transactionStr.replaceAll("\\[", "");
                        transactionStr = transactionStr.replaceAll("\\]", "");
                        System.out.println(transactionStr);

                        count++;
                    }
                }
                // While there are millions of publicaions, we only use a subset of 100,000
                if (count == 100000)
                    break;

                } catch (Exception e){
                ;
                }
                finally{
                  ;
                }
        }

        // Writes all authors and Ids to authors.txt, separated by \t
        currentdblp.writeAuthorsToFile();
        currentdblp.writeTitlesToFile();

    }
}

