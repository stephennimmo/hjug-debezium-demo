package com.rhe.trading.agg.model.etrm;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Objects;

public class EtrmTradeLeg {

    @JsonProperty("trade_leg_id")
    private int tradeLegId;

    @JsonProperty("trade_id")
    private int tradeId;

    @JsonProperty("payer_id")
    private int payerId;

    @JsonProperty("receiver_id")
    private int receiverId;

    @JsonProperty("commodity_id")
    private int commodity_id;

    @JsonProperty("location_id")
    private int locationId;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("price_currency_id")
    private int priceCurrencyId;

    @JsonProperty("quantity")
    private BigDecimal quantity;

    @JsonProperty("quantity_uom_id")
    private int quantityUomId;

    public int getTradeLegId() {
        return tradeLegId;
    }

    public void setTradeLegId(int tradeLegId) {
        this.tradeLegId = tradeLegId;
    }

    public int getTradeId() {
        return tradeId;
    }

    public void setTradeId(int tradeId) {
        this.tradeId = tradeId;
    }

    public int getPayerId() {
        return payerId;
    }

    public void setPayerId(int payerId) {
        this.payerId = payerId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public int getCommodity_id() {
        return commodity_id;
    }

    public void setCommodity_id(int commodity_id) {
        this.commodity_id = commodity_id;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getPriceCurrencyId() {
        return priceCurrencyId;
    }

    public void setPriceCurrencyId(int priceCurrencyId) {
        this.priceCurrencyId = priceCurrencyId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public int getQuantityUomId() {
        return quantityUomId;
    }

    public void setQuantityUomId(int quantityUomId) {
        this.quantityUomId = quantityUomId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EtrmTradeLeg that = (EtrmTradeLeg) o;
        return tradeLegId == that.tradeLegId && tradeId == that.tradeId && payerId == that.payerId && receiverId == that.receiverId && commodity_id == that.commodity_id && locationId == that.locationId && priceCurrencyId == that.priceCurrencyId && quantityUomId == that.quantityUomId && Objects.equals(price, that.price) && Objects.equals(quantity, that.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tradeLegId, tradeId, payerId, receiverId, commodity_id, locationId, price, priceCurrencyId, quantity, quantityUomId);
    }

    @Override
    public String toString() {
        return "EtrmTradeLeg{" +
                "tradeLegId=" + tradeLegId +
                ", tradeId=" + tradeId +
                ", payerId=" + payerId +
                ", receiverId=" + receiverId +
                ", commodity_id=" + commodity_id +
                ", locationId=" + locationId +
                ", price=" + price +
                ", priceCurrencyId=" + priceCurrencyId +
                ", quantity=" + quantity +
                ", quantityUomId=" + quantityUomId +
                '}';
    }

}
