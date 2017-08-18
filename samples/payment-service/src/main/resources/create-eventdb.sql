--drop table if exists test.PaymentEvents ;
CREATE TABLE test.PaymentEvents (
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


CREATE INDEX PaymentEvents_opId
   ON test.PaymentEvents (opId);