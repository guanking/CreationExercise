/**
 * 
 */
function checkForm1() {
	var date = document.getElementById('startdate').value;
	var stopDate = document.getElementById('enddate').value;
	if (date != "" && stopDate != "") {
		date = date.replace(/-/g, '/');
		stopDate = stopDate.replace(/-/g, '/');
		var d1 = new Date(date);
		var d2 = new Date(stopDate);
		if (Date.parse(d1) - Date.parse(d2) > 0) {
			return false;
		}
	}
	if (date != "" && stopDate == "") {
		alert('您没有输入结束时间，请重新输入！');
		return false;
	}
	if (date == "" && stopDate != "") {
		alert('您没有输入开始时间，请重新输入！');
		return false;
	}
	return true;
}