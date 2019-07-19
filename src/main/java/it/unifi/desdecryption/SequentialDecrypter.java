package it.unifi.desdecryption;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.List;

@State(Scope.Benchmark)
public class SequentialDecrypter {
    private List<String> dictionary;

    public SequentialDecrypter(List<String> dictionary) {
        this.dictionary = dictionary;
    }

    public void bruteForceDecryption(String hashedKey) {
        for(int index=0; index<dictionary.size(); index++) {
            String word = dictionary.get(index);
            SecretKey keyFromString = new SecretKeySpec(word.getBytes(), 0, word.getBytes().length, "DES");
            DesEncrypter encrypter = null;
            try {
                encrypter = new DesEncrypter();
                encrypter.initEcipher(keyFromString);
            } catch (Exception e) {
                System.out.println("Something wrong while creating the encrypter.");
            }
            try {
                String encryptWord = encrypter.encrypt(word);
                if(encryptWord.equals(hashedKey)) {
                    System.out.println("Password found: " + word);
                    break;
                }
            } catch (Exception e) {}
        }
    }

    public ArrayList<Long> bruteForceDecryption(String firstPsw, String secondPsw) {
        ArrayList<Long> durationTimes = new ArrayList<>();
        long time;

        long startTime = System.nanoTime();
        for(int index=0; index<dictionary.size(); index++) {
            String word = dictionary.get(index);
            SecretKey keyFromString = new SecretKeySpec(word.getBytes(), 0, word.getBytes().length, "DES");
            DesEncrypter encrypter = null;
            try {
                encrypter = new DesEncrypter();
                encrypter.initEcipher(keyFromString);
            } catch (Exception e) {
                System.out.println("Something wrong while creating the encrypter.");
            }
            try {
                String encryptWord = encrypter.encrypt(word);
                if(encryptWord.equals(firstPsw)) {
                    System.out.println("Password found: " + word);
                    time = System.nanoTime() - startTime;
                    System.out.println("Sequential Brute-Force attack required " + time + " nanoSec");
                    durationTimes.add(time);
                }
                else if(encryptWord.equals(secondPsw)) {
                    System.out.println("Password found: " + word);
                    time = System.nanoTime() - startTime;
                    System.out.println("Sequential Brute-Force attack required " + time + " nanoSec");
                    durationTimes.add(time);
                    break;
                }
            } catch (Exception e) {}
        }
        return durationTimes;
    }
}
