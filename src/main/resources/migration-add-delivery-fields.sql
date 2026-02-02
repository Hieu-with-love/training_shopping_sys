-- Migration: Add delivery fields to trproductorder table
-- Date: 2026-02-02

-- Add order_delivery_address column
ALTER TABLE trproductorder 
ADD COLUMN IF NOT EXISTS order_delivery_address VARCHAR(400);

-- Add order_delivery_date column (format: YYYYMMDD)
ALTER TABLE trproductorder 
ADD COLUMN IF NOT EXISTS order_delivery_date VARCHAR(8);

-- Add comments
COMMENT ON COLUMN trproductorder.order_delivery_address IS 'Delivery address for the order';
COMMENT ON COLUMN trproductorder.order_delivery_date IS 'Delivery date in YYYYMMDD format';
