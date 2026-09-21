CREATE UNIQUE INDEX uk_one_active_bill_per_table
    ON table_bills (table_master_id)
    WHERE status IN ('OPEN', 'BILL_REQUESTED');