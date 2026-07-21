-- dealing-with-contention-v2 (enhanced): reservations become provisional, category-level payment
-- HOLDS. A PENDING_PAYMENT row reserves one unit of a category's pool for its hour window and
-- occupies capacity only until it expires. The concrete vehicle is assigned later (at claim/delivery),
-- so vehicle_id is nullable for a hold and the category is recorded directly on the row.
ALTER TABLE reservation
    ADD COLUMN expires_at TIMESTAMP NULL;
ALTER TABLE reservation
    ADD COLUMN category VARCHAR(1) NULL;
ALTER TABLE reservation
    ALTER COLUMN vehicle_id DROP NOT NULL;
