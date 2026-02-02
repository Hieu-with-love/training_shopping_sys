# Database Migration Instructions

## Apply Order Page Migration

Before running the application with the new Order Page features, you must apply the database migration to add the delivery fields.

### Option 1: Using psql Command Line

```bash
# Connect to your database and run the migration
psql -U your_username -d shopping_sys -f src/main/resources/migration-add-delivery-fields.sql

# Example:
psql -U postgres -d shopping_sys -f src/main/resources/migration-add-delivery-fields.sql
```

### Option 2: Using pgAdmin or Database GUI

1. Open pgAdmin or your preferred PostgreSQL client
2. Connect to your `shopping_sys` database
3. Open the SQL query tool
4. Copy and paste the contents of `src/main/resources/migration-add-delivery-fields.sql`
5. Execute the SQL

### Option 3: Direct SQL Execution

Connect to your database and run:

```sql
-- Add order_delivery_address column
ALTER TABLE trproductorder
ADD COLUMN IF NOT EXISTS order_delivery_address VARCHAR(400);

-- Add order_delivery_date column (format: YYYYMMDD)
ALTER TABLE trproductorder
ADD COLUMN IF NOT EXISTS order_delivery_date VARCHAR(8);

-- Add comments
COMMENT ON COLUMN trproductorder.order_delivery_address IS 'Delivery address for the order';
COMMENT ON COLUMN trproductorder.order_delivery_date IS 'Delivery date in YYYYMMDD format';
```

### Verification

After running the migration, verify the columns were added:

```sql
-- Check table structure
\d trproductorder

-- Or use this query
SELECT column_name, data_type, character_maximum_length
FROM information_schema.columns
WHERE table_name = 'trproductorder'
  AND column_name IN ('order_delivery_address', 'order_delivery_date');
```

You should see:

- `order_delivery_address` | character varying | 400
- `order_delivery_date` | character varying | 8

### Rollback (if needed)

If you need to rollback this migration:

```sql
ALTER TABLE trproductorder DROP COLUMN IF EXISTS order_delivery_address;
ALTER TABLE trproductorder DROP COLUMN IF EXISTS order_delivery_date;
```

### Important Notes

1. **Backup First**: Always backup your database before running migrations
2. **Existing Data**: This migration is safe for existing data - it only adds new nullable columns
3. **IF NOT EXISTS**: The migration uses `IF NOT EXISTS` so it's safe to run multiple times
4. **Application Restart**: After migration, restart your Spring Boot application

### Next Steps

After successful migration:

1. ✅ Verify columns exist in database
2. ✅ Restart application
3. ✅ Test Order Page functionality
4. ✅ Check that orders save with delivery information

For more details, see [ORDER_PAGE_IMPLEMENTATION.md](ORDER_PAGE_IMPLEMENTATION.md)
