package it.unifi.desdecryption;

import javafx.application.Application;
import javafx.stage.Stage;
import org.openjdk.jmh.annotations.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.util.*;

@State(Scope.Benchmark)
public class TestDecryption extends Application {
    private List<String> dictionaryList = new ArrayList<>();
    private int startNumberOfThreads = 1;
    private int maxNumberOfThreads = 100;
    private int threadsIncrement = 2;
    private int numberOfAttempts = 10;
    private String title = "InitialChunkPsw";
    private static String path;
    private static String dictionaryPath;
    private static String passwordOne;
    private static String passwordTwo;

    /*Final Chunk PSWs*
    /*private String passwordOne = "mara1992";
    private String passwordTwo = "vjht1051";*/

    public void start(Stage stage) throws Exception {
        /*
         *==================================================
         *                     ATTACKS
         *==================================================
         * */

        /*Get the dictionary from file*/
        getDictionary();

        /*============================== Test ==============================*/
        //test();

        /*=========================== First Test ===========================*/
        HashMap<String, ArrayList<Float>> avgSpeedUpResults = startTests();

        /*
         *==================================================
         *                     GRAPHS
         *==================================================
         * */

        Graph graph = new Graph("Speed-Up Evalutation", path);
        graph.setSeriesTitle(passwordOne, "first");
        graph.setSeriesTitle(passwordTwo, "second");

        ArrayList<Float> firstChunkResults = avgSpeedUpResults.get("first");
        ArrayList<Float> secondChunkResults = avgSpeedUpResults.get("second");

        for(int listIndex=0; listIndex<firstChunkResults.size(); listIndex++) {
            graph.addData((threadsIncrement *listIndex)+startNumberOfThreads, firstChunkResults.get(listIndex), "first");
            graph.addData((threadsIncrement *listIndex)+startNumberOfThreads, secondChunkResults.get(listIndex), "second");
        }

        graph.showGraph();
        graph.saveAsPngAndTxt(title, firstChunkResults, secondChunkResults);

        System.out.println("======================== RESULTS ========================");
        System.out.println("First chunk results: " + firstChunkResults);
        System.out.println("Second chunk results: " + secondChunkResults);
        System.out.println("==========================================================");
    }

    public void getDictionary() {
        File dictionary = new File(dictionaryPath);
        BufferedReader dictionaryReader = null;
        try {
            dictionaryReader = new BufferedReader(new FileReader(dictionary));
        }
        catch (FileNotFoundException fnfe) {
            System.out.println("NO SUCH A FILE!");
        }
        dictionaryReader.lines().forEach(line -> {
            dictionaryList.add(line);
        });
    }

    public String getAndCryptRandomPsw(String chunkParameter) throws Exception {
        int chunkPartition = dictionaryList.size()/3;

        String randomPassword = "";
        switch (chunkParameter) {
            case "first": {
                Random rand = new Random();
                int chunkSize = chunkPartition;
                int randomIndex = rand.nextInt(chunkSize);
                randomPassword = dictionaryList.get(randomIndex);
                System.out.println("Password from first chunk: " + randomPassword);
                break;
            }
            case "second": {
                Random rand = new Random();
                int maxChunk = 2 * chunkPartition;
                int minChunk = chunkPartition;
                int randomIndex = rand.nextInt((maxChunk - minChunk) + 1) + minChunk;
                randomPassword = dictionaryList.get(randomIndex);
                System.out.println("Password from second chunk: " + randomPassword);
                break;
            }
            case "third": {
                Random rand = new Random();
                int maxChunk = dictionaryList.size() - 1;
                int minChunk = 2 * chunkPartition;
                int randomIndex = rand.nextInt((maxChunk - minChunk) + 1) + minChunk;
                randomPassword = dictionaryList.get(randomIndex);
                System.out.println("Password from third chunk: " + randomPassword);
                break;
            }
            /*If none chunk parameters are passed*/
            default:
                randomPassword = chunkParameter;
                break;
        }

        SecretKey keyFromString = new SecretKeySpec(randomPassword.getBytes(), 0,
                randomPassword.getBytes().length, "DES");
        DesEncrypter encrypter = new DesEncrypter();
        encrypter.initEcipher(keyFromString);
        return encrypter.encrypt(randomPassword);
    }

    public void test() throws Exception {
        //String password = "Vjht0409";
        String password = "mara1992";
        System.out.println("Number of threads used: " + Runtime.getRuntime().availableProcessors());

        /*Sequential Attack*/
        SequentialDecrypter seqDecr = new SequentialDecrypter(dictionaryList);
        System.out.println("==> Start the sequential Brute Force attack");
        long startTimeSeq = System.nanoTime();
        seqDecr.bruteForceDecryption(password);
        long endTimeSeq = System.nanoTime();

        long durationSeqFirstPsw = endTimeSeq - startTimeSeq;
        System.out.println("Sequential Brute-Force attack required " + durationSeqFirstPsw + " nanoSec");


        SecretKey keyFromString = new SecretKeySpec(password.getBytes(), 0,
                password.getBytes().length, "DES");
        DesEncrypter encrypter = new DesEncrypter();
        encrypter.initEcipher(keyFromString);
        String hashedKey = encrypter.encrypt(password);

        for(int i=0; i<numberOfAttempts; i++)
            speedUpEvaluation(hashedKey, Runtime.getRuntime().availableProcessors(), durationSeqFirstPsw);
    }

    public HashMap<String, ArrayList<Float>> startTests() throws Exception {
        /*=========================== First Test ===========================*/

        float firstChunkSPUsum = 0;
        float secondChunkSPUsum = 0;

        /*Hashmap initialization*/
        HashMap<String, ArrayList<Float>> speedUpResults = new HashMap<>();
        speedUpResults.put("first", new ArrayList<>());
        speedUpResults.put("second", new ArrayList<>());

        System.out.println("=============================================");

        /*Hashing of password from FIRST CHUNK*/
        String firstChunkPsw = getAndCryptRandomPsw(passwordOne);
        System.out.println("First chunk password is: " + firstChunkPsw);

        /*Hashing of password from SECOND CHUNK*/
        String secondChunkPsw = getAndCryptRandomPsw(passwordTwo);
        System.out.println("Second chunk password is: " + secondChunkPsw);

        System.out.println("=============================================");

        System.out.println("==> Start the parallel Brute Force attack");
        SequentialDecrypter seqDecr = new SequentialDecrypter(dictionaryList);
        ArrayList<Long> seqTimes = seqDecr.bruteForceDecryption(firstChunkPsw, secondChunkPsw);
        System.out.println();

        for (int numOfThreads=startNumberOfThreads; numOfThreads<=maxNumberOfThreads; numOfThreads+= threadsIncrement) {
            System.out.println("~~~~~~ NUMBER OF THREADS: " + numOfThreads + " ~~~~~~");

            for (int attempt = 0; attempt< numberOfAttempts; attempt++) {
                /*Accumulate speed-up values for first chunk*/
                firstChunkSPUsum += speedUpEvaluation(firstChunkPsw, numOfThreads, seqTimes.get(0));

                /*Accumulate speed-up values for second chunk*/
                secondChunkSPUsum += speedUpEvaluation(secondChunkPsw, numOfThreads, seqTimes.get(1));
            }
            speedUpResults.get("first").add(firstChunkSPUsum/numberOfAttempts);
            firstChunkSPUsum = 0;
            speedUpResults.get("second").add(secondChunkSPUsum/numberOfAttempts);
            secondChunkSPUsum = 0;
        }

        return speedUpResults;
    }

    public float speedUpEvaluation(String hashedKey, int numThreads, long durationSeq) {
        /*Parallel Attack*/
        ExecutorDecrypter parDecr = new ExecutorDecrypter(hashedKey, dictionaryList, numThreads);
        System.out.println("==> Start the parallel Brute Force attack");
        long startTimePar = System.nanoTime();
        parDecr.bruteForceDecryption();
        long endTimePar = System.nanoTime();

        long durationPar = endTimePar - startTimePar;
        System.out.println("Parallel Brute-Force attack required   " + durationPar + " nanoSec");

        float speedUp = (float)durationSeq/durationPar;
        System.out.println();
        System.out.println("SPEED-UP: " + speedUp);
        System.out.println();
        return speedUp;
    }

    public static void main(String[] argv) {
        path = argv[0];
        dictionaryPath = argv[1];
        passwordOne = argv[2];
        passwordTwo = argv[3];
        launch(argv);
    }
}
