package com.john.webapp.dto;

import java.time.LocalDate;

public class ChangeRequestDto {

    private Long id;
    private Long estimateId;
    private String clientEmail;
    private String requestText;
    private LocalDate requestDate;
    private String status;

    public ChangeRequestDto() {}

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }
    public Long getEstimateId()                { return estimateId; }
    public void setEstimateId(Long v)          { this.estimateId = v; }
    public String getClientEmail()             { return clientEmail; }
    public void setClientEmail(String v)       { this.clientEmail = v; }
    public String getRequestText()             { return requestText; }
    public void setRequestText(String v)       { this.requestText = v; }
    public LocalDate getRequestDate()          { return requestDate; }
    public void setRequestDate(LocalDate v)    { this.requestDate = v; }
    public String getStatus()                  { return status; }
    public void setStatus(String v)            { this.status = v; }
}