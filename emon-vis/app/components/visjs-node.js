import Ember from 'ember';
import VisJsChild from '../components/visjs-child';

export default VisJsChild.extend({
  type: 'node',

  /**
   * @public
   *
   * Unique node-identifier. Also use this value for the
   * edges.
   * @type {String}
   */
  nId: '',

  /**
   * @public
   *
   * If set, this overrides the networks global color.
   * Use a Hex-Value when setting this.
   * @type {String}
   */
  color: false,

  colorChanged: Ember.observer('color', function() {
    let container = this.get('containerLayer');
    container.updateNodeColor(this.get('nId'), this.get('color'));
  }),

  /**
   * @public
   *
   * If set this displays a label under/in the node, depending on
   * whether an image is shown or not.
   * @type {String}
   */
  label: undefined,

  labelChanged: Ember.observer('label', function() {
    let container = this.get('containerLayer');
    container.updateNodeLabel(this.get('nId'), this.get('label'));
  }),

  /**
   * @public
   *
   * If set, a given image-url will be shown as image.
   * @type {String}
   */
  image: false,

  imageChanged: Ember.observer('image', function() {
    let container = this.get('containerLayer');
    container.updateNodeImage(this.get('nId'), this.get('image'));
  })

});
