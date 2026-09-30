package com.mams.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_base_id")
    private Long sourceBaseId;

    @Column(name = "destination_base_id")
    private Long destinationBaseId;

    @Column(name = "equipment_type_id")
    private Long equipmentTypeId;

    private Long quantity;
    private String status;

    @Column(name = "transfer_date")
    private LocalDate transferDate;

    @Column(name = "reference_number")
    private String referenceNumber;

    private String notes;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSourceBaseId() { return sourceBaseId; }
    public void setSourceBaseId(Long sourceBaseId) { this.sourceBaseId = sourceBaseId; }
    public Long getDestinationBaseId() { return destinationBaseId; }
    public void setDestinationBaseId(Long destinationBaseId) { this.destinationBaseId = destinationBaseId; }
    public Long getEquipmentTypeId() { return equipmentTypeId; }
    public void setEquipmentTypeId(Long equipmentTypeId) { this.equipmentTypeId = equipmentTypeId; }
    public Long getQuantity() { return quantity; }
    public void setQuantity(Long quantity) { this.quantity = quantity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getTransferDate() { return transferDate; }
    public void setTransferDate(LocalDate transferDate) { this.transferDate = transferDate; }
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
