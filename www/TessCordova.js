function TessCordova() {
}

TessCordova.prototype.coolMethod = function (options, successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, "TessCordova", "coolMethod", [arg0]);
};

TessCordova.prototype.tessOCR = function (options, successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, "TessCordova", "tessOCR", [arg0]);
};

TessCordova.install = function () {
  if (!window.plugins) {
    window.plugins = {};
  }

  window.plugins.tesscordova = new TessCordova();

  return window.plugins.tesscordova;
};

cordova.addConstructor(TessCordova.install);
