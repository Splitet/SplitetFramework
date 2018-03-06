--drop table if exists test.OrderEvents ;
CREATE TABLE test.OrderEvents (
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


CREATE INDEX OrderEvents_opId
  ON test.OrderEvents (opId);