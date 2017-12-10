import Ember from 'ember';

export default Ember.Route.extend({
  model(params) {
    return Ember.$.getJSON('/api/operations/v1/' + params.opId).then((data) => {
      this.set('topology', data);
      let nodes = [];
      let edges = [];

      nodes.push({
        nId: data.initiatorCommand,
        label: "START",
        shape: 'diamond',
        color: '#87604e'
      });
      edges.push({
        from: data.initiatorCommand,
        label: data.initiatorCommand,
        to: this.calculateId(data.head)
      });
      nodes.push({
        nId: this.calculateId(data.head),
        label: data.head.sender,
        color: this.findColor(data.head.eventType)

      });

      this.handleEvents(nodes, edges, data.head);

      console.log(data);
      this.set('nodes', nodes);
      this.set('edges', edges);
      return {
        data: data,
        nodes: nodes,
        edges: edges
      }

    }, (status) => {
      return {
        statusMessage: `Error while fetching operation ${params.opId} status:${status}`
      }
    })
  },
  calculateId(operation) {
    return operation.sender + '_' + operation.topic;
  },
  handleEvents(nodes, edges, parent) {
    if (parent.publishedEvents !== null && parent.publishedEvents !== undefined)
      Object.keys(parent.publishedEvents).forEach((serviceName) => {
        let publishedEvent = parent.publishedEvents[serviceName];
        if (publishedEvent.type === "none" && publishedEvent.operation) {
          let operation = publishedEvent.operation;
          nodes.push({
            nId: operation.sender + '_' + operation.aggregateId,
            label: operation.sender,
            color: this.findColor(operation.transactionState)
          });
          edges.push({
            from: this.calculateId(parent),
            label: parent.topic,
            to: operation.sender + '_' + operation.aggregateId,
          });
          if (operation.transactionState === "TXN_FAILED") {
            nodes.push({
              nId: "FAIL",
              label: "FAIL",
              color: this.findColor(operation.transactionState)
            });
            edges.push({
              from: operation.sender + '_' + operation.aggregateId,
              label: operation.transactionState,
              to: "FAIL",
            });
          }

        } else {
          nodes.push({
            nId: this.calculateId(publishedEvent),
            label: publishedEvent.sender,
            color: this.findColor(publishedEvent.eventType)
          });
          edges.push({
            from: this.calculateId(parent),
            label: parent.topic,
            to: this.calculateId(publishedEvent),
          });
          let operation = publishedEvent.operation;
          if (operation && operation.transactionState === "TXN_FAILED") {
            nodes.push({
              nId: "FAIL",
              label: "FAIL",
              color: this.findColor(operation.transactionState)
            });
            edges.push({
              from: this.calculateId(publishedEvent),
              label: publishedEvent.topic,
              to: "FAIL",
            });
          }
        }


        this.handleEvents(nodes, edges, publishedEvent);
      });
  },
  findColor(eventType) {
    switch (eventType) {
      case "OP_START":
        return '#f8ffd2';
      case "EVENT":
        return '#f8ffd2';
      case "OP_SUCCESS":
        return '#03ff2f';
      case "OP_FAIL":
        return '#ff9d09';
      case "TXN_SUCCESS":
        return '#089d0a';
      case "TXN_FAILED":
        return '#ff1109';
    }
  },

});
