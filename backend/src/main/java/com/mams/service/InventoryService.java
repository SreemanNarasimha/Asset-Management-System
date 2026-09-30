package com.mams.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;

@Service
public class InventoryService {

    private final JdbcTemplate jdbcTemplate;

    public InventoryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long available(Long baseId, Long equipmentTypeId) {
        String query = """
            SELECT
                IFNULL((SELECT quantity FROM inventory_opening_balances WHERE base_id = ? AND equipment_type_id = ?), 0) +
                IFNULL((SELECT SUM(quantity) FROM purchases WHERE base_id = ? AND equipment_type_id = ?), 0) +
                IFNULL((SELECT SUM(quantity) FROM transfers WHERE destination_base_id = ? AND equipment_type_id = ? AND status = 'COMPLETED'), 0) -
                IFNULL((SELECT SUM(quantity) FROM transfers WHERE source_base_id = ? AND equipment_type_id = ? AND status = 'COMPLETED'), 0) -
                IFNULL((SELECT SUM(quantity) FROM expenditures WHERE base_id = ? AND equipment_type_id = ?), 0) -
                IFNULL((SELECT SUM(quantity) FROM assignments WHERE base_id = ? AND equipment_type_id = ? AND status = 'ACTIVE'), 0)
            AS available_qty
            """;

        Long result = jdbcTemplate.queryForObject(query, Long.class, 
            baseId, equipmentTypeId,
            baseId, equipmentTypeId,
            baseId, equipmentTypeId,
            baseId, equipmentTypeId,
            baseId, equipmentTypeId,
            baseId, equipmentTypeId
        );
        return Optional.ofNullable(result).orElse(0L);
    }
    public Map<String, Object> getDashboardMetrics(Long baseId, Long equipmentTypeId, String dateStr) {
        String baseFilter = baseId == null ? "1=1" : "base_id = " + baseId;
        String destBaseFilter = baseId == null ? "1=1" : "destination_base_id = " + baseId;
        String srcBaseFilter = baseId == null ? "1=1" : "source_base_id = " + baseId;
        String eqFilter = equipmentTypeId == null ? "1=1" : "equipment_type_id = " + equipmentTypeId;

        // Date filter
        String dateFilterPurchases = (dateStr == null || dateStr.trim().isEmpty()) ? "1=1" : "purchase_date <= '" + dateStr + "'";
        String dateFilterTransfers = (dateStr == null || dateStr.trim().isEmpty()) ? "1=1" : "transfer_date <= '" + dateStr + "'";
        String dateFilterExpenditures = (dateStr == null || dateStr.trim().isEmpty()) ? "1=1" : "expenditure_date <= '" + dateStr + "'";
        String dateFilterAssignments = (dateStr == null || dateStr.trim().isEmpty()) ? "1=1" : "assignment_date <= '" + dateStr + "'";

        String query = """
            SELECT
                IFNULL((SELECT SUM(quantity) FROM inventory_opening_balances WHERE %s AND %s), 0) as openingBalance,
                IFNULL((SELECT SUM(quantity) FROM purchases WHERE %s AND %s AND %s), 0) as purchases,
                IFNULL((SELECT SUM(quantity) FROM transfers WHERE %s AND %s AND %s), 0) as transfersIn,
                IFNULL((SELECT SUM(quantity) FROM transfers WHERE %s AND %s AND %s), 0) as transfersOut,
                IFNULL((SELECT SUM(quantity) FROM expenditures WHERE %s AND %s AND %s), 0) as expenditures,
                IFNULL((SELECT SUM(quantity) FROM assignments WHERE %s AND %s AND %s), 0) as assignments
            """.formatted(
                baseFilter, eqFilter, 
                baseFilter, eqFilter, dateFilterPurchases,
                destBaseFilter, eqFilter, dateFilterTransfers,
                srcBaseFilter, eqFilter, dateFilterTransfers,
                baseFilter, eqFilter, dateFilterExpenditures,
                baseFilter, eqFilter, dateFilterAssignments
            );

        Map<String, Object> result = jdbcTemplate.queryForMap(query);
        long opening = ((Number) result.get("openingBalance")).longValue();
        long purchases = ((Number) result.get("purchases")).longValue();
        long transIn = ((Number) result.get("transfersIn")).longValue();
        long transOut = ((Number) result.get("transfersOut")).longValue();
        long expenditures = ((Number) result.get("expenditures")).longValue();
        long assignments = ((Number) result.get("assignments")).longValue();

        long netMovement = purchases + transIn - transOut;
        long closing = opening + netMovement - expenditures;

        return Map.of(
            "openingBalance", opening,
            "purchases", purchases,
            "transfersIn", transIn,
            "transfersOut", transOut,
            "netMovement", netMovement,
            "closingBalance", closing,
            "available", closing - assignments,
            "assigned", assignments,
            "expended", expenditures,
            "transfers", transIn + transOut
        );
    }

    public java.util.List<Map<String, Object>> getInventoryGrid(Long baseId, Long equipmentTypeId, String dateStr) {
        String baseFilter = baseId == null ? "1=1" : "b.id = " + baseId;
        String eqFilter = equipmentTypeId == null ? "1=1" : "e.id = " + equipmentTypeId;

        // Date filters for subqueries
        String dateFilterPurchases = (dateStr == null || dateStr.trim().isEmpty()) ? "1=1" : "purchase_date <= '" + dateStr + "'";
        String dateFilterTransfers = (dateStr == null || dateStr.trim().isEmpty()) ? "1=1" : "transfer_date <= '" + dateStr + "'";
        String dateFilterExpenditures = (dateStr == null || dateStr.trim().isEmpty()) ? "1=1" : "expenditure_date <= '" + dateStr + "'";
        String dateFilterAssignments = (dateStr == null || dateStr.trim().isEmpty()) ? "1=1" : "assignment_date <= '" + dateStr + "'";

        String query = """
            SELECT
                b.id as baseId,
                b.name as baseName,
                e.id as equipmentTypeId,
                e.name as equipmentName,
                e.unit as unit,
                IFNULL((SELECT SUM(quantity) FROM inventory_opening_balances iob WHERE iob.base_id = b.id AND iob.equipment_type_id = e.id), 0) as openingBalance,
                IFNULL((SELECT SUM(quantity) FROM purchases p WHERE p.base_id = b.id AND p.equipment_type_id = e.id AND %s), 0) as purchases,
                IFNULL((SELECT SUM(quantity) FROM transfers t WHERE t.destination_base_id = b.id AND t.equipment_type_id = e.id AND t.status = 'COMPLETED' AND %s), 0) as transfersIn,
                IFNULL((SELECT SUM(quantity) FROM transfers t WHERE t.source_base_id = b.id AND t.equipment_type_id = e.id AND t.status = 'COMPLETED' AND %s), 0) as transfersOut,
                IFNULL((SELECT SUM(quantity) FROM expenditures ex WHERE ex.base_id = b.id AND ex.equipment_type_id = e.id AND %s), 0) as expended,
                IFNULL((SELECT SUM(quantity) FROM assignments a WHERE a.base_id = b.id AND a.equipment_type_id = e.id AND a.status = 'ACTIVE' AND %s), 0) as assigned
            FROM bases b
            CROSS JOIN equipment_types e
            WHERE %s AND %s
            """.formatted(
                dateFilterPurchases, dateFilterTransfers, dateFilterTransfers, dateFilterExpenditures, dateFilterAssignments,
                baseFilter, eqFilter
            );

        java.util.List<Map<String, Object>> rows = jdbcTemplate.queryForList(query);
        
        for (Map<String, Object> row : rows) {
            long opening = ((Number) row.get("openingBalance")).longValue();
            long purchases = ((Number) row.get("purchases")).longValue();
            long transIn = ((Number) row.get("transfersIn")).longValue();
            long transOut = ((Number) row.get("transfersOut")).longValue();
            long expended = ((Number) row.get("expended")).longValue();
            long assigned = ((Number) row.get("assigned")).longValue();

            long netMovement = purchases + transIn - transOut;
            long closing = opening + netMovement - expended;
            long available = closing - assigned;

            row.put("closingBalance", closing);
            row.put("available", available);
        }

        return rows;
    }
}
