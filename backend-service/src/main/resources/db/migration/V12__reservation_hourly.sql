-- dealing-with-contention-v2: reservations move from day-granularity (DATE) to
-- hour-granularity (TIMESTAMP). Availability and all concurrency invariants are
-- evaluated on hourly [dateFrom, dateTo) intervals.
ALTER TABLE reservation
    ALTER COLUMN date_from TYPE TIMESTAMP USING date_from::timestamp;
ALTER TABLE reservation
    ALTER COLUMN date_to TYPE TIMESTAMP USING date_to::timestamp;
