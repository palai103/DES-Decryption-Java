package it.unifi.desdecryption;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

public class Task implements Callable<Boolean> {
    private ExecutorDecrypter executor;
    private final DesEncrypter encrypter;
    private final int startIndex;
    private final int endIndex;
    private final String hashedKey;
    private final AtomicBoolean outcome;


    public Task(ExecutorDecrypter executor, DesEncrypter encrypter, int startIndex, int endIndex, String hashedKey, AtomicBoolean outcome) {
        this.executor = executor;
        this.encrypter = encrypter;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.hashedKey = hashedKey;
        this.outcome = outcome;
    }

    @Override
    public Boolean call() {
        for(int index=startIndex; index<endIndex; index++) {
            String currentWord = executor.getDictionary().get(index);
            SecretKey keyFromString = null;
            try {
                keyFromString = new SecretKeySpec(currentWord.getBytes(), 0, currentWord.getBytes().length, "DES");
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
            String cryptedPassword = null;
            try {
                encrypter.initEcipher(keyFromString);
                cryptedPassword = encrypter.encrypt(currentWord);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (cryptedPassword.equals(hashedKey)) {
                outcome.set(true);
                System.out.println("Password found: " + currentWord);
                return true;
            }
            if(outcome.get()) {
                break;
            }
        }
        return false;
    }
}
