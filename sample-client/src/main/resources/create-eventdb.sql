drop table AggregateEvent;
CREATE TABLE AggregateEvent (
	entityId bigint,
	version bigint,
	aggregateName ascii,
	opId UUID,
	opDate timestamp,
	status varchar,
	description varchar,
	PRIMARY KEY (entityId, version)
);