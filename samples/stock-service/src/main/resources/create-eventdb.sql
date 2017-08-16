--drop table if exists test.StockEvents ;
CREATE TABLE test.StockEvents (
	entityId ascii,
	version int,
	eventType ascii,
	opId ascii,
	opDate timestamp,
	status ascii,
	auditinfo ascii,
	eventData varchar,
	PRIMARY KEY (entityId, version)
);


CREATE INDEX StockEvents_opId
   ON test.StockEvents (opId);