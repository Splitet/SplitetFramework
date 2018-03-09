--drop table if exists test.PaymentEvents ;
CREATE TABLE test.PaymentEvents (
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


CREATE MATERIALIZED VIEW test.PaymentEvents_byOps AS
  SELECT
    opId,
    entityId,
    version,
    eventType,
    opDate,
    status,
    auditinfo,
    eventData
  FROM test.PaymentEvents
  WHERE opId IS NOT NULL AND entityId IS NOT NULL AND version IS NOT NULL
  PRIMARY KEY (opid, entityId, VERSION );