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

STEP #1 Clone Git repository: https://github.com/mariahuipe/CourseProject/

SETP #2: 
  Option # A: Run the Data Pre-processing of the source data set.
  1. Download data set 
       a. Go here: https://dblp.org/xml/release/
       b. Download to the file called: 	dblp-2020-11-01.xml.gz . Download to same directory where you clone my git repository
       

  1. java -Xmx8G -cp mmdb-2019-04-29.jar:. ParseDblp    > inputfp_100K.txt
  2. java -jar spmf.jar run FPClose inputfp_100K.txt outputfp.txt 0%
  3. java DblpCalcDistance
