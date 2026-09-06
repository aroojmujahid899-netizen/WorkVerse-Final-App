package com.workverse.app.models;

public class LeaveRequest {
    private String id, userId, employeeName, leaveType, fromDate, toDate, reason, status, role;
    private long timestamp;

    public LeaveRequest() {}

    public LeaveRequest(String userId, String employeeName, String leaveType,
                        String fromDate, String toDate, String reason, String role) {
        this.userId       = userId;
        this.employeeName = employeeName;
        this.leaveType    = leaveType;
        this.fromDate     = fromDate;
        this.toDate       = toDate;
        this.reason       = reason;
        this.role         = role;
        this.status       = "Pending";
        this.timestamp    = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId()           { return id; }
    public void   setId(String v)   { id = v; }

    public String getUserId()           { return userId; }
    public void   setUserId(String v)   { userId = v; }

    public String getEmployeeName()           { return employeeName; }
    public void   setEmployeeName(String v)   { employeeName = v; }

    public String getLeaveType()           { return leaveType; }
    public void   setLeaveType(String v)   { leaveType = v; }

    public String getFromDate()           { return fromDate; }
    public void   setFromDate(String v)   { fromDate = v; }

    public String getToDate()           { return toDate; }
    public void   setToDate(String v)   { toDate = v; }

    public String getReason()           { return reason; }
    public void   setReason(String v)   { reason = v; }

    public String getStatus()           { return status; }
    public void   setStatus(String v)   { status = v; }

    public String getRole()           { return role; }
    public void   setRole(String v)   { role = v; }

    public long getTimestamp()          { return timestamp; }
    public void setTimestamp(long v)    { timestamp = v; }
}
