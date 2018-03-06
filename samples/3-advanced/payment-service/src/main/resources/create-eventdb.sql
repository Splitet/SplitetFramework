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


CREATE INDEX PaymentEvents_opId
  ON test.PaymentEvents (opId);