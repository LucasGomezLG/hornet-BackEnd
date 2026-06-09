-- desglose is stored and retrieved as a plain JSON string — text is sufficient
-- and avoids Hibernate 6 JDBC type binding issues with jsonb
ALTER TABLE solicitud_items ALTER COLUMN desglose TYPE TEXT;
