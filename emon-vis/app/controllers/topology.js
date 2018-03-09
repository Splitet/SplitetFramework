import Ember from 'ember';

export default Ember.Controller.extend({
  networkOptions: {
    nodes: {
      color: '#C7F110'
    }
  },
  nodeColor: '#FF0000',
  operation: Ember.inject.service(),
  hasMessage: Ember.computed.notEmpty('model.statusMessage'),
  actions: {
    openTopology(opId) {
      this.transitionToRoute("topology", opId);
    }
  }
});
