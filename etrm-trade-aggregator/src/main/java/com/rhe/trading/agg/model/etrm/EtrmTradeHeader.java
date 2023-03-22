package com.rhe.trading.agg.model.etrm;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class EtrmTradeHeader {

    @JsonProperty("trade_id")
    private int tradeId;
    @JsonProperty("start_date")
    private int startDate;
    @JsonProperty("end_date")
    private int endDate;
    @JsonProperty("execution_timestamp")
    private long executionTimestamp;
    @JsonProperty("trade_type_id")
    private int tradeTypeId;

    public int getTradeId() {
        return tradeId;
    }

    public void setTradeId(int tradeId) {
        this.tradeId = tradeId;
    }

    public int getStartDate() {
        return startDate;
    }

    public void setStartDate(int startDate) {
        this.startDate = startDate;
    }

    public int getEndDate() {
        return endDate;
    }

    public void setEndDate(int endDate) {
        this.endDate = endDate;
    }

    public long getExecutionTimestamp() {
        return executionTimestamp;
    }

    public void setExecutionTimestamp(long executionTimestamp) {
        this.executionTimestamp = executionTimestamp;
    }

    public int getTradeTypeId() {
        return tradeTypeId;
    }

    public void setTradeTypeId(int tradeTypeId) {
        this.tradeTypeId = tradeTypeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EtrmTradeHeader that = (EtrmTradeHeader) o;
        return tradeId == that.tradeId && startDate == that.startDate && endDate == that.endDate && executionTimestamp == that.executionTimestamp && tradeTypeId == that.tradeTypeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tradeId, startDate, endDate, executionTimestamp, tradeTypeId);
    }

    @Override
    public String toString() {
        return "EtrmTradeHeader{" +
                "tradeId=" + tradeId +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", executionTimestamp=" + executionTimestamp +
                ", tradeTypeId=" + tradeTypeId +
                '}';
    }

}
