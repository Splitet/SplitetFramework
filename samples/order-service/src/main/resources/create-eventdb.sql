drop table OrderEvents;
CREATE TABLE OrderEvents (
	entityId bigint,
	version bigint,
	aggregateName ascii,
	opId UUID,
	opDate timestamp,
	status varchar,
	eventData varchar,
	PRIMARY KEY (entityId, version)
);


CREATE INDEX OrderEvents_opId
   ON test1.OrderEvents (opId);