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

    private static class Transaction {

        @JsonProperty("id")
        private String id;

        @JsonProperty("total_order")
        private int totalOrder;

        @JsonProperty("data_collection_order")
        private int dataCollectionOrder;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getTotalOrder() {
            return totalOrder;
        }

        public void setTotalOrder(int totalOrder) {
            this.totalOrder = totalOrder;
        }

        public int getDataCollectionOrder() {
            return dataCollectionOrder;
        }

        public void setDataCollectionOrder(int dataCollectionOrder) {
            this.dataCollectionOrder = dataCollectionOrder;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Transaction that = (Transaction) o;
            return totalOrder == that.totalOrder && dataCollectionOrder == that.dataCollectionOrder && Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, totalOrder, dataCollectionOrder);
        }

        @Override
        public String toString() {
            return "Transaction{" +
                    "id='" + id + '\'' +
                    ", totalOrder=" + totalOrder +
                    ", dataCollectionOrder=" + dataCollectionOrder +
                    '}';
        }

    }

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
