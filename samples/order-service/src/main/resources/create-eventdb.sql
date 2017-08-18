--drop table if exists test.OrderEvents ;
CREATE TABLE test.OrderEvents (
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


CREATE INDEX OrderEvents_opId
   ON test.OrderEvents (opId);