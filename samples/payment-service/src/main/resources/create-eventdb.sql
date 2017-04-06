drop table PaymentEvents;
CREATE TABLE PaymentEvents (
	entityId bigint,
	version bigint,
	aggregateName ascii,
	opId UUID,
	opDate timestamp,
	status varchar,
	eventData varchar,
	PRIMARY KEY (entityId, version)
);

CREATE INDEX PaymentEvents_opId
   ON test1.PaymentEvents (opId);