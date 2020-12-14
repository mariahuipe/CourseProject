# CourseProject

Maria Fernandez- Final Project. 

Pre-requirements:

- You need Java 1.8, I specifically used java version "1.8.0_271"
  If it is not installed in your system, download from:
  https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html

  I include the compile versions of the two java programs I developed, but if needed to compile, please do the following:

  javac -cp mmdb-2019-04-29.jar ParseDblp.java

  javac DblpCalcDistance.java

  The result will be the ParseDblp.class  & DblpCalcDistance.class files

**************************************************************************************************************************

STEP #1 Clone Git repository:
      https://github.com/mariahuipe/CourseProject/

STEP #2:   
  
     OPTION A: Run the Data Pre-processing of the source data set.
     
     1. Download data set 
           a. Go here: https://dblp.org/xml/release/
           b. Download to the file called: 	dblp-2020-11-01.xml.gz . Download to same directory where you clone my git repository.
           c. If in Unix/OS: unzip the gz file: 
                gunzip dblp-2020-11-01.xml.gz 
             
           This will create the dblp-2020-11-01.xml which is the input of the Prep-processing process.
           d. Make sure you have the dblp.dtd which was downloaded from my Git repository
           
      2. Run the Pre-Processing process:
         java -Xmx8G -cp mmdb-2019-04-29.jar:. ParseDblp  > inputfp_100K.txt
         
         This step will produced the following files:
            - authors.txt
            - inputfp_100K.txt
            - titles.txt
         
         NOTE: The xml file is pretty big. I was not able to process this file from my Windows laptop due to lack of memory but I was 
         able to do it from my Mac, it still takes a few minutes. This is why I am putting this step as an optional step and I am including 
         option B which is skipping this step and take the files that have been produced by the pre-processing.
         
    OPTION B: Copy pre-processed files from the back up directory:
      1. Do the following:
          cp OUT_FILES_BK/authors.txt .
          cp OUT_FILES_BK/inputfp_100K.txt .
          cp OUT_FILES_BK/titles.txt .


STEP #3: Run the SPMF library to create Closed Frequent Patterns from the inputfp_100K.txt file:
      1. Run the following:
          java -jar spmf.jar run FPClose inputfp_100K.txt outputfp.txt 0%
          
          Results: you should see the file outputfp.txt
        
STEP #4: Run the One-Pass Micrcocluster algorithm based in the paper description:
      1. Do the following: 
          java DblpCalcDistance
          
          Results: pattern_annotations.txt will be created. 
          
          
