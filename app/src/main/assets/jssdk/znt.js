(function(w) {
 var cfg = {};
 var _fns = {};
 var config = function(c) {
 for (k in c) {
 cfg[k] = c[k];
 };
 };
 var exec = (function() {
             var execute = (function() {
                                var os = -1;
                                if (/(iPhone|iPad|iPod|iOS)/.test(navigator.userAgent)) {
                                os = 1;
                                } else if (/(Android)/.test(navigator.userAgent)) {
                                os = 0;
                                };
                                return function(cb) {
                                    if (os == 0) {
                                    window.oatongJSBridge.invoke(cb);
                                    } else if (os == 1) {
                                    window.location.href = "oatongJSBridge.invoke('" + cb + "')";
                                    console.log(cb)
                                };
                            };
                            })();
             var genfnName = (function() {
                              var prefix = "zntfn";
                              var aw = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
                              var len = aw.length;
                              var m = 5;
                              var fn = function() {
                              var n = Math.floor(m + Math.random() * len);
                              var arr = [];
                              for (var i = 0; i < n; i++) {
                              var x = Math.floor(Math.random() * len);
                              arr.push(aw[x]);
                              };
                              return prefix + arr.join('');
                              };
                              return function() {
                              
                              var nm;
                              do {
                              nm = fn();
                              } while (nm in _fns);
                              return nm;
                              };
                              }());
             return function(cmd, co) {
             
             var name = genfnName();
             
             _fns[name] = function(resp) {
             var data = JSON.parse(resp)
             if (data.code) {
             if (co.success && typeof co.success == "function") {
             co.success(data.data);
             };
             } else {
             if (isNull(data.error)) {
             if (co.fail && typeof co.fail == "function") {
             co.fail(data.error);
             };
             // 兼容过去的回调
             if (co.cancel && typeof co.cancel == "function") {
             co.cancel();
             };
             }
             };
             if(data.nonstop == '0'){
             console.log("删除函数")
             delete _fns[name];
             }
             };
             var p = {
             m: name,
             cmd: cmd
             };
             for (nm in co) {
             if (typeof co[nm] !== "function") {
             p[nm] = co[nm];
             };
             };
             execute(JSON.stringify(p));
             };
             }());
 var cacheUserInfo = function(cfg) {
 exec("CacheUserInfo", cfg);
 };
 var getCacheUserInfo = function(cfg) {
 exec("GetCacheUserInfo", cfg);
 };
 var call = function(cfg) {
  exec("Call", cfg);
 };
 var vibrate = function(cfg) {
   exec("PhoneVibration", cfg);
 };
 var fingerPrint = function(cfg) {
   exec("FingerPrint", cfg);
 };
 var selectImage = function(cfg) {
   exec("SelectImage", cfg);
 };
 var getLocation = function(cfg) {
    exec("GetLocation", cfg);
 };
 var share = function(cfg) {
    exec("Share", cfg);
 };
 var wechatPay = function(cfg) {
   exec("WeChatPay", cfg);
 };
 var alipay = function(cfg) {
   exec("Alipay", cfg);
 };
 var thirdLogin = function(cfg) {
   exec("ThirdLogin", cfg);
 };
 var gyro = function(cfg) {
   exec("Gyro", cfg);
 };
 var closeGyro = function(cfg) {
    exec("CloseGyro", cfg);
 };
 var networkStatus = function(cfg) {
     exec("NetworkStatus", cfg);
 };
 var identify = function(cfg) {
     exec("UniquelyIdentifies", cfg);
 };
 var logout = function(cfg) {
     exec("Logout", cfg);
 };
 var cacheFile = function(cfg) {
     exec("CacheFile", cfg);
 };
 var cacheUserAccount = function(cfg) {
     exec("CacheUserAccount", cfg);
 };
 var deleteUserAccount = function(cfg) {
     exec("DeleteUserAccount", cfg);
 };
 var closeApp = function(cfg) {
     exec("CloseApp", cfg);
 };
 var scanCode = function(cfg) {
     exec("ScanCode", cfg);
 };
 var openMap = function(cfg) {
     exec("OpenMap", cfg);
 };
 var statusBarStyle = function(cfg) {
     exec("StatusBarStyle", cfg);
 };
 var playSound = function(cfg) {
      exec("PlaySound", cfg);
 };
 var contactList = function(cfg) {
      exec("ContactList", cfg);
 };
var deleteCacheFile = function(cfg) {
      exec("DeleteCacheFile", cfg);
 };
 var jgPushReg = function(cfg) {
     exec("JgPushReg", cfg);
 };
 var removeJgTag = function(cfg) {
     exec("RemoveJgTag", cfg);
 };


 var znt = {
 config: config,
 fn: _fns,
 cacheUserInfo: cacheUserInfo,
 getCacheUserInfo: getCacheUserInfo,
 call: call,
 vibrate: vibrate,
 fingerPrint: fingerPrint,
 selectImage: selectImage,
 getLocation: getLocation,
 share: share,
 wechatPay: wechatPay,
 alipay: alipay,
 thirdLogin: thirdLogin,
 gyro: gyro,
 closeGyro: closeGyro,
 networkStatus: networkStatus,
 identify: identify,
 logout: logout,
 cacheFile: cacheFile,
 cacheUserAccount: cacheUserAccount,
 deleteUserAccount: deleteUserAccount,
 closeApp: closeApp,
 scanCode: scanCode,
 openMap: openMap,
 statusBarStyle: statusBarStyle,
 playSound: playSound,
 contactList: contactList,
 deleteCacheFile: deleteCacheFile,
 jgPushReg: jgPushReg,
 removeJgTag: removeJgTag
 };
 w.znt = znt;
 }(window));
 
 function isNull(data) {
    return (data == "" || data == undefined || data == null) ? false : true;
}
 
function alert111(res){
    alert("1111111"+res)
    console.log(res)
}
 console.log = function(log){
    postConsoleMessage(log);
}
 
function postConsoleMessage(msg){
    var iframe = document.createElement("IFRAME");
    iframe.setAttribute("src", "ios-log:#iOS#" + msg);
    document.documentElement.appendChild(iframe);
    iframe.parentNode.removeChild(iframe);
    iframe = null;
}
 
 onerror = function(msg, url, line){
    var errormsg = "message:" + msg + "\n url:" + url + "\n line:" + line;
    postConsoleMessage(">>>>>>>>>>ERROR<<<<<<<<<<\n" + errormsg);
}
