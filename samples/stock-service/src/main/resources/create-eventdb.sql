drop table StockEvents;
CREATE TABLE StockEvents (
	entityId bigint,
	version bigint,
	aggregateName ascii,
	opId UUID,
	opDate timestamp,
	status varchar,
	eventData varchar,
	PRIMARY KEY (entityId, version)
);
CREATE INDEX StockEvents_opId
   ON test1.StockEvents (opId);