package it.unifi.desdecryption;

import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@State(Scope.Benchmark)
public class ExecutorDecrypter {
    private List<String> dictionary;
    private String hashedKey;
    private List<Task> tasksToExecute = new ArrayList<>();
    private AtomicBoolean outcome = new AtomicBoolean(false);
    private int numOfThreads;
    private int chunkSize;
    private int dictIndex = 0;

    public ExecutorDecrypter() {}

    public ExecutorDecrypter(String hashedKey, List<String> dictionary, int numOfThreads) {
        this.hashedKey = hashedKey;
        this.dictionary = dictionary;
        this.numOfThreads = numOfThreads;
    }

    public void bruteForceDecryption() {
        try {
            submitTasks();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ExecutorService executor = Executors.newFixedThreadPool(numOfThreads);
        //System.out.println("Executing tasks!");
        try {
            executor.invokeAll(tasksToExecute);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdownNow();
    }

    public void submitTasks() throws Exception {
        chunkSize = dictionary.size()/numOfThreads;
        System.out.println("Chunk size is: " + chunkSize);

        for(int taskIndex=0; taskIndex<numOfThreads; taskIndex++) {
            DesEncrypter encrypter = new DesEncrypter();
            if(dictionary.size()-(dictIndex+chunkSize)>chunkSize) {
                //System.out.println("Submitting task with boundaries: " + dictIndex + ", " + (dictIndex+chunkSize-1));
                tasksToExecute.add(new Task(this, encrypter, dictIndex, dictIndex+chunkSize , hashedKey, outcome));
                dictIndex += chunkSize;
            } else {
                chunkSize = dictionary.size()-dictIndex;
                //System.out.println("Submitting task with boundaries: " + dictIndex + ", " + (dictIndex+chunkSize-1));
                tasksToExecute.add(new Task(this, encrypter, dictIndex, dictIndex+chunkSize , hashedKey, outcome));
            }
        }

        //System.out.println("Task ready to be execute...");
    }

    public List<String> getDictionary() {
        return dictionary;
    }
}
