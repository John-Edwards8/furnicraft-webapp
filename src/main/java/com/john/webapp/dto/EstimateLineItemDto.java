package com.john.webapp.dto;

import java.math.BigDecimal;

public class EstimateLineItemDto {

    private Long estimateId;
    private Long itemId;
    private String itemName;
    private BigDecimal unitPrice;
    private Integer amount;
    private BigDecimal lineTotal;
    private CatalogItemDto.Type type;

    public EstimateLineItemDto() {}

    public Long getEstimateId()                    { return estimateId; }
    public void setEstimateId(Long v)              { this.estimateId = v; }
    public Long getItemId()                        { return itemId; }
    public void setItemId(Long v)                  { this.itemId = v; }
    public String getItemName()                    { return itemName; }
    public void setItemName(String v)              { this.itemName = v; }
    public BigDecimal getUnitPrice()               { return unitPrice; }
    public void setUnitPrice(BigDecimal v)         { this.unitPrice = v; }
    public Integer getAmount()                     { return amount; }
    public void setAmount(Integer v)               { this.amount = v; }
    public BigDecimal getLineTotal()               { return lineTotal; }
    public void setLineTotal(BigDecimal v)         { this.lineTotal = v; }
    public CatalogItemDto.Type getType()           { return type; }
    public void setType(CatalogItemDto.Type v)     { this.type = v; }
}