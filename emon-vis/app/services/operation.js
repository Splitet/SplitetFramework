import Ember from 'ember';

export default Ember.Service.extend({

  latestOperations: {statusMessage: null, operations: []},

  init() {
    this._super(...arguments);

    /*    if (!this.get('latestOperations')) {
          this.set('latestOperations', []);
        }*/

  },
  initLoadLatestOperations: Ember.on('init', function () {
    this.loadLatestOperations();
    Ember.run.later(this, () => {
      this.loadLatestOperations();
      this.initLoadLatestOperations();
    }, 1000);
  }),

  loadLatestOperations() {
    Ember.$.getJSON('/api/operations/v1/').then((data) => {
      this.set('latestOperations', {operations: data});
    }, (status) => {
      this.set('latestOperations', {statusMessage: "Error while querying Latest Operations"});
    });
  },

});
