drop table AggregateEvent;
CREATE TABLE AggregateEvent (
	entityId bigint,
	aggregateName ascii,
	opId UUID,
	opDate timestamp,
	status varchar,
	description varchar,
	PRIMARY KEY (entityId, opDate, opId, aggregateName)
);