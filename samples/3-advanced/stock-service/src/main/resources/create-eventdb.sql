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


CREATE INDEX StockEvents_opId
  ON test.StockEvents (opId);