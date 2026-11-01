package com.campus.lostfound.model;

public interface Claimable {
    boolean verifySecretMark(String providedMark);
    void confirmReceipt();
}