package com.john.webapp.dto;

import java.math.BigDecimal;

public class CatalogItemDto {

    public enum Type { MATERIAL, ACCESSORY, PROCESS }

    private Long id;
    private String name;
    private String vendorCode;
    private BigDecimal price;
    private Type type;

    public CatalogItemDto() {}

    public Long getId()            { return id; }
    public void setId(Long id)     { this.id = id; }
    public String getName()        { return name; }
    public void setName(String n)  { this.name = n; }
    public String getVendorCode()  { return vendorCode; }
    public void setVendorCode(String v) { this.vendorCode = v; }
    public BigDecimal getPrice()   { return price; }
    public void setPrice(BigDecimal p) { this.price = p; }
    public Type getType()          { return type; }
    public void setType(Type t)    { this.type = t; }
}