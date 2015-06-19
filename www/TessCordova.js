function TessCordova() {
}

TessCordova.prototype.coolMethod = function (options, successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, "TessCordova", "coolMethod", [options]);
};

TessCordova.prototype.tessOCR = function (options, successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, "TessCordova", "tessOCR", [options]);
};

TessCordova.install = function () {
  if (!window.plugins) {
    window.plugins = {};
  }

  window.plugins.tesscordova = new TessCordova();

  return window.plugins.tesscordova;
};

cordova.addConstructor(TessCordova.install);
