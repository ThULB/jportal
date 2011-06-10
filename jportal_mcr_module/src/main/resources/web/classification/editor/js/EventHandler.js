/*
 * @package classification
 * @description 
 */
var classification = classification || {};

classification.EventHandler = function(/*Object*/ src) {
	this.source = src;
	this.listeners = [];
};

( function() {
	function attach(/*function*/ listener) {
		this.listeners.push(listener);
	}

	function detach(/*function*/ listener) {
		for (var i = 0; i < this.listeners.length; i++)
			if (this.listeners[i] == listener)
				this.listeners[i].splice(i,1);
	}

	function notify(args) {
		for (var i = 0; i < this.listeners.length; i++)
			this.listeners[i](this.source, args);
	}

	classification.EventHandler.prototype.attach = attach;
	classification.EventHandler.prototype.detach = detach;
	classification.EventHandler.prototype.notify = notify;
})();
