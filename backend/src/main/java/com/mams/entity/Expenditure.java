package com.mams.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenditures")
public class Expenditure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "base_id")
    private Long baseId;

    @Column(name = "equipment_type_id")
    private Long equipmentTypeId;

    private Long quantity;
    
    @Column(name = "expenditure_date")
    private LocalDate expenditureDate;
    
    private String category;
    
    @Column(name = "reference_number")
    private String referenceNumber;
    
    private String notes;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBaseId() { return baseId; }
    public void setBaseId(Long baseId) { this.baseId = baseId; }
    public Long getEquipmentTypeId() { return equipmentTypeId; }
    public void setEquipmentTypeId(Long equipmentTypeId) { this.equipmentTypeId = equipmentTypeId; }
    public Long getQuantity() { return quantity; }
    public void setQuantity(Long quantity) { this.quantity = quantity; }
    public LocalDate getExpenditureDate() { return expenditureDate; }
    public void setExpenditureDate(LocalDate expenditureDate) { this.expenditureDate = expenditureDate; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
