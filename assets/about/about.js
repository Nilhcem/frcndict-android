// Load CSS
loadCss(android.getTheme());

window.onload = function() {
	// Application name
	document.getElementById('app-name').innerHTML = android.getAppName();

	// Application version
	document.getElementById('app-version').innerHTML = android.getAppVersion();

	// Database version
	document.getElementById('db-version').innerHTML = android.getDbVersion();

	// Close button
	document.getElementById('close-button').onclick = function() {
		android.closeDialog();
	};
};

function loadCss(css) {
	if (document.createStyleSheet) {
		document.createStyleSheet(css);
	} else {
		var styles = "@import url('" + css + "');";
		var newSS = document.createElement('link');
		newSS.rel = 'stylesheet';
		newSS.href = 'data:text/css,' + escape(styles);
		document.getElementsByTagName('head')[0].appendChild(newSS);
	}
}
