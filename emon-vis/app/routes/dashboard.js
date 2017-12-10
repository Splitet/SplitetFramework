import Ember from 'ember';

export default Ember.Route.extend({
  model(params) {
    return {
      statusMessage: params.statusMessage
    }
  },
  hasMessage: Ember.computed.notEmpty('model.statusMessage'),
});
