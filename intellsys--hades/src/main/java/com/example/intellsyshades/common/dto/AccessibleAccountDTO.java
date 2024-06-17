package com.example.intellsyshades.common.dto;

public class AccessibleAccountDTO {
  private Long customerClientId;
  private String customerClientName;
  private Long managerId;
  private String managerName;

  public AccessibleAccountDTO() {}

  public AccessibleAccountDTO(Long customerClientId, String customerClientName, Long managerId, String managerName) {
    this.customerClientId = customerClientId;
    this.customerClientName = customerClientName;
    this.managerId = managerId;
    this.managerName = managerName;
  }

  public Long getCustomerClientId() {
    return customerClientId;
  }

  public void setCustomerClientId(Long customerClientId) {
    this.customerClientId = customerClientId;
  }

  public String getCustomerClientName() {
    return customerClientName;
  }

  public void setCustomerClientName(String customerClientName) {
    this.customerClientName = customerClientName;
  }

  public Long getManagerId() {
    return managerId;
  }

  public void setManagerId(Long managerId) {
    this.managerId = managerId;
  }

  public String getManagerName() {
    return managerName;
  }

  public void setManagerName(String managerName) {
    this.managerName = managerName;
  }
}
