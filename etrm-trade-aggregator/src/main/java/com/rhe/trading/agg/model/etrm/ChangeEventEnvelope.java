package com.rhe.trading.agg.model.etrm;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class ChangeEventEnvelope<T> {

    @JsonProperty("before")
    private T before;

    @JsonProperty("after")
    private T after;

    @JsonProperty("op")
    private String op;

    @JsonProperty("ts_ms")
    private long timestamp;

    @JsonProperty("transaction")
    private Transaction transaction;

    public T getBefore() {
        return before;
    }

    public void setBefore(T before) {
        this.before = before;
    }

    public T getAfter() {
        return after;
    }

    public void setAfter(T after) {
        this.after = after;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeEventEnvelope<?> that = (ChangeEventEnvelope<?>) o;
        return timestamp == that.timestamp && Objects.equals(before, that.before) && Objects.equals(after, that.after) && Objects.equals(op, that.op) && Objects.equals(transaction, that.transaction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(before, after, op, timestamp, transaction);
    }

    @Override
    public String toString() {
        return "ChangeEventEnvelope{" +
                "before=" + before +
                ", after=" + after +
                ", op='" + op + '\'' +
                ", timestamp=" + timestamp +
                ", transaction=" + transaction +
                '}';
    }

}
