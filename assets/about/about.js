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
