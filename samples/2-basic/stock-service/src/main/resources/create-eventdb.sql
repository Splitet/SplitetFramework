--drop table if exists test.StockEvents ;
CREATE TABLE test.StockEvents (
  entityId  ASCII,
  version   INT,
  eventType ASCII,
  opId      ASCII,
  opDate    TIMESTAMP,
  status    ASCII,
  auditinfo ASCII,
  eventData VARCHAR,
  PRIMARY KEY (entityId, version)
);

CREATE MATERIALIZED VIEW test.StockEvents_byOps AS
  SELECT
    opId,
    entityId,
    version,
    eventType,
    opDate,
    status,
    auditinfo,
    eventData
  FROM test.StockEvents
  WHERE opId IS NOT NULL AND entityId IS NOT NULL AND version IS NOT NULL
  PRIMARY KEY (opid, entityId, VERSION );