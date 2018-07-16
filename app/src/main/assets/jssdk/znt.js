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
					window.jsbridge.invoke(cb);
				} else if (os == 1) {
					window.location.href = "jsbridge.invoke('" + cb + "')";
				};
			};
		})();
		var genfnName = (function() {
			var prefix = "znt_fn";
			var aw = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";
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
				var data = JSON.parse(resp);
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
							co.cancel(data.error);
						};
					}
				};
				delete _fns[name];
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
	var getVoiceText = function(cfg) {
    	exec("getVoiceText", cfg);
    };
	var znt = {
		config: config,
		fn: _fns,
		getVoiceText: getVoiceText,

	};
	w.znt = znt;
}(window));

function isNull(data) {
	return (data == "" || data == undefined || data == null) ? false : true;
}