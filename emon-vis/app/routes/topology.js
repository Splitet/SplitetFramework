import Ember from 'ember';

export default Ember.Route.extend({
  model(params) {
    return Ember.$.getJSON('/api/operations/v1/' + params.opId).then(data => {
      this.set('topology', data);
      var _self = this;
      let nodes = [];
      let edges = [];

      nodes.push({
        nId: data.initiatorCommand,
        label: data.initiatorCommand,
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
        color: '#f8ffd2'
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

    })
  },
  calculateId(operation) {
    return operation.sender + '_' + operation.topic;
  },
  handleEvents(nodes, edges, parent) {
    Object.keys(parent.publishedEvents).forEach((serviceName) => {
      let operation = parent.publishedEvents[serviceName];
      nodes.push({
        nId: this.calculateId(operation),
        label: operation.sender,
        color: this.findColor(operation)
      });
      edges.push({
        from: this.calculateId(parent),
        label: parent.topic,
        to: this.calculateId(operation)
      });
      this.handleEvents(nodes, edges, operation);
    });
  },
  findColor(operation) {
    switch (operation.eventType) {
      case "OP_START":
        return '#ff641e';
      case "EVENT":
        return '#f8ffd2';
      case "OP_SUCCESS":
        return '#03ff2f';
      case "OP_FAIL":
        return '#ff1109';
    }


  },

});
