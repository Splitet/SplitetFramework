# Eventstore Specification

This specification defines an Eventstore, consisting of a event pub/sub layer, event processing layer and a persistence layer.

The goal of this specification is to enable the creation of interoperable tools for building an Eventstore. 

## Table of Contents
- [Introduction](#eventstore-specification)
- [Overview](#overview)
- [Events](#events)
- [Event States](#event-states)
- [Eventstore](#eventstore)

# Overview
At a high level the eventstore provides a channel to publish events and subscribe to event streams.

# Events
- Transaction Id
- Event Id
- Type
- Status (New states should be handled as new events)
- TTL
- Data
- Datetime
- Initiator

# Event States
- Created
- Failed
- Succedeed
- TXN Failed
- TXN Succedeed

# Eventstore
- Publish to multi-instance of a service is optinal
- Event store should handle timeouts and failures and create events automatically to trigger compansating transactions.
- Transaction locks on application layer can not be handled by the eventstore, an application should handle it's own locks on case of cancel or fail events triggered for a process which is already running.
- Eventstore should know the worker of an event on proccessing state.
