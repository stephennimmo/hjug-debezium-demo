package com.rhe.trading.agg.model.etrm;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EtrmTransaction {

    @JsonProperty("status")
    private String status;

    @JsonProperty("id")
    private String id;

    @JsonProperty("event_count")
    private int eventCount;

    @JsonProperty("data_collections")
    private List<DataCollection> dataCollections = new ArrayList<>();

    @JsonProperty("ts_ms")
    private long timestamp;

    public static class DataCollection {

        @JsonProperty("data_collection")
        private String dataCollection;

        @JsonProperty("event_count")
        private int eventCount;

        public String getDataCollection() {
            return dataCollection;
        }

        public void setDataCollection(String dataCollection) {
            this.dataCollection = dataCollection;
        }

        public int getEventCount() {
            return eventCount;
        }

        public void setEventCount(int eventCount) {
            this.eventCount = eventCount;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DataCollection that = (DataCollection) o;
            return eventCount == that.eventCount && Objects.equals(dataCollection, that.dataCollection);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dataCollection, eventCount);
        }

        @Override
        public String toString() {
            return "DataCollection{" +
                    "dataCollection='" + dataCollection + '\'' +
                    ", eventCount=" + eventCount +
                    '}';
        }

    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getEventCount() {
        return eventCount;
    }

    public void setEventCount(int eventCount) {
        this.eventCount = eventCount;
    }

    public List<DataCollection> getDataCollections() {
        return dataCollections;
    }

    public void setDataCollections(List<DataCollection> dataCollections) {
        this.dataCollections = dataCollections;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EtrmTransaction that = (EtrmTransaction) o;
        return eventCount == that.eventCount && timestamp == that.timestamp && Objects.equals(status, that.status) && Objects.equals(id, that.id) && Objects.equals(dataCollections, that.dataCollections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, id, eventCount, dataCollections, timestamp);
    }

    @Override
    public String toString() {
        return "EtrmTransaction{" +
                "status='" + status + '\'' +
                ", id='" + id + '\'' +
                ", eventCount=" + eventCount +
                ", dataCollections=" + dataCollections +
                ", timestamp=" + timestamp +
                '}';
    }

}
